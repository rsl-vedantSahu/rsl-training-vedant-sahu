package com.subscriptiontier.discount_processor.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.subscriptiontier.discount_processor.enums.SubscriptionTier;
import com.subscriptiontier.discount_processor.exception.InvalidVoucherException;

public class SubscriptionPricingService {

	private static final int CURRENCY_SCALE = 2;
	private static final int TWELVE_MONTHS = 12;
	private static final int THIRTY_SIX_MONTHS = 36;
	private static final BigDecimal TEN_PERCENT_DISCOUNT = new BigDecimal("0.90");
	private static final BigDecimal TWENTY_FIVE_PERCENT_DISCOUNT = new BigDecimal("0.75");
	private static final BigDecimal HALF_PRICE = new BigDecimal("0.50");
	private static final BigDecimal SAVE_TWENTY_AMOUNT = new BigDecimal("20.00");
	private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(CURRENCY_SCALE);

	public BigDecimal calculateMonthlyPrice(SubscriptionTier tier, int activeMonths, String voucherCode) {
		validateInputs(tier, activeMonths);

		BigDecimal price = applyLongevityDiscount(tier.monthlyRate(), activeMonths);
		if (voucherCode != null) {
			price = applyVoucher(price, voucherCode);
		}

		return price.max(ZERO).setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
	}

	private void validateInputs(SubscriptionTier tier, int activeMonths) {
		if (tier == null) {
			throw new IllegalArgumentException("Subscription tier is required");
		}
		if (activeMonths < 0) {
			throw new IllegalArgumentException("Active months cannot be negative");
		}
	}

	private BigDecimal applyLongevityDiscount(BigDecimal price, int activeMonths) {
		if (activeMonths > THIRTY_SIX_MONTHS) {
			price = price.multiply(TWENTY_FIVE_PERCENT_DISCOUNT);
		} else if (activeMonths > TWELVE_MONTHS && activeMonths < THIRTY_SIX_MONTHS) {
			price = price.multiply(TEN_PERCENT_DISCOUNT);
		}
		return price;
	}

	private BigDecimal applyVoucher(BigDecimal price, String voucherCode) {
		return switch (voucherCode) {
			case "SAVE20" -> price.subtract(SAVE_TWENTY_AMOUNT);
			case "HALFPRICE" -> price.multiply(HALF_PRICE);
			default -> throw new InvalidVoucherException(voucherCode);
		};
	}
}
