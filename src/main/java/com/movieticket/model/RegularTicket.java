package com.movieticket.model;

/**
 * Concrete Ticket subclass for standard cinema seating.
 * Inherits from Ticket and overrides calculatePrice().
 */
public class RegularTicket extends Ticket {

    private static final long serialVersionUID = 1L;

    public RegularTicket(String ticketId, String bookingId, String movieTitle,
                         String theaterName, String showtime, String seatCode, double basePrice) {
        super(ticketId, bookingId, movieTitle, theaterName, showtime, seatCode, basePrice);
    }

    public RegularTicket(String bookingId, String movieTitle, String theaterName,
                         String showtime, String seatCode, double basePrice) {
        this("TKT-" + seatCode + "-" + bookingId, bookingId, movieTitle, theaterName, showtime, seatCode, basePrice);
    }

    @Override
    public double calculatePrice() {
        // Standard price: exactly basePrice (e.g. Rs 300)
        return this.basePrice;
    }

    @Override
    public String getCategoryName() {
        return "Regular";
    }
}
