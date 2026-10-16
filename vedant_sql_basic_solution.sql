-- ====================================================================
-- SQL (Basic) Assignment Solution
-- Database: bookstore_db
-- ====================================================================

-- Database Creation and Selection
CREATE DATABASE bookstore_db;
USE bookstore_db;

-- --------------------------------------------------------------------
-- Table Creation
-- --------------------------------------------------------------------

CREATE TABLE Authors (
    author_id INT,
    author_name VARCHAR(100),
    country VARCHAR(50)
);

CREATE TABLE Books (
    book_id INT,
    title VARCHAR(100),
    author_id INT,
    category VARCHAR(50),
    price DECIMAL(10,2),
    published_year INT
);

-- --------------------------------------------------------------------
-- Data Population
-- --------------------------------------------------------------------

-- Insert into Authors
INSERT INTO Authors (author_id, author_name, country) VALUES (1, 'Ernest Hemingway', 'USA');
INSERT INTO Authors (author_id, author_name, country) VALUES (2, 'Jane Austen', 'UK');
INSERT INTO Authors (author_id, author_name, country) VALUES (3, 'Gabriel García Márquez', 'Colombia');
INSERT INTO Authors (author_id, author_name, country) VALUES (4, 'Haruki Murakami', 'Japan');
INSERT INTO Authors (author_id, author_name, country) VALUES (5, 'Chimamanda Ngozi Adichie', 'Nigeria');

-- Insert into Books
INSERT INTO books (book_id, title, author_id, category, price, published_year) VALUES
(1, 'SQL Fundamentals', 1, 'Programming', 750.00, 2021),
(2, 'Python Programming Essentials', 1, 'Programming', 850.00, 2022),
(3, 'Silent Spring Chronicles', 2, 'Fiction', 450.00, 2019),
(4, 'Shadows of the Past', 2, 'Fiction', 650.00, 2020),
(5, 'Modern Data Systems', 3, 'Data Science', 780.00, 2024),
(6, 'Smart Algorithms', 3, 'Programming', 620.00, 2022),
(7, 'Machine Learning Basics', 4, 'Data Science', 550.00, 2021),
(8, 'The Art of Design', 4, 'Design', 400.00, 2018),
(9, 'Structure and Interpretation', 1, 'Programming', 920.00, 2023),
(10, 'History of Computing', 2, 'History', 510.00, 2017);


-- ====================================================================
-- Section 1: Basic Queries
-- ====================================================================

-- Task 1: Display all books.
SELECT * FROM books;

-- Task 2: Display only the book title and price.
SELECT title, price FROM books;

-- Task 3: Display the book title as Book Name and price as Book Price using column aliases.
SELECT title as Book_Name, price as Book_Price FROM Books;

-- Task 4: Display all unique book categories.
SELECT DISTINCT category FROM books;


-- ====================================================================
-- Section 2: Filtering Data
-- ====================================================================

-- Task 5: Display books priced above ₹700.
SELECT * FROM books WHERE price>700;

-- Task 6: Display books priced between ₹500 and ₹800.
SELECT * FROM books WHERE price BETWEEN 500 AND 800;

-- Task 7: Display books that belong to the Programming category.
SELECT * FROM books WHERE category='Programming';

-- Task 8: Display books whose titles start with the letter S.
SELECT * FROM books where title like 'S%';

-- Task 9: Display books published after 2020.
SELECT * FROM books where published_year>2020;


-- ====================================================================
-- Section 3: Sorting and Limiting Results
-- ====================================================================

-- Task 10: Display books sorted by price in descending order.
SELECT * FROM books ORDER BY price DESC;

-- Task 11: Display the top 3 most expensive books.
SELECT * FROM books ORDER BY price DESC LIMIT 3;


-- ====================================================================
-- Section 4: Aggregate Functions
-- ====================================================================

-- Task 12: Find:
-- Total number of books
SELECT COUNT(*) FROM books;

-- Total price of all books
SELECT SUM(price) FROM books;

-- Average book price
SELECT AVG(price) FROM books;

-- Highest book price
SELECT MAX(price) FROM books;

-- Lowest book price
SELECT MIN(price) FROM books;


-- ====================================================================
-- Section 5: GROUP BY and HAVING
-- ====================================================================

-- Task 13: Display the average book price for each category.
SELECT category, AVG(price) FROM books GROUP BY category;

-- Task 14: Display only those categories whose average book price is greater than ₹650.
SELECT category , AVG(price) FROM books GROUP BY category HAVING AVG(price)> 650;


-- ====================================================================
-- Section 6: Joins
-- ====================================================================

-- Task 15: Display each book along with its author's name using an INNER JOIN.
SELECT b.title, a.author_name FROM books b INNER JOIN authors a ON b.author_id = a.author_id;

-- Task 16: Display all authors along with the books they have written using a LEFT JOIN.
SELECT a.author_name, b.title FROM authors a LEFT JOIN books b ON a.author_id = b.author_id;

-- Task 17: Display all books along with their author details using a RIGHT JOIN.
SELECT b.book_id, b.title, a.author_name, a.country FROM authors a RIGHT JOIN books b ON a.author_id = b.author_id;


-- ====================================================================
-- Section 7: Data Manipulation
-- ====================================================================

-- Task 18: Insert a new book into the Books table.
INSERT into books (book_id, title, author_id, category, price, published_year)
VALUES (11, 'The Great Gatsby', 118, 'Classic Fiction', 10.99, 1925);

-- Task 19: Update the price of the book you inserted.
UPDATE books SET price=25.40 WHERE book_id =11;

-- Task 20: Delete the book record that you inserted.
DELETE FROM books WHERE book_id =11;


-- ====================================================================
-- Section 8: Creating Tables and Constraints
-- ====================================================================

-- Task 21: Create a new table named Publishers with the following columns:
-- publisher_id, publisher_name, country, established_year
-- Constraints: PRIMARY KEY, NOT NULL, UNIQUE, DEFAULT
CREATE TABLE Publishers (
    publisher_id INT PRIMARY KEY,
    publisher_name VARCHAR(100) NOT NULL,
    country VARCHAR(50) UNIQUE,
    established_year INT DEFAULT 1997
);

-- Describe table to verify constraints
DESC Publishers;