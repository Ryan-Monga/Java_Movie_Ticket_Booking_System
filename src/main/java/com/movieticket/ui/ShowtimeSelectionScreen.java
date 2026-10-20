package com.movieticket.ui;

import com.movieticket.model.Movie;
import com.movieticket.model.Showtime;
import com.movieticket.model.Theater;
import com.movieticket.model.TicketCategory;
import com.movieticket.service.MovieService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Screen B: Detailed Movie Overview and Showtime/Theatre/Category Selection.
 * Satisfies Screen B requirements.
 */
public class ShowtimeSelectionScreen extends VBox {

    private final MovieService movieService;
    private final Movie movie;
    private final Runnable onBack;
    private final BiConsumer<Showtime, TicketCategory> onProceedToSeats;

    private ComboBox<Theater> theaterCombo;
    private ComboBox<Showtime> showtimeCombo;
    private ToggleGroup categoryGroup;
    private Label availabilityLabel;
    private Button proceedBtn;

    public ShowtimeSelectionScreen(MovieService movieService, Movie movie,
                                   Runnable onBack,
                                   BiConsumer<Showtime, TicketCategory> onProceedToSeats) {
        this.movieService = movieService;
        this.movie = movie;
        this.onBack = onBack;
        this.onProceedToSeats = onProceedToSeats;

        setSpacing(24);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: transparent;");

        buildBackButton();
        buildMovieHeroBanner();
        buildShowtimeAndCategorySelection();
    }

    private void buildBackButton() {
        Button backBtn = new Button("← Back to Movies");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setOnAction(e -> onBack.run());
        getChildren().add(backBtn);
    }

    private void buildMovieHeroBanner() {
        HBox hero = new HBox(20);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.setPadding(new Insets(20));
        hero.setStyle("-fx-background-color: #111827; -fx-background-radius: 12px; -fx-border-color: #1f293d;");

        // Poster
        Node poster = UITheme.createMoviePosterCanvas(movie, 140, 190);

        // Movie Info
        VBox info = new VBox(10);
        Label titleLbl = new Label(movie.getTitle());
        titleLbl.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: #ffffff;");

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);
        Label genreBadge = new Label(movie.getGenre());
        genreBadge.getStyleClass().add("duration-badge");
        Label durBadge = new Label(movie.getFormattedDuration());
        durBadge.getStyleClass().add("duration-badge");
        Label ratingBadge = new Label(String.format("%.1f ★ IMDb", movie.getRating()));
        ratingBadge.getStyleClass().add("rating-badge");
        badges.getChildren().addAll(genreBadge, durBadge, ratingBadge);

        Label descLbl = new Label(movie.getDescription());
        descLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        descLbl.setWrapText(true);
        descLbl.setMaxWidth(600);

        Label basePriceLbl = new Label(String.format("Base Ticket Rate: ₹%.0f", movie.getBasePrice()));
        basePriceLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #f59e0b;");

        info.getChildren().addAll(titleLbl, badges, descLbl, basePriceLbl);
        hero.getChildren().addAll(poster, info);
        getChildren().add(hero);
    }

    private void buildShowtimeAndCategorySelection() {
        VBox card = new VBox(20);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: #131b2e; -fx-background-radius: 12px; -fx-border-color: #202b42;");

        Label sectionTitle = new Label("Select Theatre, Showtime & Ticket Category");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #f8fafc;");

        // Showtimes from DB
        List<Showtime> availableShowtimes = movieService.getShowtimesForMovie(movie.getId());

        HBox selectorRow = new HBox(20);
        selectorRow.setAlignment(Pos.CENTER_LEFT);

        // Showtime selection ComboBox
        VBox showtimeBox = new VBox(6);
        Label stLabel = new Label("Available Screening Time:");
        stLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #cbd5e1;");

        showtimeCombo = new ComboBox<>();
        showtimeCombo.getItems().addAll(availableShowtimes);
        if (!availableShowtimes.isEmpty()) {
            showtimeCombo.setValue(availableShowtimes.get(0));
        }
        showtimeCombo.setPrefWidth(340);
        showtimeCombo.setOnAction(e -> updateAvailabilityLabel());
        showtimeBox.getChildren().addAll(stLabel, showtimeCombo);

        // Live Seat Availability badge
        VBox availBox = new VBox(6);
        Label avTitle = new Label("Seat Inventory Status:");
        avTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #cbd5e1;");

        availabilityLabel = new Label();
        availabilityLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #10b981;");
        availBox.getChildren().addAll(avTitle, availabilityLabel);

        selectorRow.getChildren().addAll(showtimeBox, availBox);

        // Ticket Tier / Category Selection
        VBox categoryBox = new VBox(10);
        Label catLabel = new Label("Ticket Experience Tier:");
        catLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #cbd5e1;");

        categoryGroup = new ToggleGroup();

        RadioButton regularRadio = new RadioButton(String.format("Regular Seating (₹%.0f / seat)", movie.getBasePrice()));
        regularRadio.setUserData(TicketCategory.REGULAR);
        regularRadio.setToggleGroup(categoryGroup);
        regularRadio.setSelected(true);
        regularRadio.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");

        RadioButton premiumRadio = new RadioButton(String.format("Premium Recliner (₹%.0f / seat - 1.5x Multiplier)", movie.getBasePrice() * 1.5));
        premiumRadio.setUserData(TicketCategory.PREMIUM);
        premiumRadio.setToggleGroup(categoryGroup);
        premiumRadio.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");

        RadioButton imaxRadio = new RadioButton(String.format("IMAX 3D Laser (₹%.0f / seat - Immersive Audio & Curved Screen)", movie.getBasePrice() + 250.0));
        imaxRadio.setUserData(TicketCategory.IMAX);
        imaxRadio.setToggleGroup(categoryGroup);
        imaxRadio.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");

        categoryBox.getChildren().addAll(catLabel, regularRadio, premiumRadio, imaxRadio);

        // Proceed Button
        proceedBtn = new Button("Proceed to Seat Selection →");
        proceedBtn.getStyleClass().add("btn-primary");
        proceedBtn.setPrefHeight(42);
        proceedBtn.setOnAction(e -> {
            Showtime selectedShowtime = showtimeCombo.getValue();
            if (selectedShowtime == null) {
                UITheme.showAlert(javafx.scene.control.Alert.AlertType.WARNING,
                        "Selection Required", "No Showtime Selected", "Please select an available showtime.");
                return;
            }
            TicketCategory selectedCategory = (TicketCategory) categoryGroup.getSelectedToggle().getUserData();
            onProceedToSeats.accept(selectedShowtime, selectedCategory);
        });

        card.getChildren().addAll(sectionTitle, selectorRow, categoryBox, proceedBtn);
        getChildren().add(card);

        updateAvailabilityLabel();
    }

    private void updateAvailabilityLabel() {
        Showtime st = showtimeCombo.getValue();
        if (st != null) {
            int available = st.getAvailableSeatCount();
            int total = st.getTotalSeats();
            availabilityLabel.setText(String.format("%d of %d Seats Available", available, total));
            if (available == 0) {
                availabilityLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #ef4444;");
                proceedBtn.setDisable(true);
            } else {
                availabilityLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #10b981;");
                proceedBtn.setDisable(false);
            }
        } else {
            availabilityLabel.setText("No showtime selected");
            proceedBtn.setDisable(true);
        }
    }
}
