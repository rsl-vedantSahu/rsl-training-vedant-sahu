package com.e_commerce.order_processing.consumer;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.e_commerce.order_processing.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;

@Service
public class DeadLetterConsumer {

    private final Set<String> loggedDeadOrders = ConcurrentHashMap.newKeySet();

    @RabbitListener(queues = RabbitMQConfig.DLQ_QUEUE)
    public void handleDeadLetter(
            Message message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        String messageId = message.getMessageProperties().getMessageId();
        Object headerOrderIdObj = message.getMessageProperties().getHeader("order_id");
        String headerOrderId = headerOrderIdObj != null ? headerOrderIdObj.toString() : null;
        String orderId = ConsumerSupport.resolveOrderId(messageId, headerOrderId, null);

        try {
            if (!loggedDeadOrders.add(orderId)) {
                System.out.printf(
                        "[ERROR LOGGER] Duplicate DLX order detected (Idempotency check) - skipping | Order ID: %s%n",
                        orderId
                );
                ConsumerSupport.ack(channel, deliveryTag);
                return;
            }

            System.out.printf("Error Logger: Invalid order %s routed to DLX%n", orderId);
            ConsumerSupport.ack(channel, deliveryTag);
        } catch (Exception ex) {
            System.err.printf("[ERROR LOGGER] Failed to acknowledge DLX order %s: %s%n", orderId, ex.getMessage());
            ConsumerSupport.rejectWithoutRequeue(channel, deliveryTag);
        }
    }
}

