# Intro to Backend, What's API and RESTful API
## Assignment Solution

## Task 1: Backend Architecture & Request Lifecycle

Suppose a user clicks **"Add to Cart"** on the online bookstore's web frontend.

---

### 1.1 The Complete Journey of an "Add to Cart" Request

```
[Browser / Client]
       │
       ▼ (1) DNS Lookup
[DNS Resolvers] ───► Resolves domain (e.g. api.bookstore.com) to IP address (e.g. 198.51.100.42)
       │
       ▼ (2) TCP & TLS Handshake (Port 443)
[Network / Internet]
       │
       ▼ (3) HTTP POST Request with JSON Body & Auth Headers
[Web Server / Reverse Proxy (e.g., Nginx, Tomcat)]
       │
       ▼ (4) Request Routing & Dispatching
[Application / Business Logic Layer (Spring Boot Controller & Service)]
       │
       ▼ (5) Read/Write Query (Transaction)
[Database (e.g., PostgreSQL / MySQL)]
       │
       ▼ (6) Result Set
[Application Layer] ──► Formats JSON Response (HTTP 200 / 201)
       │
       ▼ (7) Encrypted Response back over TLS
[Browser / Client] ──► Updates UI Cart Badge & Displays Success Message
```

#### Step 1: DNS Lookup and IP Addresses
1. **Trigger:** The browser captures the click event and initiates an asynchronous HTTP request (e.g., via `fetch` or `Axios`) to the endpoint `https://api.bookstore.com/v1/cart/items`.
2. **IP Resolution:** Computers communicate across networks using numeric IP addresses (IPv4 or IPv6), not human-readable domain names. The browser must resolve `api.bookstore.com` to an IP address:
   - **Browser & OS Cache:** The browser first checks its internal DNS cache, then queries the Operating System DNS resolver cache and `hosts` file.
   - **Recursive Resolver (ISP / DNS Server):** If not found, a query is sent to the configured recursive resolver (such as `8.8.8.8` or ISP DNS).
   - **DNS Hierarchy:** If the recursive resolver doesn't have the entry cached, it queries the **Root Nameservers** (`.`), then the **TLD Nameservers** (`.com`), and finally the **Authoritative Nameserver** for `bookstore.com`.
   - **Resolution Result:** The authoritative DNS server returns the destination IP address (e.g., `198.51.100.42`).

#### Step 2: Standard HTTP/HTTPS Protocols and Ports
1. **TCP Connection Establishment:**
   - The browser opens a network socket to `198.51.100.42` targeting standard port **443** (the default port for HTTPS; port **80** is used for unencrypted HTTP).
   - A **TCP 3-Way Handshake** occurs:
     1. Client sends `SYN`
     2. Server responds with `SYN-ACK`
     3. Client replies with `ACK`
2. **TLS / SSL Handshake:**
   - Over port 443, the client and server negotiate encryption keys and TLS version (TLS 1.3).
   - The server presents its digital SSL/TLS Certificate to prove its identity.
   - The client validates the certificate with trusted Certificate Authorities (CAs).
   - Symmetric session encryption keys are securely exchanged. From this point forward, all payload data, headers, and paths are encrypted in transit.
3. **HTTP Request Transmission:**
   - The browser serializes and sends an HTTP request:
     ```http
     POST /v1/cart/items HTTP/1.1
     Host: api.bookstore.com
     Authorization: Bearer eyJhbGciOi...
     Content-Type: application/json
     Accept: application/json

     {
       "bookId": "b-101",
       "quantity": 1
     }
     ```

#### Step 3: Server, Application/Business Logic Layer, and Database
1. **Web Server / Reverse Proxy (e.g., Nginx, Envoy, Embedded Tomcat):**
   - Terminates TLS/SSL, inspects headers, handles rate-limiting, and forwards the raw HTTP request into the application runtime.
2. **Application / Business Logic Layer (e.g., Spring Boot):**
   - The framework maps the request URL and HTTP method to the corresponding controller and handler method.
   - Converts the incoming JSON body into an application object/DTO (`AddToCartRequest`).
   - Executes domain rules (authentication, inventory checks, business rules).
3. **Database Layer:**
   - The application layer opens a database connection/transaction.
   - Queries the database to verify stock availability (`SELECT stock_quantity FROM books WHERE id = 'b-101' FOR UPDATE`).
   - Inserts or updates the cart item record (`INSERT INTO cart_items ... ON CONFLICT DO UPDATE ...`).
   - Commits the transaction and returns the persistence status to the application layer.
4. **Returning the Response to the Client:**
   - The application layer constructs a response payload with appropriate status code (e.g., `201 Created` or `200 OK`).
   - Serializes the result to JSON.
   - The server streams the HTTP response back through the TLS tunnel:
     ```http
     HTTP/1.1 200 OK
     Content-Type: application/json

     {
       "cartItemId": "ci-501",
       "bookId": "b-101",
       "quantity": 1,
       "unitPrice": 29.99,
       "totalItems": 1
     }
     ```
   - The browser receives the response, and frontend JavaScript updates the cart counter icon and displays a success notification to the user.

