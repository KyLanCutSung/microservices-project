package com.mingming.orderservice.service.impl;

import com.mingming.orderservice.dto.InventoryResponse;
import com.mingming.orderservice.dto.OrderLineItemsDto;
import com.mingming.orderservice.dto.OrderRequest;
import com.mingming.orderservice.event.OrderPlacedEvent;
import com.mingming.orderservice.model.OrderLineItems;
import com.mingming.orderservice.model.Orders;
import com.mingming.orderservice.repo.OrderRepository;
import com.mingming.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public String placeOrder(OrderRequest orderRequest){
        Orders orders = new Orders();
        orders.setOrderNumber(UUID.randomUUID().toString());

//        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
//                .stream()
//                .map(this::mapToDto)
//                .toList();
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream().map(this::mapToDto).collect(Collectors.toList());
        orders.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes =  orders.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).collect(Collectors.toList());
        //Call Inventory Service, and place order if product is in stock
        log.info("Calling inventory service");

        Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");

        try(Tracer.SpanInScope spanInScope = tracer.withSpan(inventoryServiceLookup.start())){
            InventoryResponse[] inventoryResponsesArray = webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();

            boolean allProductsInStock = Arrays.stream(inventoryResponsesArray).allMatch(InventoryResponse::isInStock);

            if(allProductsInStock){
                orderRepository.save(orders);
                kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(orders.getOrderNumber()));
                return "Order Placed Successfully!";
            } else {
                throw new IllegalArgumentException("Product is not in stock, please try again later");
            }
        }finally {
            inventoryServiceLookup.end();
        }
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
