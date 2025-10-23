package com.chatapp.core.service;

import com.chatapp.core.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendDao {
    private final DBService dbService;

    public FriendDao(DBService dbService) {
        this.dbService = dbService;
    }

    public  User getFriendByIdStatic( Integer friendId) {
        if (friendId == null || friendId <= 0) {
            return null;
        }
        String sql = "SELECT * FROM friends WHERE id = ?";
        try (PreparedStatement stmt =dbService.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, friendId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setEmail(rs.getString("email"));
                    user.setNickname(rs.getString("nickname"));
                    user.setAvatarUrl(rs.getString("avatar_url"));
                    user.setStatus(rs.getString("status"));
                    return user;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        } catch (Exception e) {

        }
        return  null;
    }

    public void saveFriend(User user) {
        String sql = "INSERT OR REPLACE INTO friends (id, email, nickname, avatar_url, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, user.getId());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getNickname());
            stmt.setString(4, user.getAvatarUrl());
            stmt.setString(5, user.getStatus());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save friend", e);
        }
    }

    public List<User> getAllFriends() {
        String sql = "SELECT * FROM friends";
        List<User> friends = new ArrayList<>();
        try (Statement stmt = dbService.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setNickname(rs.getString("nickname"));
                user.setAvatarUrl(rs.getString("avatar_url"));
                user.setStatus(rs.getString("status"));
                friends.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get friends", e);
        }
        return friends;
    }

    public void deleteAllFriends() {
        String sql = "DELETE FROM friends";
        try (Statement stmt = dbService.getConnection().createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete friends", e);
        }
    }
}
