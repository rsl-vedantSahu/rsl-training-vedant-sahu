# rsl-training-vedant-sahu
I am a Senior QA Engineer.
I want to build a  Subscription Tier & Discount Processor backend application using java.
Current I am using Springboot for creating this project and I have completed my initial setup.

Core Requirements:

1. Tier Base Rates:

○ BASIC: $50.00 / month

○ PRO: $150.00 / month

○ ENTERPRISE: $500.00 / month

2. Longevity Discounts:

○ Accounts active for more than 12 months receive a 10% discount on their monthly rate.

○ Accounts active for more than 36 months receive a 25% discount on their monthly rate.

3. Promotional Voucher Codes:

○ Voucher "SAVE20" deducts an additional $20.00 flat fee after percentage discounts.

○ Voucher "HALFPRICE" reduces the calculated rate by 50% (applied after longevity

discounts).

○ Expired or invalid vouchers must throw a custom InvalidVoucherException (Java) /

InvalidVoucherError (Node.js).

4. Rounding & Floor Rule:

○ The final monthly total cannot drop below $0.00.

○ Currency math must be rounded accurately to two decimal places (half-up rounding).

I am following Test Driven Development methodology for creating this application.

Output Contraints:

Write JUnit 5 unit tests for `SubscriptionPricingServiceTest.java`. Do not write production code for SubscriptionPricingService or any other related classes. Also write these service tests under new directory in test.

