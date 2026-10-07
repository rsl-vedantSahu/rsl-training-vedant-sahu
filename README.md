# Bookstore REST API - RSL Training Assignment

This repository contains the solution and implementation for the assignment: **Intro to Backend, What's API and RESTful API**.

Comprehensive answers and documentation for **Task 1**, **Task 2**, and **Task 3** can be found in [ASSIGNMENT_SOLUTION.md](ASSIGNMENT_SOLUTION.md).

---

## 📚 API Endpoints Overview

| Method | Endpoint | Description | Status Code |
|---|---|---|---|
| `GET` | `/v1/books` | Fetch list of all books | `200 OK` |
| `GET` | `/v1/books/{id}` | Fetch details of a specific book by ID | `200 OK` / `404 Not Found` |
| `POST` | `/v1/books` | Add a new book to the inventory | `201 Created` |
| `PATCH` | `/v1/books/{id}` | Update price of an existing book | `200 OK` / `404 Not Found` |
| `DELETE` | `/v1/books/{id}` | Delete a book record from inventory | `204 No Content` / `404 Not Found` |
| `POST` | `/v1/orders` | Place a new book order | `201 Created` / `409 Conflict` |
| `GET` | `/v1/users/{userId}/orders` | Retrieve all orders belonging to a specific user | `200 OK` |

---

## 🚀 Running the Application

### Prerequisites
- Java 25 (or compatible JDK)

### Start the Server
```bash
./mvnw spring-boot:run
```
*(On Windows PowerShell: `.\mvnw.cmd spring-boot:run`)*

The server will start on `http://localhost:8080`.

---

## 🧪 Running Automated Tests
```bash
./mvnw test
```
*(On Windows PowerShell: `.\mvnw.cmd test`)*

All 9 integration and unit tests covering all endpoints, success, and error scenarios pass cleanly.

---

## 📖 Quick API Examples

### 1. Fetch all books
```bash
curl http://localhost:8080/v1/books
```

### 2. Place an order (Success - 201 Created)
```bash
curl -X POST http://localhost:8080/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-4021",
    "bookId": "book-101",
    "quantity": 2,
    "shippingAddress": {
      "street": "123 MG Road",
      "city": "Pune",
      "state": "Maharashtra",
      "postalCode": "411001",
      "country": "India"
    }
  }'
```

### 3. Place an order exceeding stock (Failure - 409 Conflict)
```bash
curl -X POST http://localhost:8080/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-4021",
    "bookId": "book-101",
    "quantity": 10,
    "shippingAddress": {
      "street": "123 MG Road",
      "city": "Pune",
      "state": "Maharashtra",
      "postalCode": "411001",
      "country": "India"
    }
  }'
```

### 4. Fetch orders for a user
```bash
curl http://localhost:8080/v1/users/user-4021/orders
```
