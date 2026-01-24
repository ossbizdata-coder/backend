package com.oss.controller;
import com.oss.dto.Foodhut_DaySummaryResponse;
import com.oss.dto.Foodhut_RemainingItemDto;
import com.oss.dto.Foodhut_TransactionRequest;
import com.oss.dto.Foodhut_TransactionResponse;
import com.oss.model.User;
import com.oss.repository.UserRepository;
import com.oss.service.Foodhut_TransactionService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class Foodhut_SaleController {
    private final Foodhut_TransactionService saleService;
    private final UserRepository userRepository;
    @PostMapping
    public ResponseEntity<Void> addSale(@RequestBody Foodhut_TransactionRequest req) {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                                  .orElseThrow(() -> new RuntimeException("User not found"));
        saleService.addSale(
                req.getVariationId(),
                req.getPreparedQty(),
                req.getRemainingQty(),
                req.getActionType(),
                user
                           );
        return ResponseEntity.ok().build();
    }
    @GetMapping("/day")
    public List<Foodhut_TransactionResponse> getSalesForDay(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
                                                           ) {
        return saleService.getSalesForDay(date);
    }
    @GetMapping("/day/summary")
    public Foodhut_DaySummaryResponse getSummaryForDay(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
                                                      ) {
        return saleService.getSummaryForDay(date);
    }
    @GetMapping("/remaining/list")
    public List<Foodhut_RemainingItemDto> getRemainingForDay(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return saleService.getRemainingForDay(date);
    }
}