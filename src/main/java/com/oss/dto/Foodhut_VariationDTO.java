package com.oss.dto;

public class Foodhut_VariationDTO {
    private String variation;
    private int price;
    private int cost;

    public Foodhut_VariationDTO() {}

    public Foodhut_VariationDTO(String variation, int price, int cost) {
        this.variation = variation;
        this.price = price;
        this.cost = cost;
    }

    public String getVariation() {
        return variation;
    }

    public void setVariation(String variation) {
        this.variation = variation;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
