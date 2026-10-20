package com.movieticket;

import com.movieticket.model.Booking;
import com.movieticket.model.BookingStatus;
import com.movieticket.model.Movie;
import com.movieticket.model.Showtime;
import com.movieticket.model.Theater;
import com.movieticket.model.TicketCategory;
import com.movieticket.service.SerializationService;
import com.movieticket.service.TicketFileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Ticket text file export (BufferedWriter/FileWriter) and
 * Object serialization (ObjectOutputStream/ObjectInputStream).
 * Satisfies Requirement 13.10 and Module 4 requirements.
 */
public class SerializationAndFileTest {

    private static final String TEST_EXPORT_DIR = "target/test-exports";

    private Booking createSampleBooking() {
        Movie movie = new Movie(1, "Inception", "Sci-Fi", 148, 8.8, 300.0);
        Theater theater = new Theater(1, "IMAX City", "Downtown", 5, 8);
        Showtime showtime = new Showtime(1, movie, theater, "Today", "07:30 PM", "IMAX 3D");

        return new Booking(
                "BK101",
                "Priya",
                "priya@cinepass.com",
                "9876543210",
                showtime,
                TicketCategory.REGULAR,
                List.of("C4", "C5"),
                600.0
        );
    }

    @Test
    @DisplayName("Generate readable ticket text file matching exact presentation format")
    public void testTicketFileExport() throws Exception {
        Booking booking = createSampleBooking();
        File ticketFile = TicketFileService.exportTicketToFile(booking, TEST_EXPORT_DIR);

        assertNotNull(ticketFile);
        assertTrue(ticketFile.exists(), "Ticket file must be created on disk");

        String content = Files.readString(ticketFile.toPath());

        // Verify required fields from Section 12
        assertTrue(content.contains("== MOVIE TICKET =="), "Must contain header");
        assertTrue(content.contains("ID: BK101"), "Must contain ID: BK101");
        assertTrue(content.contains("Movie: Inception"), "Must contain Movie: Inception");
        assertTrue(content.contains("Theater: IMAX City"), "Must contain Theater: IMAX City");
        assertTrue(content.contains("Time: 07:30 PM"), "Must contain Time: 07:30 PM");
        assertTrue(content.contains("Seats: C4, C5"), "Must contain Seats: C4, C5");
        assertTrue(content.contains("Amount: Rs 600"), "Must contain Amount: Rs 600");
        assertTrue(content.contains("Status: CONFIRMED"), "Must contain Status: CONFIRMED");
    }

    @Test
    @DisplayName("Serialize and deserialize bookings list using Java Object Streams")
    public void testSerializationRoundTrip() throws Exception {
        Booking b1 = createSampleBooking();
        Booking b2 = new Booking(
                "BK102",
                "Rohan",
                "rohan@cinepass.com",
                "9876543299",
                b1.getShowtime(),
                TicketCategory.PREMIUM,
                List.of("B2"),
                450.0
        );

        List<Booking> originalList = List.of(b1, b2);
        String datPath = TEST_EXPORT_DIR + "/bookings_test.dat";

        // Export (Serialization)
        File datFile = SerializationService.exportBookingsToDat(originalList, datPath);
        assertNotNull(datFile);
        assertTrue(datFile.exists());
        assertTrue(datFile.length() > 0, "Serialized .dat file should have content");

        // Inspect (Deserialization)
        List<Booking> deserializedList = SerializationService.inspectBookingsFromDat(datPath);
        assertNotNull(deserializedList);
        assertEquals(2, deserializedList.size());

        Booking restored1 = deserializedList.get(0);
        assertEquals("BK101", restored1.getBookingId());
        assertEquals("Priya", restored1.getCustomerName());
        assertEquals("C4, C5", restored1.getFormattedSeats());
        assertEquals(600.0, restored1.getTotalAmount(), 0.001);

        Booking restored2 = deserializedList.get(1);
        assertEquals("BK102", restored2.getBookingId());
        assertEquals("Rohan", restored2.getCustomerName());
        assertEquals("B2", restored2.getFormattedSeats());
        assertEquals(450.0, restored2.getTotalAmount(), 0.001);
    }
}
