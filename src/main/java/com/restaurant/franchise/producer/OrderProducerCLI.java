package com.restaurant.franchise.producer;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.restaurant.franchise.model.OrderEvent;

@Component
@ConditionalOnProperty(name = "service.type", havingValue = "producer")
public class OrderProducerCLI implements ApplicationRunner {

    private final OrderProducerService orderProducerService;

    public OrderProducerCLI(OrderProducerService orderProducerService) {
        this.orderProducerService = orderProducerService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("\n========================================================");
        System.out.println(">>> STARTING RESTAURANT ORDER CLI PRODUCER <<<");
        System.out.println("========================================================");

        // 1. Read restaurant_id (--restaurant=RESTAURANT-101 or RESTAURANT-102)
        String restaurantId = args.containsOption("restaurant")
                ? args.getOptionValues("restaurant").get(0)
                : "RESTAURANT-101";

        // 2. Read customer_name (--customer=Vedant)
        String customerName = args.containsOption("customer")
                ? args.getOptionValues("customer").get(0)
                : "Customer-" + (new Random().nextInt(900) + 100);

        // 3. Read order_id (--orderId=101)
        int orderId = args.containsOption("orderId")
                ? Integer.parseInt(args.getOptionValues("orderId").get(0))
                : new Random().nextInt(9000) + 1000;

        // 4. Read items (--items=Burger,Fries,Coke)
        List<String> items = args.containsOption("items")
                ? Arrays.asList(args.getOptionValues("items").get(0).split(","))
                : List.of("Burger", "Fries", "Soft Drink");

        // 5. Read order_status (--status=CREATED)
        String status = args.containsOption("status")
                ? args.getOptionValues("status").get(0)
                : "CREATED";

        // 6. Build the OrderEvent
        OrderEvent order = new OrderEvent(
                orderId,
                restaurantId,
                customerName,
                items,
                Instant.now(),
                status
        );

        System.out.printf("Publishing Order: ID=%d | Outlet=%s | Customer=%s | Items=%s%n",
                order.order_id(), order.restaurant_id(), order.customer_name(), order.items());

        // 7. Send the order via Kafka
        orderProducerService.sendOrder(order);

        // 8. Sleep briefly to ensure async callback prints partition & offset before exit
        Thread.sleep(1500);

        System.out.println("========================================================\n");
    }
}

