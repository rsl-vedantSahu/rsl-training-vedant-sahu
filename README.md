# SQL (Advanced) Assignment: Online Shopping Platform Database

## Overview
This repository contains the advanced SQL database implementation for an **Online Shopping Platform** (`ecommerce_db`). The project demonstrates advanced database engineering, query optimization, transaction management, procedural programming, and analytical reporting across 11 distinct sections and 20 comprehensive tasks.

---

## Database Schema

The database consists of the following core tables and audit structures:

### 1. `Customers` Table
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `customer_id` | `INT` | `PRIMARY KEY` | Unique customer identifier |
| `customer_name` | `VARCHAR(100)` | `NOT NULL` | Customer's full name |
| `city` | `VARCHAR(50)` | | Residential city |
| `email` | `VARCHAR(100)` | `UNIQUE` | Unique email address |

### 2. `Products` Table
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `product_id` | `INT` | `PRIMARY KEY` | Unique product identifier |
| `product_name` | `VARCHAR(100)` | `NOT NULL` | Product title |
| `category` | `VARCHAR(50)` | `NOT NULL` | Product category/genre |
| `price` | `DECIMAL(10,2)` | `NOT NULL` | Unit price in INR (₹) |
| `created_date` | `DATE` | `DEFAULT '2024-03-15'` | Catalog creation date |

### 3. `Orders` Table
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `order_id` | `INT` | `PRIMARY KEY` | Unique order identifier |
| `customer_id` | `INT` | `FOREIGN KEY` (references `Customers.customer_id`) | Ordering customer ID |
| `product_id` | `INT` | `FOREIGN KEY` (references `Products.product_id`) | Ordered product ID |
| `quantity` | `INT` | `NOT NULL` | Units purchased |
| `order_date` | `DATE` | `NOT NULL` | Date order placed |

### 4. `Order_Audit` Table (Created in Section 10)
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `audit_id` | `INT` | `AUTO_INCREMENT PRIMARY KEY` | Audit record identifier |
| `order_id` | `INT` | `NOT NULL` | Order ID logged |
| `customer_id` | `INT` | `NOT NULL` | Customer ID |
| `product_id` | `INT` | `NOT NULL` | Product ID |
| `quantity` | `INT` | `NOT NULL` | Quantity |
| `order_date` | `DATE` | `NOT NULL` | Original order date |
| `action_type` | `VARCHAR(20)` | `DEFAULT 'INSERT'` | Action logged |
| `action_timestamp` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | System timestamp |

---

## Steps to Execute the SQL Script

### Prerequisites
- MySQL Server (v8.0 or later) installed and running on port 3306.
- Terminal, Command Prompt, or MySQL CLI client.

### Method 1: Command Line Execution (Recommended)
From the repository root, run:
```bash
mysql -u root -p < vedant_sql_advanced_solution.sql
```

### Method 2: MySQL Interactive Prompt Execution
```sql
USE ecommerce_db;
SOURCE vedant_sql_advanced_solution.sql;
```

---

## Summary of Implemented Advanced SQL Concepts

