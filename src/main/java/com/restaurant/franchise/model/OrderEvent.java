package com.restaurant.franchise.model;

import java.time.Instant;
import java.util.List;

public record OrderEvent(
                int order_id,
                String restaurant_id,
                String customer_name,
                List<String> items,
                Instant order_time,
                String order_status) {
}
