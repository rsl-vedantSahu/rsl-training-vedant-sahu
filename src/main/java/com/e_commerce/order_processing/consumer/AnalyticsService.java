package com.e_commerce.order_processing.consumer;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.e_commerce.order_processing.config.RabbitMQConfig;
import com.e_commerce.order_processing.model.OrderPlacedEvent;
import com.rabbitmq.client.Channel;

@Service
public class AnalyticsService {

    private final Set<String> processedOrderIds = ConcurrentHashMap.newKeySet();

    @RabbitListener(queues = RabbitMQConfig.ANALYTICS_QUEUE)
    public void handleElectronicsOrder(
            OrderPlacedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            @Header(value = AmqpHeaders.MESSAGE_ID, required = false) String messageId,
            @Header(value = "order_id", required = false) String headerOrderId) {

        String orderId = ConsumerSupport.resolveOrderId(messageId, headerOrderId, event);

        try {
            if (event.quantity() <= 0) {
                System.out.printf(
                        "[ANALYTICS SERVICE] Skipping invalid electronics order (quantity <= 0) | Order ID: %s%n",
                        orderId
                );
                ConsumerSupport.ack(channel, deliveryTag);
                return;
            }

            if (!processedOrderIds.add(orderId)) {
                System.out.printf(
                        "[ANALYTICS SERVICE] Duplicate order detected (Idempotency check) - skipping | Order ID: %s%n",
                        orderId
                );
                ConsumerSupport.ack(channel, deliveryTag);
                return;
            }

            System.out.printf("Analytics Service: Recording high-value electronics sale for order %s%n", orderId);
            ConsumerSupport.ack(channel, deliveryTag);
        } catch (Exception ex) {
            System.err.printf("[ANALYTICS SERVICE] Error processing Order ID %s: %s%n", orderId, ex.getMessage());
            ConsumerSupport.rejectWithoutRequeue(channel, deliveryTag);
        }
    }
}

