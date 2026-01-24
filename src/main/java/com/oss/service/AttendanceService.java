package com.oss.service;
import com.oss.config.WorkTimeConfig;
import com.oss.model.Attendance;
import com.oss.model.AttendanceHistory;
import com.oss.model.User;
import com.oss.repository.AttendanceRepository;
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
@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                             .orElseThrow(() -> new RuntimeException("User not found"));
    }
    public Attendance getTodayAttendanceForCurrentUser() {
        ZoneId zone = WorkTimeConfig.SRI_LANKA;
        Instant start = LocalDate.now(zone).atStartOfDay(zone).toInstant();
        Instant end = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant();
        return attendanceRepository
                .findByUserAndWorkDateBetween(getCurrentUser(), start, end)
                .stream().findFirst().orElse(null);
    }
    public Attendance checkIn(Instant manualTime) {
        User user = getCurrentUser();
        ZoneId zone = WorkTimeConfig.SRI_LANKA;
        Instant start = LocalDate.now(zone).atStartOfDay(zone).toInstant();
        Instant end = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant();
        if (!attendanceRepository
                .findByUserAndWorkDateBetween(user, start, end).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already checked in");
        }
        Attendance att = new Attendance();
        att.setUser(user);
        att.setWorkDate(start);
        att.setCheckInTime(manualTime != null ? manualTime : Instant.now()); // âœ… FIX
        att.setStatus("CHECKED_IN");
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
        Instant start = LocalDate.now(zone).atStartOfDay(zone).toInstant();
        Instant end = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant();
        Attendance att = attendanceRepository
                .findByUserAndWorkDateBetween(user, start, end)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Not checked in"));
        if (att.getCheckOutTime() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already checked out");
        }
        // Capture old values for audit
        java.util.Map<String, Object> oldValues = new java.util.HashMap<>();
        oldValues.put("checkOutTime", att.getCheckOutTime());
        oldValues.put("status", att.getStatus());
        oldValues.put("totalMinutes", att.getTotalMinutes());
        att.setCheckOutTime(manualTime != null ? manualTime : Instant.now()); // âœ… FIX
        att.setStatus("COMPLETED");
        att.setTotalMinutes(att.getWorkedMinutes());
        att.setManualCheckout(manualTime != null); // âœ… FIX
        Attendance saved = attendanceRepository.save(att);
        // Create audit log
        java.util.Map<String, Object> newValues = new java.util.HashMap<>();
        newValues.put("checkOutTime", saved.getCheckOutTime().toString());
        newValues.put("status", "COMPLETED");
        newValues.put("totalMinutes", saved.getTotalMinutes());
        newValues.put("manualCheckout", manualTime != null);
        auditLogService.createAuditLog(user, "CHECK_OUT", "ATTENDANCE", saved.getId(), oldValues, newValues);
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
    // â— UNCHANGED â€” AS REQUESTED
    public Map<String, Object> calculateTodaySalary() {
        User user = getCurrentUser();
        ZoneId zone = WorkTimeConfig.SRI_LANKA;
        LocalDate today = LocalDate.now(zone);
        Instant start = today.atStartOfDay(zone).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(zone).toInstant();
        List<Attendance> attendances = attendanceRepository.findByUserAndWorkDateBetween(user, start, end);
        double hourlyRate = user.getHourlyRate() != null ? user.getHourlyRate() : 0.0;
        double totalHours = 0.0;
        double totalSalary = 0.0;
        List<Map<String, Object>> dailyBreakdown = new ArrayList<>();
        for (Attendance att : attendances) {
            long minutes = att.getTotalMinutes() != null ? att.getTotalMinutes() : 0L;
            double hours = minutes / 60.0;
            double salary = hours * hourlyRate;
            totalHours += hours;
            totalSalary += salary;
            Map<String, Object> breakdown = new HashMap<>();
            breakdown.put("date", today.toString());
            breakdown.put("hours", hours);
            breakdown.put("salary", salary);
            dailyBreakdown.add(breakdown);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("hourlyRate", hourlyRate);
        result.put("totalHours", totalHours);
        result.put("totalSalary", totalSalary);
        result.put("dailyBreakdown", dailyBreakdown);
        return result;
    }
    public Map<String, Object> calculateMyMonthlySalary(int year, int month) {
        User user = getCurrentUser();
        ZoneId zone = WorkTimeConfig.SRI_LANKA;
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
        Instant start = firstDay.atStartOfDay(zone).toInstant();
        Instant end = lastDay.plusDays(1).atStartOfDay(zone).toInstant();
        List<Attendance> attendances = attendanceRepository.findByUserAndWorkDateBetween(user, start, end);
        double hourlyRate = user.getHourlyRate() != null ? user.getHourlyRate() : 0.0;
        double totalHours = 0.0;
        double totalSalary = 0.0;
        List<Map<String, Object>> dailyBreakdown = new ArrayList<>();
        for (Attendance att : attendances) {
            LocalDate workDate = LocalDate.ofInstant(att.getWorkDate(), zone);
            long minutes = att.getTotalMinutes() != null ? att.getTotalMinutes() : 0L;
            double hours = minutes / 60.0;
            double salary = hours * hourlyRate;
            totalHours += hours;
            totalSalary += salary;
            Map<String, Object> breakdown = new HashMap<>();
            breakdown.put("date", workDate.toString());
            breakdown.put("hours", hours);
            breakdown.put("salary", salary);
            dailyBreakdown.add(breakdown);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("hourlyRate", hourlyRate);
        result.put("totalHours", totalHours);
        result.put("totalSalary", totalSalary);
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