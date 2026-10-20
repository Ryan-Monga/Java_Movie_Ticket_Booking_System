package com.movieticket.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe generator for human-readable booking IDs (e.g. BK101, BK102).
 * Supports initializing from database max ID so counters persist across restarts.
 */
public class BookingIdGenerator {

    private static final String PREFIX = "BK";
    private static final int DEFAULT_START_COUNTER = 101;
    private static final AtomicInteger counter = new AtomicInteger(DEFAULT_START_COUNTER);

    // Private constructor to prevent instantiation
    private BookingIdGenerator() {}

    /**
     * Initializes the counter based on existing booking IDs in the database.
     * @param maxNumericId highest numeric value found in database (e.g. 101 for BK101).
     */
    public static synchronized void initializeFromMax(int maxNumericId) {
        if (maxNumericId >= counter.get()) {
            counter.set(maxNumericId + 1);
        }
    }

    /**
     * Generates the next sequential unique booking ID.
     * @return Formatted string ID, e.g. "BK101"
     */
    public static String getNextId() {
        return PREFIX + counter.getAndIncrement();
    }

    /**
     * Inspects the current counter value.
     */
    public static int getCurrentCounter() {
        return counter.get();
    }

    /**
     * Resets the counter (primarily for isolated test fixtures).
     */
    public static synchronized void reset(int start) {
        counter.set(start);
    }
}
