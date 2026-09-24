package com.rsl.orderservice.model;

/**
 * Result of validating a coupon code before or during order processing.
 */
public class CouponValidationResult {

    private final boolean valid;
    private final String message;
    private final int percentOff;

    public CouponValidationResult(boolean valid, String message, int percentOff) {
        this.valid = valid;
        this.message = message;
        this.percentOff = percentOff;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    public int getPercentOff() {
        return percentOff;
    }
}
