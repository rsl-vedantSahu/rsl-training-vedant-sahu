# E-Commerce Order Processing System (RabbitMQ + Spring Boot)

An asynchronous, event-driven E-Commerce Order Processing System built with **Java 17**, **Spring Boot 4**, and **RabbitMQ** demonstrating Topic Exchange routing (wildcard and exact match), Manual Acknowledgments, Fair Dispatch (`prefetch_count = 1`), Idempotent Design, and Dead Letter Exchange (DLX) error handling.

---

## Architecture & Routing Flow

```text
  [Frontend UI / API Client]
              |
              | POST /api/orders
              v
    [OrderController & OrderProducerService]
              |
              | Routing Key: order.created.<category>
              | Message Property: messageId = <order_id>
              v
    (Topic Exchange: orders_exchange)
       |                           |
       | Binding: order.created.*  | Binding: order.created.electronics
       v                           v
 [inventory_queue]          [analytics_queue]
       |                           |
       v                           v
 [InventoryService]         [AnalyticsService]
       |
       | (If quantity <= 0: basicNack, requeue = false)
       v
 (DLX: orders_dlx) --[order.dead]--> [orders_dlq] --> [DeadLetterConsumer]
```

### Key Features Implemented
* **Topic Exchange (`orders_exchange`)**:
  * `inventory_queue` binds with `order.created.*` (receives all categories).
  * `analytics_queue` binds with `order.created.electronics` (receives only `electronics`).
* **Manual Acknowledgments (`acknowledge-mode: manual`)**: Consumers acknowledge messages explicitly with `basicAck` only after successful processing.
* **Fair Dispatch (`prefetch: 1`)**: Configured on listener channels so RabbitMQ dispatches at most one unacknowledged message per worker at a time.
* **Idempotent Design**: Each consumer tracks processed `order_id` values in a thread-safe in-memory `Set` (`ConcurrentHashMap.newKeySet()`) and skips duplicates.
* **Dead Letter Exchange (`orders_dlx` -> `orders_dlq`)**: Invalid orders (`quantity <= 0`) are explicitly rejected without requeuing (`basicNack(tag, false, false)`) by `InventoryService` and routed to `DeadLetterConsumer`.

---

## Prerequisites
* **Java 17+**
* **Docker & Docker Compose**
* **Maven** (or the included `./mvnw.cmd` / `./mvnw` wrapper)

---

## Step 1: Start RabbitMQ

### Option A: Using Docker Compose
```bash
docker-compose up -d
```

### Option B: Using `docker run`
```bash
docker run -d --name rabbitmq-broker \
  -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3-management
```

* **AMQP Port**: `5672`
* **RabbitMQ Management UI**: [http://localhost:15672](http://localhost:15672) (`guest` / `guest`)

---

## Step 2: Run the Spring Boot Application

On Windows (PowerShell/CMD):
```powershell
.\mvnw.cmd spring-boot:run
```

On macOS/Linux:
```bash
./mvnw spring-boot:run
```

The server starts on **`http://localhost:8080`**.

---

## Step 3: Trigger & Verify All Scenarios

### Using the Frontend UI
Open **[http://localhost:8080](http://localhost:8080)** in your browser to submit orders interactively (including negative quantities for DLX testing and reusing the last `order_id` for idempotency testing).

### Or Using `curl`

#### 1. Electronics Order (Processed by BOTH Inventory & Analytics)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"category": "electronics", "quantity": 2}'
```
**Expected Console Output:**
* `[ORDER PRODUCER] Published 'order_placed' | Order ID: ORD-XXXXXXXX | Routing Key: order.created.electronics | Quantity: 2`
* `[INVENTORY SERVICE] Reserved inventory | Order ID: ORD-XXXXXXXX | Category: electronics | Quantity: 2 | Routing Key: order.created.electronics`
* `Analytics Service: Recording high-value electronics sale for order ORD-XXXXXXXX`

#### 2. Clothing Order (Processed ONLY by Inventory)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"category": "clothing", "quantity": 3}'
```
**Expected Console Output:**
* `[INVENTORY SERVICE] Reserved inventory | Order ID: ORD-YYYYYYYY | Category: clothing | Quantity: 3 | Routing Key: order.created.clothing`
* *(Analytics Service ignores this message).*

#### 3. Invalid Quantity Order (Rejected to Dead Letter Exchange)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"category": "clothing", "quantity": -5}'
```
**Expected Console Output:**
* `[INVENTORY SERVICE] Rejecting invalid order (quantity <= 0) without requeuing | Order ID: ORD-ZZZZZZZZ | Quantity: -5`
* `Error Logger: Invalid order ORD-ZZZZZZZZ routed to DLX`

#### 4. Duplicate Order ID (Idempotency Check)
Send the same `orderId` twice:
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"category": "electronics", "quantity": 1, "orderId": "ORD-TEST-001"}'

curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"category": "electronics", "quantity": 1, "orderId": "ORD-TEST-001"}'
```
**Expected Console Output (2nd call):**
* `[INVENTORY SERVICE] Duplicate order detected (Idempotency check) - skipping | Order ID: ORD-TEST-001`
* `[ANALYTICS SERVICE] Duplicate order detected (Idempotency check) - skipping | Order ID: ORD-TEST-001`
