package com.e_commerce.order_processing.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public record OrderRequest(
        String category,
        int quantity,
        @JsonAlias({"orderId", "order_id"})
        String orderId
) {
}

