package org.example;

import org.example.PaymentProcessor;

public class Main {
    public static void main(String[] args) {
        PaymentProcessor processor = new PaymentProcessor();

        double amount = 100.0;
        boolean isFirstOrder = true;
        PaymentProcessor.PaymentMethod method = PaymentProcessor.PaymentMethod.CREDIT_CARD;

        double finalAmount = processor.processPayment(amount, isFirstOrder, method);
        double deliveryFee = processor.calculateDeliveryFee(finalAmount);

        System.out.println("Original Amount: $" + amount);
        System.out.println("Final Amount after discounts: $" + finalAmount);
        System.out.println("Delivery Fee: $" + deliveryFee);
        System.out.println("Total to be paid: $" + (finalAmount + deliveryFee));
    }
}