| Concept | Implementation Details | Key Syntax / Keywords |
| :--- | :--- | :--- |
| **Advanced Joins** | Multi-table relational joins, full Cartesian products, unmatched outer preservation. | `INNER JOIN`, `CROSS JOIN`, `LEFT JOIN ... WHERE NULL` |
| **Subqueries** | Scalar aggregations, nested group-level comparisons, and row-by-row correlated evaluation. | `WHERE col > (SELECT AVG(...))`, Correlated `p2.category = p1.category` |
| **Set Operations** | Combining row sets with deduplication, simulating missing set operations in MySQL. | `UNION`, `INNER JOIN` (INTERSECT), `LEFT JOIN` (EXCEPT) |
| **Window Functions** | Analytical partitioning and cumulative calculations without row collapsing. | `RANK() OVER (PARTITION BY ...)`, `LAG()`, `SUM() OVER ()` |
| **CTEs (WITH)** | Modular CTE chaining for readable sub-aggregations, and recursive iterative generation. | `WITH ... AS (...)`, `WITH RECURSIVE ... UNION ALL` |
| **Views** | Encapsulating reusable business logic and security filtering into virtual tables. | `CREATE OR REPLACE VIEW ... AS SELECT` |
| **Indexing & Performance** | B-Tree indexing on filtering columns and execution plan inspection. | `CREATE INDEX`, `EXPLAIN SELECT ...` |
| **Stored Procedures & Functions** | Encapsulating server-side operational routines and reusable scalar math logic. | `CREATE PROCEDURE`, `CREATE FUNCTION ... DETERMINISTIC` |
| **Transactions** | Maintaining ACID compliance, isolated dirty reads, and controlled rollbacks. | `START TRANSACTION`, `COMMIT`, `ROLLBACK` |
| **Triggers** | Event-driven automated auditing on row insertion. | `CREATE TRIGGER ... AFTER INSERT ON ... FOR EACH ROW` |
| **Query Optimization** | Transforming non-sargable predicates to range scans, eliminating unnecessary sorting. | Sargable date range, removing `DISTINCT`, avoiding `SELECT *` |

---

## Task Walkthrough & Query Reference

### Section 1: Advanced Joins
- **Task 1: Customer & Product Orders Join**
  ```sql
  SELECT c.customer_name, p.product_name, o.order_id 
  FROM Customers c
  JOIN Orders o ON c.customer_id = o.customer_id
  JOIN Products p ON p.product_id = o.product_id;
  ```
- **Task 2: Cartesian Product (CROSS JOIN)**
  ```sql
  SELECT c.customer_name, p.product_name
  FROM Customers c 
  CROSS JOIN Products p;
  ```
- **Task 3: Outer Join (All Customers Including Unordered)**
  ```sql
  SELECT c.customer_name, o.order_id, o.order_date
  FROM Customers c
  LEFT JOIN Orders o ON c.customer_id = o.customer_id;
  ```

---

### Section 2: Subqueries
- **Task 4: Above Average Priced Products**
  ```sql
  SELECT product_id, product_name, category, price
  FROM Products
  WHERE price > (SELECT AVG(price) FROM Products);
  ```
- **Task 5: Customers Exceeding Average Spending**
  ```sql
  SELECT c.customer_id, c.customer_name, SUM(p.price * o.quantity) AS total_spent
  FROM Customers c
  JOIN Orders o ON c.customer_id = o.customer_id
  JOIN Products p ON o.product_id = p.product_id
  GROUP BY c.customer_id, c.customer_name
  HAVING total_spent > (
      SELECT AVG(customer_spending)
      FROM (
          SELECT SUM(p2.price * o2.quantity) AS customer_spending
          FROM Orders o2
          JOIN Products p2 ON o2.product_id = p2.product_id
          GROUP BY o2.customer_id
      ) AS spending_subquery
  );
  ```
- **Task 6: Highest-Priced Product per Category (Correlated Subquery)**
  ```sql
  SELECT p1.product_id, p1.product_name, p1.category, p1.price
  FROM Products p1
  WHERE p1.price = (
      SELECT MAX(p2.price)
      FROM Products p2
      WHERE p2.category = p1.category
  );
  ```

---

### Section 3: Set Operations
- **Task 7: UNION of Two Categories**
  ```sql
  SELECT product_id, product_name, category, price FROM Products WHERE category = 'Electronics'
  UNION
  SELECT product_id, product_name, category, price FROM Products WHERE category = 'Footwear';
  ```
- **Task 8: INTERSECT & EXCEPT Alternatives**
  ```sql
  -- INTERSECT via INNER JOIN
  SELECT DISTINCT c.customer_id, c.customer_name
  FROM Customers c INNER JOIN Orders o ON c.customer_id = o.customer_id;

  -- EXCEPT via LEFT JOIN
  SELECT c.customer_id, c.customer_name
  FROM Customers c LEFT JOIN Orders o ON c.customer_id = o.customer_id
  WHERE o.order_id IS NULL;
  ```

