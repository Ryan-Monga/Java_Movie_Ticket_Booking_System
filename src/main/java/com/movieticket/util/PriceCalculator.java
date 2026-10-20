package com.movieticket.util;

import com.movieticket.model.IMAXTicket;
import com.movieticket.model.PremiumTicket;
import com.movieticket.model.RegularTicket;
import com.movieticket.model.Ticket;
import com.movieticket.model.TicketCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for pricing calculations and ticket factory methods.
 * Demonstrates modern Java switch expressions and polymorphic Ticket creation.
 */
public class PriceCalculator {

    private PriceCalculator() {}

    /**
     * Creates a polymorphic Ticket instance based on category.
     * Demonstrates switch expression (Module 1 & 2 requirements).
     */
    public static Ticket createTicket(TicketCategory category, String bookingId, String movieTitle,
                                      String theaterName, String showtime, String seatCode, double basePrice) {
        return switch (category) {
            case REGULAR -> new RegularTicket(bookingId, movieTitle, theaterName, showtime, seatCode, basePrice);
            case PREMIUM -> new PremiumTicket(bookingId, movieTitle, theaterName, showtime, seatCode, basePrice);
            case IMAX    -> new IMAXTicket(bookingId, movieTitle, theaterName, showtime, seatCode, basePrice);
        };
    }

    /**
     * Calculates total price for a collection of seats under a specific category.
     * @param category the ticket category
     * @param basePrice the base price of the movie/theatre
     * @param seatCount the number of selected seats
     * @return the total price
     */
    public static double calculateTotal(TicketCategory category, double basePrice, int seatCount) {
        if (seatCount <= 0 || basePrice <= 0) {
            return 0.0;
        }

        // Create a representative ticket to obtain polymorphic unit price
        Ticket sampleTicket = createTicket(category, "TEMP", "Sample", "Theater", "00:00", "A1", basePrice);
        double unitPrice = sampleTicket.calculatePrice();
        return unitPrice * seatCount;
    }

    /**
     * Generates a list of polymorphic Ticket instances for multiple seats.
     */
    public static List<Ticket> createTickets(TicketCategory category, String bookingId, String movieTitle,
                                            String theaterName, String showtime, List<String> seatCodes, double basePrice) {
        List<Ticket> list = new ArrayList<>();
        if (seatCodes != null) {
            for (String seat : seatCodes) {
                list.add(createTicket(category, bookingId, movieTitle, theaterName, showtime, seat, basePrice));
            }
        }
        return list;
    }
}
