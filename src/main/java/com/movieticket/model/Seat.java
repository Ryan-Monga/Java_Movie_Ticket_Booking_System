package com.movieticket.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a single seat in a cinema screen layout.
 * Rows are typically labelled 'A'-'E' and columns '1'-'8'.
 */
public class Seat implements Serializable {

    private static final long serialVersionUID = 1L;

    private char rowLabel;
    private int seatNumber;
    private String seatCode; // e.g. "C4"
    private boolean booked;

    public Seat(char rowLabel, int seatNumber, boolean booked) {
        this.rowLabel = Character.toUpperCase(rowLabel);
        this.seatNumber = seatNumber;
        this.seatCode = String.format("%c%d", this.rowLabel, this.seatNumber);
        this.booked = booked;
    }

    public Seat(char rowLabel, int seatNumber) {
        this(rowLabel, seatNumber, false);
    }

    public Seat(String seatCode) {
        if (seatCode == null || seatCode.trim().length() < 2) {
            throw new IllegalArgumentException("Invalid seat code: " + seatCode);
        }
        String clean = seatCode.trim().toUpperCase();
        this.rowLabel = clean.charAt(0);
        this.seatNumber = Integer.parseInt(clean.substring(1));
        this.seatCode = clean;
        this.booked = false;
    }

    public char getRowLabel() {
        return rowLabel;
    }

    public void setRowLabel(char rowLabel) {
        this.rowLabel = Character.toUpperCase(rowLabel);
        this.seatCode = String.format("%c%d", this.rowLabel, this.seatNumber);
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
        this.seatCode = String.format("%c%d", this.rowLabel, this.seatNumber);
    }

    public String getSeatCode() {
        return seatCode;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seat seat = (Seat) o;
        return seatCode.equalsIgnoreCase(seat.seatCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seatCode.toUpperCase());
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", seatCode, booked ? "BOOKED" : "AVAILABLE");
    }
}
