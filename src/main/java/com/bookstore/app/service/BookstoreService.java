package com.bookstore.app.service;

import com.bookstore.app.dto.CreateBookRequest;
import com.bookstore.app.dto.CreateOrderRequest;
import com.bookstore.app.exception.BadRequestException;
import com.bookstore.app.exception.InsufficientStockException;
import com.bookstore.app.exception.ResourceNotFoundException;
import com.bookstore.app.model.Book;
import com.bookstore.app.model.Order;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class BookstoreService {

    private final Map<String, Book> books = new ConcurrentHashMap<>();
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong orderCounter = new AtomicLong(98230);
    private final AtomicLong bookCounter = new AtomicLong(103);

    public BookstoreService() {
        // Initial sample data
        Book book1 = new Book("book-101", "Clean Code: A Handbook of Agile Software Craftsmanship",
                "Robert C. Martin", "978-0132350884", 34.99, 3);
        Book book2 = new Book("book-102", "Designing Data-Intensive Applications",
                "Martin Kleppmann", "978-1449373320", 42.50, 15);
        Book book3 = new Book("book-103", "Effective Java",
                "Joshua Bloch", "978-0134685991", 45.00, 8);

        books.put(book1.getId(), book1);
        books.put(book2.getId(), book2);
        books.put(book3.getId(), book3);
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books.values());
    }

    public Book getBookById(String id) {
        Book book = books.get(id);
        if (book == null) {
            throw new ResourceNotFoundException("Book not found with ID: " + id);
        }
        return book;
    }

    public Book addBook(CreateBookRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Book title is required.", "title");
        }
        if (request.getAuthor() == null || request.getAuthor().trim().isEmpty()) {
            throw new BadRequestException("Book author is required.", "author");
        }
        if (request.getPrice() < 0) {
            throw new BadRequestException("Price must be non-negative.", "price");
        }
        if (request.getStockQuantity() < 0) {
            throw new BadRequestException("Stock quantity must be non-negative.", "stockQuantity");
        }

        String id = "book-" + bookCounter.incrementAndGet();
        Book book = new Book(
                id,
                request.getTitle().trim(),
                request.getAuthor().trim(),
                request.getIsbn(),
                request.getPrice(),
                request.getStockQuantity()
        );
        books.put(id, book);
        return book;
    }

    public Book updateBookPrice(String id, Double newPrice) {
        if (newPrice == null || newPrice < 0) {
            throw new BadRequestException("A valid non-negative price is required.", "price");
        }
        Book book = getBookById(id);
        book.setPrice(newPrice);
        return book;
    }

    public void deleteBook(String id) {
        Book removed = books.remove(id);
        if (removed == null) {
            throw new ResourceNotFoundException("Book not found with ID: " + id);
        }
    }

    public synchronized Order placeOrder(CreateOrderRequest request) {
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new BadRequestException("User ID is required.", "userId");
        }
        if (request.getBookId() == null || request.getBookId().trim().isEmpty()) {
            throw new BadRequestException("Book ID is required.", "bookId");
        }
        if (request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than zero.", "quantity");
        }
        if (request.getShippingAddress() == null) {
            throw new BadRequestException("Shipping address is required.", "shippingAddress");
        }

        Book book = books.get(request.getBookId());
        if (book == null) {
            throw new ResourceNotFoundException("Book not found with ID: " + request.getBookId());
        }

        if (request.getQuantity() > book.getStockQuantity()) {
            throw new InsufficientStockException(
                    "Requested quantity exceeds available stock.",
                    book.getId(),
                    request.getQuantity(),
                    book.getStockQuantity()
            );
        }

        // Deduct inventory
        book.setStockQuantity(book.getStockQuantity() - request.getQuantity());

        double totalAmount = Math.round(book.getPrice() * request.getQuantity() * 100.0) / 100.0;
        String orderId = "ord-" + orderCounter.incrementAndGet();

        Order order = new Order(
                orderId,
                request.getUserId(),
                book.getId(),
                book.getTitle(),
                request.getQuantity(),
                book.getPrice(),
                totalAmount,
                "CONFIRMED",
                request.getShippingAddress(),
                Instant.now().toString()
        );

        orders.put(orderId, order);
        return order;
    }

    public List<Order> getOrdersByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new BadRequestException("User ID is required.", "userId");
        }
        return orders.values().stream()
                .filter(order -> userId.equals(order.getUserId()))
                .collect(Collectors.toList());
    }
}
