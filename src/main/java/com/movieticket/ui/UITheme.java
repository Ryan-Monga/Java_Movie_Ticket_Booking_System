package com.movieticket.ui;

import com.movieticket.model.Movie;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * UI Theme definitions, color tokens, and canvas graphics generators.
 * Ensures the application looks professional, cohesive, and cinema-grade.
 */
public class UITheme {

    public static final String COLOR_BG_DARK = "#0b0f17";
    public static final String COLOR_CARD_BG = "#131b2e";
    public static final String COLOR_GOLD = "#f59e0b";
    public static final String COLOR_GOLD_BRIGHT = "#fbbf24";
    public static final String COLOR_TEXT_MUTED = "#94a3b8";

    private UITheme() {}

    /**
     * Generates a sleek, cinematic movie poster graphic using JavaFX Canvas.
     * Guarantees 100% offline visual reliability without broken external images.
     */
    public static Node createMoviePosterCanvas(Movie movie, double width, double height) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Background Gradient tailored by movie genre
        Color c1 = Color.web("#1e293b");
        Color c2 = Color.web("#0f172a");

        String title = movie != null ? movie.getTitle().toLowerCase() : "";
        if (title.contains("inception")) {
            c1 = Color.web("#0c4a6e"); // Oceanic deep blue
            c2 = Color.web("#030712");
        } else if (title.contains("interstellar")) {
            c1 = Color.web("#4c1d95"); // Cosmic deep purple
            c2 = Color.web("#0f172a");
        } else if (title.contains("dark knight")) {
            c1 = Color.web("#1f2937"); // Dark Gotham slate
            c2 = Color.web("#030712");
        } else if (title.contains("avatar")) {
            c1 = Color.web("#065f46"); // Pandora emerald/cyan
            c2 = Color.web("#064e3b");
        } else if (title.contains("oppenheimer")) {
            c1 = Color.web("#78350f"); // Atomic amber
            c2 = Color.web("#1c1917");
        } else if (title.contains("spider")) {
            c1 = Color.web("#831843"); // Multiverse ruby
            c2 = Color.web("#172554");
        }

        LinearGradient grad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, c1),
                new Stop(0.65, c2),
                new Stop(1, Color.web("#080c14")));
        gc.setFill(grad);
        gc.fillRoundRect(0, 0, width, height, 12, 12);

        // Film reel decorative graphic
        gc.setStroke(Color.web("#334155", 0.4));
        gc.setLineWidth(2);
        gc.strokeRoundRect(6, 6, width - 12, height - 12, 8, 8);

        // Cinema Clapper / Camera silhouette icon
        gc.setFill(Color.web(COLOR_GOLD, 0.85));
        double iconX = width / 2 - 18;
        double iconY = height / 3 - 10;
        gc.fillRoundRect(iconX, iconY, 36, 26, 4, 4);
        gc.setFill(Color.web("#0b0f17"));
        gc.fillRect(iconX + 6, iconY + 5, 8, 16);
        gc.fillRect(iconX + 22, iconY + 5, 8, 16);

        // Title text in poster
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 13));
        String displayTitle = movie != null ? movie.getTitle() : "Movie";
        if (displayTitle.length() > 18) {
            displayTitle = displayTitle.substring(0, 15) + "...";
        }
        gc.fillText(displayTitle, 12, height - 36);

        // Genre / Rating in poster
        gc.setFill(Color.web(COLOR_GOLD));
        gc.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.SEMI_BOLD, 11));
        double rating = movie != null ? movie.getRating() : 8.0;
        gc.fillText(String.format("%.1f ★  %s", rating, movie != null ? movie.getLanguage() : "EN"), 12, height - 16);

        return canvas;
    }

    public static Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: #ffffff;");
        return label;
    }

    public static Label createSubheaderLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: " + COLOR_TEXT_MUTED + ";");
        return label;
    }

    public static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
