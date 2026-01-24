package com.oss.service;
import com.oss.dto.Foodhut_DaySummaryResponse;
import com.oss.dto.Foodhut_TransactionResponse;
import com.oss.dto.Foodhut_RemainingItemDto;
import com.oss.model.FoodhutItemVariation;
import com.oss.model.FoodhutSale;
import com.oss.model.SaleActionType;
import com.oss.model.User;
import com.oss.repository.FoodhutItemVariationRepository;
import com.oss.repository.FoodhutSaleRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class Foodhut_TransactionService {
    private final FoodhutSaleRepository saleRepo;
    private final FoodhutItemVariationRepository variationRepo;
    // ================================
    // ADD SALE ACTION
    // ================================
    public FoodhutSale addSale(
            Long variationId,
            int preparedQty,
            int remainingQty,
            SaleActionType actionType,
            User user
                              ) {
        if (preparedQty < 0 || remainingQty < 0) {
            throw new IllegalArgumentException("Quantities must be non-negative");
        }
        // Only check for PREPARED action
        if (actionType == SaleActionType.PREPARED && remainingQty > preparedQty) {
            throw new IllegalArgumentException("Remaining quantity cannot exceed prepared quantity");
        }
        FoodhutItemVariation variation = variationRepo.findById(variationId)
                                                      .orElseThrow(() -> new RuntimeException("Variation not found"));
        FoodhutSale sale = new FoodhutSale(
                variation,
                preparedQty,
                remainingQty,
                LocalDateTime.now(),
                user,
                actionType.name()
        );
        return saleRepo.save(sale);
    }
    // ================================
    // DAY SUMMARY
    // ================================
    public Foodhut_DaySummaryResponse getSummaryForDay(LocalDate date) {
        LocalDate target = (date == null) ? LocalDate.now() : date;
        LocalDateTime start = target.atStartOfDay();
        LocalDateTime end = target.atTime(LocalTime.MAX);
        List<FoodhutSale> sales = saleRepo.findAllForDay(start, end);
        int totalPrepared = 0;
        int totalRemaining = 0;
        int totalAmount = 0;
        int totalProfit = 0;
        Map<Long, Integer> preparedMap = new HashMap<>();
        Map<Long, Integer> remainingMap = new HashMap<>();
        for (FoodhutSale s : sales) {
            Long variationId = s.getItemVariation().getId();
            if ("PREPARED".equals(s.getActionType())) {
                preparedMap.merge(variationId, s.getPreparedQty(), Integer::sum);
                totalPrepared += s.getPreparedQty();
            }
            if ("REMAINING".equals(s.getActionType())) {
                remainingMap.merge(variationId, s.getRemainingQty(), Integer::sum);
                totalRemaining += s.getRemainingQty();
            }
        }
        for (Long variationId : preparedMap.keySet()) {
            int prepared = preparedMap.getOrDefault(variationId, 0);
            int remaining = remainingMap.getOrDefault(variationId, 0);
            int sold = prepared - remaining;
            if (sold > 0) {
                FoodhutItemVariation v = variationRepo.findById(variationId).orElseThrow();
                totalAmount += sold * v.getPrice();
                totalProfit += sold * (v.getPrice() - v.getCost());
            }
        }
        Foodhut_DaySummaryResponse res = new Foodhut_DaySummaryResponse();
        res.setTotalPreparedQty(totalPrepared);
        res.setTotalRemainingQty(totalRemaining);
        res.setTotalSoldQty(totalPrepared - totalRemaining);
        res.setTotalAmount(totalAmount);
        res.setTotalProfit(totalProfit);
        return res;
    }
    // ================================
    // LIST FOR DAY (ROW LEVEL)
    // ================================
    public List<Foodhut_TransactionResponse> getSalesForDay(LocalDate date) {
        LocalDate target = (date == null) ? LocalDate.now() : date;
        LocalDateTime start = target.atStartOfDay();
        LocalDateTime end = target.atTime(LocalTime.MAX);
        List<FoodhutSale> sales = saleRepo.findAllForDay(start, end);
        List<Foodhut_TransactionResponse> result = new ArrayList<>();
        for (FoodhutSale s : sales) {
            int soldQty = s.getPreparedQty() - s.getRemainingQty();
            result.add(new Foodhut_TransactionResponse(
                    s.getId(),
                    s.getItemVariation().getItem().getName(),
                    s.getItemVariation().getVariation(),
                    s.getItemVariation().getPrice(),
                    s.getItemVariation().getCost(),
                    s.getPreparedQty(),
                    s.getRemainingQty(),
                    soldQty,
                    SaleActionType.valueOf(s.getActionType()),
                    s.getTransactionTime(),
                    s.getRecordedBy().getName()
            ));
        }
        return result;
    }
    // ================================
    // REMAINING ITEMS FOR DAY
    // ================================
    public List<Foodhut_RemainingItemDto> getRemainingForDay(LocalDate date) {
        LocalDate target = (date == null) ? LocalDate.now() : date;
        LocalDateTime start = target.atStartOfDay();
        LocalDateTime end = target.atTime(LocalTime.MAX);
        List<FoodhutSale> sales = saleRepo.findAllForDay(start, end);
        Map<String, Integer> remainingMap = new HashMap<>();
        Map<String, String> itemNameMap = new HashMap<>();
        Map<String, String> variationMap = new HashMap<>();
        for (FoodhutSale s : sales) {
            String key = s.getItemVariation().getItem().getName() + "_" + s.getItemVariation().getVariation();
            remainingMap.put(key, remainingMap.getOrDefault(key, 0) + s.getRemainingQty());
            itemNameMap.put(key, s.getItemVariation().getItem().getName());
            variationMap.put(key, s.getItemVariation().getVariation());
        }
        List<Foodhut_RemainingItemDto> result = new ArrayList<>();
        for (String key : remainingMap.keySet()) {
            result.add(new Foodhut_RemainingItemDto(
                itemNameMap.get(key),
                variationMap.get(key),
                remainingMap.get(key)
            ));
        }
        return result;
    }
}