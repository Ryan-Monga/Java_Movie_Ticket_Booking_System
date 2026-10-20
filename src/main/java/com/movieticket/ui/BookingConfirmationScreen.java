package com.movieticket.ui;

import com.movieticket.exception.SeatAlreadyBookedException;
import com.movieticket.model.Booking;
import com.movieticket.model.Showtime;
import com.movieticket.model.TicketCategory;
import com.movieticket.service.BookingService;
import com.movieticket.util.BookingIdGenerator;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * Screen D: Booking Confirmation and Simulated Payment Gateway.
 * Satisfies Screen D requirements.
 */
public class BookingConfirmationScreen extends VBox {

    private final BookingService bookingService;
    private final Showtime showtime;
    private final TicketCategory category;
    private final List<String> seatCodes;
    private final double totalAmount;
    private final String customerName;
    private final String customerEmail;
    private final String customerPhone;
    private final Runnable onBack;
    private final Consumer<Booking> onBookingSuccessful;

    private Button confirmPayBtn;
    private ProgressIndicator spinner;
    private Label errorStatusLabel;

    public BookingConfirmationScreen(BookingService bookingService,
                                     Showtime showtime,
                                     TicketCategory category,
                                     List<String> seatCodes,
                                     double totalAmount,
                                     String customerName,
                                     String customerEmail,
                                     String customerPhone,
                                     Runnable onBack,
                                     Consumer<Booking> onBookingSuccessful) {
        this.bookingService = bookingService;
        this.showtime = showtime;
        this.category = category;
        this.seatCodes = seatCodes;
        this.totalAmount = totalAmount;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.onBack = onBack;
        this.onBookingSuccessful = onBookingSuccessful;

        setSpacing(20);
        setPadding(new Insets(24));
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: transparent;");

        buildLayout();
    }

    private void buildLayout() {
        VBox card = new VBox(16);
        card.setMaxWidth(620);
        card.setPadding(new Insets(28));
        card.setStyle("-fx-background-color: #111827; -fx-background-radius: 14px; -fx-border-color: #1f293d; -fx-border-width: 1px;");

        // Header
        Label title = UITheme.createHeaderLabel("Review & Confirm Booking");
        Label subtitle = UITheme.createSubheaderLabel("Please review your cinema ticket reservation details before confirming payment");
        card.getChildren().addAll(title, subtitle, new Separator());

        // Booking ID Preview
        String previewId = "BK" + BookingIdGenerator.getCurrentCounter();
        HBox idRow = createSummaryRow("Booking ID (Pending):", previewId, "#f59e0b", true);

        // Details
        String movieTitle = showtime.getMovie() != null ? showtime.getMovie().getTitle() : "Movie";
        String theaterName = showtime.getTheater() != null ? showtime.getTheater().getName() : "Theater";

        HBox movieRow = createSummaryRow("Movie Title:", movieTitle, "#ffffff", true);
        HBox theaterRow = createSummaryRow("Cinema & Screen:", theaterName + " (" + showtime.getScreenType() + ")", "#ffffff", false);
        HBox timeRow = createSummaryRow("Date & Showtime:", showtime.getShowDate() + " at " + showtime.getShowTime(), "#ffffff", false);
        HBox seatsRow = createSummaryRow("Reserved Seats:", String.join(", ", seatCodes), "#f59e0b", true);
        HBox catRow = createSummaryRow("Ticket Category:", category.getDisplayName() + " Tier", "#ffffff", false);
        HBox custRow = createSummaryRow("Customer Name:", customerName, "#ffffff", false);
        HBox contactRow = createSummaryRow("Contact Info:", customerEmail + " | " + customerPhone, "#94a3b8", false);

        HBox totalRow = createSummaryRow("Total Payable Amount:", String.format("₹%.2f", totalAmount), "#f59e0b", true);
        totalRow.setStyle("-fx-padding: 8px 0; -fx-border-color: #1f293d; -fx-border-width: 1px 0 0 0;");

        card.getChildren().addAll(idRow, movieRow, theaterRow, timeRow, seatsRow, catRow, custRow, contactRow, totalRow);

        // Simulated Payment Selector
        VBox paymentBox = new VBox(8);
        paymentBox.setPadding(new Insets(12));
        paymentBox.setStyle("-fx-background-color: #131b2e; -fx-background-radius: 8px;");

        Label payLabel = new Label("Select Simulated Payment Channel (No Real Money Charged):");
        payLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #94a3b8;");

        ToggleGroup payGroup = new ToggleGroup();
        RadioButton cardRadio = new RadioButton("Simulated Credit / Debit Card (Instant Approval)");
        cardRadio.setStyle("-fx-text-fill: #ffffff;");
        cardRadio.setToggleGroup(payGroup);
        cardRadio.setSelected(true);

        RadioButton upiRadio = new RadioButton("Simulated UPI / QR Code (Mock Payment)");
        upiRadio.setStyle("-fx-text-fill: #ffffff;");
        upiRadio.setToggleGroup(payGroup);

        paymentBox.getChildren().addAll(payLabel, cardRadio, upiRadio);
        card.getChildren().add(paymentBox);

        // Error message label
        errorStatusLabel = new Label();
        errorStatusLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #ef4444;");
        errorStatusLabel.setWrapText(true);
        card.getChildren().add(errorStatusLabel);

        // Action Buttons Row
        HBox actionsRow = new HBox(16);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);

