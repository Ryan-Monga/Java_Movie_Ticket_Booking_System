package com.movieticket.ui;

import com.movieticket.model.Booking;
import com.movieticket.model.Movie;
import com.movieticket.model.Showtime;
import com.movieticket.model.TicketCategory;
import com.movieticket.service.BookingService;
import com.movieticket.service.MovieService;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Master UI Container coordinating navigation between screens.
 */
public class CinemaAppUI extends BorderPane {

    private final MovieService movieService;
    private final BookingService bookingService;

    private StackPane contentArea;
    private Button navHomeBtn;
    private Button navBookingsBtn;
    private Button navConcurrencyBtn;
    private Button navBackupBtn;
    private Button navHelpBtn;

    // Screen references
    private HomeScreen homeScreen;
    private MyBookingsScreen myBookingsScreen;
    private ConcurrencyDemoScreen concurrencyScreen;
    private DataBackupScreen backupScreen;
    private HelpScreen helpScreen;

    public CinemaAppUI(MovieService movieService, BookingService bookingService) {
        this.movieService = movieService;
        this.bookingService = bookingService;

        setStyle("-fx-background-color: #0b0f17;");

        buildTopNavBar();
        buildContentArea();

        // Default screen
        navigateToHome();
    }

    private void buildTopNavBar() {
        HBox navBar = new HBox(20);
        navBar.getStyleClass().add("nav-bar");
        navBar.setAlignment(Pos.CENTER_LEFT);

        // Cinema Logo & Title
        HBox brand = new HBox(10);
        brand.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("🎬");
        icon.setStyle("-fx-font-size: 24px;");

        VBox brandText = new VBox(0);
        Label title = new Label("CINEPASS CINEMAS");
        title.getStyleClass().add("brand-title");
        Label subtitle = new Label("ONLINE TICKET BOOKING SYSTEM");
        subtitle.getStyleClass().add("brand-subtitle");
        brandText.getChildren().addAll(title, subtitle);

        brand.getChildren().addAll(icon, brandText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Navigation Buttons
        navHomeBtn = createNavButton("🎥 Movies", this::navigateToHome);
        navBookingsBtn = createNavButton("🎟️ My Bookings", this::navigateToBookings);
        navConcurrencyBtn = createNavButton("⚡ Concurrency Test", this::navigateToConcurrency);
        navBackupBtn = createNavButton("💾 Backup & Export", this::navigateToBackup);
        navHelpBtn = createNavButton("❓ Help & Architecture", this::navigateToHelp);

        HBox navLinks = new HBox(8);
        navLinks.setAlignment(Pos.CENTER_RIGHT);
        navLinks.getChildren().addAll(navHomeBtn, navBookingsBtn, navConcurrencyBtn, navBackupBtn, navHelpBtn);

        navBar.getChildren().addAll(brand, spacer, navLinks);
        setTop(navBar);
    }

    private Button createNavButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void buildContentArea() {
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: transparent;");
        setCenter(contentArea);
    }

    private void setScreen(Node screen, Button activeNavBtn) {
        contentArea.getChildren().setAll(screen);

        // Update active nav styling
        List.of(navHomeBtn, navBookingsBtn, navConcurrencyBtn, navBackupBtn, navHelpBtn)
                .forEach(b -> {
                    b.getStyleClass().remove("nav-button-active");
                    if (b == activeNavBtn) {
                        b.getStyleClass().add("nav-button-active");
                    }
                });
    }

    public void navigateToHome() {
        homeScreen = new HomeScreen(movieService, this::navigateToMovieShowtimes);
        setScreen(homeScreen, navHomeBtn);
    }

    public void navigateToBookings() {
        if (myBookingsScreen == null) {
            myBookingsScreen = new MyBookingsScreen(bookingService, this::navigateToTicketView);
        } else {
            myBookingsScreen.refreshTable();
        }
        setScreen(myBookingsScreen, navBookingsBtn);
    }

    public void navigateToConcurrency() {
        concurrencyScreen = new ConcurrencyDemoScreen(bookingService, movieService);
        setScreen(concurrencyScreen, navConcurrencyBtn);
    }

    public void navigateToBackup() {
        backupScreen = new DataBackupScreen(bookingService);
        setScreen(backupScreen, navBackupBtn);
    }

    public void navigateToHelp() {
        helpScreen = new HelpScreen();
        setScreen(helpScreen, navHelpBtn);
    }

    private void navigateToMovieShowtimes(Movie movie) {
        ShowtimeSelectionScreen screen = new ShowtimeSelectionScreen(
                movieService,
                movie,
                this::navigateToHome,
                this::navigateToSeatSelection
        );
        setScreen(screen, navHomeBtn);
    }

    private void navigateToSeatSelection(Showtime showtime, TicketCategory category) {
        SeatSelectionScreen screen = new SeatSelectionScreen(
                movieService,
                showtime,
                category,
                () -> navigateToMovieShowtimes(showtime.getMovie()),
                (st, cat, seats, total, name, email, phone) ->
                        navigateToConfirmation(st, cat, seats, total, name, email, phone)
        );
        setScreen(screen, navHomeBtn);
    }

    private void navigateToConfirmation(Showtime showtime, TicketCategory category,
                                       List<String> seats, double totalAmount,
                                       String name, String email, String phone) {
        BookingConfirmationScreen screen = new BookingConfirmationScreen(
                bookingService,
                showtime,
                category,
                seats,
                totalAmount,
                name,
                email,
                phone,
                () -> navigateToSeatSelection(showtime, category),
                this::navigateToTicketView
        );
        setScreen(screen, navHomeBtn);
    }

    private void navigateToTicketView(Booking booking) {
        TicketScreen screen = new TicketScreen(
                booking,
                this::navigateToHome,
                this::navigateToBookings
        );
        setScreen(screen, navBookingsBtn);
    }
}
