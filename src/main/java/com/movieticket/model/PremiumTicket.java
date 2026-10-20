package com.movieticket.model;

/**
 * Concrete Ticket subclass for premium / recliner seating.
 * Inherits from Ticket and overrides calculatePrice() with 1.5x multiplier.
 */
public class PremiumTicket extends Ticket {

    private static final long serialVersionUID = 1L;

    public PremiumTicket(String ticketId, String bookingId, String movieTitle,
                          String theaterName, String showtime, String seatCode, double basePrice) {
        super(ticketId, bookingId, movieTitle, theaterName, showtime, seatCode, basePrice);
    }

    public PremiumTicket(String bookingId, String movieTitle, String theaterName,
                          String showtime, String seatCode, double basePrice) {
        this("TKT-" + seatCode + "-" + bookingId, bookingId, movieTitle, theaterName, showtime, seatCode, basePrice);
    }

    @Override
    public double calculatePrice() {
        // Premium multiplier: 1.5x basePrice (e.g. 1.5 * 300 = Rs 450)
        return this.basePrice * 1.5;
    }

    @Override
    public String getCategoryName() {
        return "Premium";
    }
}
