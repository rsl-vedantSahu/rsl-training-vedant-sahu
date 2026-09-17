# rsl-training-vedant-sahu

# Task 1 — Write Failing Tests First (RED Phase)
## Promt1
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


# Task 2 — Critique & Audit AI-Generated Tests
## Promt2

    As a Senior QA & Test Automation Specialist  
    Audit and refine the JUnit 5 unit test suite in `SubscriptionPricingServiceTest.java` against AI test anti-patterns.  

    Identified Flaws to Fix:
    1. Weak Assertions / Value Scaling: Replaced `compareTo` in the assertion helper with strict `assertEquals` checks that verify both exact numeric equality and an explicit scale of 2 decimal places (`actual.scale() == 2`).
    2. Flawed Test Assertion: In `floorsSaveTwentyResultAtZero()`, $50 BASIC minus $20 ("SAVE20") was incorrectly expecting $0.00 instead of $30.00. Update this test case to `appliesSaveTwentyOnBasicTier()` expecting $17.50 ($50 base - 25% longevity discount = $37.50, minus $20 = $17.50).

    Output Constraints:
    Provide the refactored, production-ready `SubscriptionPricingServiceTest.java` file incorporating these audit fixes. Do NOT write implementation code.


## Audit table with at least 2 identified flaws + corrective fixes

| Identified Test Flaw / Anti-Pattern | Severity | Corrective Action / Fix Applied |
| -- | -- | -- |
| Weak Assertion Helper (compareTo vs assertEquals): The raw AI test suite used compareTo(actual) == 0. While numeric values match, compareTo ignores scale differences (e.g., 50 matches 50.00). This bypasses the requirement to verify exact currency scale formatting (2 decimal places) | Medium | Updated helper to assertEquals(new BigDecimal(expected), actual) and added assertEquals(2, actual.scale()) to strictly validate exact currency decimal precision. |
| Flawed Test Assertion Logic (floorsSaveTwentyResultAtZero): Copilot tested BASIC ($50.00) with 0 months active and "SAVE20". Mathematically, $50 - $20 = $30, but Copilot incorrectly asserted "0.00", creating a broken specification. | High | Renamed and corrected test case to appliesSaveTwentyOnBasicTier(), testing 37 active months ($50 - 25% discount = $37.50, minus $20 = $17.50) to accurately validate voucher behavior. |


# Task 3 — Implement Against Tests (GREEN Phase)
## Promt3
Act as a Senior Software Engineer and write the minimun implemnetation required to pass the tests of SubscriptionPricingServiceTest. i.e Production grade code for SubscriptionPricingService, SubscriptionTier, InvalidVoucherException and other necessary code to full fill the business requirements as mentioned earlier in detail which are
1. Tier Base Rates
2. Longevity Discounts
3. Promotional Voucher Codes
4. Rounding & Floor Rule

Please look again for Core Requirements provided in my initial and stickly align our code to the mentioned details in it. Also create a sub directories if required for code seperation.

## Bug Identified

	if (activeMonths > THIRTY_SIX_MONTHS) {
			price = price.multiply(TWENTY_FIVE_PERCENT_DISCOUNT);
		} else if (activeMonths > TWELVE_MONTHS && activeMonths < THIRTY_SIX_MONTHS) {
			price = price.multiply(TEN_PERCENT_DISCOUNT);
		}
In above AI provided implemnetation, if active months are exactly 36 then it will not get any discount, but it should be eligible to get a discount as per more than 12 month.

# Task 4 — Refactor Safely Under Test Shield (REFACTOR Phase)
## Promt4
Role: Senior Java Developer

Refactor SubscriptionPricingService.java to improve code structure, readability, and encapsulation without altering any business logic or behavioral contracts.

Refactoring Requirements:

1. Extract input validation into a private validateInputs helper method.
2. Extract discount logic into single-responsibility private helper methods (applyLongevityDiscount, applyVoucher).
3. Preserve all current boundary conditions, constants, switch expressions, and BigDecimal rounding behavior exactly as they are.

Constraint: Do NOT alter SubscriptionPricingServiceTest.java. All existing unit tests must remain 100% green.
Also create a seperate directory for keeping exception classes and enum classes. They should not be under the service directory.