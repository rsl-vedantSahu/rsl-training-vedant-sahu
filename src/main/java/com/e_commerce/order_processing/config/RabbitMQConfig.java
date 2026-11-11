package com.e_commerce.order_processing.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDERS_EXCHANGE = "orders_exchange";

    public static final String INVENTORY_QUEUE = "inventory_queue";
    public static final String ANALYTICS_QUEUE = "analytics_queue";

    public static final String INVENTORY_BINDING_KEY = "order.created.*";
    public static final String ANALYTICS_BINDING_KEY = "order.created.electronics";

    public static final String DLX_EXCHANGE = "orders_dlx";
    public static final String DLQ_QUEUE = "orders_dlq";
    public static final String DLQ_ROUTING_KEY = "order.dead";

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(ORDERS_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue inventoryQueue() {
        return QueueBuilder.durable(INVENTORY_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue analyticsQueue() {
        return QueueBuilder.durable(ANALYTICS_QUEUE).build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    @Bean
    public Binding inventoryBinding(Queue inventoryQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(inventoryQueue)
                .to(ordersExchange)
                .with(INVENTORY_BINDING_KEY);
    }

    @Bean
    public Binding analyticsBinding(Queue analyticsQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(analyticsQueue)
                .to(ordersExchange)
                .with(ANALYTICS_BINDING_KEY);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}

