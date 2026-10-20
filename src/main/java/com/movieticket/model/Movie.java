package com.movieticket.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a Movie in the cinema catalogue.
 * Implements Comparable for natural sorting by title.
 * Implements Serializable for Module 4 backup/export.
 */
public class Movie implements Serializable, Comparable<Movie> {

    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String genre;
    private int durationMinutes;
    private double rating;
    private String language;
    private String description;
    private String posterPath;
    private double basePrice;

    // Overloaded Constructor 1: Full fields
    public Movie(int id, String title, String genre, int durationMinutes,
                 double rating, String language, String description,
                 String posterPath, double basePrice) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.durationMinutes = durationMinutes;
        this.rating = rating;
        this.language = language;
        this.description = description;
        this.posterPath = posterPath;
        this.basePrice = basePrice;
    }

    // Overloaded Constructor 2: Standard defaults
    public Movie(int id, String title, String genre, int durationMinutes, double rating, double basePrice) {
        this(id, title, genre, durationMinutes, rating, "English", "No description available.", "", basePrice);
    }

    // Default constructor for serialization
    public Movie() {
        this(0, "Untitled", "General", 120, 7.0, 300.0);
    }

    // Getters and Setters demonstrating encapsulation
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return this.genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getDurationMinutes() {
        return this.durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public double getRating() {
        return this.rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosterPath() {
        return this.posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public double getBasePrice() {
        return this.basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public String getFormattedDuration() {
        int hours = this.durationMinutes / 60;
        int mins = this.durationMinutes % 60;
        return hours > 0 ? String.format("%dh %02dm", hours, mins) : String.format("%dm", mins);
    }

    @Override
    public int compareTo(Movie other) {
        if (other == null) {
            return 1;
        }
        return this.title.compareToIgnoreCase(other.title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return id == movie.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s (%s | %s | %.1f★)", title, genre, getFormattedDuration(), rating);
    }
}
