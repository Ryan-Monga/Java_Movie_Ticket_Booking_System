package com.movieticket.payment;

/**
 * Interface representing a payment-capable gateway or service.
 * Demonstrates Module 2 interface requirement.
 */
public interface Payable {

    /**
     * Executes payment for the given amount.
     * @param amount The transaction amount.
     * @return true if payment succeeded, false otherwise.
     */
    boolean pay(double amount);

    /**
     * Issues a refund for the given amount.
     * @param amount The refund amount.
     * @return true if refund succeeded, false otherwise.
     */
    boolean refund(double amount);

    /**
     * Returns descriptive transaction details for auditing.
     * @return transaction log or status string.
     */
    String getTransactionDetails();
}
