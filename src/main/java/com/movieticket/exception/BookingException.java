package com.movieticket.exception;

/**
 * Custom checked exception for general booking workflow failures,
 * such as payment failures, transaction rollback, or cancellation issues.
 */
public class BookingException extends Exception {

    public BookingException(String message) {
        super(message);
    }

    public BookingException(String message, Throwable cause) {
        super(message, cause);
    }
}
