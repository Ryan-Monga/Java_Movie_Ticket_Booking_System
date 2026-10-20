package com.movieticket.ui;

import com.movieticket.model.Booking;
import com.movieticket.model.BookingStatus;
import com.movieticket.service.BookingService;
import com.movieticket.service.TicketFileService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Screen E: Booking History, Search, Details View, and Cancellation.
 * Satisfies Screen E and Module 5 TableView requirements.
 */
public class MyBookingsScreen extends VBox {

    private final BookingService bookingService;
    private final Consumer<Booking> onViewTicket;

    private TextField searchField;
    private ComboBox<String> statusFilterCombo;
    private TableView<Booking> bookingsTable;
    private final ObservableList<Booking> tableData = FXCollections.observableArrayList();
    private Label recordCountLabel;

    public MyBookingsScreen(BookingService bookingService, Consumer<Booking> onViewTicket) {
        this.bookingService = bookingService;
        this.onViewTicket = onViewTicket;

        setSpacing(20);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: transparent;");

        buildHeaderAndToolbar();
        buildTableView();
        buildActionToolbar();
        refreshTable();
    }

    private void buildHeaderAndToolbar() {
        VBox titleBox = new VBox(4);
        Label heading = UITheme.createHeaderLabel("My Booking History");
        Label subheading = UITheme.createSubheaderLabel("View, search, export tickets, or cancel reservations");
        titleBox.getChildren().addAll(heading, subheading);

        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(12));
        filterBar.setStyle("-fx-background-color: #111827; -fx-background-radius: 10px; -fx-border-color: #1f293d;");

