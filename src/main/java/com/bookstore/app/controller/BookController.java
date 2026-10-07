package com.bookstore.app.controller;

import com.bookstore.app.dto.CreateBookRequest;
import com.bookstore.app.dto.UpdatePriceRequest;
import com.bookstore.app.model.Book;
import com.bookstore.app.service.BookstoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/books")
public class BookController {

    private final BookstoreService bookstoreService;

    public BookController(BookstoreService bookstoreService) {
        this.bookstoreService = bookstoreService;
    }

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookstoreService.getAllBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable("id") String id) {
        return ResponseEntity.ok(bookstoreService.getBookById(id));
    }

    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody CreateBookRequest request) {
        Book createdBook = bookstoreService.addBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Book> updateBookPrice(
            @PathVariable("id") String id,
            @RequestBody UpdatePriceRequest request) {
        Book updatedBook = bookstoreService.updateBookPrice(id, request.getPrice());
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable("id") String id) {
        bookstoreService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
