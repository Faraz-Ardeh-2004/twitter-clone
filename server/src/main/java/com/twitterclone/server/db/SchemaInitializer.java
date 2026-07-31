package com.twitterclone.server.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Runs the bundled {@code schema.sql} against the database on server startup.
 * The schema is written to be idempotent (CREATE ... IF NOT EXISTS / ADD COLUMN
 * IF NOT EXISTS), so running it every boot is safe and keeps a fresh database
 * ready without a manual psql step.
 */
public final class SchemaInitializer {

    private SchemaInitializer() {
    }

    public static void initialize() throws SQLException, IOException {
        String sql = readResource("/schema.sql");
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            for (String statement : splitStatements(sql)) {
                if (!statement.isBlank()) {
                    stmt.execute(statement);
                }
            }
        }
    }

    private static String readResource(String path) throws IOException {
        try (InputStream in = SchemaInitializer.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("Resource not found on classpath: " + path);
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Drop full-line SQL comments so ';' inside them can't split a statement.
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--")) {
                        continue;
                    }
                    sb.append(line).append('\n');
                }
            }
            return sb.toString();
        }
    }

    /**
     * Splits a script into individual statements on ';'. The schema uses no
     * stored procedures or dollar-quoted bodies, so a simple split is safe here.
     */
    private static String[] splitStatements(String sql) {
        return sql.split(";");
    }
}
