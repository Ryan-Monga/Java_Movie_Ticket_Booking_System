package com.movieticket.service;

import com.movieticket.db.DatabaseManager;
import com.movieticket.exception.BookingException;
import com.movieticket.exception.InvalidInputException;
import com.movieticket.exception.SeatAlreadyBookedException;
import com.movieticket.model.Booking;
import com.movieticket.model.BookingStatus;
import com.movieticket.model.Movie;
import com.movieticket.model.Showtime;
import com.movieticket.model.Theater;
import com.movieticket.model.Ticket;
import com.movieticket.model.TicketCategory;
import com.movieticket.payment.Payable;
import com.movieticket.payment.SimulatedPayment;
import com.movieticket.util.BookingIdGenerator;
import com.movieticket.util.PriceCalculator;
import com.movieticket.util.ValidationUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Core business service handling ticket booking, seat reservation,
 * concurrency locking, database transactions, and cancellations.
 * Satisfies Modules 1, 2, 3, 4, and 5 requirements.
 */
public class BookingService {

    private final MovieService movieService;
    private final Payable paymentGateway;

    // Concurrency control: Fine-grained locks per showtime ID (Module 3 requirement)
    private final ConcurrentHashMap<Integer, ReentrantLock> showtimeLocks = new ConcurrentHashMap<>();

    // Global in-memory cache for fast display & search
    private final List<Booking> bookingCache = new ArrayList<>();
    private final Map<String, Booking> bookingMap = new HashMap<>();

    public BookingService(MovieService movieService, Payable paymentGateway) {
        this.movieService = movieService != null ? movieService : new MovieService();
        this.paymentGateway = paymentGateway != null ? paymentGateway : new SimulatedPayment();
        refreshBookingsFromDatabase();
    }

    public BookingService() {
        this(new MovieService(), new SimulatedPayment());
    }

    private ReentrantLock getLockForShowtime(int showtimeId) {
        return showtimeLocks.computeIfAbsent(showtimeId, id -> new ReentrantLock(true));
    }

