package com.example.observability.service;

import com.example.observability.dto.UserRequest;
import com.example.observability.dto.UserResponse;
import com.example.observability.entity.User;
import com.example.observability.exception.DuplicateEmailException;
import com.example.observability.exception.UserNotFoundException;
import com.example.observability.repository.UserRepository;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer containing business logic for User operations,
 * instrumented with OpenTelemetry Tracing, Metrics, and correlated Logging.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final Tracer tracer;
    private final LongCounter operationCounter;
    private final DoubleHistogram operationDuration;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;

        // Initialize OpenTelemetry Tracer
        this.tracer = GlobalOpenTelemetry.getTracer("crud-otel-application", "1.0.0");

        // Initialize OpenTelemetry Meter and Custom Metrics
        Meter meter = GlobalOpenTelemetry.getMeter("crud-otel-application");
        this.operationCounter = meter.counterBuilder("users.operation.count")
                .setDescription("Counts user operations by type and status")
                .setUnit("1")
                .build();
        this.operationDuration = meter.histogramBuilder("users.operation.duration")
                .setDescription("Measures duration of user operations")
                .setUnit("ms")
                .build();
    }

    /**
     * Create a new user.
     * Wrapped with manual span 'create-user' and records metrics and logs.
     *
     * @param request request containing name and email
     * @return created user response
     */
    @Transactional
    public UserResponse createUser(UserRequest request) {
        long startTime = System.nanoTime();
        log.info("operation=create status=started name={} email={}", request.getName(), request.getEmail());

        Span span = tracer.spanBuilder("create-user")
                .setAttribute("operation.name", "create")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateEmailException("Email already exists: " + request.getEmail());
            }

            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());

            User saved = userRepository.save(user);

            // Record user.id attribute on the span as required
            span.setAttribute("user.id", String.valueOf(saved.getId()));

            recordMetrics("create", "success", startTime);
            log.info("operation=create status=succeeded user.id={}", saved.getId());
            return toResponse(saved);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            recordMetrics("create", "error", startTime);
            log.error("operation=create status=failed error={}", e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Retrieve a user by id.
     * NOTE: As required, this method has NO manual span so that only the automatic
     * HTTP server span is captured. It records metrics and logs.
     *
     * @param id user id
     * @return user response
     */
    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        long startTime = System.nanoTime();
        log.info("operation=get status=started user.id={}", id);

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

            recordMetrics("get", "success", startTime);
            log.info("operation=get status=succeeded user.id={}", id);
            return toResponse(user);
        } catch (Exception e) {
            recordMetrics("get", "error", startTime);
            log.error("operation=get status=failed user.id={} error={}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Delete a user by id.
     * Wrapped with manual span 'delete-user' which co-exists as a child span
     * under the automatic HTTP DELETE server span.
     *
     * @param id user id
     */
    @Transactional
    public void deleteUser(Long id) {
        long startTime = System.nanoTime();
        log.info("operation=delete status=started user.id={}", id);

        Span span = tracer.spanBuilder("delete-user")
                .setAttribute("operation.name", "delete")
                .setAttribute("user.id", String.valueOf(id))
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
            userRepository.delete(user);

            recordMetrics("delete", "success", startTime);
            log.info("operation=delete status=succeeded user.id={}", id);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            recordMetrics("delete", "error", startTime);
            log.error("operation=delete status=failed user.id={} error={}", id, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Helper method to record operation metrics.
     * Note: user.id is intentionally omitted to avoid high-cardinality metric issues.
     */
    private void recordMetrics(String operation, String status, long startTimeNanos) {
        long durationMs = (System.nanoTime() - startTimeNanos) / 1_000_000;
        Attributes attributes = Attributes.of(
                AttributeKey.stringKey("operation"), operation,
                AttributeKey.stringKey("status"), status
        );
        operationCounter.add(1, attributes);
        operationDuration.record(durationMs, attributes);
    }

    /**
     * Map entity to response DTO.
     */
    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
