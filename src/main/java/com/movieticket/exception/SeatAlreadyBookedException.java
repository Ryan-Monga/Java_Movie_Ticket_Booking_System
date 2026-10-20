package com.movieticket.exception;

/**
 * Custom checked exception thrown when an attempt is made to reserve
 * a seat that has already been booked for a specific showtime.
 * Satisfies Module 3 requirement for double-booking prevention.
 */
public class SeatAlreadyBookedException extends Exception {

    private final String seatCode;
    private final int showtimeId;

    public SeatAlreadyBookedException(String seatCode, int showtimeId) {
        super(String.format("Seat %s is already booked for showtime ID %d.", seatCode, showtimeId));
        this.seatCode = seatCode;
        this.showtimeId = showtimeId;
    }

    public SeatAlreadyBookedException(String message) {
        super(message);
        this.seatCode = "UNKNOWN";
        this.showtimeId = -1;
    }

    public String getSeatCode() {
        return seatCode;
    }

    public int getShowtimeId() {
        return showtimeId;
    }
}
