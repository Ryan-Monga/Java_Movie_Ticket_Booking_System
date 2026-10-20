package com.movieticket;

import com.movieticket.db.DatabaseManager;
import com.movieticket.db.SchemaInitializer;
import com.movieticket.exception.BookingException;
import com.movieticket.exception.InvalidInputException;
import com.movieticket.exception.SeatAlreadyBookedException;
import com.movieticket.model.Booking;
import com.movieticket.model.BookingStatus;
import com.movieticket.model.Showtime;
import com.movieticket.model.TicketCategory;
import com.movieticket.payment.SimulatedPayment;
import com.movieticket.service.BookingService;
import com.movieticket.service.MovieService;
import com.movieticket.util.BookingIdGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit and integration tests for BookingService operations.
 * Uses an isolated SQLite database file to avoid altering live demo data.
 * Satisfies Requirements 13.2, 13.3, 13.4, 13.5, 13.8.
 */
public class BookingServiceTest {

    private static final String TEST_DB_PATH = "target/test-db/test_booking.db";
    private static MovieService movieService;
    private static BookingService bookingService;
    private static Showtime testShowtime;

    @BeforeAll
    public static void setupDatabase() throws Exception {
        File dbFile = new File(TEST_DB_PATH);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        DatabaseManager.setDatabasePath(TEST_DB_PATH);
        SchemaInitializer.initializeDatabase();
        movieService = new MovieService();
        bookingService = new BookingService(movieService, new SimulatedPayment());

        // Get showtime 1 (Inception at IMAX City 07:30 PM)
        testShowtime = movieService.getShowtimeById(1);
        assertNotNull(testShowtime, "Test showtime 1 must exist");
    }

    @AfterAll
    public static void tearDown() {
        DatabaseManager.resetToDefaultDatabase();
    }

    @BeforeEach
    public void resetBetweenTests() throws Exception {
        try (java.sql.Connection conn = DatabaseManager.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM booking_seats;");
            stmt.execute("DELETE FROM bookings;");
            stmt.execute("UPDATE seats SET is_booked = 0, booking_id = NULL;");
        }
        BookingIdGenerator.reset(101);
        movieService.refreshCache();
        bookingService.refreshBookingsFromDatabase();
        testShowtime = movieService.getShowtimeById(1);
    }

    @Test
    @DisplayName("1. Successfully book an available seat")
    public void testBookAvailableSeat() throws Exception {
        Booking booking = bookingService.bookTickets(
                "Priya Sharma",
                "priya@cinepass.com",
                "9876543210",
                testShowtime,
                TicketCategory.REGULAR,
                List.of("A1")
        );

        assertNotNull(booking);
        assertNotNull(booking.getBookingId());
        assertTrue(booking.getBookingId().startsWith("BK"));
        assertEquals("Priya Sharma", booking.getCustomerName());
        assertEquals(300.0, booking.getTotalAmount(), 0.001);
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertTrue(testShowtime.isSeatBooked("A1"));
    }

    @Test
    @DisplayName("2. Reject duplicate seat booking with SeatAlreadyBookedException")
    public void testRejectDuplicateSeatBooking() throws Exception {
        // First book seat A2
        bookingService.bookTickets(
                "Customer One",
                "c1@cinepass.com",
                "9876543211",
                testShowtime,
                TicketCategory.REGULAR,
                List.of("A2")
        );

        // Attempt to book seat A2 again
        assertThrows(SeatAlreadyBookedException.class, () -> {
            bookingService.bookTickets(
                    "Customer Two",
                    "c2@cinepass.com",
                    "9876543212",
                    testShowtime,
                    TicketCategory.REGULAR,
                    List.of("A2")
            );
        }, "Should throw SeatAlreadyBookedException when booking seat A2 twice");
    }

