package com.oss.dto;
import com.oss.model.SaleActionType;
public class Foodhut_TransactionRequest {
    private Long variationId;
    private int preparedQty;
    private int remainingQty;
    private SaleActionType actionType;
    public Foodhut_TransactionRequest() {}
    public Long getVariationId() {
        return variationId;
    }
    public void setVariationId(Long variationId) {
        this.variationId = variationId;
    }
    public int getPreparedQty() {
        return preparedQty;
    }
    public void setPreparedQty(int preparedQty) {
        this.preparedQty = preparedQty;
    }
    public int getRemainingQty() {
        return remainingQty;
    }
    public void setRemainingQty(int remainingQty) {
        this.remainingQty = remainingQty;
    }
    public SaleActionType getActionType() {
        return actionType;
    }
    public void setActionType(SaleActionType actionType) {
        this.actionType = actionType;
    }
}