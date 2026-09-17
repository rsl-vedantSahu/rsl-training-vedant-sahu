package com.subscriptiontier.discount_processor.service;

import java.math.BigDecimal;

public enum SubscriptionTier {
	BASIC(new BigDecimal("50.00")),
	PRO(new BigDecimal("150.00")),
	ENTERPRISE(new BigDecimal("500.00"));

	private final BigDecimal monthlyRate;

	SubscriptionTier(BigDecimal monthlyRate) {
		this.monthlyRate = monthlyRate;
	}

	BigDecimal monthlyRate() {
		return monthlyRate;
	}
}
