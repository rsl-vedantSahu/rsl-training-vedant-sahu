package com.e_commerce.order_processing.producer;

import java.time.Instant;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.e_commerce.order_processing.config.RabbitMQConfig;
import com.e_commerce.order_processing.model.OrderPlacedEvent;
import com.e_commerce.order_processing.model.OrderRequest;

@Service
public class OrderProducerService {

    private final RabbitTemplate rabbitTemplate;

    public OrderProducerService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes an order_placed event to orders_exchange with routing key order.created.<category>
     * and attaches the unique order_id in AMQP message properties.
     */
    public OrderPlacedEvent publishOrder(OrderRequest request) {
        String normalizedCategory = request.category().trim().toLowerCase();
        String orderId = (request.orderId() != null && !request.orderId().isBlank())
                ? request.orderId().trim()
                : "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String routingKey = "order.created." + normalizedCategory;

        OrderPlacedEvent event = new OrderPlacedEvent(
                orderId,
                "order_placed",
                normalizedCategory,
                request.quantity(),
                Instant.now().toString()
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDERS_EXCHANGE, routingKey, event, message -> {
            message.getMessageProperties().setMessageId(orderId);
            message.getMessageProperties().setCorrelationId(orderId);
            message.getMessageProperties().setHeader("order_id", orderId);
            return message;
        });

        System.out.printf(
                "[ORDER PRODUCER] Published 'order_placed' | Order ID: %s | Routing Key: %s | Quantity: %d%n",
                orderId, routingKey, request.quantity()
        );

        return event;
    }
}

