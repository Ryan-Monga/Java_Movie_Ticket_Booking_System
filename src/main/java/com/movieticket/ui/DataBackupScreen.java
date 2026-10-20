package com.movieticket.ui;

import com.movieticket.model.Booking;
import com.movieticket.service.BookingService;
import com.movieticket.service.SerializationService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;

/**
 * Screen H: Data Backup and Object Serialization management.
 * Demonstrates Module 4 Serialization (ObjectOutputStream / ObjectInputStream).
 */
public class DataBackupScreen extends VBox {

    private final BookingService bookingService;
    private TextArea statusArea;

    public DataBackupScreen(BookingService bookingService) {
        this.bookingService = bookingService;

        setSpacing(20);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: transparent;");

        buildHeader();
        buildActionCard();
        buildDisplayArea();
    }

    private void buildHeader() {
        VBox titleBox = new VBox(4);
        Label heading = UITheme.createHeaderLabel("Module 4: Object Serialization & Backup");
        Label subheading = UITheme.createSubheaderLabel(
                "Exports live booking object graph to binary 'exports/bookings.dat' and safely inspects serialized records without overwriting authoritative database state."
        );
        titleBox.getChildren().addAll(heading, subheading);
        getChildren().add(titleBox);
    }

    private void buildActionCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #111827; -fx-background-radius: 12px; -fx-border-color: #1f293d;");

        Label cardTitle = new Label("Binary Serialization Controls");
        cardTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #ffffff;");

        HBox buttonBar = new HBox(16);
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        Button exportBtn = new Button("💾 Export Bookings to 'bookings.dat' (ObjectOutputStream)");
        exportBtn.getStyleClass().add("btn-primary");
        exportBtn.setOnAction(e -> handleExportSerialization());

        Button inspectBtn = new Button("🔍 Inspect 'bookings.dat' Archive (ObjectInputStream)");
        inspectBtn.getStyleClass().add("btn-secondary");
        inspectBtn.setOnAction(e -> handleInspectSerialization());

        buttonBar.getChildren().addAll(exportBtn, inspectBtn);

        Label notice = new Label(
                "Note: SQLite database remains the authoritative single source of truth.\n" +
                "Serialization operates as an independent academic object backup stream."
        );
        notice.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        card.getChildren().addAll(cardTitle, buttonBar, notice);
        getChildren().add(card);
    }

    private void buildDisplayArea() {
        VBox container = new VBox(8);
        VBox.setVgrow(container, Priority.ALWAYS);

        Label label = new Label("Serialization Stream Log & Data Inspector:");
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #cbd5e1;");

        statusArea = new TextArea();
        statusArea.setEditable(false);
        statusArea.getStyleClass().add("terminal-box");
        statusArea.setPrefRowCount(14);
        statusArea.setText("Ready. Click 'Export Bookings' to serialize current database state into bookings.dat.\n");
        VBox.setVgrow(statusArea, Priority.ALWAYS);

        container.getChildren().addAll(label, statusArea);
        getChildren().add(container);
    }

    private void handleExportSerialization() {
        try {
            List<Booking> currentBookings = bookingService.getAllBookings();
            File datFile = SerializationService.exportBookingsToDat(currentBookings);

            statusArea.appendText(String.format(
                    "\n[SERIALIZE SUCCESS] Exported %d booking records to '%s'\nFile size: %d bytes\nAuthoritative source: SQLite JDBC.\n",
                    currentBookings.size(), datFile.getAbsolutePath(), datFile.length()
            ));

            UITheme.showAlert(Alert.AlertType.INFORMATION, "Serialization Complete",
                    "Exported " + currentBookings.size() + " Bookings",
                    "Saved binary archive to:\n" + datFile.getAbsolutePath());
        } catch (Exception ex) {
            statusArea.appendText("\n[ERROR] Serialization failed: " + ex.getMessage() + "\n");
            UITheme.showAlert(Alert.AlertType.ERROR, "Export Failed", "Serialization Error", ex.getMessage());
        }
    }

    private void handleInspectSerialization() {
        try {
            List<Booking> restored = SerializationService.inspectBookingsFromDat();
            statusArea.appendText(String.format(
                    "\n[DESERIALIZE SUCCESS] Read %d objects from 'exports/bookings.dat':\n", restored.size()
            ));

            for (Booking b : restored) {
                statusArea.appendText(String.format(
                        " • ID: %s | Patron: %s | Seats: %s | Amount: ₹%.0f | Status: %s\n",
                        b.getBookingId(), b.getCustomerName(), b.getFormattedSeats(),
                        b.getTotalAmount(), b.getStatus().name()
                ));
            }
        } catch (Exception ex) {
            statusArea.appendText("\n[ERROR] Deserialization inspection failed: " + ex.getMessage() + "\n");
            UITheme.showAlert(Alert.AlertType.ERROR, "Inspect Failed", "Deserialization Error", ex.getMessage());
        }
    }
}