        Button backBtn = new Button("← Modify Seats");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setOnAction(e -> onBack.run());

        confirmPayBtn = new Button("Pay ₹" + String.format("%.0f", totalAmount) + " & Confirm Booking ✓");
        confirmPayBtn.getStyleClass().add("btn-primary");
        confirmPayBtn.setPrefHeight(42);
        confirmPayBtn.setOnAction(e -> executeBooking());

        spinner = new ProgressIndicator();
        spinner.setPrefSize(24, 24);
        spinner.setVisible(false);

        actionsRow.getChildren().addAll(backBtn, spinner, confirmPayBtn);
        card.getChildren().add(actionsRow);

        getChildren().add(card);
    }

    private HBox createSummaryRow(String label, String value, String valueColor, boolean bold) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        lbl.setMinWidth(170);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label val = new Label(value);
        val.setStyle(String.format("-fx-font-size: 13px; -fx-text-fill: %s; %s",
                valueColor, bold ? "-fx-font-weight: 800;" : "-fx-font-weight: 500;"));

        row.getChildren().addAll(lbl, spacer, val);
        return row;
    }

    private void executeBooking() {
        errorStatusLabel.setText("");
        confirmPayBtn.setDisable(true);
        spinner.setVisible(true);

        // Execute in background thread so UI doesn't freeze
        new Thread(() -> {
            try {
                // Short simulation pause for realism
                Thread.sleep(600);

                Booking booking = bookingService.bookTickets(
                        customerName,
                        customerEmail,
                        customerPhone,
                        showtime,
                        category,
                        seatCodes
                );

                Platform.runLater(() -> {
                    spinner.setVisible(false);
                    onBookingSuccessful.accept(booking);
                });

            } catch (SeatAlreadyBookedException e) {
                Platform.runLater(() -> {
                    spinner.setVisible(false);
                    confirmPayBtn.setDisable(false);
                    errorStatusLabel.setText("Reservation Failed: " + e.getMessage());
                    UITheme.showAlert(Alert.AlertType.ERROR, "Seat Already Booked",
                            "Double Booking Prevented",
                            "One or more selected seats were reserved by another patron. Please select different seats.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    spinner.setVisible(false);
                    confirmPayBtn.setDisable(false);
                    errorStatusLabel.setText("Booking Error: " + e.getMessage());
                    UITheme.showAlert(Alert.AlertType.ERROR, "Booking Error", "Transaction Error", e.getMessage());
                });
            }
        }).start();
    }
}
