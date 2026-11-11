package com.e_commerce.order_processing.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderPlacedEvent(
        @JsonProperty("order_id") String orderId,
        @JsonProperty("event_type") String eventType,
        String category,
        int quantity,
        @JsonProperty("created_at") String createdAt
) {
}

