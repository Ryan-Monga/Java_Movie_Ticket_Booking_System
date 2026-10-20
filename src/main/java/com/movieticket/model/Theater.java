package com.movieticket.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a cinema theatre hall with a seating dimension.
 */
public class Theater implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String location;
    private int totalRows;
    private int totalCols;

    public Theater(int id, String name, String location, int totalRows, int totalCols) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.totalRows = totalRows;
        this.totalCols = totalCols;
    }

    public Theater(int id, String name, String location) {
        this(id, name, location, 5, 8); // Default 5 rows x 8 columns as requested
    }

    public Theater() {
        this(0, "Cinema Hall", "Main Complex", 5, 8);
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getTotalRows() {
        return this.totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getTotalCols() {
        return this.totalCols;
    }

    public void setTotalCols(int totalCols) {
        this.totalCols = totalCols;
    }

    public int getTotalSeats() {
        return this.totalRows * this.totalCols;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Theater theater = (Theater) o;
        return id == theater.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", this.name, this.location);
    }
}