    /**
     * Atomically books seats for a given showtime.
     * Prevents double-booking using thread synchronization and ACID database transactions.
     */
    public Booking bookTickets(String customerName, String customerEmail, String customerPhone,
                               Showtime showtime, TicketCategory category, List<String> seatCodes)
            throws InvalidInputException, SeatAlreadyBookedException, BookingException {

        // 1. Validate inputs
        ValidationUtils.validateCustomerDetails(customerName, customerEmail, customerPhone);
        ValidationUtils.validateSeatSelection(seatCodes);
        if (showtime == null) {
            throw new InvalidInputException("A valid showtime must be selected.");
        }

        // Use HashSet to ensure no duplicate seat selections in request (Module 4 Collections requirement)
        Set<String> uniqueSeats = new HashSet<>(seatCodes);
        if (uniqueSeats.size() != seatCodes.size()) {
            throw new InvalidInputException("Duplicate seats found in your selection.");
        }

        double basePrice = showtime.getMovie() != null ? showtime.getMovie().getBasePrice() : 300.0;
        double totalAmount = PriceCalculator.calculateTotal(category, basePrice, uniqueSeats.size());

        // 2. Acquire lock for this showtime to serialize concurrent reservation requests
        ReentrantLock lock = getLockForShowtime(showtime.getId());
        lock.lock();

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Begin ACID Transaction

            // 3. Atomically verify that NONE of the seats are already booked in the DB
            String checkSeatSql = "SELECT seat_code FROM booking_seats WHERE showtime_id = ? AND seat_code = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSeatSql)) {
                checkStmt.setInt(1, showtime.getId());
                for (String seatCode : uniqueSeats) {
                    checkStmt.setString(2, seatCode.toUpperCase());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            // Seat already booked! Roll back and throw checked exception
                            conn.rollback();
                            throw new SeatAlreadyBookedException(seatCode.toUpperCase(), showtime.getId());
                        }
                    }
                }
            }

            // 4. Perform Simulated Payment
            boolean paymentSuccess = paymentGateway.pay(totalAmount);
            if (!paymentSuccess) {
                conn.rollback();
                throw new BookingException("Payment authorization failed: " + paymentGateway.getTransactionDetails());
            }

            // 5. Generate Unique Booking ID (e.g. BK101)
            String bookingId = BookingIdGenerator.getNextId();

            // 6. Insert into bookings table
            String insertBookingSql = """
                INSERT INTO bookings (id, customer_name, customer_email, customer_phone, showtime_id,
                                      ticket_category, total_amount, booking_time, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, datetime('now', 'localtime'), ?);
            """;
            try (PreparedStatement bStmt = conn.prepareStatement(insertBookingSql)) {
                bStmt.setString(1, bookingId);
                bStmt.setString(2, customerName.trim());
                bStmt.setString(3, customerEmail.trim());
                bStmt.setString(4, customerPhone.trim());
                bStmt.setInt(5, showtime.getId());
                bStmt.setString(6, category.name());
                bStmt.setDouble(7, totalAmount);
                bStmt.setString(8, BookingStatus.CONFIRMED.name());
                bStmt.executeUpdate();
            }

            // 7. Insert each seat into booking_seats and update physical seats table
            String insertBookingSeatSql = """
                INSERT INTO booking_seats (booking_id, showtime_id, seat_code)
                VALUES (?, ?, ?);
            """;
            String updateSeatSql = """
                UPDATE seats SET is_booked = 1, booking_id = ?
                WHERE showtime_id = ? AND seat_code = ?;
            """;

            try (PreparedStatement bsStmt = conn.prepareStatement(insertBookingSeatSql);
                 PreparedStatement sStmt = conn.prepareStatement(updateSeatSql)) {

                for (String seatCode : uniqueSeats) {
                    String cleanCode = seatCode.toUpperCase();

                    bsStmt.setString(1, bookingId);
                    bsStmt.setInt(2, showtime.getId());
                    bsStmt.setString(3, cleanCode);
                    bsStmt.executeUpdate();

                    sStmt.setString(1, bookingId);
                    sStmt.setInt(2, showtime.getId());
                    sStmt.setString(3, cleanCode);
                    sStmt.executeUpdate();
                }
            }

            // 8. Commit ACID transaction
            conn.commit();

            // 9. Update in-memory 2D seat grid in showtime object
            for (String seatCode : uniqueSeats) {
                showtime.markSeatBooked(seatCode);
            }

            // 10. Construct polymorphic Ticket instances (Module 2 requirement)
            List<String> sortedSeats = new ArrayList<>(uniqueSeats);
            Collections.sort(sortedSeats);
            List<Ticket> tickets = PriceCalculator.createTickets(
                    category,
                    bookingId,
                    showtime.getMovie() != null ? showtime.getMovie().getTitle() : "Movie",
                    showtime.getTheater() != null ? showtime.getTheater().getName() : "Theater",
                    showtime.getShowTime(),
                    sortedSeats,
                    basePrice
            );

            Booking newBooking = new Booking(
                    bookingId,
                    customerName.trim(),
                    customerEmail.trim(),
                    customerPhone.trim(),
                    showtime,
                    category,
                    sortedSeats,
                    tickets,
                    totalAmount,
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    BookingStatus.CONFIRMED
            );

            // Update in-memory cache
            synchronized (bookingCache) {
                bookingCache.add(0, newBooking); // Add at top
                bookingMap.put(bookingId, newBooking);
            }

            return newBooking;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            // Check if SQLite unique constraint on seats was triggered
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
                if (e.getMessage().contains("booking_seats") || e.getMessage().contains("seats")) {
                    throw new SeatAlreadyBookedException("One or more selected seats were just taken by another user.");
                }
            }
            throw new BookingException("Database transaction failed during booking: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
            lock.unlock();
        }
    }

    /**
     * Cancels an existing booking and atomically releases its seats.
     */
    public boolean cancelBooking(String bookingId) throws BookingException {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            throw new BookingException("Invalid booking ID for cancellation.");
        }

        Booking booking = getBookingById(bookingId);
        if (booking == null) {
            throw new BookingException("Booking not found: " + bookingId);
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException("Booking " + bookingId + " is already cancelled.");
        }

        int showtimeId = booking.getShowtime() != null ? booking.getShowtime().getId() : -1;
        ReentrantLock lock = showtimeId > 0 ? getLockForShowtime(showtimeId) : new ReentrantLock();
        lock.lock();

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // 1. Update booking status to CANCELLED
            String updateBookingSql = "UPDATE bookings SET status = 'CANCELLED' WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateBookingSql)) {
                pstmt.setString(1, bookingId);
                pstmt.executeUpdate();
            }

            // 2. Remove seats from booking_seats to free them
            String deleteBookingSeatsSql = "DELETE FROM booking_seats WHERE booking_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteBookingSeatsSql)) {
                pstmt.setString(1, bookingId);
                pstmt.executeUpdate();
            }

            // 3. Mark physical seats as available in seats table
            String freeSeatsSql = "UPDATE seats SET is_booked = 0, booking_id = NULL WHERE booking_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(freeSeatsSql)) {
                pstmt.setString(1, bookingId);
                pstmt.executeUpdate();
            }

            // 4. Commit transaction
            conn.commit();

            // 5. Process refund via Payable
            paymentGateway.refund(booking.getTotalAmount());

            // 6. Update in-memory state
            booking.setStatus(BookingStatus.CANCELLED);
            if (booking.getShowtime() != null) {
                for (String seatCode : booking.getSeatCodes()) {
                    booking.getShowtime().markSeatAvailable(seatCode);
                }
            }

            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback error: " + ex.getMessage());
                }
            }
            throw new BookingException("Failed to cancel booking " + bookingId + ": " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
            lock.unlock();
        }
    }

    /**
     * Clears all booking records from the database and releases all seats across all showtimes.
     * Resets the booking ID generator counter.
     */
    public synchronized boolean clearAllBookings() throws BookingException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM booking_seats;");
                stmt.execute("DELETE FROM bookings;");
                stmt.execute("UPDATE seats SET is_booked = 0, booking_id = NULL;");
            }
            conn.commit();

            // Refresh caches and reset ID counter
            movieService.refreshCache();
            refreshBookingsFromDatabase();
            BookingIdGenerator.reset(101);
            return true;
        } catch (SQLException e) {
            throw new BookingException("Failed to clear bookings from database: " + e.getMessage(), e);
        }
    }

    /**
     * Refreshes booking records from SQLite database into collections.
     */
    public synchronized void refreshBookingsFromDatabase() {
        synchronized (bookingCache) {
            bookingCache.clear();
            bookingMap.clear();

            String sql = """
                SELECT b.*, s.movie_id, s.theater_id, s.show_date, s.show_time, s.screen_type,
                       t.name as theater_name, t.location, t.total_rows, t.total_cols,
                       m.title as movie_title, m.genre, m.duration_minutes, m.rating, m.base_price
                FROM bookings b
                JOIN showtimes s ON b.showtime_id = s.id
                JOIN theaters t ON s.theater_id = t.id
                JOIN movies m ON s.movie_id = m.id
                ORDER BY b.booking_time DESC
            """;

            try (Connection conn = DatabaseManager.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    String bookingId = rs.getString("id");
                    String customerName = rs.getString("customer_name");
                    String customerEmail = rs.getString("customer_email");
                    String customerPhone = rs.getString("customer_phone");
                    int showtimeId = rs.getInt("showtime_id");
                    String ticketCatStr = rs.getString("ticket_category");
                    double totalAmount = rs.getDouble("total_amount");
                    String bookingTime = rs.getString("booking_time");
                    String statusStr = rs.getString("status");

                    Movie movie = new Movie(
                            rs.getInt("movie_id"),
                            rs.getString("movie_title"),
                            rs.getString("genre"),
                            rs.getInt("duration_minutes"),
                            rs.getDouble("rating"),
                            rs.getDouble("base_price")
                    );

                    Theater theater = new Theater(
                            rs.getInt("theater_id"),
                            rs.getString("theater_name"),
                            rs.getString("location"),
                            rs.getInt("total_rows"),
                            rs.getInt("total_cols")
                    );

                    Showtime showtime = new Showtime(
                            showtimeId,
                            movie,
                            theater,
                            rs.getString("show_date"),
                            rs.getString("show_time"),
                            rs.getString("screen_type")
                    );

                    TicketCategory category = TicketCategory.REGULAR;
                    try {
                        category = TicketCategory.valueOf(ticketCatStr);
                    } catch (Exception ignored) {}

                    BookingStatus status = BookingStatus.CONFIRMED;
                    try {
                        status = BookingStatus.valueOf(statusStr);
                    } catch (Exception ignored) {}

                    // Fetch seats for this booking
                    List<String> seats = getSeatsForBooking(conn, bookingId);
                    List<Ticket> tickets = PriceCalculator.createTickets(
                            category, bookingId, movie.getTitle(), theater.getName(),
                            showtime.getShowTime(), seats, movie.getBasePrice()
                    );

                    Booking booking = new Booking(
                            bookingId, customerName, customerEmail, customerPhone,
                            showtime, category, seats, tickets, totalAmount, bookingTime, status
                    );

                    bookingCache.add(booking);
                    bookingMap.put(bookingId, booking);
                }
            } catch (SQLException e) {
                System.err.println("Error reading bookings from DB: " + e.getMessage());
            }
        }
    }

    private List<String> getSeatsForBooking(Connection conn, String bookingId) {
        List<String> seats = new ArrayList<>();
        String sql = "SELECT seat_code FROM booking_seats WHERE booking_id = ? ORDER BY seat_code";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    seats.add(rs.getString("seat_code"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching seats for booking " + bookingId + ": " + e.getMessage());
        }
        return seats;
    }

    public List<Booking> getAllBookings() {
        synchronized (bookingCache) {
            return new ArrayList<>(bookingCache);
        }
    }

    public Booking getBookingById(String bookingId) {
        if (bookingId == null) return null;
        synchronized (bookingCache) {
            return bookingMap.get(bookingId);
        }
    }

    /**
     * Searches bookings by booking ID or customer name using Java Streams (Module 4).
     */
    public List<Booking> searchBookings(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllBookings();
        }
        String lowerQuery = query.trim().toLowerCase();
        synchronized (bookingCache) {
            return bookingCache.stream()
                    .filter(b -> b.getBookingId().toLowerCase().contains(lowerQuery)
                              || b.getCustomerName().toLowerCase().contains(lowerQuery)
                              || (b.getShowtime() != null && b.getShowtime().getMovie() != null
                                  && b.getShowtime().getMovie().getTitle().toLowerCase().contains(lowerQuery)))
                    .collect(Collectors.toList());
        }
    }

    public MovieService getMovieService() {
        return movieService;
    }

    public Payable getPaymentGateway() {
        return paymentGateway;
    }
}
