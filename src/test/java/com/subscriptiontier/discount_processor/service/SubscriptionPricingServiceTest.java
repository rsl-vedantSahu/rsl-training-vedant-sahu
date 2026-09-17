package com.subscriptiontier.discount_processor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
	void appliesTwentyFivePercentDiscountAfterMoreThanThirtySixMonths() {
		assertPrice("375.00", pricingService.calculateMonthlyPrice(SubscriptionTier.ENTERPRISE, 37, null));
	}

	@ParameterizedTest(name = "{0} active months costs {1} on BASIC")
	@CsvSource({
		"12, 50.00",
		"13, 45.00",
		"36, 45.00",
		"37, 37.50"
	})
	void appliesExpectedDiscountAtLongevityBoundaries(int activeMonths, String expectedPrice) {
		assertPrice(expectedPrice, pricingService.calculateMonthlyPrice(SubscriptionTier.BASIC, activeMonths, null));
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

	@ParameterizedTest
	@CsvSource({
		"BASIC, 0, 30.00",
		"PRO, 0, 130.00",
		"ENTERPRISE, 0, 480.00"
	})
	void clampsSaveTwentyResultsToNonNegativeValues(SubscriptionTier tier, int activeMonths,
			String expectedPrice) {
		assertPrice(expectedPrice, pricingService.calculateMonthlyPrice(tier, activeMonths, "SAVE20"));
	}

	@Test
	void rejectsNegativeActiveMonths() {
		assertThrows(IllegalArgumentException.class,
				() -> pricingService.calculateMonthlyPrice(SubscriptionTier.BASIC, -1, null));
	}

	@Test
	void rejectsNullSubscriptionTier() {
		assertThrows(IllegalArgumentException.class,
				() -> pricingService.calculateMonthlyPrice(null, 0, null));
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