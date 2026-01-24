package com.oss.service;
import com.oss.dto.Foodhut_VariationDTO;
import com.oss.model.FoodhutItem;
import com.oss.model.FoodhutItemVariation;
import com.oss.repository.FoodhutItemRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class FoodhutItemService {
    @Autowired
    private FoodhutItemRepository itemRepo;
    public FoodhutItem addItem(String name, List<Foodhut_VariationDTO> variations) {
        FoodhutItem item = new FoodhutItem();
        item.setName(name);
        List<FoodhutItemVariation> varEntities = new ArrayList<>();
        for (Foodhut_VariationDTO v : variations) {
            FoodhutItemVariation var = new FoodhutItemVariation();
            var.setVariation(v.getVariation());
            var.setPrice(v.getPrice());
            var.setCost(v.getCost()); // <-- set cost
            // ðŸ”¥ CRITICAL: set both sides
            var.setItem(item);
            varEntities.add(var);
        }
        item.setVariations(varEntities);
        // ðŸ”¥ Save ONLY the parent
        return itemRepo.save(item);
    }
    public List<FoodhutItem> getAllItems() {
        return itemRepo.findAll();
    }
}