package com.restaurant.franchise.consumer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.franchise.model.OrderEvent;

@Service
@ConditionalOnProperty(name = "service.type", havingValue = "kitchen")
public class KitchenService {

    private final ObjectMapper objectMapper;

    public KitchenService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics = "${restaurant.topic.order-created:restaurant.orders.created}", 
        groupId = "kitchen-service-group"
    )
    public void processOrderForKitchen(
            @Payload String payload,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            OrderEvent order = objectMapper.readValue(payload, OrderEvent.class);
            System.out.println("------------------------------------------------------------------");
            System.out.printf("[KITCHEN SERVICE] (Group: kitchen-service-group)%n");
            System.out.printf("  -> Received Order ID : %d%n", order.order_id());
            System.out.printf("  -> Restaurant Outlet : %s (Key: %s)%n", order.restaurant_id(), key);
            System.out.printf("  -> Partition / Offset: Partition %d / Offset %d%n", partition, offset);
            System.out.printf("  -> Action            : Cooking items: %s for %s%n", order.items(), order.customer_name());
            System.out.println("------------------------------------------------------------------");
        } catch (Exception e) {
            System.err.printf("[KITCHEN SERVICE] Error processing message at partition %d, offset %d: %s%n",
                    partition, offset, e.getMessage());
        }
    }
}

