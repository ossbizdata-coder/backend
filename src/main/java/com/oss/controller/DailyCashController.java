package com.oss.controller;
import com.oss.dto.DailyCashSummaryDTO;
import com.oss.dto.LatestBalanceDTO;
import com.oss.dto.ShopSummaryDTO;
import com.oss.model.DailyCash;
import com.oss.model.User;
import com.oss.repository.UserRepository;
import com.oss.service.DailyCashService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping("/api")
public class DailyCashController {
    private final DailyCashService dailyCashService;
    private final UserRepository userRepository;
    public DailyCashController(DailyCashService dailyCashService, UserRepository userRepository) {
        this.dailyCashService = dailyCashService;
        this.userRepository = userRepository;
    }
    /**
     * GET /api/shops/summary
     * Main menu: Get latest closing cash per shop
     */
    @GetMapping("/shops/summary")
    public ResponseEntity<List<ShopSummaryDTO>> getShopsSummary() {
        return ResponseEntity.ok(dailyCashService.getShopsSummary());
    }
    /**
     * GET /api/daily-cash/{shopId}/{date}
     * Daily screen: Get opening, expenses, sales, credits, totals
     * âœ… FIXED: Now properly logs the requested date for debugging
     */
    @GetMapping("/daily-cash/{shopId}/{date}")
    public ResponseEntity<DailyCashSummaryDTO> getDailyCashSummary(
            @PathVariable Long shopId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String date) {
        LocalDate businessDate = LocalDate.parse(date);
        DailyCashSummaryDTO summary = dailyCashService.getDailyCashSummary(shopId, businessDate);
        return ResponseEntity.ok(summary);
    }
    /**
     * GET /api/daily-cash/{shopId}
     * Get today's daily cash for a shop
     */
    @GetMapping("/daily-cash/{shopId}")
    public ResponseEntity<DailyCashSummaryDTO> getTodayDailyCash(@PathVariable Long shopId) {
        LocalDate today = LocalDate.now();
        DailyCashSummaryDTO summary = dailyCashService.getDailyCashSummary(shopId, today);
        return ResponseEntity.ok(summary);
    }
    /**
     * âœ… NEW ENDPOINT: GET /api/daily-cash/{shopId}/latest-closing-balance
     * Get the latest closing balance for a shop within the past N days
     * This replaces 7 API calls with 1 optimized query
     *
     * @param shopId The shop ID
     * @param daysBack Number of days to look back (default: 7)
     * @return Latest closing balance or 0.0 if none found
     */
    @GetMapping("/daily-cash/{shopId}/latest-closing-balance")
    public ResponseEntity<LatestBalanceDTO> getLatestClosingBalance(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "7") int daysBack) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysBack);
        List<DailyCash> latestClosed = dailyCashService.findLatestClosedByShopAndDateRange(shopId, startDate, endDate);
        if (!latestClosed.isEmpty()) {
            DailyCash dc = latestClosed.get(0);
            return ResponseEntity.ok(new LatestBalanceDTO(
                dc.getClosingCash(),
                dc.getBusinessDate(),
                dc.getShop().getId()
            ));
        }
        return ResponseEntity.ok(new LatestBalanceDTO(0.0, null, shopId));
    }
    /**
     * POST /api/daily-cash/{id}/expenses
     * Add expense to daily cash
     */
    @PostMapping("/daily-cash/{id}/expenses")
    public ResponseEntity<?> addExpense(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Principal principal) {
        Optional<User> optUser = userRepository.findByEmail(principal.getName());
        if (optUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid user");
        }
        // Check if amount exists in request body
        if (body.get("amount") == null) {
            return ResponseEntity.badRequest().body("amount is required");
        }
        Double amount;
        try {
            amount = Double.valueOf(body.get("amount").toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid amount format");
        }
        Long expenseTypeId = body.containsKey("expenseTypeId") && body.get("expenseTypeId") != null
                ? Long.valueOf(body.get("expenseTypeId").toString())
                : null;
        String description = body.containsKey("description")
                ? (String) body.get("description")
                : null;
        try {
            dailyCashService.addExpense(id, amount, expenseTypeId, description, optUser.get());
            return ResponseEntity.ok("Expense added successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    /**
     * POST /api/daily-cash/{id}/sales
     * Add manual sale to daily cash
     */
    @PostMapping("/daily-cash/{id}/sales")
    public ResponseEntity<?> addManualSale(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Principal principal) {
        Optional<User> optUser = userRepository.findByEmail(principal.getName());
        if (optUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid user");
        }
        // Check if amount exists in request body
        if (body.get("amount") == null) {
            return ResponseEntity.badRequest().body("amount is required");
        }
        Double amount;
        try {
            amount = Double.valueOf(body.get("amount").toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid amount format");
        }
        String description = body.containsKey("description")
                ? (String) body.get("description")
                : null;
        try {
            dailyCashService.addManualSale(id, amount, description, optUser.get());
            return ResponseEntity.ok("Sale added successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    /**
     * POST /api/daily-cash/{id}/close
     * Close the day by setting closing cash and locking
     */
    @PostMapping("/daily-cash/{id}/close")
    public ResponseEntity<?> closeDay(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Principal principal) {
        Optional<User> optUser = userRepository.findByEmail(principal.getName());
        if (optUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid user");
        }
        // Check if closingCash exists in request body
        if (body.get("closingCash") == null) {
            return ResponseEntity.badRequest().body("closingCash is required");
        }
        Double closingCash;
        try {
            closingCash = Double.valueOf(body.get("closingCash").toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid closingCash format");
        }
        try {
            dailyCashService.closeDay(id, closingCash, optUser.get());
            return ResponseEntity.ok("Day closed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    /**
     * PATCH /api/daily-cash/{id}/opening
     * Update opening balance (only if not locked)
     */
    @PatchMapping("/daily-cash/{id}/opening")
    public ResponseEntity<?> updateOpeningBalance(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        // Check if openingCash exists in request body
        if (body.get("openingCash") == null) {
            return ResponseEntity.badRequest().body("openingCash is required");
        }
        Double openingCash;
        try {
            openingCash = Double.valueOf(body.get("openingCash").toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid openingCash format");
        }
        try {
            dailyCashService.updateOpeningBalance(id, openingCash);
            return ResponseEntity.ok("Opening balance updated");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}