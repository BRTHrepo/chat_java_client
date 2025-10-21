package com.chatapp.core.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestDao {
    private final DBService dbService;

    public FriendRequestDao(DBService dbService) {
        this.dbService = dbService;
    }

    public void saveFriendRequest(int fromUserId, int toUserId, String requestDate) {
        String sql = "INSERT INTO friend_requests (from_user_id, to_user_id, request_date) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, fromUserId);
            stmt.setInt(2, toUserId);
            stmt.setString(3, requestDate);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save friend request", e);
        }
    }

    public List<FriendRequestRecord> getAllFriendRequests() {
        String sql = "SELECT * FROM friend_requests";
        List<FriendRequestRecord> requests = new ArrayList<>();
        try (Statement stmt = dbService.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                FriendRequestRecord req = new FriendRequestRecord();
                req.id = rs.getInt("id");
                req.fromUserId = rs.getInt("from_user_id");
                req.toUserId = rs.getInt("to_user_id");
                req.requestDate = rs.getString("request_date");
                requests.add(req);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get friend requests", e);
        }
        return requests;
    }

    public void deleteAllFriendRequests() {
        String sql = "DELETE FROM friend_requests";
        try (Statement stmt = dbService.getConnection().createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete friend requests", e);
        }
    }

    // Egyszerű POJO a barátkérés rekordhoz
    public static class FriendRequestRecord {
        public int id;
        public int fromUserId;
        public int toUserId;
        public String requestDate;
    }
}
