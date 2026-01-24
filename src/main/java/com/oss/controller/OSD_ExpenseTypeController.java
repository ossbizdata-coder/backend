package com.oss.controller;

import com.oss.model.ExpenseType;
import com.oss.repository.ExpenseTypeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/expenses/types", "/api/expense-types"})
public class OSD_ExpenseTypeController {

    private final ExpenseTypeRepository expenseTypeRepository;

    public OSD_ExpenseTypeController(ExpenseTypeRepository expenseTypeRepository) {
        this.expenseTypeRepository = expenseTypeRepository;
    }

    @PostMapping
    public ResponseEntity<?> addExpenseType(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String shopType = body.get("shopType");

        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body("Expense type name is required");
        }

        if (shopType == null || shopType.isEmpty()) {
            return ResponseEntity.badRequest().body("Shop type is required");
        }

        ExpenseType expenseType = new ExpenseType();
        expenseType.setName(name);
        expenseType.setShopType(shopType);

        ExpenseType saved = expenseTypeRepository.save(expenseType);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseType>> getExpenseTypes(
            @RequestParam(required = false) String shopType,
            @RequestParam(required = false) String department) {

        // Support both shopType and department parameters
        String filterType = department != null && !department.isEmpty() ? department : shopType;

        if (filterType != null && !filterType.isEmpty()) {
            return ResponseEntity.ok(expenseTypeRepository.findByShopType(filterType));
        }

        return ResponseEntity.ok(expenseTypeRepository.findAll());
    }
}

