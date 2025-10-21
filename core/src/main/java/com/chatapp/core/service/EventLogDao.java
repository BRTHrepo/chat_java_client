package com.chatapp.core.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventLogDao {
    private final DBService dbService;

    public EventLogDao(DBService dbService) {
        this.dbService = dbService;
    }

    public void logEvent(String eventType, String eventTime, String details) {
        String sql = "INSERT INTO event_log (event_type, event_time, details) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(sql)) {
            stmt.setString(1, eventType);
            stmt.setString(2, eventTime);
            stmt.setString(3, details);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to log event", e);
        }
    }

    public List<EventLogRecord> getAllEvents() {
        String sql = "SELECT * FROM event_log ORDER BY event_time DESC";
        List<EventLogRecord> events = new ArrayList<>();
        try (Statement stmt = dbService.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                EventLogRecord rec = new EventLogRecord();
                rec.id = rs.getInt("id");
                rec.eventType = rs.getString("event_type");
                rec.eventTime = rs.getString("event_time");
                rec.details = rs.getString("details");
                events.add(rec);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get event logs", e);
        }
        return events;
    }

    public void deleteAllEvents() {
        String sql = "DELETE FROM event_log";
        try (Statement stmt = dbService.getConnection().createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete event logs", e);
        }
    }

    // Egyszerű POJO az eseménylog rekordhoz
    public static class EventLogRecord {
        public int id;
        public String eventType;
        public String eventTime;
        public String details;
    }
}
