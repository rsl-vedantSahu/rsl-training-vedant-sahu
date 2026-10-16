-- ====================================================================
-- SQL (Advanced) Assignment Solution
-- Author: Vedant Sahu
-- Database: ecommerce_db
-- ====================================================================

-- --------------------------------------------------------------------
-- Database Initialization
-- --------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS ecommerce_db;
USE ecommerce_db;

-- --------------------------------------------------------------------
-- Table Creation
-- --------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS Customers (
    customer_id INT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    city VARCHAR(50),
    email VARCHAR(100) UNIQUE
);

CREATE TABLE IF NOT EXISTS Products (
    product_id INT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_date DATE DEFAULT '2024-03-15'
);

CREATE TABLE IF NOT EXISTS Orders (
    order_id INT PRIMARY KEY,
    customer_id INT,
    product_id INT,
    quantity INT,
    order_date DATE,
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id),
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

-- --------------------------------------------------------------------
-- Data Population
-- --------------------------------------------------------------------

-- Insert into Customers (10 customers)
INSERT INTO Customers (customer_id, customer_name, city, email) VALUES
(1, 'Aarav Sharma', 'Mumbai', 'aarav.sharma@example.com'),
(2, 'Diya Patel', 'Ahmedabad', 'diya.patel@example.com'),
(3, 'Rohan Verma', 'Delhi', 'rohan.verma@example.com'),
(4, 'Ananya Iyer', 'Bengaluru', 'ananya.iyer@example.com'),
(5, 'Kabir Mehta', 'Pune', 'kabir.mehta@example.com'),
(6, 'Pooja Reddy', 'Hyderabad', 'pooja.reddy@example.com'),
(7, 'Siddharth Rao', 'Chennai', 'siddharth.rao@example.com'),
(8, 'Neha Joshi', 'Jaipur', 'neha.joshi@example.com'),
(9, 'Vikram Malhotra', 'Kolkata', 'vikram.malhotra@example.com'),
(10, 'Sneha Kulkarni', 'Pune', 'sneha.kulkarni@example.com');

-- Insert into Products (10 products)
INSERT INTO Products (product_id, product_name, category, price, created_date) VALUES
(1, 'Wireless Mouse', 'Electronics', 799.00, '2024-01-10'),
(2, 'Mechanical Keyboard', 'Electronics', 2499.00, '2024-01-15'),
(3, 'Noise Cancelling Headphones', 'Electronics', 6999.00, '2024-02-01'),
(4, 'Smartwatch Pro', 'Electronics', 4500.00, '2024-02-10'),
(5, 'Running Shoes', 'Footwear', 3200.00, '2024-02-15'),
(6, 'Casual Sneakers', 'Footwear', 1800.00, '2024-03-01'),
(7, 'Flip Flops', 'Footwear', 499.00, '2024-03-05'),
(8, 'Cotton T-Shirt', 'Clothing', 599.00, '2024-03-12'),
(9, 'Denim Jacket', 'Clothing', 2199.00, '2024-03-20'),
(10, 'Leather Wallet', 'Accessories', 899.00, '2024-04-01');

-- Insert into Orders (20 orders)
INSERT INTO Orders (order_id, customer_id, product_id, quantity, order_date) VALUES
(101, 1, 1, 2, '2024-01-15'),
(102, 1, 3, 1, '2024-01-18'),
(103, 2, 2, 1, '2024-02-01'),
(104, 2, 8, 3, '2024-02-05'),
(105, 3, 4, 1, '2024-02-12'),
(106, 3, 5, 1, '2024-02-20'),
(107, 4, 6, 2, '2024-03-02'),
(108, 4, 10, 1, '2024-03-10'),
(109, 5, 7, 2, '2024-03-15'),
(110, 5, 9, 1, '2024-03-22'),
(111, 6, 2, 2, '2024-04-05'),
(112, 6, 3, 1, '2024-04-12'),
(113, 7, 5, 1, '2024-04-18'),
(114, 7, 1, 1, '2024-04-25'),
(115, 8, 8, 4, '2024-05-02'),
(116, 8, 4, 1, '2024-05-14'),
(117, 1, 6, 1, '2024-05-20'),
(118, 2, 9, 1, '2024-06-01'),
(119, 3, 1, 1, '2024-06-15'),
(120, 4, 5, 2, '2024-06-28');


-- ====================================================================
-- Section 1: Advanced Joins
-- ====================================================================

