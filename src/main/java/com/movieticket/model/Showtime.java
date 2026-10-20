package com.movieticket.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a scheduled movie screening in a specific theatre.
 * Demonstrates Module 1 (2D arrays for seating layout) and
 * Module 2 (nested class Seat, encapsulation, this keyword).
 */
public class Showtime implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Nested Seat class demonstrating OOP nested class architecture.
     */
    public static class ShowtimeSeat extends Seat {
        private static final long serialVersionUID = 1L;

        public ShowtimeSeat(char rowLabel, int seatNumber, boolean booked) {
            super(rowLabel, seatNumber, booked);
        }

        public ShowtimeSeat(char rowLabel, int seatNumber) {
            super(rowLabel, seatNumber);
        }
    }

    private int id;
    private Movie movie;
    private Theater theater;
    private String showDate; // e.g. "2026-10-15"
    private String showTime; // e.g. "07:30 PM"
    private String screenType; // e.g. "IMAX", "Standard"

    // Meaningful Two-Dimensional Array for Seat Layout (Module 1 Requirement)
    private boolean[][] seatGrid;       // [rows][cols]: true = booked, false = available
    private Seat[][] seatMatrix;        // [rows][cols]: Seat objects for each position

    public Showtime(int id, Movie movie, Theater theater, String showDate, String showTime, String screenType) {
        this.id = id;
        this.movie = movie;
        this.theater = theater;
        this.showDate = showDate;
        this.showTime = showTime;
        this.screenType = screenType != null ? screenType : "Standard";

        int rows = theater != null ? theater.getTotalRows() : 5;
        int cols = theater != null ? theater.getTotalCols() : 8;
        initializeSeatGrid(rows, cols);
    }

    public Showtime(int id, Movie movie, Theater theater, String showDate, String showTime) {
        this(id, movie, theater, showDate, showTime, "Standard");
    }

    public Showtime() {
        this(0, null, null, "2026-10-15", "07:30 PM", "Standard");
    }

    /**
     * Initializes the two-dimensional array representing the cinema layout.
     * Demonstrates nested loops and array indexing.
     */
    public final void initializeSeatGrid(int rows, int cols) {
        this.seatGrid = new boolean[rows][cols];
        this.seatMatrix = new Seat[rows][cols];

        for (int r = 0; r < rows; r++) {
            char rowChar = (char) ('A' + r);
            for (int c = 0; c < cols; c++) {
                int seatNum = c + 1;
                this.seatGrid[r][c] = false;
                this.seatMatrix[r][c] = new Seat(rowChar, seatNum, false);
            }
        }
    }

    /**
     * Checks if a seat at 2D coordinate [row][col] is booked.
     */
    public boolean isSeatBooked(int row, int col) {
        if (row >= 0 && row < seatGrid.length && col >= 0 && col < seatGrid[0].length) {
            return seatGrid[row][col];
        }
        return true;
    }

    /**
     * Checks if a seat code (e.g. "C4") is booked using 2D array coordinates.
     */
    public boolean isSeatBooked(String seatCode) {
        if (seatCode == null || seatCode.length() < 2) return true;
        int[] coords = parseCoordinates(seatCode);
        if (coords == null) return true;
        return seatGrid[coords[0]][coords[1]];
    }

    /**
     * Marks a seat as booked in the 2D array representation.
     */
    public void markSeatBooked(String seatCode) {
        int[] coords = parseCoordinates(seatCode);
        if (coords != null) {
            seatGrid[coords[0]][coords[1]] = true;
            if (seatMatrix != null && seatMatrix[coords[0]][coords[1]] != null) {
                seatMatrix[coords[0]][coords[1]].setBooked(true);
            }
        }
    }

    /**
     * Marks a seat as available in the 2D array representation.
     */
    public void markSeatAvailable(String seatCode) {
        int[] coords = parseCoordinates(seatCode);
        if (coords != null) {
            seatGrid[coords[0]][coords[1]] = false;
            if (seatMatrix != null && seatMatrix[coords[0]][coords[1]] != null) {
                seatMatrix[coords[0]][coords[1]].setBooked(false);
            }
        }
    }

    /**
     * Helper method to map a seat code like "C4" to 2D indices [row, col].
     */
    public int[] parseCoordinates(String seatCode) {
        if (seatCode == null || seatCode.trim().length() < 2) return null;
        String clean = seatCode.trim().toUpperCase();
        char rowChar = clean.charAt(0);
        int r = rowChar - 'A';
        try {
            int c = Integer.parseInt(clean.substring(1)) - 1;
            if (r >= 0 && r < seatGrid.length && c >= 0 && c < seatGrid[0].length) {
                return new int[]{r, c};
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    /**
     * Calculates available seats using enhanced for-loop over the 2D array.
     */
    public int getAvailableSeatCount() {
        int count = 0;
        // Enhanced for-loop over 2D array (Module 1 requirement)
        for (boolean[] row : seatGrid) {
            for (boolean booked : row) {
                if (!booked) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getTotalSeats() {
        if (seatGrid == null || seatGrid.length == 0) return 0;
        return seatGrid.length * seatGrid[0].length;
    }

    public List<Seat> getAllSeatsList() {
        List<Seat> list = new ArrayList<>();
        if (seatMatrix != null) {
            // Enhanced for loops over Seat matrix
            for (Seat[] row : seatMatrix) {
                for (Seat seat : row) {
                    list.add(seat);
                }
            }
        }
        return list;
    }

    // Getters and Setters
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Movie getMovie() {
        return this.movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Theater getTheater() {
        return this.theater;
    }

    public void setTheater(Theater theater) {
        this.theater = theater;
    }

    public String getShowDate() {
        return this.showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    public String getShowTime() {
        return this.showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public String getScreenType() {
        return this.screenType;
    }

    public void setScreenType(String screenType) {
        this.screenType = screenType;
    }

    public boolean[][] getSeatGrid() {
        return this.seatGrid;
    }

    public Seat[][] getSeatMatrix() {
        return this.seatMatrix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Showtime showtime = (Showtime) o;
        return id == showtime.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String movieTitle = movie != null ? movie.getTitle() : "Movie";
        String theaterName = theater != null ? theater.getName() : "Theater";
        return String.format("%s at %s (%s, %s)", movieTitle, theaterName, showTime, showDate);
    }
}
