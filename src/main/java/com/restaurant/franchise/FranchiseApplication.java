package com.restaurant.franchise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FranchiseApplication {

    public static void main(String[] args) {
        boolean hasServiceType = false;
        for (String arg : args) {
            if (arg.contains("service.type")) {
                hasServiceType = true;
                break;
            }
        }

        if (!hasServiceType) {
            System.out.println("\n======================================================================");
            System.out.println("⚠️  PLEASE SPECIFY A SERVICE TO RUN USING: --service.type=<service>");
            System.out.println("  Examples:");
            System.out.println("    --service.type=kitchen");
            System.out.println("    --service.type=inventory");
            System.out.println("    --service.type=notification");
            System.out.println("    --service.type=producer --restaurant=RESTAURANT-101 --customer=Vedant");
            System.out.println("======================================================================\n");
        }

        SpringApplication.run(FranchiseApplication.class, args);
    }

}
