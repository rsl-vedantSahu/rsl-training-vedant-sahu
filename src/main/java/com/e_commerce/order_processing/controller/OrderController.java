package com.e_commerce.order_processing.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.e_commerce.order_processing.model.OrderPlacedEvent;
import com.e_commerce.order_processing.model.OrderRequest;
import com.e_commerce.order_processing.producer.OrderProducerService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderProducerService orderProducerService;

    public OrderController(OrderProducerService orderProducerService) {
        this.orderProducerService = orderProducerService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> placeOrder(@RequestBody OrderRequest request) {
        if (request == null || request.category() == null || request.category().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", "Category is required (e.g., electronics, clothing)"
            ));
        }

        OrderPlacedEvent event = orderProducerService.publishOrder(request);
        String routingKey = "order.created." + event.category();

        return ResponseEntity.accepted().body(Map.of(
                "status", "SUCCESS",
                "message", "Order placed successfully and published to RabbitMQ",
                "order_id", event.orderId(),
                "routing_key", routingKey,
                "category", event.category(),
                "quantity", event.quantity()
        ));
    }
}

