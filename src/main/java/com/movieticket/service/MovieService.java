package com.movieticket.service;

import com.movieticket.db.DatabaseManager;
import com.movieticket.model.Movie;
import com.movieticket.model.Showtime;
import com.movieticket.model.Theater;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service managing movies, theatres, and showtime schedules.
 * Demonstrates Module 4 collections (ArrayList, HashMap) and
 * Java Streams/lambdas for filtering and sorting.
 */
public class MovieService {

    private final List<Movie> movieCache = new ArrayList<>();
    private final Map<Integer, Movie> movieMap = new HashMap<>();
    private final Map<Integer, Theater> theaterMap = new HashMap<>();

    public MovieService() {
        refreshCache();
    }

    /**
     * Refreshes local in-memory collections from the database.
     */
    public synchronized void refreshCache() {
        movieCache.clear();
        movieMap.clear();
        theaterMap.clear();

        try (Connection conn = DatabaseManager.getConnection()) {
            // Load Theaters
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM theaters")) {
                while (rs.next()) {
                    Theater theater = new Theater(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("location"),
                            rs.getInt("total_rows"),
                            rs.getInt("total_cols")
                    );
                    theaterMap.put(theater.getId(), theater);
                }
            }

            // Load Movies
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM movies ORDER BY title ASC")) {
                while (rs.next()) {
                    Movie movie = new Movie(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("genre"),
                            rs.getInt("duration_minutes"),
                            rs.getDouble("rating"),
                            rs.getString("language"),
                            rs.getString("description"),
                            rs.getString("poster_path"),
                            rs.getDouble("base_price")
                    );
                    movieCache.add(movie);
                    movieMap.put(movie.getId(), movie);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading movies from database: " + e.getMessage());
        }
    }

    public List<Movie> getAllMovies() {
        return new ArrayList<>(movieCache);
    }

    public Movie getMovieById(int id) {
        return movieMap.get(id);
    }

    public Theater getTheaterById(int id) {
        return theaterMap.get(id);
    }

    /**
     * Case-insensitive partial search using Java Streams and lambdas (Module 4 requirement).
     */
    public List<Movie> searchMovies(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllMovies();
        }
        String lowerQuery = keyword.trim().toLowerCase();
        return movieCache.stream()
                .filter(m -> m.getTitle().toLowerCase().contains(lowerQuery)
                          || m.getGenre().toLowerCase().contains(lowerQuery)
                          || m.getDescription().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    /**
     * Filters movies by genre using Java Streams and lambdas (Module 4 requirement).
     */
    public List<Movie> filterByGenre(String genre) {
        if (genre == null || genre.equalsIgnoreCase("All Genres") || genre.trim().isEmpty()) {
            return getAllMovies();
        }
        String lowerGenre = genre.trim().toLowerCase();
        return movieCache.stream()
                .filter(m -> m.getGenre().toLowerCase().contains(lowerGenre))
                .collect(Collectors.toList());
    }

    /**
     * Sorts movies using a Comparator and Java Streams (Module 4 requirement).
     */
    public List<Movie> sortMovies(Comparator<Movie> comparator) {
        return movieCache.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all distinct genres available across movies.
     */
    public List<String> getAllGenres() {
        List<String> genres = new ArrayList<>();
        genres.add("All Genres");
        for (Movie m : movieCache) {
            String[] parts = m.getGenre().split("/");
            for (String p : parts) {
                String clean = p.trim();
                if (!genres.contains(clean) && !clean.isEmpty()) {
                    genres.add(clean);
                }
            }
        }
        return genres;
    }

    /**
     * Retrieves all showtimes for a specific movie, populating the 2D seat grid from DB.
     */
    public List<Showtime> getShowtimesForMovie(int movieId) {
        List<Showtime> list = new ArrayList<>();
        String sql = """
            SELECT s.*, t.name as theater_name, t.location, t.total_rows, t.total_cols
            FROM showtimes s
            JOIN theaters t ON s.theater_id = t.id
            WHERE s.movie_id = ?
            ORDER BY s.show_date, s.show_time
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, movieId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Movie movie = getMovieById(movieId);
                    Theater theater = getTheaterById(rs.getInt("theater_id"));
                    if (theater == null) {
                        theater = new Theater(
                                rs.getInt("theater_id"),
                                rs.getString("theater_name"),
                                rs.getString("location"),
                                rs.getInt("total_rows"),
                                rs.getInt("total_cols")
                        );
                    }

                    Showtime showtime = new Showtime(
                            rs.getInt("id"),
                            movie,
                            theater,
                            rs.getString("show_date"),
                            rs.getString("show_time"),
                            rs.getString("screen_type")
                    );

                    // Populate current seat booking statuses from DB into 2D grid
                    populateSeatGridFromDatabase(conn, showtime);
                    list.add(showtime);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching showtimes for movie " + movieId + ": " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves a specific Showtime by ID with its live 2D seat grid.
     */
    public Showtime getShowtimeById(int showtimeId) {
        String sql = "SELECT * FROM showtimes WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, showtimeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Movie movie = getMovieById(rs.getInt("movie_id"));
                    Theater theater = getTheaterById(rs.getInt("theater_id"));
                    Showtime st = new Showtime(
                            rs.getInt("id"),
                            movie,
                            theater,
                            rs.getString("show_date"),
                            rs.getString("show_time"),
                            rs.getString("screen_type")
                    );
                    populateSeatGridFromDatabase(conn, st);
                    return st;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading showtime by ID " + showtimeId + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Reads booked seats from the database and updates the Showtime 2D grid.
     */
    public void populateSeatGridFromDatabase(Connection conn, Showtime showtime) throws SQLException {
        String sql = "SELECT seat_code, is_booked FROM seats WHERE showtime_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, showtime.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String seatCode = rs.getString("seat_code");
                    int isBooked = rs.getInt("is_booked");
                    if (isBooked == 1) {
                        showtime.markSeatBooked(seatCode);
                    } else {
                        showtime.markSeatAvailable(seatCode);
                    }
                }
            }
        }
    }
}
