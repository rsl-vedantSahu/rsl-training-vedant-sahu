# Observability Skeleton - OpenTelemetry CRUD Application

This repository contains a Spring Boot User CRUD application instrumented with **OpenTelemetry** for full-stack distributed observability (Traces, Metrics, and Logs). It demonstrates both **automatic instrumentation** (using the OpenTelemetry Java Agent) and **manual instrumentation** (using the OpenTelemetry Java API).

---

## 1. Prerequisites

- **Java Development Kit (JDK)**: Java 17 or higher (tested with JDK 25 LTS)
- **Apache Maven**: Version 3.8+ (or Maven Wrapper `./mvnw`)
- **OpenTelemetry Java Agent**: `opentelemetry-javaagent.jar` (v2.34.0+ included in root or downloaded from [OpenTelemetry Java Releases](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases))
- **cURL**: For issuing HTTP requests

---

## 2. How to Build the Application

Build the executable Spring Boot JAR using Maven:

```bash
mvn clean package -DskipTests
```
*(On Windows with wrapper: `.\mvnw.cmd clean package -DskipTests`)*

This produces the fat JAR:
`target/observability-skeleton-0.0.1-SNAPSHOT.jar`

---

## 3. How to Run with the OpenTelemetry Java Agent

Run the packaged JAR attaching the OpenTelemetry Java Agent via the `-javaagent` flag:

```bash
java -javaagent:opentelemetry-javaagent.jar \
  -Dotel.service.name=crud-user-service \
  -Dotel.traces.exporter=logging \
  -Dotel.metrics.exporter=logging \
  -Dotel.logs.exporter=logging \
  -Dotel.metric.export.interval=5000 \
  -jar target/observability-skeleton-0.0.1-SNAPSHOT.jar
```

### On Windows PowerShell:
```powershell
java -javaagent:opentelemetry-javaagent.jar `
  -Dotel.service.name=crud-user-service `
  -Dotel.traces.exporter=logging `
  -Dotel.metrics.exporter=logging `
  -Dotel.logs.exporter=logging `
  -Dotel.metric.export.interval=5000 `
  -jar target\observability-skeleton-0.0.1-SNAPSHOT.jar
```

### Optional: Exporting via OTLP (e.g. to OpenTelemetry Collector, Jaeger, Prometheus)
```bash
java -javaagent:opentelemetry-javaagent.jar \
  -Dotel.service.name=crud-user-service \
  -Dotel.traces.exporter=otlp \
  -Dotel.metrics.exporter=otlp \
  -Dotel.exporter.otlp.endpoint=http://localhost:4317 \
  -jar target/observability-skeleton-0.0.1-SNAPSHOT.jar
```

The application starts on `http://localhost:8080`.

---

## 4. Required OpenTelemetry Configuration

1. **OpenTelemetry API Dependency (`pom.xml`)**:
   - Managed via `io.opentelemetry:opentelemetry-bom` (version `1.34.1`).
   - Declares `io.opentelemetry:opentelemetry-api` in `<dependencies>`.
2. **Log Correlation Pattern (`src/main/resources/application.properties`)**:
   ```properties
   spring.application.name=crud-user-service
   server.port=8080
   logging.pattern.level=%5p [trace_id=%X{trace_id} span_id=%X{span_id}]
   ```
   The Java Agent automatically populates SLF4J MDC with the current `trace_id` and `span_id`.

---

## 5. Example Commands for Generating API Traffic

The following commands test both successful operations and failure scenarios:

### 1. Create User (Alice) - Success (201 Created)
```bash
curl -i -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com"}'
```

### 2. Retrieve User - Success (200 OK)
```bash
curl -i http://localhost:8080/users/1
```

### 3. Retrieve Non-Existent User - Failure (404 Not Found)
```bash
curl -i http://localhost:8080/users/999
```

### 4. Delete User - Success (204 No Content)
```bash
curl -i -X DELETE http://localhost:8080/users/1
```

### 5. Delete Non-Existent User - Failure (404 Not Found)
```bash
curl -i -X DELETE http://localhost:8080/users/999
```

### 6. Create User (Bob) - Success (201 Created)
```bash
curl -i -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Bob","email":"bob@example.com"}'
```

