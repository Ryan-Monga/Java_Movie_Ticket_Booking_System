package com.movieticket.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Screen I: Academic architecture guide and syllabus mapping.
 */
public class HelpScreen extends VBox {

    public HelpScreen() {
        setSpacing(16);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: transparent;");

        buildLayout();
    }

    private void buildLayout() {
        VBox titleBox = new VBox(4);
        Label heading = UITheme.createHeaderLabel("System Architecture & Academic Module Reference");
        Label subheading = UITheme.createSubheaderLabel(
                "Complete mapping of project implementation to core Java academic syllabus modules"
        );
        titleBox.getChildren().addAll(heading, subheading);

        VBox content = new VBox(14);
        content.getChildren().addAll(
                createModuleCard(
                        "Module 1: Java Fundamentals",
                        "• 2D Arrays: Implemented in Showtime.java (boolean[][] seatGrid and Seat[][] seatingMatrix for 5x8 seating layout).\n" +
                        "• Enhanced For-Loops: Used to iterate movie lists, showtime seats, bookings, and 2D arrays.\n" +
                        "• String Handling: Regex input validation in ValidationUtils.java, case-insensitive partial searches, formatted ticket IDs.\n" +
                        "• Control Flow: Modern Java switch expressions in PriceCalculator.java, input guards, and ternary expressions."
                ),
                createModuleCard(
                        "Module 2: Object-Oriented Programming (OOP)",
                        "• Classes & Objects: Movie, Theater, Showtime, Seat, Ticket, Booking with full encapsulation.\n" +
                        "• Nested Class: Showtime.ShowtimeSeat demonstrated as a nested domain class.\n" +
                        "• Inheritance & Abstract Classes: abstract class Ticket extended by RegularTicket, PremiumTicket, and IMAXTicket.\n" +
                        "• Runtime Polymorphism: Dynamic method dispatch on Ticket.calculatePrice() across categories.\n" +
                        "• Interfaces: Payable interface implemented by SimulatedPayment with pay() and refund().\n" +
                        "• Static ID Generator: BookingIdGenerator with atomic monotonic sequence across restarts."
                ),
                createModuleCard(
                        "Module 3: Exceptions & Multithreading",
                        "• Custom Checked Exception: SeatAlreadyBookedException thrown when duplicate seat reservations occur.\n" +
                        "• Concurrency & Race Conditions: Fine-grained ReentrantLock per showtime and SQLite ACID transactions.\n" +
                        "• Double Booking Prevention: Validated with CountDownLatch concurrent tests in ConcurrencyBookingTest & ConcurrencyDemoScreen.\n" +
                        "• Thread Safety: AtomicInteger, ConcurrentHashMap, and synchronized collections."
                ),
                createModuleCard(
                        "Module 4: Files, Streams, Serialization & Collections",
                        "• Serialization: ObjectOutputStream and ObjectInputStream in SerializationService.java exporting to 'exports/bookings.dat'.\n" +
                        "• File I/O: BufferedWriter and FileWriter with try-with-resources generating formatted 'exports/ticket_BK101.txt'.\n" +
                        "• Collections Framework: ArrayList for movie/booking lists, HashMap for ID lookups, HashSet for unique seat selections.\n" +
                        "• Java Streams & Lambdas: filterByGenre(), searchMovies(), searchBookings(), and Comparator sorting."
                ),
                createModuleCard(
                        "Module 5: JavaFX Desktop GUI & JDBC Persistence",
                        "• JavaFX Controls: GridPane for seating layout, TableView for booking history, FlowPane cards, ComboBoxes, and CSS styling.\n" +
                        "• SQLite JDBC: DatabaseManager with foreign key PRAGMA, PreparedStatement queries, and ACID transactions.\n" +
                        "• Single Source of Truth: SQLite database acts as authoritative state; collections refresh after mutations."
                )
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(titleBox, scroll);
    }

    private VBox createModuleCard(String title, String details) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: #111827; -fx-background-radius: 10px; -fx-border-color: #1f293d;");

        Label cardTitle = new Label(title);
        cardTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #f59e0b;");

        Label cardBody = new Label(details);
        cardBody.setStyle("-fx-font-size: 13px; -fx-text-fill: #cbd5e1; -fx-line-spacing: 4px;");
        cardBody.setWrapText(true);

        card.getChildren().addAll(cardTitle, cardBody);
        return card;
    }
}
