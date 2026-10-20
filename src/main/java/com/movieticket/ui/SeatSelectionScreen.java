package com.movieticket.ui;

import com.movieticket.model.Seat;
import com.movieticket.model.Showtime;
import com.movieticket.model.TicketCategory;
import com.movieticket.service.MovieService;
import com.movieticket.util.PriceCalculator;
import com.movieticket.util.ValidationUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Screen C: Visual 5x8 Seat Selection Grid with dynamic price calculation
 * and customer contact details form.
 * Satisfies Screen C requirements and Module 1 2D array representation.
 */
public class SeatSelectionScreen extends VBox {

    public interface ConfirmationCallback {
        void onProceed(Showtime showtime, TicketCategory category,
                       List<String> selectedSeats, double totalAmount,
                       String customerName, String customerEmail, String customerPhone);
    }

    private final MovieService movieService;
    private final Showtime showtime;
    private final TicketCategory category;
    private final Runnable onBack;
    private final ConfirmationCallback onProceed;

    // Set of selected seat codes (e.g. "C4", "C5")
    private final Set<String> selectedSeats = new HashSet<>();

    // UI components
    private GridPane gridPane;
    private Label selectedSeatsLabel;
    private Label seatCountLabel;
    private Label totalAmountLabel;
    private TextField nameField;
    private TextField emailField;
    private TextField phoneField;
    private Label validationMessageLabel;
    private Button confirmBtn;

    public SeatSelectionScreen(MovieService movieService, Showtime showtime,
                               TicketCategory category, Runnable onBack,
                               ConfirmationCallback onProceed) {
        this.movieService = movieService;
        this.showtime = showtime;
        this.category = category != null ? category : TicketCategory.REGULAR;
        this.onBack = onBack;
        this.onProceed = onProceed;

        setSpacing(20);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: transparent;");

        buildHeader();
        buildMainLayout();
    }

    private void buildHeader() {
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setOnAction(e -> onBack.run());

        VBox titleBox = new VBox(2);
        String movieTitle = showtime.getMovie() != null ? showtime.getMovie().getTitle() : "Movie";
        String theaterName = showtime.getTheater() != null ? showtime.getTheater().getName() : "Theater";
        Label title = UITheme.createHeaderLabel(movieTitle + " — Select Seats");
        Label subtitle = UITheme.createSubheaderLabel(String.format("%s | %s (%s) | %s Tier",
                theaterName, showtime.getShowTime(), showtime.getShowDate(), category.getDisplayName()));
        titleBox.getChildren().addAll(title, subtitle);

        topBar.getChildren().addAll(backBtn, titleBox);
        getChildren().add(topBar);
    }

    private void buildMainLayout() {
        HBox contentBox = new HBox(28);
        contentBox.setAlignment(Pos.TOP_LEFT);

        // Left Container: Screen Curve + 5x8 Grid + Legend
        VBox seatingArea = new VBox(16);
        seatingArea.setAlignment(Pos.CENTER);
        seatingArea.setPadding(new Insets(20));
        seatingArea.setStyle("-fx-background-color: #111827; -fx-background-radius: 12px; -fx-border-color: #1f293d;");
        seatingArea.setPrefWidth(560);

        // Cinema Screen Graphic
        VBox screenBox = new VBox(4);
        screenBox.getStyleClass().add("cinema-screen-indicator");
        screenBox.setPrefWidth(480);
        Label screenLbl = new Label("ALL EYES THIS WAY — CINEMA SCREEN");
        screenLbl.getStyleClass().add("screen-text");
        screenBox.getChildren().add(screenLbl);

        // 5x8 Interactive GridPane
        gridPane = buildSeatingGrid();

        // Legend
        HBox legendBox = buildLegend();

        seatingArea.getChildren().addAll(screenBox, gridPane, legendBox);

        // Right Container: Dynamic Price Summary + Customer Details Form
        VBox sidebar = buildSidebar();
        HBox.setHgrow(sidebar, Priority.ALWAYS);

        contentBox.getChildren().addAll(seatingArea, sidebar);
        getChildren().add(contentBox);
    }

