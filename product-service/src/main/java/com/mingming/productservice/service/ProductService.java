package com.mingming.productservice.service;

import com.mingming.productservice.dto.ProductRequest;
import com.mingming.productservice.dto.ProductResponse;

import java.util.List;

public interface ProductService {
    void createProduct(ProductRequest productRequest);
    List<ProductResponse> getAllProducts();
}
