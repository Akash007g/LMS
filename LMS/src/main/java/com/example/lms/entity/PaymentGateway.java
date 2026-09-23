package com.example.lms.entity;

public enum PaymentGateway {
    RAZORPAY("Razorpay"),
    STRIPE("Stripe"),
    PAYPAL("PayPal");

    private final String label;

    PaymentGateway(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}