package com.bookstore.app.dto;

public class UpdatePriceRequest {
    private Double price;

    public UpdatePriceRequest() {
    }

    public UpdatePriceRequest(Double price) {
        this.price = price;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
