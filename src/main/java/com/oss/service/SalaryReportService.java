package com.oss.service;

import com.oss.dto.OSS_DailySalaryDto;
import com.oss.model.Attendance;
import com.oss.model.Role;
import com.oss.model.StaffSalaryReport;
import com.oss.model.User;
import com.oss.repository.AttendanceRepository;
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
        if (admin.getRole() != Role.SUPERADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied"
            );
        }
        ZoneId zone = ZoneId.of("Asia/Colombo");
        Instant from = LocalDate.of(year, month, 1).atStartOfDay(zone).toInstant();
        Instant to = LocalDate.of(year, month, 1).withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth()).atStartOfDay(zone).toInstant();
        return attendanceRepository.getMonthlyStaffSalary(from, to);
    }

    public Map<String, Object> calculateMyMonthlySalary(int year, int month) {
        User user = getCurrentUser();
        ZoneId zone = ZoneId.of("Asia/Colombo");
        Instant from = LocalDate.of(year, month, 1).atStartOfDay(zone).toInstant();
        Instant to = LocalDate.of(year, month, 1).withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth()).atStartOfDay(zone).toInstant();
        List<Attendance> list = attendanceRepository.findByUserAndWorkDateBetweenOrderByWorkDateDesc(user, from, to);

        List<OSS_DailySalaryDto> daily = new ArrayList<>();
        double totalSalary = 0.0;
        int daysWorked = 0;
        final double MIN_HOURS = 6.0; // Minimum hours to qualify for full daily salary

        Double dailySalaryRate = user.getDailySalary() != null ? user.getDailySalary() : 0.0;
        Double deductionRate = user.getDeductionRatePerHour() != null ? user.getDeductionRatePerHour() : 0.0;

        for (Attendance a : list) {
            if (a.getCheckOutTime() == null) continue;

            long minutes = a.getWorkedMinutes();
            double hours = minutes / 60.0;
            double daySalary = 0.0;
            boolean qualified = false;

            // Check if qualified for full daily salary
            if (hours >= MIN_HOURS) {
                daySalary = dailySalaryRate;
                daysWorked++;
                qualified = true;

                // Add overtime if any
                if (a.getOvertimeHours() != null && a.getOvertimeHours() > 0) {
                    double overtimePay = a.getOvertimeHours() * deductionRate;
                    daySalary += overtimePay;
                }

                // Deduct if any
                if (a.getDeductionHours() != null && a.getDeductionHours() > 0) {
                    double deductionAmount = a.getDeductionHours() * deductionRate;
                    daySalary -= deductionAmount;
                }
            } else {
                // Didn't qualify - no salary if less than 6 hours
                daySalary = 0.0;
            }

            totalSalary += daySalary;

            // Create daily breakdown with overtime/deduction info
            OSS_DailySalaryDto dayDto = new OSS_DailySalaryDto();
            dayDto.setDate(a.getWorkDate().atZone(zone).toLocalDate());
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
        res.put("dailyBreakdown", daily);
        res.put("minHoursRequired", MIN_HOURS);
        return res;
    }

    public Map<String, Object> getUserMonthlySalary(Long userId, int year, int month) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        ZoneId zone = ZoneId.of("Asia/Colombo");
        Instant from = LocalDate.of(year, month, 1).atStartOfDay(zone).toInstant();
        Instant to = LocalDate.of(year, month, 1).withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth()).atStartOfDay(zone).toInstant();
        List<Attendance> list = attendanceRepository.findByUserAndWorkDateBetweenOrderByWorkDateDesc(user, from, to);

        double totalSalary = 0.0;
        int daysWorked = 0;
        final double MIN_HOURS = 6.0;

        Double dailySalaryRate = user.getDailySalary() != null ? user.getDailySalary() : 0.0;
        Double deductionRate = user.getDeductionRatePerHour() != null ? user.getDeductionRatePerHour() : 0.0;

        List<Map<String, Object>> dailyBreakdown = new ArrayList<>();

        for (Attendance a : list) {
            long minutes = a.getWorkedMinutes();
            double hours = minutes / 60.0;
            double daySalary = 0.0;
            boolean qualified = false;

            if (hours >= MIN_HOURS) {
                daySalary = dailySalaryRate;
                daysWorked++;
                qualified = true;

                // Add overtime
                if (a.getOvertimeHours() != null && a.getOvertimeHours() > 0) {
                    daySalary += a.getOvertimeHours() * deductionRate;
                }

                // Deduct
                if (a.getDeductionHours() != null && a.getDeductionHours() > 0) {
                    daySalary -= a.getDeductionHours() * deductionRate;
                }
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
            dailyBreakdown.add(day);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("userId", user.getId());
        res.put("name", user.getName());
        res.put("email", user.getEmail());
        res.put("dailySalary", dailySalaryRate);
        res.put("deductionRatePerHour", deductionRate);
        res.put("totalDaysWorked", daysWorked);
        res.put("totalSalary", totalSalary);
        res.put("dailyBreakdown", dailyBreakdown);
        res.put("minHoursRequired", MIN_HOURS);
        return res;
    }

}
