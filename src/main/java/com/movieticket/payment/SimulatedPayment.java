package com.movieticket.payment;

import java.io.Serializable;
import java.util.UUID;

/**
 * Simulated payment provider implementation with explicit success and failure simulation.
 * Satisfies Module 2 polymorphism and Payable contract.
 */
public class SimulatedPayment implements Payable, Serializable {

    private static final long serialVersionUID = 1L;

    private final String paymentMethod;
    private String transactionId;
    private boolean lastPaymentSuccess;
    private String lastMessage;

    // Flag allowing tests or demo to force a simulated failure
    private boolean forceFailure = false;

    public SimulatedPayment() {
        this("Simulated Card");
    }

    public SimulatedPayment(String paymentMethod) {
        this.paymentMethod = paymentMethod != null ? paymentMethod : "Simulated Payment";
        this.transactionId = "TXN-INIT";
        this.lastPaymentSuccess = false;
        this.lastMessage = "Initialized";
    }

    public void setForceFailure(boolean forceFailure) {
        this.forceFailure = forceFailure;
    }

    @Override
    public boolean pay(double amount) {
        if (amount <= 0) {
            this.lastPaymentSuccess = false;
            this.lastMessage = "Payment rejected: Invalid amount " + amount;
            return false;
        }

        if (forceFailure) {
            this.lastPaymentSuccess = false;
            this.lastMessage = "Payment failed: Gateway rejected transaction (Simulated Failure).";
            return false;
        }

        this.transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.lastPaymentSuccess = true;
        this.lastMessage = String.format("Payment of Rs %.2f successful via %s [%s]",
                amount, this.paymentMethod, this.transactionId);
        return true;
    }

    @Override
    public boolean refund(double amount) {
        if (amount <= 0) {
            this.lastMessage = "Refund rejected: Invalid amount " + amount;
            return false;
        }

        String refundId = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.lastMessage = String.format("Refund of Rs %.2f processed via %s [%s]",
                amount, this.paymentMethod, refundId);
        return true;
    }

    @Override
    public String getTransactionDetails() {
        return String.format("[%s] %s (Method: %s)",
                this.transactionId, this.lastMessage, this.paymentMethod);
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public boolean isLastPaymentSuccess() {
        return lastPaymentSuccess;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}