---

### 1.2 Specific Responsibilities of the Application/Business Logic Layer

During the "Add to Cart" interaction, the Application/Business Logic layer acts as the brain of the backend system with the following responsibilities:

1. **Authentication and Identity Verification:**
   - Validates the security token (e.g., JWT in the `Authorization: Bearer <token>` header) or session cookie.
   - Ensures the token is cryptographically valid, unexpired, and not revoked.
   - Extracts the authenticated `userId` associated with the request.

2. **Authorization and Access Control:**
   - Verifies whether the requesting user has permission to modify the target cart.
   - Ensures users cannot manipulate another customer's shopping cart.

3. **Input Validation and Sanitization:**
   - Checks that required fields are present (e.g., `bookId` is not null, `quantity` is a positive integer > 0).
   - Enforces constraint bounds (e.g., maximum allowed items per single addition, such as `quantity <= 10`).

4. **Inventory Availability Checks & Business Rules:**
   - Queries inventory to verify the book exists and is actively listed for sale (not discontinued or archived).
   - Validates that current stock is sufficient for the requested quantity (`stock >= requestedQuantity`).
   - Evaluates cart-level limits (e.g., maximum total cart value or per-user purchase quantity limits for special editions).

5. **Pricing and Promotional Calculations:**
   - Retrieves the authoritative, current unit price of the book directly from the database (never trusts prices submitted by client frontends).
   - Applies any active discounts, member pricing, or promotional coupons.

6. **State Management & Transaction Orchestration:**
   - Manages database transaction boundaries (`@Transactional`).
   - Handles concurrent cart modifications safely (e.g., using optimistic or pessimistic database locking).
   - Updates cart aggregates (item counts, subtotal).

7. **Structured Response and Error Handling:**
   - On success: Returns appropriate HTTP status (`200 OK` or `201 Created`) with standardized JSON.
   - On failure: Translates domain exceptions into clear HTTP status codes (`400 Bad Request`, `401 Unauthorized`, `404 Not Found`, `409 Conflict`) with actionable error messages.

---

## Task 2: RESTful Endpoint Design

### 2.1 REST API Endpoint Design Table

Below is the completed design table adhering to RESTful conventions, resource-based lowercase plural nouns, API versioning (`/v1`), and correct HTTP verbs:

| Requirement / Operation | HTTP Method | Endpoint (URL Path) | Safe? (Yes/No) | Idempotent? (Yes/No) | Brief Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Fetch list of all books** | `GET` | `/v1/books` | **Yes** | **Yes** | Retrieves a list of all available books in the bookstore inventory. |
| **Fetch details of a specific book by ID** | `GET` | `/v1/books/{id}` | **Yes** | **Yes** | Retrieves detailed information for a specific book identified by its unique ID. |
| **Add a new book to the inventory** | `POST` | `/v1/books` | **No** | **No** | Creates and appends a new book record to the inventory. |
| **Update price of an existing book** | `PATCH` | `/v1/books/{id}` | **No** | **No\*** | Partially updates the price attribute of an existing book identified by its ID. |
| **Delete a book record from inventory** | `DELETE` | `/v1/books/{id}` | **No** | **Yes** | Removes a specific book record from the inventory by its ID. |
| **Place a new book order** | `POST` | `/v1/orders` | **No** | **No** | Creates a new order for books placed by a customer. |
| **Retrieve all orders belonging to a specific user** | `GET` | `/v1/users/{userId}/orders` | **Yes** | **Yes** | Retrieves all order history records belonging to the specified user. |

> **\* Note on `PATCH` Idempotency:** According to the official HTTP/1.1 RFC 5789 specification, `PATCH` is non-idempotent by default because partial modifications can be relative (e.g., incrementing an amount). However, setting a field to a fixed value (e.g., `{"price": 19.99}`) produces an idempotent result in practical implementation. If replacing the entire resource representation, `PUT` is used and is strictly idempotent.

---

### 2.2 Design Principles & Properties Explanation

- **Safe Methods (`GET`):**
  - An HTTP method is **Safe** if it does not alter the state of the server. Calling it only reads data and produces no side effects.
  - `GET` is safe. `POST`, `PUT`, `PATCH`, and `DELETE` modify server state and are **Not Safe**.
- **Idempotent Methods (`GET`, `PUT`, `DELETE`):**
  - An HTTP method is **Idempotent** if making multiple identical requests has the same intended effect on server state as making a single request.
  - `GET`: Calling it once or ten times leaves server state unchanged. (Idempotent: Yes)
  - `DELETE`: Calling `DELETE /v1/books/42` once deletes the book. Calling it again leaves the book deleted. The state of the server is identical. (Idempotent: Yes)
  - `POST`: Calling `POST /v1/orders` multiple times will create multiple distinct orders and charge the customer multiple times. (Idempotent: No)
