package com.oss.controller;

import com.oss.dto.Foohut_ItemRequest;
import com.oss.model.FoodhutItem;
import com.oss.service.FoodhutItemService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping({"/api/items", "/items"})
public class Foodhut_ItemController {

    @Autowired
    private FoodhutItemService itemService;

    @PostMapping
    public ResponseEntity<?> addItem(@RequestBody Foohut_ItemRequest req) {
        itemService.addItem(req.getName(), req.getVariations());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<FoodhutItem> getAllItems() {
        return itemService.getAllItems();
    }


}
