package com.chatapp.core.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBService {
    private static final String DB_URL = "jdbc:sqlite:chatapp.db";
    private Connection connection;

    public DBService() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            initializeTables();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to SQLite database", e);
        }
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
                "is_from_me INTEGER" +
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
