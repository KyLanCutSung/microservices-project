package com.mingming.orderservice.service;

import com.mingming.orderservice.dto.OrderRequest;
import com.mingming.orderservice.model.Orders;

import java.util.List;

public interface OrderService {
    void placeOrder(OrderRequest orderRequest);
    List<Orders> findAll();
}