---

### Section 4: Window Functions
- **Task 9: Price Ranking within Category**
  ```sql
  SELECT product_id, product_name, category, price,
         RANK() OVER (PARTITION BY category ORDER BY price DESC) AS price_rank
  FROM Products;
  ```
- **Task 10: Previous Price (LAG) and Running Total**
  ```sql
  SELECT product_id, product_name, price,
         LAG(price, 1) OVER (ORDER BY product_id) AS previous_product_price,
         SUM(price) OVER (ORDER BY product_id) AS running_total_price
  FROM Products;
  ```

---

### Section 5: Common Table Expressions (CTEs)
- **Task 11: Customers Above Average Spending (CTE)**
  ```sql
  WITH CustomerSpending AS (
      SELECT c.customer_id, c.customer_name, SUM(p.price * o.quantity) AS total_purchase_amount
      FROM Customers c
      JOIN Orders o ON c.customer_id = o.customer_id
      JOIN Products p ON o.product_id = p.product_id
      GROUP BY c.customer_id, c.customer_name
  ),
  AverageSpending AS (
      SELECT AVG(total_purchase_amount) AS avg_amount FROM CustomerSpending
  )
  SELECT cs.customer_id, cs.customer_name, cs.total_purchase_amount
  FROM CustomerSpending cs
  CROSS JOIN AverageSpending a
  WHERE cs.total_purchase_amount > a.avg_amount;
  ```
- **Task 12: Recursive CTE (Sequence 1 to 10)**
  ```sql
  WITH RECURSIVE NumberSequence AS (
      SELECT 1 AS number
      UNION ALL
      SELECT number + 1 FROM NumberSequence WHERE number < 10
  )
  SELECT number FROM NumberSequence;
  ```

---

### Section 6: Views
- **Task 13: View for Products Above ₹1,000**
  ```sql
  CREATE OR REPLACE VIEW ExpensiveProductsView AS
  SELECT product_id, product_name, category, price
  FROM Products
  WHERE price > 1000.00;

  SELECT * FROM ExpensiveProductsView;
  ```

---

### Section 7: Indexes and Query Performance
- **Task 14: Category Index Creation**
  ```sql
  CREATE INDEX idx_products_category ON Products(category);
  ```
- **Task 15: Query Plan Inspection (EXPLAIN)**
  ```sql
  EXPLAIN SELECT * FROM Products WHERE category = 'Electronics';
  ```

---

### Section 8: Stored Procedures and Functions
- **Task 16: Category Retrieval Stored Procedure**
  ```sql
  DELIMITER //
  CREATE PROCEDURE GetProductsByCategory(IN p_category VARCHAR(50))
  BEGIN
      SELECT product_id, product_name, category, price
      FROM Products
      WHERE category = p_category;
  END //
  DELIMITER ;

  CALL GetProductsByCategory('Electronics');
  ```
- **Task 17: Order Total Calculation Scalar Function**
  ```sql
  DELIMITER //
  CREATE FUNCTION CalculateOrderTotal(p_price DECIMAL(10,2), p_quantity INT)
  RETURNS DECIMAL(10,2)
  DETERMINISTIC
  NO SQL
  BEGIN
      RETURN p_price * p_quantity;
  END //
  DELIMITER ;

  SELECT o.order_id, p.product_name, CalculateOrderTotal(p.price, o.quantity) AS total_order_price
  FROM Orders o JOIN Products p ON o.product_id = p.product_id LIMIT 5;
  ```

---

### Section 9: Transactions
- **Task 18: Price Update with COMMIT and ROLLBACK**
  ```sql
  -- Rollback Demonstration:
  START TRANSACTION;
  UPDATE Products SET price = 999.00 WHERE product_id = 1;
  ROLLBACK;

  -- Commit Demonstration:
  START TRANSACTION;
  UPDATE Products SET price = 849.00 WHERE product_id = 1;
  COMMIT;
  ```

