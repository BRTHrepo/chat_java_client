package com.chatapp.core.service;

import com.chatapp.core.model.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDao {
    private final DBService dbService;

    public MessageDao(DBService dbService) {
        this.dbService = dbService;
    }

    public void saveMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, nickname, msg_type, content, sent_date, delivered, read_status, is_from_me) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, message.getSenderId());
            stmt.setInt(2, message.getReceiverId());
            stmt.setString(3, message.getSenderNickname());
            stmt.setString(4, message.getMsgType());
            stmt.setString(5, message.getContent());
            stmt.setString(6, message.getSentDate());
            stmt.setInt(7, message.isDelivered() ? 1 : 0);
            stmt.setInt(8, message.isRead() ? 1 : 0);
            stmt.setInt(9, message.isFromMe() ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save message", e);
        }
    }

    public List<Message> getMessagesWithFriend(int friendId) {
        String sql = "SELECT * FROM messages WHERE sender_id = ? OR receiver_id = ? ORDER BY sent_date ASC";
        List<Message> messages = new ArrayList<>();
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, friendId);
            stmt.setInt(2, friendId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Message msg = new Message();
                msg.setId(rs.getInt("id"));
                msg.setSenderId(rs.getInt("sender_id"));
                msg.setReceiverId(rs.getInt("receiver_id"));
                msg.setSenderNickname(rs.getString("nickname"));
                msg.setMsgType(rs.getString("msg_type"));
                msg.setContent(rs.getString("content"));
                msg.setSentDate(rs.getString("sent_date"));
                msg.setDelivered(rs.getInt("delivered") == 1);
                msg.setRead(rs.getInt("read_status") == 1);
                msg.setFromMe(rs.getInt("is_from_me") == 1);
                messages.add(msg);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get messages", e);
        }
        return messages;
    }

    public void deleteAllMessages() {
        String sql = "DELETE FROM messages";
        try (Statement stmt = dbService.getConnection().createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete messages", e);
        }
    }
}
