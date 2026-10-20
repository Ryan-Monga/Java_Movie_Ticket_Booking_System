package com.movieticket;

import com.movieticket.db.DatabaseManager;
import com.movieticket.db.SchemaInitializer;
import com.movieticket.service.BookingService;
import com.movieticket.service.MovieService;
import com.movieticket.ui.CinemaAppUI;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Main application entry point for CinePass Online Movie Ticket Booking System.
 * Sets up SQLite database persistence, initializes services, and launches the JavaFX GUI.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Initialize SQLite Database Schema & Initial Data
            DatabaseManager.resetToDefaultDatabase();
            SchemaInitializer.initializeDatabase();

            // 2. Initialize Service Layer
            MovieService movieService = new MovieService();
            BookingService bookingService = new BookingService(movieService, null);

            // 3. Initialize JavaFX UI
            CinemaAppUI root = new CinemaAppUI(movieService, bookingService);
            Scene scene = new Scene(root, 1140, 780);

            // 4. Attach Modern Cinema CSS
            URL cssUrl = getClass().getResource("/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            // 5. Configure Primary Window
            primaryStage.setTitle("CinePass Cinemas — Online Movie Ticket Booking System");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(980);
            primaryStage.setMinHeight(680);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Failed to launch CinePass Cinema Application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
