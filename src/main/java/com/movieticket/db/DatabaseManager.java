package com.movieticket.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages SQLite database connections and configuration.
 * Satisfies Module 5 JDBC requirements.
 */
public class DatabaseManager {

    private static final String DEFAULT_DB_PATH = "data/movieticket.db";
    private static String currentDbUrl = "jdbc:sqlite:" + DEFAULT_DB_PATH;

    private DatabaseManager() {}

    /**
     * Configures the active SQLite database URL.
     * Useful for running JUnit tests against isolated databases.
     * @param dbPath path to the sqlite file or ":memory:"
     */
    public static synchronized void setDatabasePath(String dbPath) {
        if (":memory:".equals(dbPath)) {
            currentDbUrl = "jdbc:sqlite::memory:";
        } else {
            File dbFile = new File(dbPath);
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            currentDbUrl = "jdbc:sqlite:" + dbPath;
        }
    }

    public static synchronized void resetToDefaultDatabase() {
        setDatabasePath(DEFAULT_DB_PATH);
    }

    /**
     * Obtains a new JDBC Connection with foreign keys enabled.
     * @return Connection instance
     * @throws SQLException on database connection error
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(currentDbUrl);
        // Enable foreign key support in SQLite
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            // Set busy timeout so concurrent transactions wait briefly rather than failing immediately
            stmt.execute("PRAGMA busy_timeout = 5000;");
        }
        return conn;
    }

    public static String getCurrentDbUrl() {
        return currentDbUrl;
    }
}
