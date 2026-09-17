package com.subscriptiontier.discount_processor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.subscriptiontier.discount_processor.enums.SubscriptionTier;
import com.subscriptiontier.discount_processor.exception.InvalidVoucherException;

class SubscriptionPricingServiceTest {

	private SubscriptionPricingService pricingService;

	@BeforeEach
	void setUp() {
		pricingService = new SubscriptionPricingService();
	}

	@Test
	void chargesBasicTierBaseRate() {
		assertPrice("50.00", pricingService.calculateMonthlyPrice(SubscriptionTier.BASIC, 0, null));
	}

	@Test
	void chargesProTierBaseRate() {
		assertPrice("150.00", pricingService.calculateMonthlyPrice(SubscriptionTier.PRO, 0, null));
	}

	@Test
	void chargesEnterpriseTierBaseRate() {
		assertPrice("500.00", pricingService.calculateMonthlyPrice(SubscriptionTier.ENTERPRISE, 0, null));
	}

	@Test
	void doesNotApplyLongevityDiscountAtExactlyTwelveMonths() {
		assertPrice("50.00", pricingService.calculateMonthlyPrice(SubscriptionTier.BASIC, 12, null));
	}

	@Test
	void appliesTenPercentDiscountAfterMoreThanTwelveMonths() {
		assertPrice("135.00", pricingService.calculateMonthlyPrice(SubscriptionTier.PRO, 13, null));
	}

	@Test
	void doesNotApplyTwentyFivePercentDiscountAtExactlyThirtySixMonths() {
		assertPrice("150.00", pricingService.calculateMonthlyPrice(SubscriptionTier.PRO, 36, null));
	}

	@Test
	void appliesTwentyFivePercentDiscountAfterMoreThanThirtySixMonths() {
		assertPrice("375.00", pricingService.calculateMonthlyPrice(SubscriptionTier.ENTERPRISE, 37, null));
	}

	@Test
	void appliesSaveTwentyAfterLongevityDiscount() {
		assertPrice("115.00", pricingService.calculateMonthlyPrice(SubscriptionTier.PRO, 13, "SAVE20"));
	}

	@Test
	void appliesHalfPriceAfterLongevityDiscount() {
		assertPrice("67.50", pricingService.calculateMonthlyPrice(SubscriptionTier.PRO, 13, "HALFPRICE"));
	}

	@Test
	void roundsHalfUpToTwoDecimalPlacesAfterPercentageDiscounts() {
		assertPrice("22.50", pricingService.calculateMonthlyPrice(SubscriptionTier.BASIC, 13, "HALFPRICE"));
	}

	@Test
	void appliesSaveTwentyOnBasicTier() {
		assertPrice("17.50", pricingService.calculateMonthlyPrice(SubscriptionTier.BASIC, 37, "SAVE20"));
	}

	@Test
	void rejectsAnInvalidVoucher() {
		assertThrows(InvalidVoucherException.class,
				() -> pricingService.calculateMonthlyPrice(SubscriptionTier.BASIC, 0, "NOT-A-VOUCHER"));
	}

	@Test
	void rejectsAnExpiredVoucher() {
		assertThrows(InvalidVoucherException.class,
				() -> pricingService.calculateMonthlyPrice(SubscriptionTier.BASIC, 0, "EXPIRED"));
	}

	private void assertPrice(String expected, BigDecimal actual) {
		assertEquals(new BigDecimal(expected), actual);
		assertEquals(2, actual.scale());
	}

}