    @Test
    @DisplayName("3. Book multiple seats atomically (C4, C5 totalling Rs 600)")
    public void testAtomicMultiSeatBooking() throws Exception {
        Booking booking = bookingService.bookTickets(
                "Priya",
                "priya@cinepass.com",
                "9876543210",
                testShowtime,
                TicketCategory.REGULAR,
                List.of("C4", "C5")
        );

        assertNotNull(booking);
        assertEquals(2, booking.getSeatCount());
        assertEquals("C4, C5", booking.getFormattedSeats());
        assertEquals(600.0, booking.getTotalAmount(), 0.001, "Two seats must total Rs 600");
        assertTrue(testShowtime.isSeatBooked("C4"));
        assertTrue(testShowtime.isSeatBooked("C5"));
    }

    @Test
    @DisplayName("4. Multi-seat atomic failure leaves no partial reservations")
    public void testAtomicMultiSeatFailureRollback() throws Exception {
        // Pre-book seat B1
        bookingService.bookTickets(
                "Early Bird",
                "early@cinepass.com",
                "9876543213",
                testShowtime,
                TicketCategory.REGULAR,
                List.of("B1")
        );

        // Attempt to book B1 and B2 together (B1 is already booked, B2 is free)
        assertThrows(SeatAlreadyBookedException.class, () -> {
            bookingService.bookTickets(
                    "Late Bird",
                    "late@cinepass.com",
                    "9876543214",
                    testShowtime,
                    TicketCategory.REGULAR,
                    List.of("B1", "B2")
            );
        });

        // Verify B2 was NOT booked because the transaction was rolled back atomically
        assertFalse(testShowtime.isSeatBooked("B2"), "Seat B2 must remain available after rollback");
    }

    @Test
    @DisplayName("5. Cancel booking and release seats")
    public void testCancelBookingAndReleaseSeats() throws Exception {
        Booking booking = bookingService.bookTickets(
                "Alice",
                "alice@cinepass.com",
                "9876543215",
                testShowtime,
                TicketCategory.REGULAR,
                List.of("D1")
        );

        assertTrue(testShowtime.isSeatBooked("D1"));
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        // Cancel the booking
        boolean cancelled = bookingService.cancelBooking(booking.getBookingId());
        assertTrue(cancelled, "Cancellation should return true");
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());

        // Verify seat is available again in 2D grid and can be booked by someone else
        assertFalse(testShowtime.isSeatBooked("D1"));

        // Now book D1 again with another user
        Booking rebooked = bookingService.bookTickets(
                "Bob",
                "bob@cinepass.com",
                "9876543216",
                testShowtime,
                TicketCategory.REGULAR,
                List.of("D1")
        );
        assertNotNull(rebooked);
        assertEquals(BookingStatus.CONFIRMED, rebooked.getStatus());
    }

    @Test
    @DisplayName("6. Validation catches empty customer name, bad email, bad phone")
    public void testCustomerInputValidation() {
        assertThrows(InvalidInputException.class, () -> {
            bookingService.bookTickets("", "valid@email.com", "9876543210", testShowtime, TicketCategory.REGULAR, List.of("E1"));
        });

        assertThrows(InvalidInputException.class, () -> {
            bookingService.bookTickets("Valid Name", "invalid-email", "9876543210", testShowtime, TicketCategory.REGULAR, List.of("E1"));
        });

        assertThrows(InvalidInputException.class, () -> {
            bookingService.bookTickets("Valid Name", "valid@email.com", "123", testShowtime, TicketCategory.REGULAR, List.of("E1"));
        });

        assertThrows(InvalidInputException.class, () -> {
            bookingService.bookTickets("Valid Name", "valid@email.com", "9876543210", testShowtime, TicketCategory.REGULAR, List.of());
        });
    }

    @Test
    @DisplayName("7. Validation catches invalid seat identifier (e.g. Z9)")
    public void testInvalidSeatIdentifier() {
        assertThrows(InvalidInputException.class, () -> {
            bookingService.bookTickets("Valid Name", "valid@email.com", "9876543210", testShowtime, TicketCategory.REGULAR, List.of("Z9"));
        });
    }
}
