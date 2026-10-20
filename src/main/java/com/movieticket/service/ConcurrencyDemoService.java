package com.movieticket.service;

import com.movieticket.exception.SeatAlreadyBookedException;
import com.movieticket.model.Booking;
import com.movieticket.model.Showtime;
import com.movieticket.model.TicketCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service specifically demonstrating multi-threaded race conditions,
 * synchronization locks, and double-booking prevention under concurrent load.
 * Satisfies Module 3 Concurrency requirements.
 */
public class ConcurrencyDemoService {

    public record ConcurrencyResult(
            String contestedSeat,
            String winningThread,
            String losingThread,
            List<String> executionLogs,
            boolean hasRaceConditionProtected
    ) {}

    private final BookingService bookingService;

    public ConcurrencyDemoService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Executes two simultaneous threads attempting to book the exact same seat (e.g. C4).
     * Uses a CountDownLatch so both threads start at the exact same instant.
     */
    public ConcurrencyResult runContentionSimulation(Showtime showtime, String targetSeat) {
        String seatToContest = (targetSeat != null && !targetSeat.trim().isEmpty()) ? targetSeat.trim().toUpperCase() : "C4";
        List<String> logs = Collections.synchronizedList(new ArrayList<>());

        AtomicReference<String> winner = new AtomicReference<>(null);
        AtomicReference<String> loser = new AtomicReference<>(null);

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finishGate = new CountDownLatch(2);

        // Thread 1: User Alice / Priya attempting to book seatToContest
        Thread thread1 = new Thread(() -> {
            try {
                startGate.await(); // Synchronize release
                logs.add("[Thread-1] Initiating booking attempt for seat " + seatToContest + "...");
                Booking b = bookingService.bookTickets(
                        "Priya Sharma",
                        "priya@cinepass.com",
                        "9876543210",
                        showtime,
                        TicketCategory.REGULAR,
                        List.of(seatToContest)
                );
                winner.compareAndSet(null, "Thread-1");
                logs.add("Thread-1: Successfully booked seat " + seatToContest + " [Booking ID: " + b.getBookingId() + "]");
            } catch (SeatAlreadyBookedException e) {
                loser.set("Thread-1");
                logs.add("Thread-1: FAILED - SeatAlreadyBookedException: " + e.getMessage());
            } catch (Exception e) {
                logs.add("Thread-1: FAILED - " + e.getClass().getSimpleName() + ": " + e.getMessage());
            } finally {
                finishGate.countDown();
            }
        }, "BookingThread-1");

        // Thread 2: User Bob attempting to book the EXACT SAME seatToContest
        Thread thread2 = new Thread(() -> {
            try {
                startGate.await(); // Synchronize release
                logs.add("[Thread-2] Initiating booking attempt for seat " + seatToContest + "...");
                Booking b = bookingService.bookTickets(
                        "Bob Johnson",
                        "bob@cinepass.com",
                        "9123456789",
                        showtime,
                        TicketCategory.REGULAR,
                        List.of(seatToContest)
                );
                winner.compareAndSet(null, "Thread-2");
                logs.add("Thread-2: Successfully booked seat " + seatToContest + " [Booking ID: " + b.getBookingId() + "]");
            } catch (SeatAlreadyBookedException e) {
                loser.set("Thread-2");
                logs.add("Thread-2: FAILED - SeatAlreadyBookedException: " + e.getMessage());
            } catch (Exception e) {
                logs.add("Thread-2: FAILED - " + e.getClass().getSimpleName() + ": " + e.getMessage());
            } finally {
                finishGate.countDown();
            }
        }, "BookingThread-2");

        thread1.start();
        thread2.start();

        // Release both threads simultaneously
        startGate.countDown();

        try {
            finishGate.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean protectedAgainstDoubleBooking = winner.get() != null && loser.get() != null;
        return new ConcurrencyResult(
                seatToContest,
                winner.get(),
                loser.get(),
                new ArrayList<>(logs),
                protectedAgainstDoubleBooking
        );
    }
}
