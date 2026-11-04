package com.restaurant.franchise.producer;

import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.franchise.model.OrderEvent;

@Service
public class OrderProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${restaurant.topic.order-created:restaurant.orders.created}")
    private String defaultTopicName;

    public OrderProducerService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendOrder(OrderEvent order) {
        sendOrder(defaultTopicName, order);
    }

    public void sendOrder(String topicName, OrderEvent order) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(order);

            CompletableFuture<SendResult<String, String>> future = 
                    kafkaTemplate.send(topicName, order.restaurant_id(), jsonPayload);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    RecordMetadata metadata = result.getRecordMetadata();
                    System.out.printf(
                        ">>> [PRODUCER SUCCESS] Order ID: %d | Key: %s -> Partition: %d | Offset: %d%n",
                        order.order_id(),
                        order.restaurant_id(),
                        metadata.partition(),
                        metadata.offset()
                    );
                } else {
                    System.err.println("Failed to send order: " + ex.getMessage());
                }
            });
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing OrderEvent to JSON: " + e.getMessage());
        }
    }
}
