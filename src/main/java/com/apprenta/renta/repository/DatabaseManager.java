package com.apprenta.renta.repository;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class DatabaseManager {

    private static DatabaseManager instance;
    private final Connection connection;
    private static final String DB_PATH = obtenerRutaDB();

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

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
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

    private static String obtenerRutaDB() {
        final String carpeta = System.getenv("APPDATA") + "/AppRenta";
        new java.io.File(carpeta).mkdirs();
        return carpeta + "/rentaAutonomos.db";
    }
}
