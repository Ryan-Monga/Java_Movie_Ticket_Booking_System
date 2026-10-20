package com.movieticket.util;

import com.movieticket.exception.InvalidInputException;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Validates user form inputs before processing bookings.
 * Demonstrates Module 1 string handling, regular expressions, and custom exception triggers.
 */
public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+()\\-\\s]{7,15}$");
    private static final Pattern SEAT_PATTERN =
            Pattern.compile("^[A-E][1-8]$", Pattern.CASE_INSENSITIVE);

    private ValidationUtils() {}

    public static void validateCustomerDetails(String name, String email, String phone) throws InvalidInputException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Customer name cannot be empty.");
        }
        if (name.trim().length() < 2) {
            throw new InvalidInputException("Customer name must be at least 2 characters long.");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new InvalidInputException("Customer email cannot be empty.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new InvalidInputException("Invalid email format: " + email);
        }

        if (phone == null || phone.trim().isEmpty()) {
            throw new InvalidInputException("Customer phone number cannot be empty.");
        }
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new InvalidInputException("Invalid phone number format: " + phone);
        }
    }

    public static void validateSeatSelection(Collection<String> seats) throws InvalidInputException {
        if (seats == null || seats.isEmpty()) {
            throw new InvalidInputException("Please select at least one seat to proceed.");
        }

        for (String seat : seats) {
            if (seat == null || !SEAT_PATTERN.matcher(seat.trim()).matches()) {
                throw new InvalidInputException("Invalid seat identifier: " + seat + ". Expected format: A1 to E8.");
            }
        }
    }
}
