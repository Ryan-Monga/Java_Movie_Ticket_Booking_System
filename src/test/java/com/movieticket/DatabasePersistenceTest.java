package com.movieticket;

import com.movieticket.db.DatabaseManager;
import com.movieticket.db.SchemaInitializer;
import com.movieticket.model.Booking;
import com.movieticket.model.Showtime;
import com.movieticket.model.TicketCategory;
import com.movieticket.payment.SimulatedPayment;
import com.movieticket.service.BookingService;
import com.movieticket.service.MovieService;
import com.movieticket.util.BookingIdGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that SQLite database persistence and Booking ID generation
 * survive across simulated application restarts.
 * Satisfies Requirements 13.6 and 13.7.
 */
public class DatabasePersistenceTest {

    private static final String PERSISTENCE_DB_PATH = "target/test-db/persistence_test.db";

    @BeforeAll
    public static void setup() throws Exception {
        File db = new File(PERSISTENCE_DB_PATH);
        if (db.exists()) {
            db.delete();
        }
        DatabaseManager.setDatabasePath(PERSISTENCE_DB_PATH);
        SchemaInitializer.initializeDatabase();
    }

    @AfterAll
    public static void tearDown() {
        DatabaseManager.resetToDefaultDatabase();
    }

    @Test
    @DisplayName("Verify booking persists and ID counters resume correctly across restart")
    public void testPersistenceAcrossApplicationRestart() throws Exception {
        // App Instance 1: Startup
        MovieService app1MovieService = new MovieService();
        BookingService app1BookingService = new BookingService(app1MovieService, new SimulatedPayment());
        Showtime st1 = app1MovieService.getShowtimeById(1);

        // Book 2 seats C4, C5
        Booking booking1 = app1BookingService.bookTickets(
                "Priya",
                "priya@cinepass.com",
                "9876543210",
                st1,
                TicketCategory.REGULAR,
                List.of("C4", "C5")
        );

        String firstId = booking1.getBookingId();
        assertNotNull(firstId);
        assertTrue(firstId.startsWith("BK"));

        // ==========================================
        // SIMULATE APPLICATION RESTART
        // ==========================================
        // Fresh instances connected to the same persistent SQLite database
        SchemaInitializer.initializeDatabase(); // Syncs BookingIdGenerator from DB
        MovieService app2MovieService = new MovieService();
        BookingService app2BookingService = new BookingService(app2MovieService, new SimulatedPayment());
        Showtime st2 = app2MovieService.getShowtimeById(1);

        // 1. Verify previous booking is retrieved from database
        Booking restoredBooking = app2BookingService.getBookingById(firstId);
        assertNotNull(restoredBooking, "Booking " + firstId + " must be present after restart");
        assertEquals("Priya", restoredBooking.getCustomerName());
        assertEquals("C4, C5", restoredBooking.getFormattedSeats());
        assertEquals(600.0, restoredBooking.getTotalAmount(), 0.001);

        // 2. Verify seats C4 and C5 are recognized as booked in fresh Showtime 2D grid
        assertTrue(st2.isSeatBooked("C4"), "Seat C4 must remain booked after restart");
        assertTrue(st2.isSeatBooked("C5"), "Seat C5 must remain booked after restart");
        assertFalse(st2.isSeatBooked("C6"), "Seat C6 should remain available");

        // 3. Verify next booking receives an incrementally unique ID without conflict
        Booking booking2 = app2BookingService.bookTickets(
                "Rohan",
                "rohan@cinepass.com",
                "9876543299",
                st2,
                TicketCategory.REGULAR,
                List.of("C6")
        );

        String secondId = booking2.getBookingId();
        assertNotNull(secondId);
        assertFalse(firstId.equals(secondId), "New booking ID must not collide with existing ID");
    }
}
