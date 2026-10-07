package com.bookstore.app.controller;

import com.bookstore.app.dto.CreateOrderRequest;
import com.bookstore.app.model.Order;
import com.bookstore.app.service.BookstoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/v1/orders")
public class OrderController {

    private final BookstoreService bookstoreService;

    public OrderController(BookstoreService bookstoreService) {
        this.bookstoreService = bookstoreService;
    }

    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody CreateOrderRequest request) {
        Order createdOrder = bookstoreService.placeOrder(request);
        URI location = URI.create("/v1/orders/" + createdOrder.getOrderId());
        return ResponseEntity.created(location).body(createdOrder);
    }
}
