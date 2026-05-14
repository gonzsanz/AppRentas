package com.apprenta.renta.repository;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class DatabaseManager {

    private static final String DB_PATH = "rentaAutonomos.db";
    private static DatabaseManager instance;
    private final Connection connection;

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            initSchema();
        } catch (final Exception e) {
            throw new RuntimeException("No se pudo conectar a la base de datos", e);
        }
    }

    public static DatabaseManager getInstance() {
        return instance == null ? new DatabaseManager() : instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initSchema() throws Exception {
        final InputStream is = getClass().getResourceAsStream("/db/schema.sql");
        if (is == null) throw new RuntimeException("No se encontró schema.sql");
        final String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        for (final String stmt : sql.split(";")) {
            String trimmed = stmt.trim();
            if (!trimmed.isEmpty()) {
                connection.createStatement().execute(trimmed);
            }
        }
    }
}
