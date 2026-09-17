# Task 6: TDD & AI Collaboration Reflection

## Executive Summary
This reflection evaluates the role of Generative AI (GitHub Copilot) throughout a Test-Driven Development (TDD) workflow for building a Spring Boot Subscription Tier & Discount Processor. By enforcing strict TDD red-green-refactor cycles, we balanced rapid code generation with essential human engineering oversight.

---

## 1. Where AI Accelerated Development
GitHub Copilot dramatically reduced initial setup friction and repetitive boilerplate construction:
* **Rapid Test Skeleton Generation:** In Task 1, Copilot synthesized an initial JUnit 5 test suite in seconds, covering fundamental business paths like base rates (`BASIC`, `PRO`, `ENTERPRISE`) and basic voucher applications (`SAVE20`, `HALFPRICE`).
* **Boilerplate Reduction:** Copilot instantly generated repetitive domain structures, such as Java `enum` values, custom exceptions (`InvalidVoucherException`), and Java 17 `switch` expressions for pattern matching.
* **Parameterized Test Scaffolding:** In Task 5, Copilot accelerated the creation of JUnit 5 `@ParameterizedTest` and `@CsvSource` annotations, generating tabular boundary checks across active month thresholds (12, 13, 36, 37 months) without manual syntax typing.

---

## 2. Where AI Introduced Flawed or Weak Logic
Despite efficiency gains, Copilot introduced subtle bugs and anti-patterns that required human audit and intervention:
* **Weak Assertions (Scale Oversights):** Raw AI tests originally used numeric `compareTo` checks or standard equality without scale checks. This bypassed `BigDecimal` precision rules, allowing values like `50` to pass when financial formatting explicitly required `50.00` (scale of 2).
* **Flawed Mathematical Logic:** In early test generation, Copilot produced a broken test assertion (`floorsSaveTwentyResultAtZero`) expecting `$0.00` on a `$50.00` base price minus a `$20.00` voucher, creating an invalid test specification.
* **Boundary Condition Blind Spots:** During initial implementation generation, Copilot wrote `activeMonths > 12 && activeMonths < 36`, causing users at exactly 36 months to receive a `0%` discount instead of the required `10%` longevity discount.

---

## 3. How TDD Prevented Technical Debt
The disciplined TDD workflow served as an indispensable guardrail against AI-generated technical debt:
* **Audit Before Implementation:** Reviewing raw AI tests (Task 2) ensured that bad assertions and broken business math were eliminated before writing production code.
* **The "Test Shield" for Safe Refactoring:** In Task 4, having a passing test suite provided complete confidence to clean up conditional logic, enforce single-responsibility helper methods, and apply Java 17 features without risking behavioral regressions.
* **Verification of Complex Boundaries:** Adversarial edge-case testing in Task 5 forced explicit verification of boundary conditions (`activeMonths < 0`, `tier == null`, exact month transitions), ensuring production code handles real-world edge cases robustly.

---

## Conclusion
AI tools like GitHub Copilot act as powerful execution amplifiers, but they lack true domain context and edge-case intuition. Combining AI speed with strict TDD practices ensures high productivity while keeping software quality, precision, and security under human control.