    private GridPane buildSeatingGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        int rows = 5;
        int cols = 8;

        // Top Column Labels (1 to 8)
        for (int c = 1; c <= cols; c++) {
            Label colLbl = new Label(String.valueOf(c));
            colLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #64748b;");
            colLbl.setMinWidth(42);
            colLbl.setAlignment(Pos.CENTER);
            grid.add(colLbl, c, 0);
        }

        // Rows A to E
        for (int r = 0; r < rows; r++) {
            char rowChar = (char) ('A' + r);

            // Left Row Label
            Label leftRowLbl = new Label(String.valueOf(rowChar));
            leftRowLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 800; -fx-text-fill: #f59e0b;");
            leftRowLbl.setMinWidth(24);
            leftRowLbl.setAlignment(Pos.CENTER);
            grid.add(leftRowLbl, 0, r + 1);

            for (int c = 1; c <= cols; c++) {
                String seatCode = String.format("%c%d", rowChar, c);
                boolean isBooked = showtime.isSeatBooked(seatCode);

                Button seatBtn = new Button(seatCode);
                seatBtn.getStyleClass().add("seat-button");

                if (isBooked) {
                    seatBtn.getStyleClass().add("seat-booked");
                    seatBtn.setDisable(true);
                    seatBtn.setTooltip(new Tooltip("Seat " + seatCode + " is already booked."));
                } else {
                    seatBtn.getStyleClass().add("seat-available");
                    seatBtn.setTooltip(new Tooltip("Seat " + seatCode + " available - Click to select."));

                    seatBtn.setOnAction(e -> {
                        if (selectedSeats.contains(seatCode)) {
                            selectedSeats.remove(seatCode);
                            seatBtn.getStyleClass().remove("seat-selected");
                            seatBtn.getStyleClass().add("seat-available");
                        } else {
                            selectedSeats.add(seatCode);
                            seatBtn.getStyleClass().remove("seat-available");
                            seatBtn.getStyleClass().add("seat-selected");
                        }
                        updateSummaryPanel();
                    });
                }

                grid.add(seatBtn, c, r + 1);
            }

            // Right Row Label
            Label rightRowLbl = new Label(String.valueOf(rowChar));
            rightRowLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 800; -fx-text-fill: #f59e0b;");
            rightRowLbl.setMinWidth(24);
            rightRowLbl.setAlignment(Pos.CENTER);
            grid.add(rightRowLbl, cols + 1, r + 1);
        }

