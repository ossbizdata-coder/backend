package com.oss.controller;
import com.oss.dto.OSD_TransactionResponse;
import com.oss.dto.OSD_TransactionUpdateRequest;
import com.oss.model.Credit;
import com.oss.model.Role;
import com.oss.model.Transaction;
import com.oss.model.User;
import com.oss.repository.CreditRepository;
import com.oss.repository.TransactionRepository;
import com.oss.repository.UserRepository;
import com.oss.service.OSD_TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api")
public class OSD_TransactionController {
    private final OSD_TransactionService OSDTransactionService;
    private final TransactionRepository transactionRepo;
    private final UserRepository userRepository;
    private final CreditRepository creditRepository;
    public OSD_TransactionController(OSD_TransactionService OSDTransactionService,
                                     TransactionRepository transactionRepo,
                                     UserRepository userRepository,
                                     CreditRepository creditRepository) {
        this.OSDTransactionService = OSDTransactionService;
        this.transactionRepo = transactionRepo;
        this.userRepository = userRepository;
        this.creditRepository = creditRepository;
    }
    @PostMapping("/transactions")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body,
                                    Principal principal) {
        Optional<User> optUser = userRepository.findByEmail(principal.getName());
        if (optUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid user");
        }
        OSDTransactionService.saveTransaction(body, optUser.get());
        return ResponseEntity.ok("Transaction recorded");
    }
    @GetMapping("/transactions/daily")
    public List<OSD_TransactionResponse> getTodayTransactions(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String date,
            Principal principal) {
        // Get current user to check permissions
        User currentUser = null;
        if (principal != null) {
            currentUser = userRepository.findByEmail(principal.getName()).orElse(null);
        }
        boolean isSuperAdmin = currentUser != null && currentUser.getRole() == Role.SUPERADMIN;
        ZoneId zone = ZoneId.of("Asia/Colombo");
        LocalDate targetDate;
        // Parse date parameter or use today in Sri Lanka timezone
        if (date != null && !date.isEmpty()) {
            targetDate = LocalDate.parse(date);
        } else {
            targetDate = LocalDate.now(zone);
        }
        // Convert to UTC midnight epoch milliseconds for SINGLE DAY
        Long dateMillis = targetDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();
        System.out.println("=== DEBUG: /api/transactions/daily ===");
        System.out.println("Target Date: " + targetDate);
        System.out.println("Date epoch (UTC): " + dateMillis);
        System.out.println("Department filter: " + department);
        System.out.println("Category filter: " + category);
        // DEBUG: Check what values exist in database
        List<Transaction> allTransactions = transactionRepo.findAll();
        System.out.println("Total transactions in database: " + allTransactions.size());
        if (!allTransactions.isEmpty()) {
            System.out.println("Sample business_date values from DB:");
            allTransactions.stream().limit(5).forEach(t -> {
                System.out.println("  ID: " + t.getId() +
                    ", business_date: " + t.getBusinessDate() +
                    " (as date: " + (t.getBusinessDate() != null ?
                        java.time.Instant.ofEpochMilli(t.getBusinessDate()).atZone(ZoneId.of("UTC")).toLocalDate() : "null") + ")" +
                    ", dept: " + t.getDepartment());
            });
        }
        List<Transaction> transactions;
        // Query database for specific date
        if (department != null && !department.isEmpty()) {
            transactions = transactionRepo.findByDepartmentAndBusinessDate(department, dateMillis);
        } else {
            transactions = transactionRepo.findByBusinessDate(dateMillis);
        }
        System.out.println("Found " + transactions.size() + " transactions for date " + dateMillis);
        // Apply category filter if specified
        if (category != null && !category.isEmpty()) {
            transactions = transactions.stream()
                    .filter(t -> category.equals(t.getCategory()))
                    .collect(Collectors.toList());
            System.out.println("After category filter: " + transactions.size());
        }
        System.out.println("Total transactions to return: " + transactions.size());
        System.out.println("=====================================");
        // Convert to DTO to flatten expense type fields and set permissions
        return transactions.stream()
                .map(t -> {
                    OSD_TransactionResponse dto = OSD_TransactionResponse.from(t);
                    dto.setCanEdit(isSuperAdmin);
                    dto.setCanDelete(isSuperAdmin);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    @GetMapping("/transactions/by-date")
    public ResponseEntity<List<OSD_TransactionResponse>> getTransactionsByDate(
            @RequestParam String date,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String category,
            Principal principal) {
        // Get current user to check permissions
        User currentUser = null;
        if (principal != null) {
            currentUser = userRepository.findByEmail(principal.getName()).orElse(null);
        }
        boolean isSuperAdmin = currentUser != null && currentUser.getRole() == Role.SUPERADMIN;
        System.out.println("=== DEBUG: /api/transactions/by-date ===");
        System.out.println("Date parameter: " + date);
        System.out.println("Department filter: " + department);
        System.out.println("Category filter: " + category);
        try {
            // Parse date string (format: YYYY-MM-DD)
            LocalDate targetDate = LocalDate.parse(date);
            // Convert to UTC midnight epoch milliseconds
            Long dateMillis = targetDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();
            System.out.println("Parsed date: " + targetDate);
            System.out.println("Date epoch (UTC): " + dateMillis);
            List<Transaction> transactions;
            // Query based on filters
            if (department != null && !department.isEmpty() && category != null && !category.isEmpty()) {
                transactions = transactionRepo.findByDepartmentAndCategoryAndBusinessDate(
                    department, category, dateMillis
                );
            } else if (department != null && !department.isEmpty()) {
                transactions = transactionRepo.findByDepartmentAndBusinessDate(department, dateMillis);
            } else {
                transactions = transactionRepo.findByBusinessDate(dateMillis);
            }
            System.out.println("Found " + transactions.size() + " transactions");
            System.out.println("=====================================");
            // Convert to DTO and set permissions
            List<OSD_TransactionResponse> response = transactions.stream()
                    .map(t -> {
                        OSD_TransactionResponse dto = OSD_TransactionResponse.from(t);
                        dto.setCanEdit(isSuperAdmin);
                        dto.setCanDelete(isSuperAdmin);
                        return dto;
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error parsing date or fetching transactions: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/transactions/department-summary")
    public ResponseEntity<?> getDepartmentSummary(
            @RequestParam String department,
            @RequestParam(required = false) String date) {
        ZoneId zone = ZoneId.of("Asia/Colombo");
        LocalDate targetDate;
        // Parse date parameter or use today
        if (date != null && !date.isEmpty()) {
            targetDate = LocalDate.parse(date);
        } else {
            targetDate = LocalDate.now(zone);
        }
        // Convert to UTC midnight epoch milliseconds (how database stores business_date)
        Long dateMillis = targetDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();
        // Get all transactions for the department on the target date
        List<Transaction> transactions = transactionRepo.findByDepartmentAndBusinessDate(department, dateMillis);
        // Get all credits for the department on the target date
        List<Credit> credits = creditRepository.findByDepartmentAndTransactionDate(department, targetDate);
        Double totalCredits = creditRepository.sumCreditsByDepartmentAndDate(department, targetDate);
        if (totalCredits == null) totalCredits = 0.0;
        // Initialize values
        Double openingBalance = 0.0;
        Double closingBalance = 0.0;
        Double totalExpenses = 0.0;
        // Lists to track detailed items
        List<Map<String, Object>> expenseItems = new java.util.ArrayList<>();
        List<Map<String, Object>> salesItems = new java.util.ArrayList<>();
        Map<Long, Map<String, Object>> expensesByType = new HashMap<>();
        // Process transactions
        for (Transaction t : transactions) {
            if ("SALE".equals(t.getCategory())) {
                if (t.getOpeningBalance() != null && t.getOpeningBalance() > 0) {
                    openingBalance = t.getOpeningBalance();
                }
                if (t.getClosingBalance() != null && t.getClosingBalance() > 0) {
                    closingBalance = t.getClosingBalance();
                }
                // Add to sales items list
                Map<String, Object> saleItem = new HashMap<>();
                saleItem.put("id", t.getId());
                saleItem.put("item", t.getItemName());
                saleItem.put("amount", t.getAmount());
                saleItem.put("comment", t.getComment());
                saleItem.put("transactionDate", t.getTransactionTime());
                salesItems.add(saleItem);
            } else if ("EXPENSE".equals(t.getCategory())) {
                totalExpenses += t.getAmount();
                // Add to expense items list
                Map<String, Object> expenseItem = new HashMap<>();
                expenseItem.put("id", t.getId());
                expenseItem.put("expenseTypeName", t.getExpenseType() != null ? t.getExpenseType().getName() : "Unknown");
                expenseItem.put("amount", t.getAmount());
                expenseItem.put("comment", t.getComment());
                expenseItem.put("transactionDate", t.getTransactionTime());
                expenseItems.add(expenseItem);
                // Group by expense type for breakdown
                if (t.getExpenseType() != null) {
                    Long typeId = t.getExpenseType().getId();
                    expensesByType.computeIfAbsent(typeId, k -> {
                        Map<String, Object> typeMap = new HashMap<>();
                        typeMap.put("expenseTypeId", typeId);
                        typeMap.put("expenseTypeName", t.getExpenseType().getName());
                        typeMap.put("totalAmount", 0.0);
                        return typeMap;
                    });
                    Map<String, Object> typeMap = expensesByType.get(typeId);
                    typeMap.put("totalAmount", (Double) typeMap.get("totalAmount") + t.getAmount());
                }
            }
        }
        // Process credits
        List<Map<String, Object>> creditItems = new java.util.ArrayList<>();
        for (Credit c : credits) {
            Map<String, Object> creditItem = new HashMap<>();
            creditItem.put("id", c.getId());
            creditItem.put("userName", c.getUser() != null ? c.getUser().getName() : "Unknown");
            creditItem.put("amount", c.getAmount());
            creditItem.put("reason", c.getReason());
            creditItem.put("isPaid", c.getIsPaid());
            creditItem.put("transactionDate", c.getCreatedAt());
            creditItems.add(creditItem);
        }
        // Calculate sales using formula: Sales = Closing - Opening + Expenses + Credits
        Double calculatedSales = closingBalance - openingBalance + totalExpenses + totalCredits;
        // Determine profit margin based on department
        Double profitMargin = switch (department.toUpperCase()) {
            case "CAFE" -> 12.0;
            case "BOOKSHOP" -> 15.0;
            case "FOODHUT" -> 20.0;
            default -> 10.0;
        };
        // Calculate profit: Profit = Sales Ã— (Profit Margin / 100)
        Double profit = calculatedSales * (profitMargin / 100.0);
        Double profitPercentage = profitMargin / 100.0;
        // Build response
        Map<String, Object> summary = new HashMap<>();
        summary.put("department", department);
        summary.put("date", targetDate.toString());
        summary.put("openingBalance", openingBalance);
        summary.put("closingBalance", closingBalance);
        summary.put("totalExpenses", totalExpenses);
        summary.put("totalCredits", totalCredits);
        summary.put("calculatedSales", calculatedSales);
        summary.put("profit", profit);
        summary.put("profitPercentage", profitPercentage);
        summary.put("profitMargin", profitMargin); // Also include as percentage number
        summary.put("expenseItems", expenseItems);
        summary.put("creditItems", creditItems);
        summary.put("salesItems", salesItems);
        summary.put("expenseBreakdown", new java.util.ArrayList<>(expensesByType.values()));
        return ResponseEntity.ok(summary);
    }
    @GetMapping("/transactions/department-cash-total")
    public ResponseEntity<?> getDepartmentCashTotal(@RequestParam String department) {
        Transaction latestSale = transactionRepo.findLatestClosingBalanceByDepartment(department);
        Map<String, Object> response = new HashMap<>();
        response.put("department", department);
        if (latestSale != null && latestSale.getClosingBalance() != null) {
            response.put("cashTotal", latestSale.getClosingBalance());
            response.put("lastUpdated", latestSale.getTransactionTime());
        } else {
            response.put("cashTotal", 0.0);
            response.put("lastUpdated", null);
        }
        return ResponseEntity.ok(response);
    }
    @GetMapping("/transactions/daily-summary")
    public ResponseEntity<?> getDailySummary(@RequestParam String department) {
        ZoneId zone = ZoneId.of("Asia/Colombo");
        LocalDate today = LocalDate.now(zone);
        Long todayMillis = today.atStartOfDay(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli();
        // Calculate today's total expenses for the department
        Double totalExpenses = transactionRepo.sumExpensesByDepartmentAndDate(department, todayMillis);
        // Get today's sales entry if exists
        Instant start = today.atStartOfDay(zone).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(zone).toInstant();
        List<Transaction> salesEntries = transactionRepo.findByDepartmentAndCategoryAndTimeRange(
                department, "SALE", start, end
        );
        // Convert to DTOs
        List<OSD_TransactionResponse> salesDTOs = salesEntries.stream()
                .map(OSD_TransactionResponse::from)
                .collect(Collectors.toList());
        Map<String, Object> summary = new HashMap<>();
        summary.put("department", department);
        summary.put("date", today);
        summary.put("totalExpenses", totalExpenses);
        summary.put("salesEntries", salesDTOs);
        return ResponseEntity.ok(summary);
    }
    @GetMapping("/transactions/debug-dates")
    public ResponseEntity<?> debugDates() {
        Map<String, Object> debug = new HashMap<>();
        // Show server timezone info
        debug.put("serverDefaultZone", ZoneId.systemDefault().getId());
        debug.put("serverCurrentTime", Instant.now().toString());
        // Show what UTC today is
        LocalDate utcToday = LocalDate.now(ZoneId.of("UTC"));
        Long utcTodayMillis = utcToday.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();
        debug.put("utcToday", utcToday.toString());
        debug.put("utcTodayEpoch", utcTodayMillis);
        // Show what Asia/Colombo today is
        LocalDate colomboToday = LocalDate.now(ZoneId.of("Asia/Colombo"));
        Long colomboTodayMillis = colomboToday.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();
        debug.put("colomboToday", colomboToday.toString());
        debug.put("colomboTodayEpoch", colomboTodayMillis);
        // Show all unique business_date values in database
        List<Long> uniqueDates = transactionRepo.findAll().stream()
            .map(Transaction::getBusinessDate)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        List<Map<String, Object>> dateInfo = new java.util.ArrayList<>();
        for (Long dateMillis : uniqueDates) {
            if (dateMillis != null) {
                Map<String, Object> info = new HashMap<>();
                info.put("epochMillis", dateMillis);
                info.put("asUTCDate", Instant.ofEpochMilli(dateMillis).atZone(ZoneId.of("UTC")).toLocalDate().toString());
                info.put("asColomboDate", Instant.ofEpochMilli(dateMillis).atZone(ZoneId.of("Asia/Colombo")).toLocalDate().toString());
                // Count transactions with this date
                long count = transactionRepo.findAll().stream()
                    .filter(t -> dateMillis.equals(t.getBusinessDate()))
                    .count();
                info.put("transactionCount", count);
                dateInfo.add(info);
            }
        }
        debug.put("uniqueBusinessDatesInDB", dateInfo);
        return ResponseEntity.ok(debug);
    }
    /**
     * UPDATE TRANSACTION - SUPERADMIN ONLY
     * PUT /api/transactions/{id}
     */
    @PutMapping("/transactions/{id}")
    public ResponseEntity<?> updateTransaction(
            @PathVariable Long id,
            @RequestBody OSD_TransactionUpdateRequest request,
            Principal principal) {
        // Get current user
        Optional<User> optUser = userRepository.findByEmail(principal.getName());
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user");
        }
        User user = optUser.get();
        // Check if user is SUPERADMIN
        if (user.getRole() != Role.SUPERADMIN) {
            System.err.println("ACCESS DENIED: User " + user.getEmail() + " attempted to edit transaction " + id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only SUPERADMIN can edit transactions");
        }
        try {
            Transaction updated = OSDTransactionService.updateTransaction(id, request, user);
            OSD_TransactionResponse response = OSD_TransactionResponse.from(updated);
            System.out.println("Transaction " + id + " updated by SUPERADMIN: " + user.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    /**
     * DELETE TRANSACTION - SUPERADMIN ONLY
     * DELETE /api/transactions/{id}
     */
    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<?> deleteTransaction(
            @PathVariable Long id,
            Principal principal) {
        // Get current user
        Optional<User> optUser = userRepository.findByEmail(principal.getName());
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user");
        }
        User user = optUser.get();
        // Check if user is SUPERADMIN
        if (user.getRole() != Role.SUPERADMIN) {
            System.err.println("ACCESS DENIED: User " + user.getEmail() + " attempted to delete transaction " + id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only SUPERADMIN can delete transactions");
        }
        try {
            OSDTransactionService.deleteTransaction(id, user);
            System.out.println("Transaction " + id + " deleted by SUPERADMIN: " + user.getEmail());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Transaction deleted successfully");
            response.put("id", id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}