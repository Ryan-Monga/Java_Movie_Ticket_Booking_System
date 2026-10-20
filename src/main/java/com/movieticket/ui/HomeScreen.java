package com.movieticket.ui;

import com.movieticket.model.Movie;
import com.movieticket.service.MovieService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Screen A: Movie Catalogue with search, genre filters, and sort options.
 * Satisfies Screen A requirements and Module 4 Streams/Lambdas/Comparators.
 */
public class HomeScreen extends VBox {

    private final MovieService movieService;
    private final Consumer<Movie> onMovieSelected;

    private TextField searchField;
    private ComboBox<String> genreFilterCombo;
    private ComboBox<String> sortCombo;
    private FlowPane moviesGrid;
    private Label resultsCountLabel;

    public HomeScreen(MovieService movieService, Consumer<Movie> onMovieSelected) {
        this.movieService = movieService;
        this.onMovieSelected = onMovieSelected;

        setSpacing(20);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: transparent;");

        buildHeaderAndFilters();
        buildMoviesContainer();
        applyFilters();
    }

    private void buildHeaderAndFilters() {
        // Banner Header
        VBox titleBox = new VBox(4);
        Label heading = UITheme.createHeaderLabel("Now Showing at CinePass Cinemas");
        Label subheading = UITheme.createSubheaderLabel("Explore the latest blockbusters and reserve your cinema experience");
        titleBox.getChildren().addAll(heading, subheading);

        // Filter / Search Toolbar
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(14));
        filterBar.setStyle("-fx-background-color: #111827; -fx-background-radius: 10px; -fx-border-color: #1f293d; -fx-border-radius: 10px;");

        // Search Input
        searchField = new TextField();
        searchField.setPromptText("🔍 Search movies by title or synopsis...");
        searchField.setPrefWidth(280);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Genre ComboBox
        genreFilterCombo = new ComboBox<>();
        genreFilterCombo.getItems().addAll(movieService.getAllGenres());
        genreFilterCombo.setValue("All Genres");
        genreFilterCombo.setOnAction(e -> applyFilters());

        // Sort ComboBox
        sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Sort: Title (A-Z)", "Sort: Rating (High to Low)", "Sort: Duration");
        sortCombo.setValue("Sort: Title (A-Z)");
        sortCombo.setOnAction(e -> applyFilters());

        // Reset Button
        Button resetBtn = new Button("Clear Filters");
        resetBtn.getStyleClass().add("btn-secondary");
        resetBtn.setOnAction(e -> {
            searchField.clear();
            genreFilterCombo.setValue("All Genres");
            sortCombo.setValue("Sort: Title (A-Z)");
            applyFilters();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        resultsCountLabel = new Label("6 movies available");
        resultsCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-font-weight: 600;");

        filterBar.getChildren().addAll(searchField, genreFilterCombo, sortCombo, resetBtn, spacer, resultsCountLabel);
        getChildren().addAll(titleBox, filterBar);
    }

    private void buildMoviesContainer() {
        moviesGrid = new FlowPane();
        moviesGrid.setHgap(20);
        moviesGrid.setVgap(20);
        moviesGrid.setAlignment(Pos.TOP_LEFT);
        moviesGrid.setStyle("-fx-background-color: transparent;");

        ScrollPane scrollPane = new ScrollPane(moviesGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().add(scrollPane);
    }

    private void applyFilters() {
        String query = searchField.getText();
        String selectedGenre = genreFilterCombo.getValue();
        String selectedSort = sortCombo.getValue();

        // 1. Filter by search query using Java Streams
        List<Movie> filtered = movieService.searchMovies(query);

        // 2. Filter by genre
        if (selectedGenre != null && !selectedGenre.equalsIgnoreCase("All Genres")) {
            filtered = filtered.stream()
                    .filter(m -> m.getGenre().toLowerCase().contains(selectedGenre.toLowerCase()))
                    .toList();
        }

        // 3. Sort using modern Java Comparator
        Comparator<Movie> comparator = Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER);
        if ("Sort: Rating (High to Low)".equals(selectedSort)) {
            comparator = Comparator.comparingDouble(Movie::getRating).reversed();
        } else if ("Sort: Duration".equals(selectedSort)) {
            comparator = Comparator.comparingInt(Movie::getDurationMinutes).reversed();
        }

        List<Movie> sortedMovies = filtered.stream().sorted(comparator).toList();
        resultsCountLabel.setText(sortedMovies.size() + " movies available");

        renderMovieCards(sortedMovies);
    }

    private void renderMovieCards(List<Movie> movies) {
        moviesGrid.getChildren().clear();

        if (movies.isEmpty()) {
            VBox emptyBox = new VBox(12);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(40));
            Label emptyLbl = new Label("No movies match your current search or filters.");
            emptyLbl.setStyle("-fx-font-size: 15px; -fx-text-fill: #94a3b8;");
            emptyBox.getChildren().add(emptyLbl);
            moviesGrid.getChildren().add(emptyBox);
            return;
        }

        for (Movie movie : movies) {
            moviesGrid.getChildren().add(createMovieCard(movie));
        }
    }

    private Node createMovieCard(Movie movie) {
        VBox card = new VBox(10);
        card.getStyleClass().add("movie-card");
        card.setPrefWidth(250);
        card.setMaxWidth(250);

        // Sleek Cinema Poster Canvas
        Node poster = UITheme.createMoviePosterCanvas(movie, 222, 170);

        // Title and Rating Row
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(movie.getTitle());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(160);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label ratingLabel = new Label(String.format("%.1f ★", movie.getRating()));
        ratingLabel.getStyleClass().add("rating-badge");

        titleRow.getChildren().addAll(titleLabel, spacer, ratingLabel);

        // Genre & Details Row
        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label durationLabel = new Label(movie.getFormattedDuration());
        durationLabel.getStyleClass().add("duration-badge");

        Label langLabel = new Label(movie.getLanguage());
        langLabel.getStyleClass().add("duration-badge");

        metaRow.getChildren().addAll(durationLabel, langLabel);

        // Excerpt
        Label descLabel = new Label(movie.getDescription());
        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(40);

        // Price and Action Button
        HBox actionRow = new HBox(8);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        VBox priceBox = new VBox(2);
        Label fromLabel = new Label("From");
        fromLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");
        Label priceLabel = new Label(String.format("₹%.0f", movie.getBasePrice()));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #f59e0b;");
        priceBox.getChildren().addAll(fromLabel, priceLabel);

        Region actSpacer = new Region();
        HBox.setHgrow(actSpacer, Priority.ALWAYS);

        Button bookBtn = new Button("Book Tickets");
        bookBtn.getStyleClass().add("btn-primary");
        bookBtn.setOnAction(e -> onMovieSelected.accept(movie));

        actionRow.getChildren().addAll(priceBox, actSpacer, bookBtn);

        card.getChildren().addAll(poster, titleRow, metaRow, descLabel, actionRow);

        // Card click navigates to showtime selection
        card.setOnMouseClicked(e -> onMovieSelected.accept(movie));

        return card;
    }
}
