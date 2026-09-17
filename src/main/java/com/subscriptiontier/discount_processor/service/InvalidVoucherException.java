package com.subscriptiontier.discount_processor.service;

public class InvalidVoucherException extends RuntimeException {

	public InvalidVoucherException(String voucherCode) {
		super("Invalid or expired voucher: " + voucherCode);
	}
}
