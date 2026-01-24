package com.oss.dto;
import java.util.List;
public class Foohut_ItemRequest {
    private String name;
    private List<Foodhut_VariationDTO> variations;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<Foodhut_VariationDTO> getVariations() {
        return variations;
    }
    public void setVariations(List<Foodhut_VariationDTO> variations) {
        this.variations = variations;
    }
}