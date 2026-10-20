package com.movieticket.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a confirmed or cancelled ticket booking transaction.
 * Implements Serializable for Module 4 backup and Comparable for chronological ordering.
 */
public class Booking implements Serializable, Comparable<Booking> {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String bookingId; // e.g. "BK101"
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Showtime showtime;
    private TicketCategory ticketCategory;
    private List<String> seatCodes;
    private List<Ticket> tickets;
    private double totalAmount;
    private String bookingTime;
    private BookingStatus status;

    public Booking(String bookingId, String customerName, String customerEmail,
                   String customerPhone, Showtime showtime, TicketCategory ticketCategory,
                   List<String> seatCodes, List<Ticket> tickets, double totalAmount,
                   String bookingTime, BookingStatus status) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.showtime = showtime;
        this.ticketCategory = ticketCategory != null ? ticketCategory : TicketCategory.REGULAR;
        this.seatCodes = seatCodes != null ? new ArrayList<>(seatCodes) : new ArrayList<>();
        Collections.sort(this.seatCodes);
        this.tickets = tickets != null ? new ArrayList<>(tickets) : new ArrayList<>();
        this.totalAmount = totalAmount;
        this.bookingTime = bookingTime != null ? bookingTime : LocalDateTime.now().format(FORMATTER);
        this.status = status != null ? status : BookingStatus.CONFIRMED;
    }

    public Booking(String bookingId, String customerName, String customerEmail,
                   String customerPhone, Showtime showtime, TicketCategory ticketCategory,
                   List<String> seatCodes, double totalAmount) {
        this(bookingId, customerName, customerEmail, customerPhone, showtime,
             ticketCategory, seatCodes, new ArrayList<>(), totalAmount,
             LocalDateTime.now().format(FORMATTER), BookingStatus.CONFIRMED);
    }

    public Booking() {
        this("BK000", "Anonymous", "anon@cinepass.com", "9999999999",
             null, TicketCategory.REGULAR, new ArrayList<>(), 0.0);
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public void setShowtime(Showtime showtime) {
        this.showtime = showtime;
    }

    public TicketCategory getTicketCategory() {
        return ticketCategory;
    }

    public void setTicketCategory(TicketCategory ticketCategory) {
        this.ticketCategory = ticketCategory;
    }

    public List<String> getSeatCodes() {
        return seatCodes;
    }

    public void setSeatCodes(List<String> seatCodes) {
        this.seatCodes = seatCodes != null ? new ArrayList<>(seatCodes) : new ArrayList<>();
        Collections.sort(this.seatCodes);
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets != null ? new ArrayList<>(tickets) : new ArrayList<>();
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(String bookingTime) {
        this.bookingTime = bookingTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getFormattedSeats() {
        if (seatCodes == null || seatCodes.isEmpty()) return "None";
        return String.join(", ", seatCodes);
    }

    public int getSeatCount() {
        return seatCodes != null ? seatCodes.size() : 0;
    }

    @Override
    public int compareTo(Booking other) {
        if (other == null) return 1;
        // Sort in descending order of booking ID/time for recency
        return other.bookingId.compareTo(this.bookingId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(bookingId, booking.bookingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId);
    }

    @Override
    public String toString() {
        String movie = (showtime != null && showtime.getMovie() != null)
                ? showtime.getMovie().getTitle() : "Movie";
        return String.format("Booking[%s | %s | %s | %s | Rs %.2f | %s]",
                bookingId, customerName, movie, getFormattedSeats(), totalAmount, status);
    }
}
