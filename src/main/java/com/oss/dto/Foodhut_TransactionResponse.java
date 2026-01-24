package com.oss.dto;

import com.oss.model.SaleActionType;
import java.time.LocalDateTime;

public class Foodhut_TransactionResponse {

    private Long saleId;
    private String itemName;
    private String variation;
    private int price;
    private int cost;
    private int preparedQty;
    private int remainingQty;
    private int soldQty;
    private SaleActionType actionType;
    private LocalDateTime transactionTime;
    private String recordedBy;

    public Foodhut_TransactionResponse(
            Long saleId,
            String itemName,
            String variation,
            int price,
            int cost,
            int preparedQty,
            int remainingQty,
            int soldQty,
            SaleActionType actionType,
            LocalDateTime transactionTime,
            String recordedBy
                                      ) {
        this.saleId = saleId;
        this.itemName = itemName;
        this.variation = variation;
        this.price = price;
        this.cost = cost;
        this.preparedQty = preparedQty;
        this.remainingQty = remainingQty;
        this.soldQty = soldQty;
        this.actionType = actionType;
        this.transactionTime = transactionTime;
        this.recordedBy = recordedBy;
    }

    public Foodhut_TransactionResponse() {}

    public Long getSaleId() { return saleId; }
    public String getItemName() { return itemName; }
    public String getVariation() { return variation; }
    public int getPrice() { return price; }
    public int getCost() { return cost; }
    public int getPreparedQty() { return preparedQty; }
    public int getRemainingQty() { return remainingQty; }
    public int getSoldQty() { return soldQty; }
    public SaleActionType getActionType() { return actionType; }
    public LocalDateTime getTransactionTime() { return transactionTime; }
    public String getRecordedBy() { return recordedBy; }

    public void setSaleId(Long saleId) { this.saleId = saleId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setVariation(String variation) { this.variation = variation; }
    public void setPrice(int price) { this.price = price; }
    public void setCost(int cost) { this.cost = cost; }
    public void setPreparedQty(int preparedQty) { this.preparedQty = preparedQty; }
    public void setRemainingQty(int remainingQty) { this.remainingQty = remainingQty; }
    public void setSoldQty(int soldQty) { this.soldQty = soldQty; }
    public void setActionType(SaleActionType actionType) { this.actionType = actionType; }
    public void setTransactionTime(LocalDateTime transactionTime) { this.transactionTime = transactionTime; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
}
