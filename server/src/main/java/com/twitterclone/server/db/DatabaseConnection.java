package com.twitterclone.server.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * ============================================================
 * Owner: AmirAli (Database) | Phase 0 - Day 1-2
 * ============================================================
 * Singleton for managing the connection pool with HikariCP. All DAOs
 * (UserDAO, TweetDAO, ...) should get their Connection from this single
 * instance; never create a separate DriverManager.getConnection elsewhere.
 *
 * TODO(AmirAli):
 *  1. Fill in the URL/USER/PASSWORD with your local database info (or read
 *     them from a config.properties file / environment variables instead of
 *     hardcoding them).
 *  2. Run schema.sql against your database to create the tables.
 *  3. Run a simple test in main or in ServerMain: getConnection() plus a
 *     "SELECT 1" to confirm the connection is healthy (per the Phase 0 DoD).
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private final HikariDataSource dataSource;

    // TODO(AmirAli): replace these values with your actual database settings.
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/twitter_clone";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "CHANGE_ME";

    private DatabaseConnection() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(JDBC_URL);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);

        // TODO(AmirAli): these defaults are reasonable, but tune them if
        // needed (e.g. for the Phase 4 concurrency test with 3-4 clients).
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(5000);

        this.dataSource = new HikariDataSource(config);
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /** Gets a Connection from the pool. Always close it with try-with-resources! */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