        searchField = new TextField();
        searchField.setPromptText("🔍 Search by Booking ID, Customer Name, or Movie...");
        searchField.setPrefWidth(320);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable());

        statusFilterCombo = new ComboBox<>();
        statusFilterCombo.getItems().addAll("All Statuses", "CONFIRMED", "CANCELLED");
        statusFilterCombo.setValue("All Statuses");
        statusFilterCombo.setOnAction(e -> filterTable());

        Button refreshBtn = new Button("↻ Refresh List");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> refreshTable());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        recordCountLabel = new Label("0 records");
        recordCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-font-weight: 600;");

        filterBar.getChildren().addAll(searchField, statusFilterCombo, refreshBtn, spacer, recordCountLabel);
        getChildren().addAll(titleBox, filterBar);
    }

    private void buildTableView() {
        bookingsTable = new TableView<>();
        bookingsTable.setItems(tableData);
        VBox.setVgrow(bookingsTable, Priority.ALWAYS);

        // Column 1: Booking ID
        TableColumn<Booking, String> idCol = new TableColumn<>("Booking ID");
        idCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getBookingId()));
        idCol.setPrefWidth(95);

        // Column 2: Customer Name
        TableColumn<Booking, String> nameCol = new TableColumn<>("Patron Name");
        nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCustomerName()));
        nameCol.setPrefWidth(120);

        // Column 3: Movie Title
        TableColumn<Booking, String> movieCol = new TableColumn<>("Movie");
        movieCol.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getShowtime() != null && cell.getValue().getShowtime().getMovie() != null
                        ? cell.getValue().getShowtime().getMovie().getTitle() : "N/A"));
        movieCol.setPrefWidth(140);

        // Column 4: Theater
        TableColumn<Booking, String> theaterCol = new TableColumn<>("Theater");
        theaterCol.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getShowtime() != null && cell.getValue().getShowtime().getTheater() != null
                        ? cell.getValue().getShowtime().getTheater().getName() : "N/A"));
        theaterCol.setPrefWidth(120);

        // Column 5: Showtime
        TableColumn<Booking, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getShowtime() != null ? cell.getValue().getShowtime().getShowTime() : "N/A"));
        timeCol.setPrefWidth(85);

        // Column 6: Seats
        TableColumn<Booking, String> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedSeats()));
        seatsCol.setPrefWidth(85);

        // Column 7: Category
        TableColumn<Booking, String> catCol = new TableColumn<>("Tier");
        catCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTicketCategory().getDisplayName()));
        catCol.setPrefWidth(90);

        // Column 8: Total Amount
        TableColumn<Booking, String> amtCol = new TableColumn<>("Total");
        amtCol.setCellValueFactory(cell -> new SimpleStringProperty(String.format("₹%.0f", cell.getValue().getTotalAmount())));
        amtCol.setPrefWidth(85);

        // Column 9: Status (Color badge)
        TableColumn<Booking, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus().name()));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    if ("CONFIRMED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: 800;");
                    } else {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 800;");
                    }
                }
            }
        });

        // Column 10: Date/Time
        TableColumn<Booking, String> dateCol = new TableColumn<>("Date Booked");
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getBookingTime()));
        dateCol.setPrefWidth(135);

        bookingsTable.getColumns().addAll(List.of(
                idCol, nameCol, movieCol, theaterCol, timeCol, seatsCol, catCol, amtCol, statusCol, dateCol
        ));

        // Placeholder for empty table
        Label placeholder = new Label("No bookings recorded in database yet.");
        placeholder.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        bookingsTable.setPlaceholder(placeholder);

        getChildren().add(bookingsTable);
    }

    private void buildActionToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        Button viewBtn = new Button("🎟️ View Selected Ticket");
        viewBtn.getStyleClass().add("btn-primary");
        viewBtn.setOnAction(e -> {
            Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UITheme.showAlert(Alert.AlertType.WARNING, "Selection Required", "No Booking Selected", "Please select a booking row from the table.");
                return;
            }
            onViewTicket.accept(selected);
        });

        Button exportBtn = new Button("📄 Export Ticket (.txt)");
        exportBtn.getStyleClass().add("btn-secondary");
        exportBtn.setOnAction(e -> {
            Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UITheme.showAlert(Alert.AlertType.WARNING, "Selection Required", "No Booking Selected", "Please select a booking to export.");
                return;
            }
            try {
                File file = TicketFileService.exportTicketToFile(selected);
                UITheme.showAlert(Alert.AlertType.INFORMATION, "Ticket Exported", "Export Successful",
                        "Ticket text file saved to:\n" + file.getAbsolutePath());
            } catch (Exception ex) {
                UITheme.showAlert(Alert.AlertType.ERROR, "Export Failed", "Error writing file", ex.getMessage());
            }
        });

        Button cancelBtn = new Button("✕ Cancel Selected Booking");
        cancelBtn.getStyleClass().add("btn-danger");
        cancelBtn.setOnAction(e -> handleCancelBooking());

        Button clearAllBtn = new Button("🗑️ Clear All Bookings");
        clearAllBtn.getStyleClass().add("btn-secondary");
        clearAllBtn.setStyle("-fx-text-fill: #fca5a5; -fx-border-color: #7f1d1d;");
        clearAllBtn.setOnAction(e -> handleClearAllBookings());

        bar.getChildren().addAll(viewBtn, exportBtn, cancelBtn, clearAllBtn);
        getChildren().add(bar);
    }

    public void refreshTable() {
        bookingService.refreshBookingsFromDatabase();
        filterTable();
    }

    private void filterTable() {
        String query = searchField != null ? searchField.getText() : "";
        String statusFilter = statusFilterCombo != null ? statusFilterCombo.getValue() : "All Statuses";

        List<Booking> list = bookingService.searchBookings(query);

        if (statusFilter != null && !statusFilter.equalsIgnoreCase("All Statuses")) {
            list = list.stream()
                    .filter(b -> b.getStatus().name().equalsIgnoreCase(statusFilter))
                    .toList();
        }

        tableData.setAll(list);
        if (recordCountLabel != null) {
            recordCountLabel.setText(list.size() + " records");
        }
    }

    private void handleCancelBooking() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UITheme.showAlert(Alert.AlertType.WARNING, "Selection Required", "No Booking Selected", "Please select a booking from the table to cancel.");
            return;
        }

        if (selected.getStatus() == BookingStatus.CANCELLED) {
            UITheme.showAlert(Alert.AlertType.INFORMATION, "Already Cancelled", "Booking Inactive", "This booking is already cancelled.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Cancellation");
        confirmAlert.setHeaderText("Cancel Booking " + selected.getBookingId() + "?");
        confirmAlert.setContentText("This will release seats " + selected.getFormattedSeats() +
                " and process a simulated refund of ₹" + String.format("%.0f", selected.getTotalAmount()) + ".");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = bookingService.cancelBooking(selected.getBookingId());
                if (success) {
                    refreshTable();
                    UITheme.showAlert(Alert.AlertType.INFORMATION, "Cancelled Successfully",
                            "Seats Released",
                            "Booking " + selected.getBookingId() + " has been cancelled. Seats " +
                            selected.getFormattedSeats() + " are now available for booking again.");
                }
            } catch (Exception ex) {
                UITheme.showAlert(Alert.AlertType.ERROR, "Cancellation Failed", "Error processing cancellation", ex.getMessage());
            }
        }
    }

    private void handleClearAllBookings() {
        if (tableData.isEmpty()) {
            UITheme.showAlert(Alert.AlertType.INFORMATION, "No Bookings", "Table Already Empty", "There are currently no bookings to clear.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Clear All Bookings");
        confirmAlert.setHeaderText("Delete ALL booking history and reset seat availability?");
        confirmAlert.setContentText("This will purge all booking records from the SQLite database and make ALL seats available again across all movie showtimes. This cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = bookingService.clearAllBookings();
                if (success) {
                    refreshTable();
                    UITheme.showAlert(Alert.AlertType.INFORMATION, "Bookings Cleared",
                            "All Bookings Purged",
                            "All booking records have been deleted and all cinema seats have been released.");
                }
            } catch (Exception ex) {
                UITheme.showAlert(Alert.AlertType.ERROR, "Clear Failed", "Error purging bookings", ex.getMessage());
            }
        }
    }
}
