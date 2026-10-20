package com.movieticket.db;

import com.movieticket.util.BookingIdGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes database tables, indexes, constraints, and initial cinema seed data.
 * Satisfies Section 9 startup and JDBC schema requirements.
 */
public class SchemaInitializer {

    private SchemaInitializer() {}

    /**
     * Creates all required tables and seeds initial data if tables are empty.
     */
    public static void initializeDatabase() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            createTables(conn);
            seedInitialData(conn);
            syncBookingIdCounter(conn);
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {

            // 1. Movies Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS movies (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    genre TEXT NOT NULL,
                    duration_minutes INTEGER NOT NULL,
                    rating REAL NOT NULL,
                    language TEXT NOT NULL,
                    description TEXT,
                    poster_path TEXT,
                    base_price REAL NOT NULL DEFAULT 300.0
                );
            """);

            // 2. Theaters Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS theaters (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    location TEXT NOT NULL,
                    total_rows INTEGER NOT NULL DEFAULT 5,
                    total_cols INTEGER NOT NULL DEFAULT 8
                );
            """);

            // 3. Showtimes Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS showtimes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    movie_id INTEGER NOT NULL,
                    theater_id INTEGER NOT NULL,
                    show_date TEXT NOT NULL,
                    show_time TEXT NOT NULL,
                    screen_type TEXT NOT NULL DEFAULT 'Standard',
                    FOREIGN KEY (movie_id) REFERENCES movies (id) ON DELETE CASCADE,
                    FOREIGN KEY (theater_id) REFERENCES theaters (id) ON DELETE CASCADE
                );
            """);

            // 4. Bookings Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bookings (
                    id TEXT PRIMARY KEY,
                    customer_name TEXT NOT NULL,
                    customer_email TEXT NOT NULL,
                    customer_phone TEXT NOT NULL,
                    showtime_id INTEGER NOT NULL,
                    ticket_category TEXT NOT NULL,
                    total_amount REAL NOT NULL,
                    booking_time TEXT NOT NULL,
                    status TEXT NOT NULL,
                    FOREIGN KEY (showtime_id) REFERENCES showtimes (id)
                );
            """);

            // 5. Booking Seats (Mapping table with strict unique constraint to prevent double-booking)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS booking_seats (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    booking_id TEXT NOT NULL,
                    showtime_id INTEGER NOT NULL,
                    seat_code TEXT NOT NULL,
                    FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
                    FOREIGN KEY (showtime_id) REFERENCES showtimes (id) ON DELETE CASCADE,
                    UNIQUE(showtime_id, seat_code)
                );
            """);

            // 6. Seats Table (Physical seat inventory representation per showtime)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS seats (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    showtime_id INTEGER NOT NULL,
                    row_label TEXT NOT NULL,
                    seat_num INTEGER NOT NULL,
                    seat_code TEXT NOT NULL,
                    is_booked INTEGER NOT NULL DEFAULT 0,
                    booking_id TEXT,
                    FOREIGN KEY (showtime_id) REFERENCES showtimes (id) ON DELETE CASCADE,
                    FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE SET NULL,
                    UNIQUE(showtime_id, seat_code)
                );
            """);
        }
    }

    private static void seedInitialData(Connection conn) throws SQLException {
        // Check if movies already seeded
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) AS total FROM movies")) {
            if (rs.next() && rs.getInt("total") > 0) {
                return; // Already initialized
            }
        }

        // Insert sample movies (At least 6 movies, including Inception)
        String insertMovieSql = """
            INSERT INTO movies (title, genre, duration_minutes, rating, language, description, poster_path, base_price)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?);
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(insertMovieSql)) {
            // 1. Inception
            pstmt.setString(1, "Inception");
            pstmt.setString(2, "Sci-Fi / Action");
            pstmt.setInt(3, 148);
            pstmt.setDouble(4, 8.8);
            pstmt.setString(5, "English");
            pstmt.setString(6, "A thief who steals corporate secrets through dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.");
            pstmt.setString(7, "inception.png");
            pstmt.setDouble(8, 300.0);
            pstmt.executeUpdate();

            // 2. Interstellar
            pstmt.setString(1, "Interstellar");
            pstmt.setString(2, "Sci-Fi / Adventure");
            pstmt.setInt(3, 169);
            pstmt.setDouble(4, 8.7);
            pstmt.setString(5, "English");
            pstmt.setString(6, "When Earth becomes uninhabitable in the future, a farmer and ex-NASA pilot, Joseph Cooper, is tasked to pilot a spacecraft along with a team of researchers.");
            pstmt.setString(7, "interstellar.png");
            pstmt.setDouble(8, 300.0);
            pstmt.executeUpdate();

            // 3. The Dark Knight
            pstmt.setString(1, "The Dark Knight");
            pstmt.setString(2, "Action / Crime / Drama");
            pstmt.setInt(3, 152);
            pstmt.setDouble(4, 9.0);
            pstmt.setString(5, "English");
            pstmt.setString(6, "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests.");
            pstmt.setString(7, "dark_knight.png");
            pstmt.setDouble(8, 300.0);
            pstmt.executeUpdate();

            // 4. Avatar: The Way of Water
            pstmt.setString(1, "Avatar: The Way of Water");
            pstmt.setString(2, "Sci-Fi / Adventure");
            pstmt.setInt(3, 192);
            pstmt.setDouble(4, 7.6);
            pstmt.setString(5, "English");
            pstmt.setString(6, "Jake Sully lives with his newfound family formed on the extrasolar moon Pandora. Once a familiar threat returns, Jake must work with Neytiri to protect their home.");
            pstmt.setString(7, "avatar.png");
            pstmt.setDouble(8, 300.0);
            pstmt.executeUpdate();

            // 5. Oppenheimer
            pstmt.setString(1, "Oppenheimer");
            pstmt.setString(2, "Biography / Drama / History");
            pstmt.setInt(3, 180);
            pstmt.setDouble(4, 8.9);
            pstmt.setString(5, "English");
            pstmt.setString(6, "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb during World War II.");
            pstmt.setString(7, "oppenheimer.png");
            pstmt.setDouble(8, 300.0);
            pstmt.executeUpdate();

            // 6. Spider-Man: Across the Spider-Verse
            pstmt.setString(1, "Spider-Man: Across the Spider-Verse");
            pstmt.setString(2, "Animation / Action / Adventure");
            pstmt.setInt(3, 140);
            pstmt.setDouble(4, 8.7);
            pstmt.setString(5, "English");
            pstmt.setString(6, "Miles Morales catapults across the Multiverse, where he encounters a team of Spider-People charged with protecting its very existence.");
            pstmt.setString(7, "spiderman.png");
            pstmt.setDouble(8, 300.0);
            pstmt.executeUpdate();
        }

        // Insert sample Theatres
        String insertTheaterSql = """
            INSERT INTO theaters (name, location, total_rows, total_cols)
            VALUES (?, ?, ?, ?);
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(insertTheaterSql)) {
            // 1. IMAX City
            pstmt.setString(1, "IMAX City");
            pstmt.setString(2, "Downtown Metropolis");
            pstmt.setInt(3, 5);
            pstmt.setInt(4, 8);
            pstmt.executeUpdate();

            // 2. PVR Grand Cinema
            pstmt.setString(1, "PVR Grand Cinema");
            pstmt.setString(2, "Metro Mall Promenade");
            pstmt.setInt(3, 5);
            pstmt.setInt(4, 8);
            pstmt.executeUpdate();

            // 3. Cinepolis Horizon
            pstmt.setString(1, "Cinepolis Horizon");
            pstmt.setString(2, "Central Boulevard");
            pstmt.setInt(3, 5);
            pstmt.setInt(4, 8);
            pstmt.executeUpdate();
        }

        // Insert sample Showtimes
        String insertShowtimeSql = """
            INSERT INTO showtimes (movie_id, theater_id, show_date, show_time, screen_type)
            VALUES (?, ?, ?, ?, ?);
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(insertShowtimeSql)) {
            // Inception at IMAX City (Showtime 1: 07:30 PM - Required demonstration showtime!)
            pstmt.setInt(1, 1);
            pstmt.setInt(2, 1);
            pstmt.setString(3, "Today");
            pstmt.setString(4, "07:30 PM");
            pstmt.setString(5, "IMAX 3D");
            pstmt.executeUpdate();

            // Inception at IMAX City (Showtime 2: 10:15 PM)
            pstmt.setInt(1, 1);
            pstmt.setInt(2, 1);
            pstmt.setString(3, "Today");
            pstmt.setString(4, "10:15 PM");
            pstmt.setString(5, "IMAX 3D");
            pstmt.executeUpdate();

            // Inception at PVR Grand (Showtime 3: 04:30 PM)
            pstmt.setInt(1, 1);
            pstmt.setInt(2, 2);
            pstmt.setString(3, "Today");
            pstmt.setString(4, "04:30 PM");
            pstmt.setString(5, "Standard");
            pstmt.executeUpdate();

            // Interstellar at IMAX City (Showtime 4: 02:00 PM)
            pstmt.setInt(1, 2);
            pstmt.setInt(2, 1);
            pstmt.setString(3, "Today");
            pstmt.setString(4, "02:00 PM");
            pstmt.setString(5, "IMAX 3D");
            pstmt.executeUpdate();

            // The Dark Knight at Cinepolis Horizon (Showtime 5: 06:00 PM)
            pstmt.setInt(1, 3);
            pstmt.setInt(2, 3);
            pstmt.setString(3, "Today");
            pstmt.setString(4, "06:00 PM");
            pstmt.setString(5, "Dolby Atmos");
            pstmt.executeUpdate();

            // Avatar at IMAX City (Showtime 6: 08:15 PM)
            pstmt.setInt(1, 4);
            pstmt.setInt(2, 1);
            pstmt.setString(3, "Today");
            pstmt.setString(4, "08:15 PM");
            pstmt.setString(5, "IMAX 3D");
            pstmt.executeUpdate();

            // Oppenheimer at PVR Grand (Showtime 7: 01:00 PM)
            pstmt.setInt(1, 5);
            pstmt.setInt(2, 2);
            pstmt.setString(3, "Today");
            pstmt.setString(4, "01:00 PM");
            pstmt.setString(5, "Standard");
            pstmt.executeUpdate();

            // Spider-Man at Cinepolis Horizon (Showtime 8: 11:30 AM)
            pstmt.setInt(1, 6);
            pstmt.setInt(2, 3);
            pstmt.setString(3, "Today");
            pstmt.setString(4, "11:30 AM");
            pstmt.setString(5, "Standard");
            pstmt.executeUpdate();
        }

        // Initialize 5x8 Seats for each showtime
        seedSeatsForShowtimes(conn);
    }

    private static void seedSeatsForShowtimes(Connection conn) throws SQLException {
        String insertSeatSql = """
            INSERT OR IGNORE INTO seats (showtime_id, row_label, seat_num, seat_code, is_booked)
            VALUES (?, ?, ?, ?, 0);
        """;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM showtimes");
             PreparedStatement pstmt = conn.prepareStatement(insertSeatSql)) {

            while (rs.next()) {
                int showtimeId = rs.getInt("id");
                for (int r = 0; r < 5; r++) {
                    char rowChar = (char) ('A' + r);
                    for (int c = 1; c <= 8; c++) {
                        pstmt.setInt(1, showtimeId);
                        pstmt.setString(2, String.valueOf(rowChar));
                        pstmt.setInt(3, c);
                        pstmt.setString(4, String.format("%c%d", rowChar, c));
                        pstmt.addBatch();
                    }
                }
            }
            pstmt.executeBatch();
        }
    }

    private static void syncBookingIdCounter(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM bookings")) {

            int maxId = 100;
            while (rs.next()) {
                String idStr = rs.getString("id");
                if (idStr != null && idStr.startsWith("BK")) {
                    try {
                        int num = Integer.parseInt(idStr.substring(2));
                        if (num > maxId) {
                            maxId = num;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            BookingIdGenerator.initializeFromMax(maxId);
        }
    }
}
