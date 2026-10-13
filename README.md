# Leave Management System API

A RESTful Leave Management API built with **Spring Boot** following clean layered architecture, best practices, and strict input validation. The API allows employees to submit and manage leave requests, backed by an in-memory thread-safe data store.

---

## Table of Contents
- [Project Overview](#project-overview)
- [Key Features](#key-features)
- [Package Architecture](#package-architecture)
- [Tech Stack](#tech-stack)
- [Build and Run Instructions](#build-and-run-instructions)
- [Configuration and Spring Profiles](#configuration-and-spring-profiles)
- [API Endpoint Summary](#api-endpoint-summary)
  - [Sample Request and Response Payloads](#sample-request-and-response-payloads)
- [Testing Instructions](#testing-instructions)

---

## Project Overview

This project implements an HR Leave Management system API. It enables users to:
- Retrieve all leave requests or a single request by ID
- Submit new leave requests with automated ID assignment
- Replace existing leave requests (`PUT`) or apply partial updates (`PATCH`)
- Delete leave requests
- Receive structured error messages for invalid inputs or missing resources

The application uses an in-memory `ConcurrentHashMap` repository for persistence without requiring an external database.

---

## Key Features

- **Layered Architecture**: Strict separation of concerns across `Controller` → `Service` → `Repository`.
- **Constructor-Based Dependency Injection**: All dependencies are injected via constructors using `private final` fields for immutability and testability.
- **DTOs and Bean Validation**: Dedicated request and response models (`LeaveRequest`, `LeaveResponse`, `LeavePartialUpdateRequest`, `ErrorResponse`) with Jakarta Bean Validation (`@NotNull`, `@NotBlank`, `@Min`).
- **Object Mapping with MapStruct**: High-performance, compile-time mapping between entities and DTOs.
- **Global Exception Handling**: Centralized exception handling via `@RestControllerAdvice` mapping validation errors to `400 BAD_REQUEST`, resource misses to `404 NOT_FOUND`, and unhandled errors to `500 INTERNAL_SERVER_ERROR`.
- **Profile Configuration**: Multi-environment profile management using Spring Profiles (`dev` profile configured).
- **Automated Test Suite**:
  - Unit tests for the service layer with JUnit 5 & Mockito.
  - Integration/slice tests for REST controllers using `MockMvc`.

---

## Package Architecture

The application strictly adheres to the package structure specified in the assignment:

```text
com.leavemanagement.leave_management
├── configuration      # Application-level configuration classes (AppConfig)
├── controller         # REST Controller endpoints (LeaveManagementController)
├── dto                # Request & response data transfer objects (LeaveRequest, LeaveResponse, LeavePartialUpdateRequest, ErrorResponse)
├── exception          # Global exception handler and custom exceptions (LeaveManagementGlobalException, LeaveNotFoundException)
├── mapper             # MapStruct mapper interface (LeaveMapper)
├── model              # Core domain models (Leave)
├── repository         # In-memory thread-safe repository (LeaveManagementRepository)
└── service            # Business logic and coordination (LeaveManagementService)
```

---

## Tech Stack

- **Java**: 25
- **Spring Boot**: 4.1.1 (Spring Framework 7.x)
- **Mapping**: MapStruct 1.6.3
- **Validation**: Hibernate Validator / Jakarta Validation
- **Testing**: JUnit 5, Mockito, AssertJ, Spring Test MockMvc
- **Build Tool**: Apache Maven

---

## Build and Run Instructions

### Prerequisites
- JDK 25 installed and configured (`JAVA_HOME` set)
- Apache Maven installed (or use `./mvnw`)

### 1. Build the Application
```bash
mvn clean compile
```

### 2. Run the Application
```bash
mvn spring-boot:run
```
By default, the application runs on port **8080** under the **`dev`** profile:
- Server URL: `http://localhost:8080`
- Base API Endpoint: `http://localhost:8080/leaves`

---

## Configuration and Spring Profiles

Configuration is managed via YAML files:
- **`src/main/resources/application.yaml`**: Main configuration setting the application name and activating the `dev` profile:
  ```yaml
  spring:
    application:
      name: leave-management
    profiles:
      active: dev
  ```
- **`src/main/resources/application-dev.yaml`**: Profile-specific settings for the development environment:
  ```yaml
  server:
    port: 8080

  logging:
    level:
      com.leavemanagement.leave_management: DEBUG
      org.springframework.web: INFO
  ```

---

## API Endpoint Summary

| Method | Endpoint | Description | Request Body | Response Status |
|---|---|---|---|---|
| **GET** | `/leaves` | Retrieve all leave requests | None | `200 OK` |
| **GET** | `/leaves/{id}` | Retrieve a leave request by ID | None | `200 OK` / `404 Not Found` |
| **POST** | `/leaves` | Create a new leave request | `LeaveRequest` (JSON) | `201 Created` / `400 Bad Request` |
| **PUT** | `/leaves/{id}` | Replace an existing leave request | `LeaveRequest` (JSON) | `200 OK` / `404 Not Found` |
| **PATCH** | `/leaves/{id}` | Partially update an existing leave | `LeavePartialUpdateRequest` (JSON) | `200 OK` / `404 Not Found` |
| **DELETE** | `/leaves/{id}` | Delete a leave request | None | `204 No Content` / `404 Not Found` |

---

### Sample Request and Response Payloads

#### 1. Create a Leave Request (`POST /leaves`)
**Request Body:**
```json
{
  "employeeId": 101,
  "leaveType": "Sick Leave",
  "startDate": "2024-11-01",
  "endDate": "2024-11-05",
  "reason": "Viral fever and doctor consultation",
  "status": "Pending"
}
```

**Response (`201 Created`):**
```json
{
  "id": 11,
  "employeeId": 101,
  "leaveType": "Sick Leave",
  "startDate": "2024-11-01",
  "endDate": "2024-11-05",
  "reason": "Viral fever and doctor consultation",
  "status": "Pending"
}
```

#### 2. Partial Update (`PATCH /leaves/11`)
**Request Body:**
```json
{
  "status": "Approved"
}
```

**Response (`200 OK`):**
```json
{
  "id": 11,
  "employeeId": 101,
  "leaveType": "Sick Leave",
  "startDate": "2024-11-01",
  "endDate": "2024-11-05",
  "reason": "Viral fever and doctor consultation",
  "status": "Approved"
}
```

#### 3. Error Response (Validation Error - `400 Bad Request`)
```json
{
  "timestamp": "2026-10-13T19:50:00",
  "status": 400,
  "error": "Validation Error",
  "message": "{reason=Reason is required, employeeId=Employee ID must be greater than 0}"
}
```

#### 4. Error Response (Not Found - `404 Not Found`)
```json
{
  "timestamp": "2026-10-13T19:50:00",
  "status": 404,
  "error": "Leave Not Found",
  "message": "Leave with id :999 not found"
}
```

---

## Testing Instructions

The project includes unit tests for the service layer and MockMvc tests for the REST controller layer.

### Run All Tests
Execute the entire test suite via Maven:
```bash
mvn test
```

### Run Specific Test Classes
- **Service Layer Unit Tests (Mockito & JUnit 5):**
  ```bash
  mvn test -Dtest=LeaveManagementServiceTest
  ```
- **Controller Layer MockMvc Tests:**
  ```bash
  mvn test -Dtest=LeaveManagementControllerTest
  ```

### Test Suite Summary
- **Total Tests**: 23
- **Failures**: 0
- **Errors**: 0
- **Coverage**:
  - CRUD operations and exception paths in `LeaveManagementServiceTest` (11 tests)
  - HTTP endpoints, validation errors, and JSON serialization in `LeaveManagementControllerTest` (11 tests)
  - Application context loading in `LeaveManagementApplicationTests` (1 test)
