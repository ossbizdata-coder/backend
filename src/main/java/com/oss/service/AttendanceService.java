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
        return attendanceRepository
                .findByUserAndWorkDate(getCurrentUser(), today)
                .orElse(null);
    }
    public Attendance checkIn(Instant manualTime) {
        User user = getCurrentUser();
        ZoneId zone = WorkTimeConfig.SRI_LANKA;
        LocalDate today = LocalDate.now(zone);

        Optional<Attendance> existingOpt = attendanceRepository.findByUserAndWorkDate(user, today);

        if (existingOpt.isPresent()) {
            // Update existing record instead of creating duplicate
            Attendance attendance = existingOpt.get();
            attendance.setCheckInTime(Instant.now());
            attendance.setStatus(AttendanceStatus.CHECKED_IN);
            attendance.setCheckOutTime(null); // Clear checkout if re-checking in
            return attendanceRepository.save(attendance);
        }

        // Create new record for today
        Attendance att = new Attendance();
        att.setUser(user);
        att.setWorkDate(today);
        att.setCheckInTime(Instant.now());
        att.setStatus(AttendanceStatus.CHECKED_IN);
        att.setManualCheckout(false);
        Attendance saved = attendanceRepository.save(att);
        // Create audit log
        java.util.Map<String, Object> newValues = new java.util.HashMap<>();
        newValues.put("id", saved.getId());
        newValues.put("userId", user.getId());
        newValues.put("workDate", saved.getWorkDate().toString());
        newValues.put("checkInTime", saved.getCheckInTime().toString());
        newValues.put("status", "CHECKED_IN");
        newValues.put("manualCheckIn", manualTime != null);
        auditLogService.createAuditLog(user, "CHECK_IN", "ATTENDANCE", saved.getId(), null, newValues);
        return saved;
    }
    // Change checkOut to return Attendance instead of void
    @Transactional
    public Attendance checkOut(Instant manualTime) {
        User user = getCurrentUser();
        ZoneId zone = WorkTimeConfig.SRI_LANKA;
        LocalDate today = LocalDate.now(zone);

        Optional<Attendance> existingOpt = attendanceRepository.findByUserAndWorkDate(user, today);

        if (existingOpt.isPresent()) {
            // Update existing record
            Attendance attendance = existingOpt.get();
            attendance.setCheckOutTime(Instant.now());
            attendance.setStatus(AttendanceStatus.COMPLETED);
            attendance.setTotalMinutes(attendance.getWorkedMinutes());
            return attendanceRepository.save(attendance);
        } else {
            // Create new record with checkout only
            Attendance attendance = new Attendance();
            attendance.setUser(user);
            attendance.setWorkDate(today);
            attendance.setCheckOutTime(Instant.now());
            attendance.setStatus(AttendanceStatus.COMPLETED);
            return attendanceRepository.save(attendance);
        }
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
            map.put("checkInTime", a.getCheckInTime() != null ? a.getCheckInTime().toString() : "");
            map.put("checkOutTime", a.getCheckOutTime() != null ? a.getCheckOutTime().toString() : "");
            map.put("status", a.getStatus() != null ? a.getStatus() : "");
            map.put("manualCheckout", a.isManualCheckout());
            map.put("totalMinutes", a.getTotalMinutes() != null ? a.getTotalMinutes() : 0);
            // Add overtime/deduction fields
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

        Attendance att = attendanceRepository.findByUserAndWorkDate(user, today).orElse(null);

        double dailySalaryRate = user.getDailySalary() != null ? user.getDailySalary() : 0.0;
        double deductionRate = user.getDeductionRatePerHour() != null ? user.getDeductionRatePerHour() : 0.0;
        double totalHours = 0.0;
        double totalSalary = 0.0;
        final double MIN_HOURS = 6.0;

        List<Map<String, Object>> dailyBreakdown = new ArrayList<>();

        if (att != null && att.getTotalMinutes() != null) {
            long minutes = att.getTotalMinutes();
            double hours = minutes / 60.0;
            totalHours = hours;

            // Calculate salary based on new logic
            if (hours >= MIN_HOURS) {
                totalSalary = dailySalaryRate;

                // Add overtime
                if (att.getOvertimeHours() != null && att.getOvertimeHours() > 0) {
                    totalSalary += att.getOvertimeHours() * (user.getHourlyRate() != null ? user.getHourlyRate() : 0.0);
                }

                // Subtract deduction
                if (att.getDeductionHours() != null && att.getDeductionHours() > 0) {
                    totalSalary -= att.getDeductionHours() * deductionRate;
                }
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
        result.put("deductionRatePerHour", deductionRate);
        result.put("totalHours", totalHours);
        result.put("totalSalary", totalSalary);
        result.put("minHoursRequired", MIN_HOURS);
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

            // ✅ FIX: Handle legacy "WORKING" status records without timestamps
            if (att.getStatus() == AttendanceStatus.WORKING && att.getCheckInTime() == null && att.getCheckOutTime() == null) {
                // Legacy record - assume full day worked (8 hours)
                hours = 8.0;
                qualified = true;
                daySalary = dailySalaryRate;
                daysWorked++;

                // Still apply overtime and deductions from the record
                if (att.getOvertimeHours() != null && att.getOvertimeHours() > 0) {
                    daySalary += att.getOvertimeHours() * hourlyRate;
                }
                if (att.getDeductionHours() != null && att.getDeductionHours() > 0) {
                    daySalary -= att.getDeductionHours() * deductionRate;
                }
            }
            // ✅ Handle modern records with timestamps
            else if (att.getCheckInTime() != null && att.getCheckOutTime() != null) {
                long minutes = att.getTotalMinutes() != null ? att.getTotalMinutes() : 0L;
                hours = minutes / 60.0;

                // Check if qualified for full daily salary
                if (hours >= MIN_HOURS) {
                    daySalary = dailySalaryRate;
                    daysWorked++;
                    qualified = true;

                    // Add overtime if any
                    if (att.getOvertimeHours() != null && att.getOvertimeHours() > 0) {
                        daySalary += att.getOvertimeHours() * hourlyRate;
                    }

                    // Deduct if any
                    if (att.getDeductionHours() != null && att.getDeductionHours() > 0) {
                        daySalary -= att.getDeductionHours() * deductionRate;
                    }
                }
            }
            // ✅ Handle NOT_WORKING status
            else if (att.getStatus() == AttendanceStatus.NOT_WORKING) {
                hours = 0.0;
                qualified = false;
                daySalary = 0.0;
            }
            // ✅ Handle incomplete records (checked in but not checked out)
            else {
                // Still in progress or incomplete - don't count
                if (att.getCheckInTime() != null) {
                    long minutes = att.getTotalMinutes() != null ? att.getTotalMinutes() : 0L;
                    hours = minutes / 60.0;
                }
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
