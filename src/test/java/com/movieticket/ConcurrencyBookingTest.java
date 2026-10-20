package com.movieticket;

import com.movieticket.db.DatabaseManager;
import com.movieticket.db.SchemaInitializer;
import com.movieticket.model.Showtime;
import com.movieticket.payment.SimulatedPayment;
import com.movieticket.service.BookingService;
import com.movieticket.service.ConcurrencyDemoService;
import com.movieticket.service.MovieService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Concurrency test simulating two concurrent threads competing for the exact same seat.
 * Satisfies Requirement 13.9 and Section 7 double-booking race condition demonstration.
 */
public class ConcurrencyBookingTest {

    private static final String CONCURRENCY_DB_PATH = "target/test-db/concurrency_test.db";
    private static MovieService movieService;
    private static BookingService bookingService;
    private static ConcurrencyDemoService concurrencyService;
    private static Showtime testShowtime;

    @BeforeAll
    public static void setup() throws Exception {
        File db = new File(CONCURRENCY_DB_PATH);
        if (db.exists()) {
            db.delete();
        }
        DatabaseManager.setDatabasePath(CONCURRENCY_DB_PATH);
        SchemaInitializer.initializeDatabase();
        movieService = new MovieService();
        bookingService = new BookingService(movieService, new SimulatedPayment());
        concurrencyService = new ConcurrencyDemoService(bookingService);
        testShowtime = movieService.getShowtimeById(1);
    }

    @AfterAll
    public static void tearDown() {
        DatabaseManager.resetToDefaultDatabase();
    }

    @Test
    @DisplayName("Simulate two threads attempting to book seat C4 concurrently - only one succeeds")
    public void testTwoThreadsBookingSameSeat() {
        System.out.println("=== Starting Concurrency Double-Booking Contention Test ===");

        ConcurrencyDemoService.ConcurrencyResult result =
                concurrencyService.runContentionSimulation(testShowtime, "C4");

        // Print execution logs as required by Section 7
        for (String logLine : result.executionLogs()) {
            System.out.println(logLine);
        }

        assertNotNull(result.winningThread(), "One thread must succeed in booking seat C4");
        assertNotNull(result.losingThread(), "The contested thread must fail with SeatAlreadyBookedException");
        assertTrue(result.hasRaceConditionProtected(), "System must atomically protect against double booking");

        // Verify that seat C4 is marked booked
        assertTrue(testShowtime.isSeatBooked("C4"), "Seat C4 must be marked booked");

        System.out.println("=== Concurrency Test Passed: Double booking successfully prevented ===");
    }
}
