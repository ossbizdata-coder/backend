package com.oss.dto;

public class Foodhut_DaySummaryResponse {

    private long totalPreparedQty;
    private long totalRemainingQty;
    private long totalSoldQty;
    private long totalAmount;
    private long totalProfit;

    public Foodhut_DaySummaryResponse(
            long totalPreparedQty,
            long totalRemainingQty,
            long totalSoldQty,
            long totalAmount,
            long totalProfit
                                     ) {
        this.totalPreparedQty = totalPreparedQty;
        this.totalRemainingQty = totalRemainingQty;
        this.totalSoldQty = totalSoldQty;
        this.totalAmount = totalAmount;
        this.totalProfit = totalProfit;
    }

    public Foodhut_DaySummaryResponse() {}

    public long getTotalPreparedQty() { return totalPreparedQty; }
    public void setTotalPreparedQty(long totalPreparedQty) { this.totalPreparedQty = totalPreparedQty; }

    public long getTotalRemainingQty() { return totalRemainingQty; }
    public void setTotalRemainingQty(long totalRemainingQty) { this.totalRemainingQty = totalRemainingQty; }

    public long getTotalSoldQty() { return totalSoldQty; }
    public void setTotalSoldQty(long totalSoldQty) { this.totalSoldQty = totalSoldQty; }

    public long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(long totalAmount) { this.totalAmount = totalAmount; }

    public long getTotalProfit() { return totalProfit; }
    public void setTotalProfit(long totalProfit) { this.totalProfit = totalProfit; }
}
