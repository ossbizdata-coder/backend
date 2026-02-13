package com.oss.controller;
import com.oss.dto.*;
import com.oss.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }
    /**
     * GET /api/reports/bank-deposits?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
     * Get all bank deposits for a date range (SUPERADMIN only)
     */
    @GetMapping("/bank-deposits")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<List<BankDepositDTO>> getBankDeposits(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getBankDeposits(startDate, endDate));
    }
    /**
     * GET /api/reports/expenses/daily/{date}
     * Get daily expense report (SUPERADMIN only)
     */
    @GetMapping("/expenses/daily/{date}")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<DailyExpenseReportDTO> getDailyExpenseReport(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reportService.getDailyExpenseReport(date));
    }
    /**
     * GET /api/reports/expenses/monthly/{year}/{month}
     * Get monthly expense report (SUPERADMIN only)
     */
    @GetMapping("/expenses/monthly/{year}/{month}")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<MonthlyExpenseReportDTO> getMonthlyExpenseReport(
            @PathVariable int year,
            @PathVariable int month) {
        // Validate month
        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reportService.getMonthlyExpenseReport(year, month));
    }
    /**
     * GET /api/reports/expenses/by-category?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
     * Get expenses grouped by category (SUPERADMIN only)
     */
    @GetMapping("/expenses/by-category")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<ExpenseByCategoryReportDTO> getExpensesByCategory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getExpensesByCategory(startDate, endDate));
    }
    /**
     * GET /api/reports/expenses/by-shop?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
     * Get expenses grouped by shop (SUPERADMIN only)
     */
    @GetMapping("/expenses/by-shop")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<ExpenseByShopReportDTO> getExpensesByShop(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getExpensesByShop(startDate, endDate));
    }
    /**
     * GET /api/reports/business-summary
     * Get overall business performance summary (SUPERADMIN only)
     */
    @GetMapping("/business-summary")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<BusinessSummaryDTO> getBusinessSummary() {
        return ResponseEntity.ok(reportService.getBusinessSummary());
    }
    /**
     * GET /api/reports/staff-performance?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
     * Get staff performance metrics using pre-calculated daily summaries (SUPERADMIN only)
     * This is optimized for fast loading - uses daily_summaries table
     */
    @GetMapping("/staff-performance")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<?> getStaffPerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getStaffPerformance(startDate, endDate));
    }
    /**
     * GET /api/reports/shop-performance/{shopId}?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
     * Get shop performance metrics using pre-calculated daily summaries (SUPERADMIN only)
     * This is optimized for fast loading - uses daily_summaries table
     */
    @GetMapping("/shop-performance/{shopId}")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<?> getShopPerformance(
            @PathVariable Long shopId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getShopPerformance(shopId, startDate, endDate));
    }
}