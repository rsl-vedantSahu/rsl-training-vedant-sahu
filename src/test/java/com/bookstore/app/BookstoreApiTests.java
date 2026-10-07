package com.bookstore.app;

import com.bookstore.app.controller.BookController;
import com.bookstore.app.controller.OrderController;
import com.bookstore.app.controller.UserController;
import com.bookstore.app.exception.GlobalExceptionHandler;
import com.bookstore.app.service.BookstoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookstoreApiTests {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        BookstoreService service = new BookstoreService();
        BookController bookController = new BookController(service);
        OrderController orderController = new OrderController(service);
        UserController userController = new UserController(service);

        mockMvc = MockMvcBuilders
                .standaloneSetup(bookController, orderController, userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetAllBooks() throws Exception {
        mockMvc.perform(get("/v1/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", notNullValue()));
    }

    @Test
    void testGetBookById() throws Exception {
        mockMvc.perform(get("/v1/books/book-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("book-101")))
                .andExpect(jsonPath("$.title", containsString("Clean Code")));
    }

    @Test
    void testAddNewBook() throws Exception {
        String newBookJson = """
                {
                    "title": "Refactoring",
                    "author": "Martin Fowler",
                    "isbn": "978-0134757599",
                    "price": 39.99,
                    "stockQuantity": 10
                }
                """;

        mockMvc.perform(post("/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newBookJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", startsWith("book-")))
                .andExpect(jsonPath("$.title", is("Refactoring")))
                .andExpect(jsonPath("$.price", is(39.99)));
    }

    @Test
    void testUpdateBookPrice() throws Exception {
        String updatePriceJson = """
                {
                    "price": 38.50
                }
                """;

        mockMvc.perform(patch("/v1/books/book-102")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePriceJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("book-102")))
                .andExpect(jsonPath("$.price", is(38.50)));
    }

    @Test
    void testDeleteBook() throws Exception {
        mockMvc.perform(delete("/v1/books/book-103"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/books/book-103"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("RESOURCE_NOT_FOUND")));
    }

    @Test
    void testPlaceOrderSuccess() throws Exception {
        String orderJson = """
                {
                    "userId": "user-4021",
                    "bookId": "book-101",
                    "quantity": 2,
                    "shippingAddress": {
                        "street": "123 MG Road, Suite 4B",
                        "city": "Pune",
                        "state": "Maharashtra",
                        "postalCode": "411001",
                        "country": "India"
                    }
                }
                """;

        mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.orderId", startsWith("ord-")))
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.quantity", is(2)))
                .andExpect(jsonPath("$.totalAmount", is(69.98)));
    }

    @Test
    void testPlaceOrderInsufficientStockFailure() throws Exception {
        // book-101 has stock of 3 in new BookstoreService. Requesting 10 will fail with 409 Conflict
        String orderJson = """
                {
                    "userId": "user-4021",
                    "bookId": "book-101",
                    "quantity": 10,
                    "shippingAddress": {
                        "street": "123 MG Road, Suite 4B",
                        "city": "Pune",
                        "state": "Maharashtra",
                        "postalCode": "411001",
                        "country": "India"
                    }
                }
                """;

        mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.errorCode", is("INSUFFICIENT_STOCK")))
                .andExpect(jsonPath("$.field", is("quantity")))
                .andExpect(jsonPath("$.message", containsString("exceeds available stock")));
    }

    @Test
    void testGetOrdersByUserId() throws Exception {
        mockMvc.perform(get("/v1/users/user-4021/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }
}
