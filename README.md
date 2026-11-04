# Restaurant Franchise Order Event Processing System

An end-to-end, event-driven order processing system built with **Apache Kafka**, **Docker**, and **Spring Boot (Java 25)** demonstrating core Kafka capabilities: pub/sub messaging, key-based partition pinning, independent consumer groups, failure isolation, and offset replay.

---

## 🏛️ Architecture Overview

A restaurant franchise operates two outlets: **`RESTAURANT-101`** and **`RESTAURANT-102`**. Orders are published to a single topic (`restaurant.orders.created`) with `restaurant_id` used as the message key to ensure that orders from the same outlet always land in the same partition.

Three independent downstream services process these events concurrently:
1. **Kitchen Service** (`kitchen-service-group`): Prepares food tickets for cooks.
2. **Inventory Service** (`inventory-service-group`): Deducts ingredient stock.
3. **Notification Service** (`notification-service-group`): Sends customer alerts.

```text
               +----------------------------------------+
               |          Order Producer (CLI)          |
               +-------------------+--------------------+
                                   |
              Key: restaurant_id   |  Topic: restaurant.orders.created (3 Partitions)
                                   v
             +--------------------------------------------+
             |            Apache Kafka Broker             |
             |   [Partition 0] [Partition 1] [Partition 2]|
             +---------------------+----------------------+
                                   |
       +---------------------------+---------------------------+
       |                           |                           |
       v                           v                           v
+------------------+      +------------------+      +---------------------+
|  Kitchen Service |      | Inventory Service|      | Notification Service|
| (kitchen-group)  |      | (inventory-group)|      | (notification-group)|
+------------------+      +------------------+      +---------------------+
```

---

## 📋 Prerequisites
* **Docker & Docker Compose** (Docker Desktop running)
* **Java 17+ / Java 25**
* **Maven** (or the included `./mvnw.cmd` wrapper)

---

## 🚀 Step 1: Start the Kafka Environment

Start the single-node Apache Kafka KRaft broker:
```bash
docker-compose up -d
```

Verify that the broker is running:
```bash
docker ps
```

---

## 🛠️ Step 2: Create the Topic with Kafka CLI

Create the `restaurant.orders.created` topic with **3 partitions** and a replication factor of **1**:

```bash
docker exec -it kafka-broker /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create \
  --topic restaurant.orders.created \
  --partitions 3 \
  --replication-factor 1
```

Verify the topic details:
```bash
docker exec -it kafka-broker /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic restaurant.orders.created
```

---

## 🖥️ Step 3: Run the Consumers

Open **3 separate terminal windows** for each downstream service:

### Terminal 1: Kitchen Service
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--service.type=kitchen"
```

### Terminal 2: Inventory Service
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--service.type=inventory"
```

### Terminal 3: Customer Notification Service
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--service.type=notification"
```

Each consumer will connect to Kafka, join its own consumer group, and wait for order events.

---

## 📦 Step 4: Run the CLI Order Producer

Open **Terminal 4** to produce orders for `RESTAURANT-101` and `RESTAURANT-102`.

### Produce Order for Outlet 101:
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--service.type=producer --restaurant=RESTAURANT-101 --customer=Vedant --items=Burger,Fries --orderId=1001"
```

### Produce Order for Outlet 102:
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--service.type=producer --restaurant=RESTAURANT-102 --customer=Rahul --items=Pizza,Coke --orderId=1002"
```

### 🔍 Verification:
Notice how:
* All orders for `RESTAURANT-101` consistently route to the **same partition**.
* All orders for `RESTAURANT-102` consistently route to their respective partition.
* All 3 consumers in Terminals 1, 2, and 3 process each message independently!

---

## 🧪 Step 5: Demonstrate Failure Isolation & Recovery

1. **Stop One Consumer**:
   * Go to **Terminal 1 (Kitchen Service)** and press `Ctrl + C` to terminate it.
2. **Continue Producing Orders**:
   * In **Terminal 4**, produce two more orders:
     ```powershell
     .\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--service.type=producer --restaurant=RESTAURANT-101 --customer=Sneha --orderId=1003"
     ```
3. **Observe Isolation**:
   * **Terminal 2 (Inventory)** and **Terminal 3 (Notification)** continue processing orders seamlessly in real time.
4. **Restart Kitchen Service**:
   * In **Terminal 1**, restart Kitchen Service:
     ```powershell
     .\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--service.type=kitchen"
     ```
   * Kitchen Service resumes from its last committed offset and immediately catches up on all missed orders!

---

## 🔄 Step 6: Demonstrate Offset Replay via Kafka CLI

Demonstrate replaying historical orders by resetting a consumer group's offset to the beginning.

1. **Stop the Consumer**:
   * Stop **Terminal 3 (Customer Notification Service)** (`Ctrl + C`). *(Consumer group must be inactive during offset reset)*.
2. **Reset Offsets to Earliest using Kafka CLI**:
   ```bash
   docker exec -it kafka-broker /opt/kafka/bin/kafka-consumer-groups.sh \
     --bootstrap-server localhost:9092 \
     --group notification-service-group \
     --topic restaurant.orders.created \
     --reset-offsets \
     --to-earliest \
     --execute
   ```
3. **Verify Committed Offsets**:
   ```bash
   docker exec -it kafka-broker /opt/kafka/bin/kafka-consumer-groups.sh \
     --bootstrap-server localhost:9092 \
     --group notification-service-group \
     --describe
   ```
4. **Restart Notification Service**:
   * Start **Terminal 3** again:
     ```powershell
     .\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--service.type=notification"
     ```
   * The Notification Service will re-read and reprocess all historical orders from offset 0!

