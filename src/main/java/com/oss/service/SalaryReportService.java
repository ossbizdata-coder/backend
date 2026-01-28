package com.oss.service;
import com.oss.dto.OSS_DailySalaryDto;
import com.oss.model.Attendance;
import com.oss.model.AttendanceStatus;
import com.oss.model.Role;
import com.oss.model.StaffSalaryReport;
import com.oss.model.User;
import com.oss.repository.AttendanceRepository;
import com.oss.repository.CreditRepository;
import com.oss.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class SalaryReportService {
    private final AttendanceRepository attendanceRepository;
    private final CreditRepository creditRepository;
    private final UserRepository userRepository;
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    public List<StaffSalaryReport> getMonthlyStaffSalary(
            int year,
            int month
                                                        ) {
        User admin = getCurrentUser();
        if (admin.getRole() != Role.SUPERADMIN && admin.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied"
            );
        }
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        return attendanceRepository.getMonthlyStaffSalary(from, to);
    }
    public Map<String, Object> calculateMyMonthlySalary(int year, int month) {
        User user = getCurrentUser();
        ZoneId zone = ZoneId.of("Asia/Colombo");
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        List<Attendance> list = attendanceRepository.findByUserAndWorkDateBetweenOrderByWorkDateDesc(user, from, to);
        List<OSS_DailySalaryDto> daily = new ArrayList<>();
        double totalSalary = 0.0;
        int daysWorked = 0;
        final double MIN_HOURS = 6.0; // Minimum hours to qualify for full daily salary
        Double dailySalaryRate = user.getDailySalary() != null ? user.getDailySalary() : 0.0;
        Double deductionRate = user.getDeductionRatePerHour() != null ? user.getDeductionRatePerHour() : 0.0;
        for (Attendance a : list) {
            double hours = 0.0;
            boolean qualified = false;
            if (a.getCheckInTime() != null && a.getCheckOutTime() != null) {
                hours = java.time.Duration.between(a.getCheckInTime(), a.getCheckOutTime()).toMinutes() / 60.0;
                qualified = hours >= MIN_HOURS;
            }
            double daySalary = 0.0;
            if (qualified) {
                daySalary = dailySalaryRate;
                if (a.getOvertimeHours() != null && a.getOvertimeHours() > 0) {
                    daySalary += a.getOvertimeHours() * (user.getHourlyRate() != null ? user.getHourlyRate() : 0.0);
                }
                if (a.getDeductionHours() != null && a.getDeductionHours() > 0) {
                    daySalary -= a.getDeductionHours() * deductionRate;
                }
                daysWorked++;
            } else {
                // Didn't qualify - no salary if less than 6 hours
                daySalary = 0.0;
            }
            totalSalary += daySalary;
            // Create daily breakdown with overtime/deduction info
            OSS_DailySalaryDto dayDto = new OSS_DailySalaryDto();
            dayDto.setDate(a.getWorkDate());
            dayDto.setHours(hours);
            dayDto.setSalary(daySalary);
            dayDto.setOvertimeHours(a.getOvertimeHours() != null ? a.getOvertimeHours() : 0.0);
            dayDto.setDeductionHours(a.getDeductionHours() != null ? a.getDeductionHours() : 0.0);
            dayDto.setOvertimeReason(a.getOvertimeReason());
            dayDto.setDeductionReason(a.getDeductionReason());
            dayDto.setQualified(qualified);
            daily.add(dayDto);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("dailySalary", dailySalaryRate);
        res.put("deductionRatePerHour", deductionRate);
        res.put("totalDaysWorked", daysWorked);
        res.put("totalSalary", totalSalary);
        res.put("minHoursRequired", MIN_HOURS);
        res.put("dailyBreakdown", daily);
        return res;
    }
    public Map<String, Object> getUserMonthlySalary(Long userId, int year, int month) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        List<Attendance> list = attendanceRepository.findByUserAndWorkDateBetweenOrderByWorkDateDesc(user, from, to);
        double totalSalary = 0.0;
        int daysWorked = 0;
        final double MIN_HOURS = 6.0;
        Double dailySalaryRate = user.getDailySalary() != null ? user.getDailySalary() : 0.0;
        Double hourlyRate = user.getHourlyRate() != null ? user.getHourlyRate() : 0.0;
        Double deductionRate = user.getDeductionRatePerHour() != null ? user.getDeductionRatePerHour() : 0.0;
        List<Map<String, Object>> dailyBreakdown = new ArrayList<>();

        for (Attendance a : list) {
            double hours = 0.0;
            double daySalary = 0.0;
            boolean qualified = false;

            // ✅ FIX: Handle legacy "WORKING" status records without timestamps
            if (a.getStatus() == AttendanceStatus.WORKING && a.getCheckInTime() == null && a.getCheckOutTime() == null) {
                // Legacy record - assume full day worked (8 hours)
                hours = 8.0;
                qualified = true;
                daySalary = dailySalaryRate;
                daysWorked++;

                // Still apply overtime and deductions
                if (a.getOvertimeHours() != null && a.getOvertimeHours() > 0) {
                    daySalary += a.getOvertimeHours() * hourlyRate;
                }
                if (a.getDeductionHours() != null && a.getDeductionHours() > 0) {
                    daySalary -= a.getDeductionHours() * deductionRate;
                }
            }
            // ✅ Handle modern records with timestamps
            else if (a.getCheckInTime() != null && a.getCheckOutTime() != null) {
                long minutes = a.getWorkedMinutes();
                hours = minutes / 60.0;

                if (hours >= MIN_HOURS) {
                    daySalary = dailySalaryRate;
                    daysWorked++;
                    qualified = true;

                    // Add overtime
                    if (a.getOvertimeHours() != null && a.getOvertimeHours() > 0) {
                        daySalary += a.getOvertimeHours() * hourlyRate;
                    }
                    // Deduct
                    if (a.getDeductionHours() != null && a.getDeductionHours() > 0) {
                        daySalary -= a.getDeductionHours() * deductionRate;
                    }
                }
            }
            // ✅ Handle NOT_WORKING or incomplete records
            else if (a.getStatus() == AttendanceStatus.NOT_WORKING) {
                hours = 0.0;
                qualified = false;
                daySalary = 0.0;
            }

            totalSalary += daySalary;

            Map<String, Object> day = new HashMap<>();
            day.put("date", a.getWorkDate().toString());
            day.put("hours", hours);
            day.put("salary", daySalary);
            day.put("overtimeHours", a.getOvertimeHours() != null ? a.getOvertimeHours() : 0.0);
            day.put("deductionHours", a.getDeductionHours() != null ? a.getDeductionHours() : 0.0);
            day.put("overtimeReason", a.getOvertimeReason());
            day.put("deductionReason", a.getDeductionReason());
            day.put("qualified", qualified);
            day.put("status", a.getStatus().name());
            dailyBreakdown.add(day);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("userId", user.getId());
        res.put("name", user.getName());
        res.put("email", user.getEmail());
        res.put("dailySalary", dailySalaryRate);
        res.put("hourlyRate", hourlyRate);
        res.put("deductionRatePerHour", deductionRate);
        res.put("totalDaysWorked", daysWorked);

        // ✅ NEW: Get credits for this month and deduct from salary
        Double totalCredits = creditRepository.sumCreditsByUserIdAndDateRange(userId, from, to);
        if (totalCredits == null) {
            totalCredits = 0.0;
        }

        // Calculate final salary after credits deduction
        double finalSalary = totalSalary - totalCredits;

        res.put("baseSalary", totalSalary);        // Salary before credits
        res.put("totalCredits", totalCredits);      // Credits to deduct
        res.put("totalSalary", finalSalary);        // Final salary after credits
        res.put("dailyBreakdown", dailyBreakdown);
        res.put("minHoursRequired", MIN_HOURS);
        return res;
    }
}