-- Task 1: Display each customer along with the products they ordered.
SELECT c.customer_name, p.product_name, o.order_id 
FROM Customers c
JOIN Orders o ON c.customer_id = o.customer_id
JOIN Products p ON p.product_id = o.product_id;

-- Task 2: Display all possible combinations of customers and products using a CROSS JOIN.
SELECT c.customer_name, p.product_name
FROM Customers c 
CROSS JOIN Products p;

-- Task 3: Display all customers and their orders, including customers who have not placed any orders.
SELECT c.customer_name, o.order_id, o.order_date
FROM Customers c
LEFT JOIN Orders o ON c.customer_id = o.customer_id;


-- ====================================================================
-- Section 2: Subqueries
-- ====================================================================

-- Task 4: Find products priced above the average product price.
SELECT product_id, product_name, category, price
FROM Products
WHERE price > (SELECT AVG(price) FROM Products);

-- Task 5: Display customers whose total purchase amount is greater than the average customer purchase amount.
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

-- Task 6: Find the highest-priced product in each category using a correlated subquery.
SELECT p1.product_id, p1.product_name, p1.category, p1.price
FROM Products p1
WHERE p1.price = (
    SELECT MAX(p2.price)
    FROM Products p2
    WHERE p2.category = p1.category
);


-- ====================================================================
-- Section 3: Set Operations
-- ====================================================================

-- Task 7: Combine products from two different categories using UNION.
SELECT product_id, product_name, category, price
FROM Products
WHERE category = 'Electronics'
UNION
SELECT product_id, product_name, category, price
FROM Products
WHERE category = 'Footwear';

-- Task 8: Write MySQL alternatives for INTERSECT and EXCEPT using INNER JOIN and LEFT JOIN.

-- Alternative for INTERSECT using INNER JOIN:
-- Finds customers who exist in Customers AND have placed orders
SELECT DISTINCT c.customer_id, c.customer_name
FROM Customers c
INNER JOIN Orders o ON c.customer_id = o.customer_id;

-- Alternative for EXCEPT using LEFT JOIN:
-- Finds customers in Customers EXCEPT those who have placed orders
SELECT c.customer_id, c.customer_name
FROM Customers c
LEFT JOIN Orders o ON c.customer_id = o.customer_id
WHERE o.order_id IS NULL;


-- ====================================================================
-- Section 4: Window Functions
-- ====================================================================

-- Task 9: Rank products by price within each category.
SELECT 
    product_id,
    product_name,
    category,
    price,
    RANK() OVER (PARTITION BY category ORDER BY price DESC) AS price_rank
FROM Products;

-- Task 10: Display the previous product price (LAG) and a running total of product prices using window functions.
SELECT 
    product_id,
    product_name,
    price,
    LAG(price, 1) OVER (ORDER BY product_id) AS previous_product_price,
    SUM(price) OVER (ORDER BY product_id) AS running_total_price
FROM Products;


-- ====================================================================
-- Section 5: Common Table Expressions (CTEs)
-- ====================================================================

-- Task 11: Create a CTE to display customers whose total purchase amount is above the average.
WITH CustomerSpending AS (
    SELECT 
        c.customer_id,
        c.customer_name,
        SUM(p.price * o.quantity) AS total_purchase_amount
    FROM Customers c
    JOIN Orders o ON c.customer_id = o.customer_id
    JOIN Products p ON o.product_id = p.product_id
    GROUP BY c.customer_id, c.customer_name
),
AverageSpending AS (
    SELECT AVG(total_purchase_amount) AS avg_amount 
    FROM CustomerSpending
)
SELECT 
    cs.customer_id,
    cs.customer_name,
    cs.total_purchase_amount
FROM CustomerSpending cs
CROSS JOIN AverageSpending a
WHERE cs.total_purchase_amount > a.avg_amount;

-- Task 12: Create a recursive CTE to generate a sequence of numbers from 1 to 10.
WITH RECURSIVE NumberSequence AS (
    SELECT 1 AS number
    UNION ALL
    SELECT number + 1 
    FROM NumberSequence 
    WHERE number < 10
)
SELECT number FROM NumberSequence;


-- ====================================================================
-- Section 6: Views
-- ====================================================================

-- Task 13: Create a view that displays products priced above ₹1,000 and retrieve data from the view.
CREATE OR REPLACE VIEW ExpensiveProductsView AS
SELECT product_id, product_name, category, price
FROM Products
WHERE price > 1000.00;

-- Retrieve data from view
SELECT * FROM ExpensiveProductsView;


