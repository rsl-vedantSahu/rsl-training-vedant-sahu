package com.subscriptiontier.discount_processor.exception;

public class InvalidVoucherException extends RuntimeException {

	public InvalidVoucherException(String voucherCode) {
		super("Invalid or expired voucher: " + voucherCode);
	}
}
