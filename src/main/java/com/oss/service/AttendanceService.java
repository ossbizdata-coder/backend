package com.oss.service;
import com.oss.config.WorkTimeConfig;
import com.oss.model.Attendance;
import com.oss.model.AttendanceHistory;
import com.oss.model.AttendanceStatus;
import com.oss.model.User;
import com.oss.repository.AttendanceRepository;
import com.oss.repository.CreditRepository;
import com.oss.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final CreditRepository creditRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new RuntimeException("User not found");
        }
        String email = auth.getName();
        if (email == null) {
            throw new RuntimeException("User not found");
        }
        return userRepository.findByEmail(email)
                             .orElseThrow(() -> new RuntimeException("User not found"));
    }
    public Attendance getTodayAttendanceForCurrentUser() {
        ZoneId zone = WorkTimeConfig.SRI_LANKA;
        LocalDate today = LocalDate.now(zone);
        // ✅ Get LATEST record to handle duplicates
        return attendanceRepository
                .findLatestByUserAndWorkDate(getCurrentUser(), today)
                .orElse(null);
    }
    public Attendance checkIn(Instant manualTime) {
        return checkIn(manualTime, null);
    }

    public Attendance checkIn(Instant manualTime, String timezone) {
        User user = getCurrentUser();

        // ✅ Use timezone from request, or default to configured zone
        ZoneId zone = timezone != null ? ZoneId.of(timezone) : WorkTimeConfig.DEFAULT_ZONE;
        LocalDate today = LocalDate.now(zone);

        // Find or create today's attendance record - use the LATEST (bulletproof)
        Optional<Attendance> existingOpt = attendanceRepository.findLatestByUserAndWorkDate(user, today);

        Attendance attendance;
        if (existingOpt.isPresent()) {
            attendance = existingOpt.get();
            System.out.println("✅ checkIn: Updating EXISTING record ID: " + attendance.getId());
        } else {
            attendance = new Attendance();
            attendance.setUser(user);
            attendance.setWorkDate(today);
            System.out.println("✅ checkIn: Creating NEW record for: " + today);
        }

        // Simple logic: User clicked YES
        attendance.setIsWorking(true);  // ✅ SIMPLE FLAG
        attendance.setStatus(AttendanceStatus.WORKING);  // For compatibility

        // Save and return
        return attendanceRepository.save(attendance);
    }
    // Change checkOut to return Attendance instead of void
    @Transactional
    public Attendance checkOut(Instant manualTime) {
        return checkOut(manualTime, null);
    }

    @Transactional
    public Attendance checkOut(Instant manualTime, String timezone) {
        User user = getCurrentUser();

        // ✅ Use timezone from request, or default to configured zone
        ZoneId zone = timezone != null ? ZoneId.of(timezone) : WorkTimeConfig.DEFAULT_ZONE;
        LocalDate today = LocalDate.now(zone);

        // ✅ BULLETPROOF: Find existing record first
        Optional<Attendance> existingOpt = attendanceRepository.findLatestByUserAndWorkDate(user, today);

        Attendance attendance;
        if (existingOpt.isPresent()) {
            attendance = existingOpt.get();
            System.out.println("✅ checkOut: Updating EXISTING record ID: " + attendance.getId());
        } else {
            attendance = new Attendance();
            attendance.setUser(user);
            attendance.setWorkDate(today);
            System.out.println("✅ checkOut: Creating NEW record for: " + today);
        }

        // Update to NOT_WORKING status
        attendance.setIsWorking(false);
        attendance.setStatus(AttendanceStatus.NOT_WORKING);

        return attendanceRepository.save(attendance);
    }

    /**
     * Update today's attendance status
     * Used by mobile app to set WORKING or NOT_WORKING when user clicks YES/NO
     */
    @Transactional
    public Attendance updateTodayStatus(String statusString) {
        User user = getCurrentUser();
        ZoneId zone = WorkTimeConfig.SRI_LANKA;
        LocalDate today = LocalDate.now(zone);

        // Validate and parse status
        AttendanceStatus status;
        try {
            status = AttendanceStatus.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Must be: WORKING or NOT_WORKING");
        }

        // ✅ BULLETPROOF: Find existing record first
        Optional<Attendance> existingOpt = attendanceRepository.findLatestByUserAndWorkDate(user, today);

        Attendance attendance;
        if (existingOpt.isPresent()) {
            // ✅ CRITICAL: Use existing record to ensure UPDATE not INSERT
            attendance = existingOpt.get();
            System.out.println("✅ Updating EXISTING record ID: " + attendance.getId() + " for date: " + today);
        } else {
            // Create new record only if none exists
            attendance = new Attendance();
            attendance.setUser(user);
            attendance.setWorkDate(today);
            System.out.println("✅ Creating NEW record for date: " + today);
        }

        // Update status
        attendance.setStatus(status);
        attendance.setIsWorking(status != AttendanceStatus.NOT_WORKING);

        Attendance saved = attendanceRepository.save(attendance);
        System.out.println("✅ Saved record ID: " + saved.getId() + " with status: " + saved.getStatus());

        return saved;
    }

    public List<AttendanceHistory> getMyAttendanceHistory() {
        return attendanceRepository.getMyHistory(getCurrentUser());
    }
    public List<Map<String, Object>> getAllAttendance() {
        List<Map<String, Object>> res = new ArrayList<>();
        for (Attendance a : attendanceRepository.findAll()) {
            Map<String, Object> map = new HashMap<>();
            // Handle potentially deleted users gracefully
            try {
                if (a.getUser() != null) {
                    map.put("userId", a.getUser().getId());
                    map.put("userName", a.getUser().getName());
                    map.put("userEmail", a.getUser().getEmail());
                } else {
                    map.put("userId", null);
                    map.put("userName", "[Deleted User]");
                    map.put("userEmail", "");
                }
            } catch (Exception e) {
                // User was deleted but attendance record still references them
                map.put("userId", null);
                map.put("userName", "[Deleted User]");
                map.put("userEmail", "");
            }
            map.put("workDate", a.getWorkDate() != null ? a.getWorkDate().toString() : "");
            map.put("status", a.getStatus() != null ? a.getStatus() : "");
            map.put("isWorking", a.getIsWorking() != null ? a.getIsWorking() : true);
            map.put("overtimeHours", a.getOvertimeHours() != null ? a.getOvertimeHours() : 0.0);
            map.put("deductionHours", a.getDeductionHours() != null ? a.getDeductionHours() : 0.0);
            map.put("overtimeReason", a.getOvertimeReason() != null ? a.getOvertimeReason() : "");
            map.put("deductionReason", a.getDeductionReason() != null ? a.getDeductionReason() : "");
            res.add(map);
        }
        return res;
    }
    // ===================== SALARY CALCULATION =====================
    public Map<String, Object> calculateTodaySalary() {
        User user = getCurrentUser();
        ZoneId zone = WorkTimeConfig.SRI_LANKA;
        LocalDate today = LocalDate.now(zone);

        // ✅ Get LATEST record to handle duplicates
        Attendance att = attendanceRepository.findLatestByUserAndWorkDate(user, today).orElse(null);

        double dailySalaryRate = user.getDailySalary() != null ? user.getDailySalary() : 0.0;
        double hourlyRate = user.getHourlyRate() != null ? user.getHourlyRate() : 0.0;
        double deductionRate = user.getDeductionRatePerHour() != null ? user.getDeductionRatePerHour() : 0.0;
        double totalSalary = 0.0;
        double hours = 0.0;

        List<Map<String, Object>> dailyBreakdown = new ArrayList<>();

        if (att != null && att.getIsWorking() != null && att.getIsWorking()) {
            // User clicked YES - they worked today
            hours = 8.0;  // Standard work day
            totalSalary = dailySalaryRate;

            // Add overtime
            if (att.getOvertimeHours() != null && att.getOvertimeHours() > 0) {
                totalSalary += att.getOvertimeHours() * hourlyRate;
            }

            // Subtract deduction
            if (att.getDeductionHours() != null && att.getDeductionHours() > 0) {
                totalSalary -= att.getDeductionHours() * deductionRate;
            }

            Map<String, Object> breakdown = new HashMap<>();
            breakdown.put("date", today.toString());
            breakdown.put("hours", hours);
            breakdown.put("salary", totalSalary);
            breakdown.put("overtimeHours", att.getOvertimeHours() != null ? att.getOvertimeHours() : 0.0);
            breakdown.put("deductionHours", att.getDeductionHours() != null ? att.getDeductionHours() : 0.0);
            breakdown.put("overtimeReason", att.getOvertimeReason());
            breakdown.put("deductionReason", att.getDeductionReason());
            dailyBreakdown.add(breakdown);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dailySalary", dailySalaryRate);
        result.put("hourlyRate", hourlyRate);
        result.put("deductionRatePerHour", deductionRate);
        result.put("totalHours", hours);
        result.put("totalSalary", totalSalary);
        result.put("dailyBreakdown", dailyBreakdown);
        return result;
    }
    public Map<String, Object> calculateMyMonthlySalary(int year, int month) {
        User user = getCurrentUser();
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        List<Attendance> attendances = attendanceRepository.findByUserAndWorkDateBetween(user, firstDay, lastDay);

        double dailySalaryRate = user.getDailySalary() != null ? user.getDailySalary() : 0.0;
        double hourlyRate = user.getHourlyRate() != null ? user.getHourlyRate() : 0.0;
        double deductionRate = user.getDeductionRatePerHour() != null ? user.getDeductionRatePerHour() : 0.0;
        double totalSalary = 0.0;
        int daysWorked = 0;
        final double MIN_HOURS = 6.0;

        List<Map<String, Object>> dailyBreakdown = new ArrayList<>();

        for (Attendance att : attendances) {
            double hours = 0.0;
            double daySalary = 0.0;
            boolean qualified = false;

            // ✅ SIMPLE LOGIC: Just check the isWorking flag
            if (att.getIsWorking() != null && att.getIsWorking()) {
                // User clicked YES - they worked
                hours = 8.0;
                qualified = true;
                daySalary = dailySalaryRate;
                daysWorked++;

                // Add overtime if any
                if (att.getOvertimeHours() != null && att.getOvertimeHours() > 0) {
                    daySalary += att.getOvertimeHours() * hourlyRate;
                }

                // Deduct if any
                if (att.getDeductionHours() != null && att.getDeductionHours() > 0) {
                    daySalary -= att.getDeductionHours() * deductionRate;
                }
            } else {
                // User clicked NO - they didn't work
                hours = 0.0;
                qualified = false;
                daySalary = 0.0;
            }

            totalSalary += daySalary;

            Map<String, Object> breakdown = new HashMap<>();
            breakdown.put("date", att.getWorkDate().toString());
            breakdown.put("hours", hours);
            breakdown.put("salary", daySalary);
            breakdown.put("overtimeHours", att.getOvertimeHours() != null ? att.getOvertimeHours() : 0.0);
            breakdown.put("deductionHours", att.getDeductionHours() != null ? att.getDeductionHours() : 0.0);
            breakdown.put("overtimeReason", att.getOvertimeReason());
            breakdown.put("deductionReason", att.getDeductionReason());
            breakdown.put("qualified", qualified);
            breakdown.put("status", att.getStatus().name());
            dailyBreakdown.add(breakdown);
        }

        // ✅ NEW: Get credits for this month and deduct from salary
        Double totalCredits = creditRepository.sumCreditsByUserIdAndDateRange(user.getId(), firstDay, lastDay);
        if (totalCredits == null) {
            totalCredits = 0.0;
        }

        // Calculate final salary after credits deduction
        double finalSalary = totalSalary - totalCredits;

        Map<String, Object> result = new HashMap<>();
        result.put("dailySalary", dailySalaryRate);
        result.put("hourlyRate", hourlyRate);
        result.put("deductionRatePerHour", deductionRate);
        result.put("totalDaysWorked", daysWorked);
        result.put("baseSalary", totalSalary);        // Salary before credits
        result.put("totalCredits", totalCredits);      // Credits to deduct
        result.put("totalSalary", finalSalary);        // Final salary after credits
        result.put("minHoursRequired", MIN_HOURS);
        result.put("dailyBreakdown", dailyBreakdown);
        return result;
    }
    // Add this method to match controller
    public Attendance getToday() {
        return getTodayAttendanceForCurrentUser();
    }
    // Add this method to match controller
    public List<AttendanceHistory> myHistory() {
        return getMyAttendanceHistory();
    }
    // Add this method to match controller
    public List<Map<String, Object>> allAttendance() {
        return getAllAttendance();
    }
}
