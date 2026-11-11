package com.e_commerce.order_processing.consumer;

import java.io.IOException;

import com.e_commerce.order_processing.model.OrderPlacedEvent;
import com.rabbitmq.client.Channel;

final class ConsumerSupport {

    private ConsumerSupport() {
    }

    static String resolveOrderId(String messageId, String headerOrderId, OrderPlacedEvent event) {
        if (messageId != null && !messageId.isBlank()) {
            return messageId;
        }
        if (headerOrderId != null && !headerOrderId.isBlank()) {
            return headerOrderId;
        }
        return (event != null && event.orderId() != null) ? event.orderId() : "UNKNOWN";
    }

    static void ack(Channel channel, long deliveryTag) throws IOException {
        channel.basicAck(deliveryTag, false);
    }

    static void rejectWithoutRequeue(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, false);
        } catch (IOException ioEx) {
            System.err.println("Failed to nack message: " + ioEx.getMessage());
        }
    }
}

