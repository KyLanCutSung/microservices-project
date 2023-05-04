package com.mingming.inventoryservice.controller;

import com.mingming.inventoryservice.dto.InventoryResponse;
import com.mingming.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    //http://localhost:8083/api/inventory/iphone-13,iphone13-red
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode) throws Exception{
        return inventoryService.isInStock(skuCode);
    }
}
