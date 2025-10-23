package com.chatapp.core.service;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBService {
    private Connection connection;

    public DBService() {
        try {
            String dbPath = getDatabasePath(); // Új metódus hívása
            connection = DriverManager.getConnection(dbPath);
            initializeTables();
        } catch (SQLException | URISyntaxException e) { // URISyntaxException ismételt kivétel
            throw new RuntimeException("Failed to connect to SQLite database", e);
        }
    }

    private String getDatabasePath() throws URISyntaxException {
        // A JAR fájl elérési útjának lekérése
        String jarPath = DBService.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        File jarFile = new File(jarPath);
        File jarDir = jarFile.getParentFile(); // A JAR fájlt tartalmazó könyvtár

        // Az adatbázisfájl elérési útjának felépítése a JAR könyvtárához képest
        return "jdbc:sqlite:" + jarDir.getAbsolutePath() + File.separator + "chatapp.db";
    }

    private void initializeTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Üzenetek tábla
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "sender_id INTEGER," +
                "receiver_id INTEGER," +
                "nickname TEXT," +
                "msg_type TEXT," +
                "content TEXT," +
                "sent_date TEXT," +
                "delivered INTEGER," +
                "read_status INTEGER," +
                "is_from_me INTEGER," +
                "server_id  INTEGER UNIQUE," +
                "confirmed INTEGER DEFAULT 0" +
                ")"
            );
            // Barátok tábla
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS friends (" +
                "id INTEGER PRIMARY KEY," +
                "email TEXT," +
                "nickname TEXT," +
                "avatar_url TEXT," +
                "status TEXT" +
                ")"
            );
            // Barátkérések tábla
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS friend_requests (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "from_user_id INTEGER," +
                "to_user_id INTEGER," +
                "request_date TEXT" +
                ")"
            );
            // Eseménylog tábla
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS event_log (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "event_type TEXT," +
                "event_time TEXT," +
                "details TEXT" +
                ")"
            );
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
