
import org.example.PaymentProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for PaymentProcessor class
 * Uses Black-box, White-box, and Integration testing approaches
 */
public class PaymentProcessorTest {

    private PaymentProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new PaymentProcessor();
    }

    @Nested
    @DisplayName("Black-box Tests - Boundary Value Analysis")
    class BoundaryValueTests {

        @Test
        @DisplayName("Test zero amount (boundary)")
        void testZeroAmount() {
            assertThrows(IllegalArgumentException.class, () ->
                    processor.processPayment(0.0, false, PaymentProcessor.PaymentMethod.CASH));
        }

        @Test
        @DisplayName("Test negative amount (boundary)")
        void testNegativeAmount() {
            assertThrows(IllegalArgumentException.class, () ->
                    processor.processPayment(-1.0, false, PaymentProcessor.PaymentMethod.CASH));
        }

        @Test
        @DisplayName("Test minimum positive amount (boundary)")
        void testMinimumPositiveAmount() {
            double result = processor.processPayment(0.01, false, PaymentProcessor.PaymentMethod.CASH);
            assertEquals(0.01, result, 0.001);
        }

        @Test
        @DisplayName("Test delivery fee boundary - exactly $50")
        void testDeliveryFeeBoundaryExact50() {
            double fee = processor.calculateDeliveryFee(50.0);
            assertEquals(0.0, fee);
        }

        @Test
        @DisplayName("Test delivery fee boundary - just under $50")
        void testDeliveryFeeBoundaryUnder50() {
            double fee = processor.calculateDeliveryFee(49.99);
            assertEquals(5.0, fee);
        }

        @Test
        @DisplayName("Test delivery fee boundary - just over $50")
        void testDeliveryFeeBoundaryOver50() {
            double fee = processor.calculateDeliveryFee(50.01);
            assertEquals(0.0, fee);
        }
    }

    @Nested
    @DisplayName("Black-box Tests - Equivalence Partitioning")
    class EquivalencePartitioningTests {

        @Test
        @DisplayName("Valid amount, first order, credit card - should include tax after discount")
        void testValidAmountFirstOrderCreditCard() {
            double result = processor.processPayment(100.0, true, PaymentProcessor.PaymentMethod.CREDIT_CARD);
            // Expected with tax after discount: (100 * 0.85) * 1.15 = 97.75
            // But current implementation gives: 85.0 (tax ignored)
            // This test will FAIL and reveal the tax calculation bug
            assertEquals(97.75, result, 0.01, "Tax should be applied after discount calculation");
        }

        @Test
        @DisplayName("Valid amount, not first order, PayPal - should include tax")
        void testValidAmountNotFirstOrderPayPal() {
            double result = processor.processPayment(100.0, false, PaymentProcessor.PaymentMethod.PAYPAL);
            // Expected with tax: (100 * 0.98) * 1.15 = 112.7
            // But current implementation gives: 98.0 (tax ignored)
            assertEquals(112.7, result, 0.01, "Tax should be applied to final amount");
        }

        @Test
        @DisplayName("Valid amount, first order, cash - should include tax after discount")
        void testValidAmountFirstOrderCash() {
            double result = processor.processPayment(100.0, true, PaymentProcessor.PaymentMethod.CASH);
            // Expected with tax after discount: (100 * 0.90) * 1.15 = 103.5
            // But current implementation gives: 90.0 (tax ignored)
            assertEquals(103.5, result, 0.01, "Tax should be applied after first order discount");
        }

        @Test
        @DisplayName("Valid amount, not first order, cash - should include tax")
        void testValidAmountNotFirstOrderCash() {
            double result = processor.processPayment(100.0, false, PaymentProcessor.PaymentMethod.CASH);
            // Expected with tax: 100 * 1.15 = 115.0
            // But current implementation gives: 100.0 (tax ignored)
            assertEquals(115.0, result, 0.01, "Tax should be applied even with no discounts");
        }
    }

    @Nested
    @DisplayName("Black-box Tests - Decision Table Testing")
    class DecisionTableTests {

        @Test
        @DisplayName("Decision Table: First Order=T, Credit Card - expect tax after discounts")
        void testDecisionTable_FirstOrder_CreditCard() {
            double result = processor.processPayment(200.0, true, PaymentProcessor.PaymentMethod.CREDIT_CARD);
            // Expected: (200 * 0.85) * 1.15 = 195.5
            // Current buggy: 170.0 (tax ignored)
            assertEquals(195.5, result, 0.01, "Should apply 15% tax after 15% total discount");
        }

        @Test
        @DisplayName("Decision Table: First Order=T, PayPal - expect tax after discounts")
        void testDecisionTable_FirstOrder_PayPal() {
            double result = processor.processPayment(200.0, true, PaymentProcessor.PaymentMethod.PAYPAL);
            // Expected: (200 * 0.88) * 1.15 = 202.4
            // Current buggy: 176.0 (tax ignored)
            assertEquals(202.4, result, 0.01, "Should apply 15% tax after 12% total discount");
        }

        @Test
        @DisplayName("Decision Table: First Order=T, Cash - expect tax after discount")
        void testDecisionTable_FirstOrder_Cash() {
            double result = processor.processPayment(200.0, true, PaymentProcessor.PaymentMethod.CASH);
            // Expected: (200 * 0.90) * 1.15 = 207.0
            // Current buggy: 180.0 (tax ignored)
            assertEquals(207.0, result, 0.01, "Should apply 15% tax after 10% first order discount");
        }

        @Test
        @DisplayName("Decision Table: First Order=F, Credit Card - expect tax after discount")
        void testDecisionTable_NotFirstOrder_CreditCard() {
            double result = processor.processPayment(200.0, false, PaymentProcessor.PaymentMethod.CREDIT_CARD);
            // Expected: (200 * 0.95) * 1.15 = 218.5
            // Current buggy: 190.0 (tax ignored)
            assertEquals(218.5, result, 0.01, "Should apply 15% tax after 5% credit card discount");
        }

        @Test
        @DisplayName("Decision Table: First Order=F, PayPal - expect tax after discount")
        void testDecisionTable_NotFirstOrder_PayPal() {
            double result = processor.processPayment(200.0, false, PaymentProcessor.PaymentMethod.PAYPAL);
            // Expected: (200 * 0.98) * 1.15 = 225.4
            // Current buggy: 196.0 (tax ignored)
            assertEquals(225.4, result, 0.01, "Should apply 15% tax after 2% PayPal discount");
        }

        @Test
        @DisplayName("Decision Table: First Order=F, Cash - expect tax on full amount")
        void testDecisionTable_NotFirstOrder_Cash() {
            double result = processor.processPayment(200.0, false, PaymentProcessor.PaymentMethod.CASH);
            // Expected: 200 * 1.15 = 230.0 (no discount, but tax applied)
            // Current buggy: 200.0 (tax ignored)
            assertEquals(230.0, result, 0.01, "Should apply 15% tax with no discounts");
        }
    }


    @Nested
    @DisplayName("White-box Tests - Statement Coverage")
    class StatementCoverageTests {

        @Test
        @DisplayName("Cover all switch cases")
        void testAllSwitchCases() {
            // Test CREDIT_CARD case
            processor.processPayment(100.0, false, PaymentProcessor.PaymentMethod.CREDIT_CARD);

            // Test PAYPAL case
            processor.processPayment(100.0, false, PaymentProcessor.PaymentMethod.PAYPAL);

            // Test CASH case (default - no additional discount)
            processor.processPayment(100.0, false, PaymentProcessor.PaymentMethod.CASH);
        }

        @Test
        @DisplayName("Cover if condition for first order discount")
        void testFirstOrderCondition() {
            // Test both true and false branches
            processor.processPayment(100.0, true, PaymentProcessor.PaymentMethod.CASH);
            processor.processPayment(100.0, false, PaymentProcessor.PaymentMethod.CASH);
        }

        @Test
        @DisplayName("Cover exception throwing path")
        void testExceptionPath() {
            assertThrows(IllegalArgumentException.class, () ->
                    processor.processPayment(-10.0, false, PaymentProcessor.PaymentMethod.CASH));
        }
    }

    @Nested
    @DisplayName("White-box Tests - Branch Coverage")
    class BranchCoverageTests {

        @Test
        @DisplayName("Test both branches of amount validation")
        void testAmountValidationBranches() {
            // Valid amount branch
            assertDoesNotThrow(() ->
                    processor.processPayment(50.0, false, PaymentProcessor.PaymentMethod.CASH));

            // Invalid amount branch
            assertThrows(IllegalArgumentException.class, () ->
                    processor.processPayment(0.0, false, PaymentProcessor.PaymentMethod.CASH));
        }

        @Test
        @DisplayName("Test both branches of first order condition")
        void testFirstOrderBranches() {
            double withDiscount = processor.processPayment(100.0, true, PaymentProcessor.PaymentMethod.CASH);
            double withoutDiscount = processor.processPayment(100.0, false, PaymentProcessor.PaymentMethod.CASH);

            assertTrue(withDiscount < withoutDiscount);
        }

        @Test
        @DisplayName("Test both branches of delivery fee calculation")
        void testDeliveryFeeBranches() {
            double feeForLowAmount = processor.calculateDeliveryFee(30.0);
            double feeForHighAmount = processor.calculateDeliveryFee(60.0);

            assertEquals(5.0, feeForLowAmount);
            assertEquals(0.0, feeForHighAmount);
        }
    }

    @Nested
    @DisplayName("White-box Tests - Path Coverage")
    class PathCoverageTests {

        @Test
        @DisplayName("Path: Invalid amount -> Exception")
        void testInvalidAmountPath() {
            assertThrows(IllegalArgumentException.class, () ->
                    processor.processPayment(-5.0, true, PaymentProcessor.PaymentMethod.CREDIT_CARD));
        }

        @Test
        @DisplayName("Path: Valid amount, First order, Credit card - tax calculation bug")
        void testValidAmountFirstOrderCreditCardPath() {
            double result = processor.processPayment(100.0, true, PaymentProcessor.PaymentMethod.CREDIT_CARD);
            // This path should: calculate discount (15%), then apply tax (15%) to discounted amount
            // Expected: (100 * 0.85) * 1.15 = 97.75
            // But gets: 85.0 because tax calculation is ignored
            assertEquals(97.75, result, 0.01, "Tax should be applied after discount in this execution path");
        }

        @Test
        @DisplayName("Path: Valid amount, Not first order, PayPal - unused tax variable")
        void testValidAmountNotFirstOrderPayPalPath() {
            double result = processor.processPayment(100.0, false, PaymentProcessor.PaymentMethod.PAYPAL);
            // This path calculates taxedAmount but doesn't use it in final calculation
            // Expected: (100 * 0.98) * 1.15 = 112.7
            // But gets: 98.0 because finalAmount = discountedAmount (ignoring tax)
            assertEquals(112.7, result, 0.01, "Tax calculation path is executed but result ignored");
        }

        @Test
        @DisplayName("Path: Valid amount, First order, Cash - discount only path reveals tax bug")
        void testValidAmountFirstOrderCashPath() {
            double result = processor.processPayment(100.0, true, PaymentProcessor.PaymentMethod.CASH);
            // Path: amount validation -> first order discount -> no payment method discount -> final calc
            // Tax should still be applied: (100 * 0.90) * 1.15 = 103.5
            // But gets: 90.0 because tax is calculated but never used
            assertEquals(103.5, result, 0.01, "Even cash payments should include tax calculation");
        }
    }

    @Nested
    @DisplayName("Integration Tests - Main Class Workflow")
    class IntegrationTests {

        @Test
        @DisplayName("Integration: Process payment and calculate delivery fee - tax bug affects total")
        void testProcessPaymentAndDeliveryFee() {
            // Simulate the Main class workflow
            double amount = 100.0;
            boolean isFirstOrder = true;
            PaymentProcessor.PaymentMethod method = PaymentProcessor.PaymentMethod.CREDIT_CARD;

            double finalAmount = processor.processPayment(amount, isFirstOrder, method);
            double deliveryFee = processor.calculateDeliveryFee(finalAmount);
            double total = finalAmount + deliveryFee;

            // Expected with correct tax: (100 * 0.85) * 1.15 = 97.75
            // Current buggy implementation: 85.0
            assertEquals(97.75, finalAmount, 0.01, "Tax calculation bug affects final amount");
            assertEquals(0.0, deliveryFee); // Both 97.75 and 85.0 are > 50, so no delivery fee
            assertEquals(97.75, total, 0.01, "Total calculation affected by tax bug");
        }

        @Test
        @DisplayName("Integration: Low amount triggers delivery fee but tax calculation is wrong")
        void testLowAmountTriggersDeliveryFee() {
            double amount = 30.0;
            boolean isFirstOrder = false;
            PaymentProcessor.PaymentMethod method = PaymentProcessor.PaymentMethod.CASH;

            double finalAmount = processor.processPayment(amount, isFirstOrder, method);
            double deliveryFee = processor.calculateDeliveryFee(finalAmount);
            double total = finalAmount + deliveryFee;

            // Expected with tax: 30 * 1.15 = 34.5
            // Current buggy: 30.0 (no tax applied)
            assertEquals(34.5, finalAmount, 0.01, "Tax should be applied even to small amounts");
            assertEquals(5.0, deliveryFee); // Both 34.5 and 30.0 are < 50, so delivery fee applies
            assertEquals(39.5, total, 0.01, "Total should include tax on payment amount");
        }

        @Test
        @DisplayName("Integration: Discounted amount with tax affects delivery fee calculation")
        void testDiscountedAmountAffectsDeliveryFee() {
            double amount = 55.0;
            boolean isFirstOrder = true; // 10% discount
            PaymentProcessor.PaymentMethod method = PaymentProcessor.PaymentMethod.CASH;

            double finalAmount = processor.processPayment(amount, isFirstOrder, method);
            double deliveryFee = processor.calculateDeliveryFee(finalAmount);

            // Expected with tax after discount: (55 * 0.9) * 1.15 = 56.925 ≈ 56.93
            // Current buggy: 49.5 (no tax)
            assertEquals(56.93, finalAmount, 0.01, "Tax should be applied after discount");

            // Expected delivery fee: 0.0 (56.93 > 50)
            // Current buggy: 5.0 (49.5 < 50)
            assertEquals(0.0, deliveryFee, "With correct tax calculation, no delivery fee should apply");
        }

        @Test
        @DisplayName("Integration: PayPal discount with tax calculation bug")
        void testPayPalDiscountTaxBug() {
            double amount = 60.0;
            boolean isFirstOrder = false;
            PaymentProcessor.PaymentMethod method = PaymentProcessor.PaymentMethod.PAYPAL;

            double finalAmount = processor.processPayment(amount, isFirstOrder, method);
            double deliveryFee = processor.calculateDeliveryFee(finalAmount);

            // Expected: (60 * 0.98) * 1.15 = 67.62
            // Current buggy: 58.8 (no tax)
            assertEquals(67.62, finalAmount, 0.01, "PayPal discount should be followed by tax calculation");
            assertEquals(0.0, deliveryFee); // Both expected and buggy amounts > 50
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Very small amount precision - tax should still apply")
        void testVerySmallAmount() {
            double result = processor.processPayment(0.10, false, PaymentProcessor.PaymentMethod.CASH);
            // Expected with tax: 0.10 * 1.15 = 0.115 ≈ 0.12 (rounded)
            // Current buggy: 0.10 (no tax)
            assertEquals(0.12, result, 0.001, "Tax should apply even to very small amounts");
        }

        @Test
        @DisplayName("Large amount with compound discounts and tax")
        void testLargeAmount() {
            double result = processor.processPayment(999999.99, true, PaymentProcessor.PaymentMethod.CREDIT_CARD);
            // Expected: (999999.99 * 0.85) * 1.15 = 977499.99
            // Current buggy: 849999.99 (no tax)
            assertEquals(977499.99, result, 0.01, "Large amounts should include tax calculation");
        }

        @Test
        @DisplayName("Rounding precision test with tax")
        void testRoundingPrecision() {
            double result = processor.processPayment(33.333, true, PaymentProcessor.PaymentMethod.PAYPAL);
            // Expected: (33.333 * 0.88) * 1.15 = 33.69 (rounded from 33.693044)
            // Current buggy: 29.33 (no tax applied)
            assertEquals(33.69, result, 0.01, "Complex calculations should include tax and proper rounding");
        }

        @Test
        @DisplayName("Boundary case: Amount that changes delivery fee eligibility due to missing tax")
        void testDeliveryFeeBoundaryWithTax() {
            // Choose amount where discount brings it close to $50, but tax should push it over
            double amount = 46.0;
            double result = processor.processPayment(amount, true, PaymentProcessor.PaymentMethod.CASH); // 10% discount

            // Expected: (46 * 0.90) * 1.15 = 47.61 (still < 50, delivery fee applies)
            // Current buggy: 41.4 (< 50, delivery fee applies)
            assertEquals(47.61, result, 0.01, "Tax calculation affects delivery fee eligibility");

            double deliveryFee = processor.calculateDeliveryFee(result);
            assertEquals(5.0, deliveryFee, "Should charge delivery fee for amounts under $50 after tax");
        }
    }

}
