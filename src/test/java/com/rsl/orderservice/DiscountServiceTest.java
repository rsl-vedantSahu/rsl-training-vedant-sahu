package com.rsl.orderservice;

import com.rsl.orderservice.model.Coupon;
import com.rsl.orderservice.model.CouponValidationResult;
import com.rsl.orderservice.model.Customer;
import com.rsl.orderservice.repository.CouponRepository;
import com.rsl.orderservice.service.DiscountService;
import com.rsl.orderservice.service.PricingService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscountServiceTest {

    private DiscountService newDiscountService() {
        CouponRepository coupons = new CouponRepository();
        coupons.save(new Coupon("SAVE10", 10));
        return new DiscountService(coupons, new PricingService());
    }

    @Test
    void noCouponAndNonMemberMeansNoDiscount() {
        DiscountService discounts = newDiscountService();
        Customer bob = new Customer("C-2", "Bob", false);

        assertEquals(0, discounts.discountCents(1000, bob, null));
    }

    @Test
    void unknownCouponCodeIsIgnoredNotFatal() {
        DiscountService discounts = newDiscountService();
        Customer bob = new Customer("C-2", "Bob", false);

        // A coupon code the customer mistyped must not bring the order down.
        assertDoesNotThrow(() -> discounts.discountCents(1000, bob, "BLACKFRIDAY"));
    }

    @Test
    void validateCouponReturnsSuccessForValidCode() {
        DiscountService discounts = newDiscountService();
        CouponValidationResult result = discounts.validateCoupon("SAVE10");

        assertTrue(result.isValid());
        assertEquals(10, result.getPercentOff());
        assertTrue(result.getMessage().contains("SAVE10"));
    }

    @Test
    void validateCouponReturnsFailureForInvalidCode() {
        DiscountService discounts = newDiscountService();
        CouponValidationResult result = discounts.validateCoupon("BLACKFRIDAY");

        assertFalse(result.isValid());
        assertEquals(0, result.getPercentOff());
        assertTrue(result.getMessage().contains("invalid or expired"));
    }
}
