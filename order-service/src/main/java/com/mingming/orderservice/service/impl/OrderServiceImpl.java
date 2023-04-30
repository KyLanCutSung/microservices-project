package com.mingming.orderservice.service.impl;

import com.mingming.orderservice.dto.OrderLineItemsDto;
import com.mingming.orderservice.dto.OrderRequest;
import com.mingming.orderservice.model.OrderLineItems;
import com.mingming.orderservice.model.Orders;
import com.mingming.orderservice.repo.OrderRepository;
import com.mingming.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public void placeOrder(OrderRequest orderRequest){
        Orders orders = new Orders();
        orders.setOrderNumber(UUID.randomUUID().toString());

//        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
//                .stream()
//                .map(this::mapToDto)
//                .toList();
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream().map(this::mapToDto).collect(Collectors.toList());
        orders.setOrderLineItemsList(orderLineItems);
        orderRepository.save(orders);
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());

        return orderLineItems;
    }
    public List<Orders> findAll(){
        List<Orders> orders = orderRepository.findAll();
        return orders;
    }
}
