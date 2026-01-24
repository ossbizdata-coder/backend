package com.oss.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
@Entity
@Table(name = "foodhut_item_variations")
public class FoodhutItemVariation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    @JsonBackReference
    private FoodhutItem item;
    private String variation;
    private int price;
    private int cost; // <-- Added cost field
    public FoodhutItemVariation() {}
    public FoodhutItemVariation(Long id, FoodhutItem item, String variation, int price, int cost) {
        this.id = id;
        this.item = item;
        this.variation = variation;
        this.price = price;
        this.cost = cost;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public FoodhutItem getItem() {
        return item;
    }
    public void setItem(FoodhutItem item) {
        this.item = item;
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
    public String getVariationName() {
        return this.variation;
    }
}