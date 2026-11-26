# Telemetry Evidence Screenshots

This directory contains the captured terminal evidence demonstrating the OpenTelemetry instrumentation for the assignment deliverables.

| File | Description | Assignment Requirement |
|---|---|---|
| **`image1.png`** | **Trace Evidence**: Shows the automatic HTTP Tomcat server span (`DELETE /users/{id}`) and the manual service child span (`delete-user`) sharing the same `trace_id` (`509d0baa3e7f1cd49a56c52b2df809f9`). Also shows the failed `POST /users` error span (409 Conflict). | Section 3 (Trace Evidence) |
| **`image2.png`** | **Metrics Evidence**: Shows the custom counter `users.operation.count` (types: `create`, `get`, `delete`; status: `success`, `error`) and the histogram `users.operation.duration`. Demonstrates strict low cardinality (no `user.id`). | Section 3 (Metrics Evidence) |
| **`image3.png`** | **Log Evidence**: Shows application SLF4J logs with MDC trace correlation (`[trace_id=... span_id=...]`) for operations that started, succeeded, and failed. | Section 3 (Log Evidence) |
