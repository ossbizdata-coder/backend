package com.oss.controller;

import com.oss.model.StaffSalaryReport;
import com.oss.service.AttendanceService;
import com.oss.service.SalaryReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/salary")
@RequiredArgsConstructor
public class OSS_SalaryController {

    private final AttendanceService attendanceService;
    private final SalaryReportService salaryReportService;

    // ======================
    // TODAY SALARY (ME)
    // ======================
    @GetMapping("/today")
    public Map<String, Object> todaySalary() {
        return attendanceService.calculateTodaySalary();
    }

    // ======================
    // ADMIN: MONTHLY STAFF SALARY
    // ======================
    @GetMapping("/admin/monthly")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public List<StaffSalaryReport> monthlyStaffSalary(
            @RequestParam int year,
            @RequestParam int month
                                                     ) {
        return salaryReportService.getMonthlyStaffSalary(year, month);
    }

    // ======================
    // MY MONTHLY SALARY
    // ======================
    @GetMapping("/me/monthly")
    public Map<String, Object> myMonthlySalary(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return attendanceService.calculateMyMonthlySalary(year, month);
    }

    // ======================
    // USER MONTHLY SALARY (FOR ADMIN)
    // ======================
    @GetMapping("/user/{userId}/monthly")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public Map<String, Object> getUserMonthlySalary(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return salaryReportService.getUserMonthlySalary(userId, year, month);
    }
}