---

### Section 10: SQL Triggers
- **Task 19: Order Audit Automated Trigger**
  ```sql
  DELIMITER //
  CREATE TRIGGER trg_after_order_insert
  AFTER INSERT ON Orders
  FOR EACH ROW
  BEGIN
      INSERT INTO Order_Audit (order_id, customer_id, product_id, quantity, order_date, action_type, action_timestamp)
      VALUES (NEW.order_id, NEW.customer_id, NEW.product_id, NEW.quantity, NEW.order_date, 'INSERT', NOW());
  END //
  DELIMITER ;

  INSERT INTO Orders (order_id, customer_id, product_id, quantity, order_date)
  VALUES (121, 1, 2, 1, '2024-07-01');

  SELECT * FROM Order_Audit;
  ```

---

### Section 11: Performance Tuning
- **Task 20: Performance Optimization & Best Practices**
  - **Unoptimized Example:**
    ```sql
    SELECT DISTINCT * FROM Products WHERE YEAR(created_date) = 2024 AND category = 'Electronics';
    ```
  - **Applied Improvements:**
    1. Replaced `SELECT *` with explicit columns (`product_id, product_name, category, price`).
    2. Removed redundant `DISTINCT` deduplication pass.
    3. Replaced non-sargable `YEAR(created_date)` function with index-friendly date range:
       `created_date >= '2024-01-01' AND created_date < '2025-01-01'`.
    4. Supported query with a composite index `(category, created_date)`.
  - **Optimized Query:**
    ```sql
    CREATE INDEX idx_products_category_date ON Products(category, created_date);

    SELECT product_id, product_name, category, price
    FROM Products
    WHERE category = 'Electronics'
      AND created_date >= '2024-01-01' 
      AND created_date < '2025-01-01';
    ```

---

## Required Deliverables & Screenshots Checklist

Per assignment requirements, execution screenshots for the following 9 areas are captured:

| # | Required Concept | Related Task | Screenshot Path |
| :-: | :--- | :--- | :--- |
| 1 | **Advanced Join query** | Task 1 & Task 3 | [`screenshots/advance-join-1.png`](screenshots/advance-join-1.png), [`advance-join-2.png`](screenshots/advance-join-2.png), [`advance-join-3.png`](screenshots/advance-join-3.png) |
| 2 | **Subquery** | Task 4, 5 & 6 | [`screenshots/subquery-sql-advance-1.png`](screenshots/subquery-sql-advance-1.png), [`subquery-sql-advance-2.png`](screenshots/subquery-sql-advance-2.png) |
| 3 | **Window Function** | Task 9 or Task 10 | [`screenshots/CTE.png`](screenshots/CTE.png) |
| 4 | **CTE** | Task 11 or Task 12 | [`screenshots/CTE.png`](screenshots/CTE.png) |
| 5 | **View** | Task 13 | [`screenshots/VIEW.png`](screenshots/VIEW.png) |
| 6 | **Stored Procedure or Function** | Task 16 or Task 17 | [`screenshots/Stored Procedures.png`](screenshots/Stored%20Procedures.png), [`screenshots/Functions.png`](screenshots/Functions.png) |
| 7 | **Trigger Execution** | Task 19 | [`screenshots/SQL Triggers.png`](screenshots/SQL%20Triggers.png) |
| 8 | **Transaction (COMMIT / ROLLBACK)** | Task 18 | [`screenshots/Transactions.png`](screenshots/Transactions.png) |
| 9 | **EXPLAIN Query Output** | Task 15 or Task 20 | [`screenshots/EXPLAIN.png`](screenshots/EXPLAIN.png) |

---

## Submission Details
- **Branch Name:** `vedant_sahu_sql_advance`
- **Solution File:** `vedant_sql_advanced_solution.sql`
- **Documentation:** `README.md`