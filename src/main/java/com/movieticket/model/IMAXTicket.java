package com.movieticket.model;

/**
 * Concrete Ticket subclass for IMAX 3D immersive cinema experience.
 * Inherits from Ticket and overrides calculatePrice() with IMAX premium.
 */
public class IMAXTicket extends Ticket {

    private static final long serialVersionUID = 1L;

    public IMAXTicket(String ticketId, String bookingId, String movieTitle,
                      String theaterName, String showtime, String seatCode, double basePrice) {
        super(ticketId, bookingId, movieTitle, theaterName, showtime, seatCode, basePrice);
    }

    public IMAXTicket(String bookingId, String movieTitle, String theaterName,
                      String showtime, String seatCode, double basePrice) {
        this("TKT-" + seatCode + "-" + bookingId, bookingId, movieTitle, theaterName, showtime, seatCode, basePrice);
    }

    @Override
    public double calculatePrice() {
        // IMAX Experience: basePrice + Rs 250 (e.g. 300 + 250 = Rs 550)
        return this.basePrice + 250.0;
    }

    @Override
    public String getCategoryName() {
        return "IMAX 3D";
    }
}
