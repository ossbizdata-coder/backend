package com.oss.dto;
public class Foodhut_RemainingItemDto {
    private String itemName;
    private String variation;
    private int remaining;
    public Foodhut_RemainingItemDto(String itemName, String variation, int remaining) {
        this.itemName = itemName;
        this.variation = variation;
        this.remaining = remaining;
    }
    public String getItemName() { return itemName; }
    public String getVariation() { return variation; }
    public int getRemaining() { return remaining; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setVariation(String variation) { this.variation = variation; }
    public void setRemaining(int remaining) { this.remaining = remaining; }
}