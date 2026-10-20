package com.movieticket.ui;

import com.movieticket.model.Showtime;
import com.movieticket.service.BookingService;
import com.movieticket.service.ConcurrencyDemoService;
import com.movieticket.service.MovieService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Screen G: Live interactive demonstration of multithreading, concurrency locks,
 * and double-booking race condition prevention.
 * Satisfies Module 3 requirements.
 */
public class ConcurrencyDemoScreen extends VBox {

    private final BookingService bookingService;
    private final MovieService movieService;
    private final ConcurrencyDemoService concurrencyService;

    private ComboBox<Showtime> showtimeCombo;
    private TextField seatField;
    private TextArea terminalArea;
    private Button runDemoBtn;
    private ProgressIndicator spinner;

    public ConcurrencyDemoScreen(BookingService bookingService, MovieService movieService) {
        this.bookingService = bookingService;
        this.movieService = movieService;
        this.concurrencyService = new ConcurrencyDemoService(bookingService);

        setSpacing(20);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: transparent;");

        buildHeader();
        buildControlCard();
        buildTerminalConsole();
    }

    private void buildHeader() {
        VBox titleBox = new VBox(4);
        Label heading = UITheme.createHeaderLabel("Module 3: Concurrency & Double-Booking Prevention");
        Label subheading = UITheme.createSubheaderLabel(
                "Simulates two simultaneous Java threads attempting to book the exact same contested seat at the identical millisecond."
        );
        titleBox.getChildren().addAll(heading, subheading);
        getChildren().add(titleBox);
    }

    private void buildControlCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: #111827; -fx-background-radius: 10px; -fx-border-color: #1f293d;");

        Label cardTitle = new Label("Contention Scenario Setup");
        cardTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #ffffff;");

        HBox controls = new HBox(16);
        controls.setAlignment(Pos.CENTER_LEFT);

        // Showtime selection
        List<Showtime> showtimes = movieService.getShowtimesForMovie(1); // Default to Inception
        showtimeCombo = new ComboBox<>();
        showtimeCombo.getItems().addAll(showtimes);
        if (!showtimes.isEmpty()) {
            showtimeCombo.setValue(showtimes.get(0));
        }
        showtimeCombo.setPrefWidth(300);

        // Contested Seat input
        seatField = new TextField("C4");
        seatField.setPrefWidth(90);
        seatField.setPromptText("e.g. C4");

        runDemoBtn = new Button("⚡ Launch Concurrent Contention Test");
        runDemoBtn.getStyleClass().add("btn-primary");
        runDemoBtn.setOnAction(e -> executeConcurrencyTest());

        spinner = new ProgressIndicator();
        spinner.setPrefSize(20, 20);
        spinner.setVisible(false);

        controls.getChildren().addAll(
                new Label("Showtime:") {{ setStyle("-fx-text-fill: #94a3b8;"); }},
                showtimeCombo,
                new Label("Target Seat:") {{ setStyle("-fx-text-fill: #94a3b8;"); }},
                seatField,
                runDemoBtn,
                spinner
        );

        Label desc = new Label(
                "• Thread-1 represents Customer A (Priya)\n" +
                "• Thread-2 represents Customer B (Bob)\n" +
                "• Both threads are released concurrently via CountDownLatch to trigger a race condition on SQLite and Java locks."
        );
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        card.getChildren().addAll(cardTitle, controls, desc);
        getChildren().add(card);
    }

    private void buildTerminalConsole() {
        VBox consoleContainer = new VBox(8);
        VBox.setVgrow(consoleContainer, Priority.ALWAYS);

        Label termLabel = new Label("Live Execution Terminal Logs:");
        termLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #cbd5e1;");

        terminalArea = new TextArea();
        terminalArea.setEditable(false);
        terminalArea.getStyleClass().add("terminal-box");
        terminalArea.setPrefRowCount(14);
        terminalArea.setText(
                "=== Concurrency Test Console Ready ===\n" +
                "Click 'Launch Concurrent Contention Test' to simulate simultaneous reservations for seat C4.\n"
        );
        VBox.setVgrow(terminalArea, Priority.ALWAYS);

        consoleContainer.getChildren().addAll(termLabel, terminalArea);
        getChildren().add(consoleContainer);
    }

    private void executeConcurrencyTest() {
        Showtime st = showtimeCombo.getValue();
        if (st == null) {
            UITheme.showAlert(javafx.scene.control.Alert.AlertType.WARNING, "Selection Required", "No Showtime", "Please select a showtime.");
            return;
        }

        String targetSeat = seatField.getText().trim().toUpperCase();
        if (targetSeat.isEmpty()) {
            targetSeat = "C4";
        }

        runDemoBtn.setDisable(true);
        spinner.setVisible(true);

        terminalArea.appendText("\n[START] Initializing 2 competing threads for seat " + targetSeat + "...\n");
        final String contestedSeat = targetSeat;

        new Thread(() -> {
            ConcurrencyDemoService.ConcurrencyResult result =
                    concurrencyService.runContentionSimulation(st, contestedSeat);

            Platform.runLater(() -> {
                for (String logLine : result.executionLogs()) {
                    terminalArea.appendText(logLine + "\n");
                }

                if (result.hasRaceConditionProtected()) {
                    terminalArea.appendText("\n>>> SUCCESS: Race condition handled cleanly! Double booking was prevented.\n");
                    terminalArea.appendText(">>> Winner: " + result.winningThread() + "\n");
                    terminalArea.appendText(">>> Blocked with SeatAlreadyBookedException: " + result.losingThread() + "\n");
                } else {
                    terminalArea.appendText("\n>>> WARNING: Check lock coordination.\n");
                }

                spinner.setVisible(false);
                runDemoBtn.setDisable(false);
            });
        }).start();
    }
}