-- ====================================================================
-- Section 7: Indexes and Query Performance
-- ====================================================================

-- Task 14: Create an index on the category column of the Products table.
CREATE INDEX idx_products_category ON Products(category);

-- Task 15: Use EXPLAIN to analyze a query that filters products by category.
EXPLAIN SELECT * FROM Products WHERE category = 'Electronics';


-- ====================================================================
-- Section 8: Stored Procedures and Functions
-- ====================================================================

-- Task 16: Create a stored procedure to retrieve products by category.
DROP PROCEDURE IF EXISTS GetProductsByCategory;

DELIMITER //

CREATE PROCEDURE GetProductsByCategory(IN p_category VARCHAR(50))
BEGIN
    SELECT product_id, product_name, category, price
    FROM Products
    WHERE category = p_category;
END //

DELIMITER ;

-- Test procedure
CALL GetProductsByCategory('Electronics');

-- Task 17: Create a function to calculate the total price of an order (price × quantity).
DROP FUNCTION IF EXISTS CalculateOrderTotal;

DELIMITER //

CREATE FUNCTION CalculateOrderTotal(p_price DECIMAL(10,2), p_quantity INT)
RETURNS DECIMAL(10,2)
DETERMINISTIC
NO SQL
BEGIN
    RETURN p_price * p_quantity;
END //

DELIMITER ;

-- Test function
SELECT 
    o.order_id,
    p.product_name,
    p.price,
    o.quantity,
    CalculateOrderTotal(p.price, o.quantity) AS total_order_price
FROM Orders o
JOIN Products p ON o.product_id = p.product_id
LIMIT 5;


-- ====================================================================
-- Section 9: Transactions
-- ====================================================================

-- Task 18: Write a transaction that updates a product's price and demonstrate the use of COMMIT and ROLLBACK.

-- Part 1: Demonstrate ROLLBACK
START TRANSACTION;
UPDATE Products SET price = 999.00 WHERE product_id = 1;
SELECT product_id, product_name, price FROM Products WHERE product_id = 1;
ROLLBACK;
SELECT product_id, product_name, price FROM Products WHERE product_id = 1;

-- Part 2: Demonstrate COMMIT
START TRANSACTION;
UPDATE Products SET price = 849.00 WHERE product_id = 1;
COMMIT;
SELECT product_id, product_name, price FROM Products WHERE product_id = 1;


-- ====================================================================
-- Section 10: SQL Triggers
-- ====================================================================

-- Task 19: Create a trigger that automatically records new orders in an Order_Audit table whenever a new order is inserted.

CREATE TABLE IF NOT EXISTS Order_Audit (
    audit_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    customer_id INT,
    product_id INT,
    quantity INT,
    order_date DATE,
    action_type VARCHAR(20) DEFAULT 'INSERT',
    action_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DROP TRIGGER IF EXISTS trg_after_order_insert;

DELIMITER //

CREATE TRIGGER trg_after_order_insert
AFTER INSERT ON Orders
FOR EACH ROW
BEGIN
    INSERT INTO Order_Audit (order_id, customer_id, product_id, quantity, order_date, action_type, action_timestamp)
    VALUES (NEW.order_id, NEW.customer_id, NEW.product_id, NEW.quantity, NEW.order_date, 'INSERT', NOW());
END //

DELIMITER ;

-- Test trigger
INSERT INTO Orders (order_id, customer_id, product_id, quantity, order_date)
VALUES (121, 1, 2, 1, '2024-07-01');

SELECT * FROM Order_Audit;


-- ====================================================================
-- Section 11: Performance Tuning
-- ====================================================================

-- Task 20: Optimize the queries by applying SQL performance tuning best practices:
-- 1. Avoid SELECT *.
-- 2. Remove unnecessary DISTINCT.
-- 3. Avoid functions on indexed columns (make date comparison sargable).
-- 4. Use an index-friendly query.

-- Composite index to support the optimized query:
CREATE INDEX idx_products_category_date ON Products(category, created_date);

-- Optimized Query:
SELECT product_id, product_name, category, price
FROM Products
WHERE category = 'Electronics'
  AND created_date >= '2024-01-01' 
  AND created_date < '2025-01-01';

-- Explain plan for optimized query:
EXPLAIN SELECT product_id, product_name, category, price
FROM Products
WHERE category = 'Electronics'
  AND created_date >= '2024-01-01' 
  AND created_date < '2025-01-01';
