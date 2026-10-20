package com.movieticket.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract Ticket class demonstrating Module 2 OOP inheritance and runtime polymorphism.
 * Subclasses override calculatePrice() with category-specific pricing formulas.
 */
public abstract class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String ticketId;
    protected String bookingId;
    protected String movieTitle;
    protected String theaterName;
    protected String showtime;
    protected String seatCode;
    protected double basePrice;

    public Ticket(String ticketId, String bookingId, String movieTitle,
                  String theaterName, String showtime, String seatCode, double basePrice) {
        this.ticketId = ticketId;
        this.bookingId = bookingId;
        this.movieTitle = movieTitle;
        this.theaterName = theaterName;
        this.showtime = showtime;
        this.seatCode = seatCode;
        this.basePrice = basePrice;
    }

    public Ticket() {
        this("TKT-000", "BK000", "Movie", "Cinema", "00:00", "A1", 300.0);
    }

    /**
     * Polymorphic method overridden by subclasses to apply tier-based pricing logic.
     * @return calculated ticket price.
     */
    public abstract double calculatePrice();

    /**
     * Returns ticket category name.
     */
    public abstract String getCategoryName();

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getTheaterName() {
        return theaterName;
    }

    public void setTheaterName(String theaterName) {
        this.theaterName = theaterName;
    }

    public String getShowtime() {
        return showtime;
    }

    public void setShowtime(String showtime) {
        this.showtime = showtime;
    }

    public String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return Objects.equals(ticketId, ticket.ticketId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId);
    }

    @Override
    public String toString() {
        return String.format("%s Ticket [%s | Seat: %s | Rs %.2f]",
                getCategoryName(), ticketId, seatCode, calculatePrice());
    }
}
