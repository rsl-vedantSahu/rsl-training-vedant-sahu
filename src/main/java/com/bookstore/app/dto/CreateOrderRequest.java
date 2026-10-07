package com.bookstore.app.dto;

import com.bookstore.app.model.ShippingAddress;

public class CreateOrderRequest {
    private String userId;
    private String bookId;
    private int quantity;
    private ShippingAddress shippingAddress;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(String userId, String bookId, int quantity, ShippingAddress shippingAddress) {
        this.userId = userId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.shippingAddress = shippingAddress;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddress shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
