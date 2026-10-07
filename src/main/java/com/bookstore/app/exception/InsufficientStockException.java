package com.bookstore.app.exception;

public class InsufficientStockException extends RuntimeException {
    private final String bookId;
    private final int requestedQuantity;
    private final int availableStock;

    public InsufficientStockException(String message, String bookId, int requestedQuantity, int availableStock) {
        super(message);
        this.bookId = bookId;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

    public String getBookId() {
        return bookId;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}