### 7. Create Duplicate User (Bob) - Failure (409 Conflict)
```bash
curl -i -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Bob Duplicate","email":"bob@example.com"}'
```

> **Automated Verification Script**: You can run `.\test_telemetry.ps1` in PowerShell to automatically boot the service with the agent, fire all 7 test requests, and flush the telemetry.

---

## 6. Where to View Traces, Metrics, and Logs

When running with logging exporters (`-Dotel.traces.exporter=logging -Dotel.metrics.exporter=logging -Dotel.logs.exporter=logging`):

1. **Traces**:
   - Spans are written to the console/stderr by `io.opentelemetry.exporter.logging.LoggingSpanExporter`.
   - Each entry contains the span name, `trace_id`, `span_id`, span kind (`SERVER`, `INTERNAL`, `CLIENT`), and attributes (e.g., `user.id`, `operation.name`, HTTP status codes).
2. **Metrics**:
   - Periodic dumps are written every 2–5 seconds by `io.opentelemetry.exporter.logging.LoggingMetricExporter`.
   - Inspect custom metrics:
     - `users.operation.count`: LongSum counter showing operation totals broken down by `operation` (`create|get|delete`) and `status` (`success|error`).
     - `users.operation.duration`: Histogram showing latency distribution, min, max, sum, and count in milliseconds.
     - Note that exemplars link metric spikes directly to the offending `traceId`.
3. **Logs**:
   - Application logs appear in stdout/stderr formatted as:
     `INFO [trace_id=509d0baa3e7f1cd49a56c52b2df809f9 span_id=c01b4e25e7a668ae] ... : operation=delete status=started user.id=1`
   - You can cross-correlate any log line with its corresponding trace by matching the `trace_id`.

If configured with an OpenTelemetry Collector or backend:
- **Jaeger UI**: View trace graphs and child hierarchies at `http://localhost:16686`.
- **Prometheus**: Query `users_operation_count_total` at `http://localhost:9090`.
- **Grafana**: Build unified dashboards combining traces, metrics, and Loki logs.

---

## 7. Manual vs. Automatic Instrumentation

This application illustrates how automatic and manual instrumentation complement each other:

| Feature | Automatic Instrumentation | Manual Instrumentation |
|---|---|---|
| **Provided By** | OpenTelemetry Java Agent (`opentelemetry-javaagent.jar`) | OpenTelemetry Java API (`io.opentelemetry.api.*`) |
| **Code Changes** | **Zero code changes**. Attached via JVM parameter. | Explicit Java code in `UserService.java`. |
| **HTTP Spans** | Generates incoming root server spans: `POST /users`, `GET /users/{id}`, `DELETE /users/{id}`. | **None**. We deliberately avoid manual HTTP spans to prevent redundant trace duplication. |
| **Database Spans** | Automatically instruments JDBC queries (`select`, `insert`, `delete from users`) and Hibernate operations. | Not required for data layer. |
| **Business Logic** | Blind to domain operations and custom internal status. | Creates `create-user` and `delete-user` child spans, records business exceptions (`IllegalArgumentException`), and attaches attributes (`operation.name`, `user.id`). |
| **Metrics** | JVM metrics (CPU, memory, threads, class loaders). | Custom application metrics (`users.operation.count`, `users.operation.duration`) with controlled low cardinality. |

### Expected DELETE Trace Hierarchy:
```
HTTP DELETE /users/{id}        [automatic - Tomcat server span]
└── delete-user                 [manual - UserService child span]
    ├── UserRepository.findById [automatic - Spring Data span]
    ├── UserRepository.delete   [automatic - Spring Data span]
    └── DELETE ... FROM users   [automatic - JDBC client span]
```
Both the automatic HTTP span and the manual `delete-user` span share the same `trace_id`, providing complete visibility from HTTP entry to database execution.

---

## 8. Full Assignment Solutions & Assessment Answers

For complete answers to the:
1. Hands-on practical implementation code and screenshots
2. Telemetry evidence extracts
3. 5-question troubleshooting exercise
4. Best practices review of `updateProfile`
5. 10 MCQ knowledge check questions with detailed technical reasoning

Please refer to [OpenTelemetry_Assignment_Solutions.md](file:///e:/RSL%20Training%20Assignments/rsl-training-vedant-sahu/OpenTelemetry_Assignment_Solutions.md).
