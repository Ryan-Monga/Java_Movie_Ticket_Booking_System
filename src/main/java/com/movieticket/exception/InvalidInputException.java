package com.movieticket.exception;

/**
 * Custom exception thrown when user input fails business validation rules.
 */
public class InvalidInputException extends Exception {

    public InvalidInputException(String message) {
        super(message);
    }
}
