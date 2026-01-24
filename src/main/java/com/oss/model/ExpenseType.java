package com.oss.model;
import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table(name = "expense_types")
@Data
public class ExpenseType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(name = "shop_type", nullable = false)
    private String shopType; // CAFE, BOOKSHOP, FOODHUT, COMMON
}