        return grid;
    }

    private HBox buildLegend() {
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER);
        legend.setPadding(new Insets(12, 0, 0, 0));

        HBox avItem = createLegendItem("Available", "seat-available");
        HBox selItem = createLegendItem("Selected", "seat-selected");
        HBox bkdItem = createLegendItem("Booked", "seat-booked");

        legend.getChildren().addAll(avItem, selItem, bkdItem);
        return legend;
    }

    private HBox createLegendItem(String labelText, String styleClass) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER);
        Region sample = new Region();
        sample.setPrefSize(16, 16);
        sample.getStyleClass().addAll("seat-button", styleClass);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-font-weight: 600;");
        box.getChildren().addAll(sample, lbl);
        return box;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(16);
        sidebar.getStyleClass().add("sidebar-panel");

        Label summaryHeader = new Label("Booking & Price Breakdown");
        summaryHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #ffffff;");

        // Selected Seats Display
        VBox seatsBox = new VBox(4);
        Label sTitle = new Label("Selected Seats:");
        sTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        selectedSeatsLabel = new Label("None selected");
        selectedSeatsLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #f59e0b;");
        seatsBox.getChildren().addAll(sTitle, selectedSeatsLabel);

        // Seat Count & Category
        HBox countBox = new HBox(12);
        countBox.setAlignment(Pos.CENTER_LEFT);
        seatCountLabel = new Label("0 Seats");
        seatCountLabel.getStyleClass().add("duration-badge");
        Label catLabel = new Label(category.getDisplayName());
        catLabel.getStyleClass().add("duration-badge");
        countBox.getChildren().addAll(seatCountLabel, catLabel);

        // Total Price Display
        VBox totalBox = new VBox(2);
        Label tTitle = new Label("Total Payable Amount:");
        tTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        totalAmountLabel = new Label("₹0.00");
        totalAmountLabel.getStyleClass().add("total-amount-label");
        totalBox.getChildren().addAll(tTitle, totalAmountLabel);

        // Customer Details Form
        Label custHeader = new Label("Customer Information");
        custHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #ffffff;");

        nameField = new TextField();
        nameField.setPromptText("Full Name (e.g. Priya)");
        nameField.setText("Priya"); // Seed example customer name as required by Section 12

        emailField = new TextField();
        emailField.setPromptText("Email Address (e.g. priya@cinepass.com)");
        emailField.setText("priya@cinepass.com");

        phoneField = new TextField();
        phoneField.setPromptText("Phone Number (e.g. 9876543210)");
        phoneField.setText("9876543210");

        validationMessageLabel = new Label();
        validationMessageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444; -fx-font-weight: 600;");
        validationMessageLabel.setWrapText(true);

        confirmBtn = new Button("Proceed to Confirmation →");
        confirmBtn.getStyleClass().add("btn-primary");
        confirmBtn.setPrefHeight(42);
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setDisable(true); // Disabled until at least one seat is selected

        confirmBtn.setOnAction(e -> handleProceedToConfirmation());

        sidebar.getChildren().addAll(
                summaryHeader, seatsBox, countBox, totalBox,
                new Region() {{ setPrefHeight(8); }},
                custHeader, nameField, emailField, phoneField,
                validationMessageLabel, confirmBtn
        );

        return sidebar;
    }

    private void updateSummaryPanel() {
        if (selectedSeats.isEmpty()) {
            selectedSeatsLabel.setText("None selected");
            seatCountLabel.setText("0 Seats");
            totalAmountLabel.setText("₹0.00");
            confirmBtn.setDisable(true);
        } else {
            List<String> sortedList = new ArrayList<>(selectedSeats);
            Collections.sort(sortedList);
            selectedSeatsLabel.setText(String.join(", ", sortedList));
            seatCountLabel.setText(sortedList.size() + " Seat" + (sortedList.size() > 1 ? "s" : ""));

            double basePrice = showtime.getMovie() != null ? showtime.getMovie().getBasePrice() : 300.0;
            double total = PriceCalculator.calculateTotal(category, basePrice, sortedList.size());
            totalAmountLabel.setText(String.format("₹%.2f", total));
            confirmBtn.setDisable(false);
        }
    }

    private void handleProceedToConfirmation() {
        validationMessageLabel.setText("");

        // 1. Validate seats selected
        if (selectedSeats.isEmpty()) {
            validationMessageLabel.setText("Please select at least one seat.");
            return;
        }

        // 2. Validate customer form input
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        try {
            ValidationUtils.validateCustomerDetails(name, email, phone);
            ValidationUtils.validateSeatSelection(selectedSeats);
        } catch (Exception ex) {
            validationMessageLabel.setText(ex.getMessage());
            return;
        }

        List<String> sortedSeats = new ArrayList<>(selectedSeats);
        Collections.sort(sortedSeats);

        double basePrice = showtime.getMovie() != null ? showtime.getMovie().getBasePrice() : 300.0;
        double total = PriceCalculator.calculateTotal(category, basePrice, sortedSeats.size());

        onProceed.onProceed(showtime, category, sortedSeats, total, name.trim(), email.trim(), phone.trim());
    }
}