- **Resource Naming Guidelines:**
  - Resource paths use **lowercase plural nouns** (`/v1/books`, `/v1/orders`, `/v1/users`).
  - Hierarchical relationships are represented through nested paths (e.g., `/v1/users/{userId}/orders`).
  - Verbs belong in the HTTP method (`GET`, `POST`, `DELETE`), not in the URL path (e.g., avoid `/v1/getBooks` or `/v1/createOrder`).

---

## Task 3: Data Exchange & JSON Payloads

For the **"Place a new book order"** operation (`POST /v1/orders`), the HTTP request and response structures are designed below.

---

### 3.1 Scenario 1: Successful Order Creation (201 Created)

#### Request Details
- **HTTP Method:** `POST`
- **Request URL:** `https://api.bookstore.com/v1/orders`

#### Request Headers
```http
POST /v1/orders HTTP/1.1
Host: api.bookstore.com
Content-Type: application/json
Accept: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Idempotency-Key: 7b862590-4416-432d-96ef-f77eec82631a
```

#### Request JSON Body
```json
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
```

#### Expected Successful Response (201 Created)

#### Response Headers
```http
HTTP/1.1 201 Created
Content-Type: application/json
Location: /v1/orders/ord-98231
Date: Wed, 07 Oct 2026 16:15:00 GMT
```

#### Response JSON Body
```json
{
  "orderId": "ord-98231",
  "userId": "user-4021",
  "bookId": "book-101",
  "bookTitle": "Clean Code: A Handbook of Agile Software Craftsmanship",
  "quantity": 2,
  "unitPrice": 34.99,
  "totalAmount": 69.98,
  "status": "CONFIRMED",
  "shippingAddress": {
    "street": "123 MG Road, Suite 4B",
    "city": "Pune",
    "state": "Maharashtra",
    "postalCode": "411001",
    "country": "India"
  },
  "createdAt": "2026-10-07T16:15:00Z"
}
```

---

### 3.2 Scenario 2: Error Handling Scenario (409 Conflict)

#### Failure Scenario Description:
**Requested Quantity Exceeds Available Stock.**
The user attempts to purchase 10 copies of a book (`book-101`), but the inventory currently only has 3 copies available in stock. The request cannot be completed because it conflicts with the current resource state.

- **HTTP Status Code:** `409 Conflict`  
  *(Note: `409 Conflict` is the semantically accurate status code when a business constraint or resource state conflict like inventory stock occurs. In strict input syntax validation errors, `400 Bad Request` or `422 Unprocessable Entity` is used).*

#### Request Triggering the Error
```http
POST /v1/orders HTTP/1.1
Host: api.bookstore.com
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1Ni...

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
```

#### Response Headers
```http
HTTP/1.1 409 Conflict
Content-Type: application/json
Date: Wed, 07 Oct 2026 16:15:05 GMT
```

#### Structured Error JSON Response Body
```json
{
  "status": 409,
  "errorCode": "INSUFFICIENT_STOCK",
  "message": "Requested quantity exceeds available stock.",
  "field": "quantity",
  "details": {
    "requestedQuantity": 10,
    "availableStock": 3,
    "bookId": "book-101"
  },
  "timestamp": "2026-10-07T16:15:05Z"
}
```

#### Key Structure Elements of the Error Response:
1. **`status`**: Standard HTTP integer status code (409).
2. **`errorCode`**: Machine-readable, unique uppercase business error code (`INSUFFICIENT_STOCK`) enabling client applications to programmatically handle this specific condition.
3. **`message`**: Clear, human-readable description explaining why the request failed.
4. **`field`**: Targeted field indicator pointing directly to the invalid parameter (`quantity`).
5. **`details`**: Contextual metadata (requested quantity vs. available stock) to help client interfaces present intelligent remediation options (e.g., *"Only 3 copies left in stock"*).
6. **`timestamp`**: ISO-8601 UTC timestamp for auditing and log correlation.

---

## Application Implementation Guide

The Spring Boot backend implementation included in this project provides a working RESTful service matching these exact specifications:

1. **Books Management:**
   - `GET /v1/books` - Retrieve all books
   - `GET /v1/books/{id}` - Retrieve book by ID
   - `POST /v1/books` - Add a new book
   - `PATCH /v1/books/{id}` - Update book price
   - `DELETE /v1/books/{id}` - Remove a book
2. **Orders & User Operations:**
   - `POST /v1/orders` - Place order with inventory stock validation
   - `GET /v1/users/{userId}/orders` - Retrieve order history for a user
3. **Error Handling:**
   - Centralized `@RestControllerAdvice` returning the standard structured JSON error format for `409 Conflict`, `404 Not Found`, and `400 Bad Request`.
