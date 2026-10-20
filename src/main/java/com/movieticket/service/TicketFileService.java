package com.movieticket.service;

import com.movieticket.model.Booking;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Service for generating formatted cinema ticket text files.
 * Satisfies Module 4 File I/O requirements (FileWriter, BufferedWriter, try-with-resources).
 */
public class TicketFileService {

    private static final String DEFAULT_EXPORT_DIR = "exports";

    private TicketFileService() {}

    /**
     * Exports a booking confirmation ticket as a readable text file.
     * Output format strictly matches the specification in Section 12.
     *
     * @param booking the booking to generate the ticket for
     * @return the generated File reference
     * @throws IOException if a file writing error occurs
     */
    public static File exportTicketToFile(Booking booking) throws IOException {
        return exportTicketToFile(booking, DEFAULT_EXPORT_DIR);
    }

    /**
     * Overloaded method allowing custom destination directory for tests or backups.
     */
    public static File exportTicketToFile(Booking booking, String directoryPath) throws IOException {
        if (booking == null) {
            throw new IllegalArgumentException("Cannot export null booking.");
        }

        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = String.format("ticket_%s.txt", booking.getBookingId());
        File ticketFile = new File(dir, fileName);

        String movieTitle = (booking.getShowtime() != null && booking.getShowtime().getMovie() != null)
                ? booking.getShowtime().getMovie().getTitle() : "Movie";

        String theaterName = (booking.getShowtime() != null && booking.getShowtime().getTheater() != null)
                ? booking.getShowtime().getTheater().getName() : "Cinema Hall";

        String showTime = (booking.getShowtime() != null)
                ? booking.getShowtime().getShowTime() : "00:00";

        // Try-with-resources ensures stream is closed cleanly (Module 4 Requirement)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ticketFile))) {
            writer.write("========================================");
            writer.newLine();
            writer.write("          == MOVIE TICKET ==");
            writer.newLine();
            writer.write("========================================");
            writer.newLine();
            writer.write("ID: " + booking.getBookingId());
            writer.newLine();
            writer.write("Movie: " + movieTitle);
            writer.newLine();
            writer.write("Theater: " + theaterName);
            writer.newLine();
            writer.write("Time: " + showTime);
            writer.newLine();
            writer.write("Seats: " + booking.getFormattedSeats());
            writer.newLine();
            writer.write(String.format("Amount: Rs %.0f", booking.getTotalAmount()));
            writer.newLine();
            writer.write("Status: " + booking.getStatus().name());
            writer.newLine();
            writer.write("----------------------------------------");
            writer.newLine();
            writer.write("Customer: " + booking.getCustomerName());
            writer.newLine();
            writer.write("Category: " + booking.getTicketCategory().getDisplayName());
            writer.newLine();
            writer.write("Date Booked: " + booking.getBookingTime());
            writer.newLine();
            writer.write("========================================");
            writer.newLine();
            writer.write(" [||||||||||||||||||||||||||||||||||||] ");
            writer.newLine();
            writer.write("    Enjoy your show at CinePass Cinemas! ");
            writer.newLine();
            writer.write("========================================");
            writer.newLine();
            writer.flush();
        }

        return ticketFile;
    }
}
