package com.bookstore.app.controller;

import com.bookstore.app.model.Order;
import com.bookstore.app.service.BookstoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final BookstoreService bookstoreService;

    public UserController(BookstoreService bookstoreService) {
        this.bookstoreService = bookstoreService;
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable("userId") String userId) {
        List<Order> userOrders = bookstoreService.getOrdersByUserId(userId);
        return ResponseEntity.ok(userOrders);
    }
}
