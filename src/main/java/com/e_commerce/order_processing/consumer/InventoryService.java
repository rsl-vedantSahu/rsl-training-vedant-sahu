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
public class InventoryService {

    private final Set<String> processedOrderIds = ConcurrentHashMap.newKeySet();

    @RabbitListener(queues = RabbitMQConfig.INVENTORY_QUEUE)
    public void handleOrder(
            OrderPlacedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            @Header(value = AmqpHeaders.MESSAGE_ID, required = false) String messageId,
            @Header(value = "order_id", required = false) String headerOrderId,
            @Header(value = AmqpHeaders.RECEIVED_ROUTING_KEY, required = false) String routingKey) {

        String orderId = ConsumerSupport.resolveOrderId(messageId, headerOrderId, event);

        try {
            if (event.quantity() <= 0) {
                System.out.printf(
                        "[INVENTORY SERVICE] Rejecting invalid order (quantity <= 0) without requeuing | Order ID: %s | Quantity: %d%n",
                        orderId, event.quantity()
                );
                ConsumerSupport.rejectWithoutRequeue(channel, deliveryTag);
                return;
            }

            if (!processedOrderIds.add(orderId)) {
                System.out.printf(
                        "[INVENTORY SERVICE] Duplicate order detected (Idempotency check) - skipping | Order ID: %s%n",
                        orderId
                );
                ConsumerSupport.ack(channel, deliveryTag);
                return;
            }

            System.out.printf(
                    "[INVENTORY SERVICE] Reserved inventory | Order ID: %s | Category: %s | Quantity: %d | Routing Key: %s%n",
                    orderId, event.category(), event.quantity(), routingKey
            );
            ConsumerSupport.ack(channel, deliveryTag);
        } catch (Exception ex) {
            System.err.printf("[INVENTORY SERVICE] Error processing Order ID %s: %s%n", orderId, ex.getMessage());
            ConsumerSupport.rejectWithoutRequeue(channel, deliveryTag);
        }
    }
}

