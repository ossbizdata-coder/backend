package com.oss.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "foodhut_items")
public class FoodhutItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(
            mappedBy = "item",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonManagedReference
    private List<FoodhutItemVariation> variations;

    public FoodhutItem() {}

    public FoodhutItem(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name;
    }

    public List<FoodhutItemVariation> getVariations() {
        // Do not return empty list if variations is null, return null instead
        return variations;
    }

    public void setVariations(List<FoodhutItemVariation> variations) {
        this.variations = variations;
    }
}
