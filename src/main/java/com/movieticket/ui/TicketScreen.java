package com.movieticket.ui;

import com.movieticket.model.Booking;
import com.movieticket.service.TicketFileService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.File;

/**
 * Screen E / F: Cinema Ticket Confirmation View with text export (.txt).
 * Satisfies Screen F and Module 4 Ticket Export requirements.
 */
public class TicketScreen extends VBox {

    private final Booking booking;
    private final Runnable onHome;
    private final Runnable onMyBookings;

    public TicketScreen(Booking booking, Runnable onHome, Runnable onMyBookings) {
        this.booking = booking;
        this.onHome = onHome;
        this.onMyBookings = onMyBookings;

        setSpacing(24);
        setPadding(new Insets(24));
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: transparent;");

        buildLayout();
    }

    private void buildLayout() {
        // Success Header Banner
        VBox banner = new VBox(4);
        banner.setAlignment(Pos.CENTER);
        Label successIcon = new Label("🎉");
        successIcon.setStyle("-fx-font-size: 36px;");
        Label successTitle = UITheme.createHeaderLabel("Booking Confirmed Successfully!");
        Label successSubtitle = UITheme.createSubheaderLabel("Your cinema pass has been reserved and registered in the database.");
        banner.getChildren().addAll(successIcon, successTitle, successSubtitle);

        // Cinema Ticket Stub Card
        VBox ticketCard = new VBox(12);
        ticketCard.getStyleClass().add("ticket-stub");
        ticketCard.setMaxWidth(500);

        Label cinemaHeader = new Label("CINEPASS CINEMAS — ADMISSION PASS");
        cinemaHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #f59e0b; -fx-alignment: center;");
        cinemaHeader.setMaxWidth(Double.MAX_VALUE);
        cinemaHeader.setAlignment(Pos.CENTER);

        Separator sep1 = new Separator();

        String movieTitle = (booking.getShowtime() != null && booking.getShowtime().getMovie() != null)
                ? booking.getShowtime().getMovie().getTitle() : "Movie";

        String theaterName = (booking.getShowtime() != null && booking.getShowtime().getTheater() != null)
                ? booking.getShowtime().getTheater().getName() : "Theater";

        String showTime = (booking.getShowtime() != null)
                ? booking.getShowtime().getShowTime() : "00:00";

        ticketCard.getChildren().addAll(
                cinemaHeader,
                sep1,
                createField("BOOKING ID:", booking.getBookingId(), "#f59e0b", true),
                createField("MOVIE:", movieTitle, "#ffffff", true),
                createField("THEATER:", theaterName, "#ffffff", false),
                createField("TIME:", showTime, "#ffffff", true),
                createField("SEATS:", booking.getFormattedSeats(), "#f59e0b", true),
                createField("AMOUNT:", String.format("Rs %.0f", booking.getTotalAmount()), "#f59e0b", true),
                createField("STATUS:", booking.getStatus().name(), "#10b981", true),
                new Separator(),
                createField("PATRON:", booking.getCustomerName(), "#ffffff", false),
                createField("TIER:", booking.getTicketCategory().getDisplayName(), "#94a3b8", false),
                createField("TIMESTAMP:", booking.getBookingTime(), "#64748b", false),
                new Separator()
        );

        // Barcode Representation Graphic
        Label barcodeLabel = new Label("||| | ||||| || |||||| | |||| ||| ||||||| ||||");
        barcodeLabel.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        barcodeLabel.setMaxWidth(Double.MAX_VALUE);
        barcodeLabel.setAlignment(Pos.CENTER);
        ticketCard.getChildren().add(barcodeLabel);

        // Action Buttons Row
        HBox actions = new HBox(14);
        actions.setAlignment(Pos.CENTER);

        Button exportBtn = new Button("📄 Export Ticket to .TXT");
        exportBtn.getStyleClass().add("btn-primary");
        exportBtn.setOnAction(e -> exportTicketTxt());

        Button myBookingsBtn = new Button("🎟️ My Bookings");
        myBookingsBtn.getStyleClass().add("btn-secondary");
        myBookingsBtn.setOnAction(e -> onMyBookings.run());

        Button newBookingBtn = new Button("🎬 Book Another Movie");
        newBookingBtn.getStyleClass().add("btn-secondary");
        newBookingBtn.setOnAction(e -> onHome.run());

        actions.getChildren().addAll(exportBtn, myBookingsBtn, newBookingBtn);

        getChildren().addAll(banner, ticketCard, actions);
    }

    private HBox createField(String label, String value, String color, boolean bold) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label l = new Label(label);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #94a3b8;");
        l.setMinWidth(110);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label v = new Label(value);
        v.setStyle(String.format("-fx-font-size: 13px; -fx-text-fill: %s; %s",
                color, bold ? "-fx-font-weight: 800;" : "-fx-font-weight: 500;"));

        row.getChildren().addAll(l, spacer, v);
        return row;
    }

    private void exportTicketTxt() {
        try {
            File exported = TicketFileService.exportTicketToFile(booking);
            UITheme.showAlert(Alert.AlertType.INFORMATION, "Ticket Exported",
                    "Ticket File Created Successfully",
                    "Ticket text file saved to:\n" + exported.getAbsolutePath());
        } catch (Exception ex) {
            UITheme.showAlert(Alert.AlertType.ERROR, "Export Failed",
                    "Could not write ticket text file", ex.getMessage());
        }
    }
}
