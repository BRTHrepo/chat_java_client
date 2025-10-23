package com.chatapp.core.service;

import com.chatapp.core.model.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDao {
    private final DBService dbService;

    public MessageDao(DBService dbService) {
        this.dbService = dbService;
        // Ensure the confirmed column exists (migrate if needed)
        try {
            Statement stmt = dbService.getConnection().createStatement();
            stmt.executeUpdate("ALTER TABLE messages ADD COLUMN confirmed INTEGER DEFAULT 0");
        } catch (SQLException e) {
            // Already exists or migration failed, ignore if already exists
        }
    }

    public boolean tableExists(String tableName) {
        try {
            DatabaseMetaData metaData = dbService.getConnection().getMetaData();

            try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                return rs.next(); // true, ha létezik a tábla
            } catch (SQLException e) {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public void saveMessage(Message message) {
        String tableName = "messages";
        if (tableExists(tableName)) {
            // ellenörizzük a serverId mezőt, szerepel -e már az adatbázisban
            String checkSql = "SELECT COUNT(*) AS count FROM messages WHERE server_id = ?";
            try (PreparedStatement checkStmt = dbService.getConnection().prepareStatement(checkSql)) {
                checkStmt.setInt(1, message.getServerId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt("count") > 0) {
                    // Már létezik ilyen serverId, nem szúrjuk be újra
                    return;
                }
            } catch (SQLException e) {
            }
        }

        String sql = "INSERT INTO messages (sender_id, receiver_id, nickname, msg_type, content, sent_date, delivered, read_status, is_from_me, server_id, confirmed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            stmt.setInt(10, message.getServerId());
            stmt.setInt(11, message.isConfirmed() ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save message", e);
        }
    }

    public List<Message> getMessagesWithFriend(Integer friendId) {
        if (friendId == null) {
            return new ArrayList<>();
        }
        String sql = "SELECT * FROM messages WHERE sender_id = ? OR receiver_id = ? ORDER BY server_id ASC";
        List<Message> messages = new ArrayList<>();
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, friendId);
            stmt.setInt(2, friendId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Message msg = new Message();
                msg.setId(rs.getInt("id"));
                msg.setServerId(rs.getInt("server_id"));
                msg.setSenderId(rs.getInt("sender_id"));
                msg.setReceiverId(rs.getInt("receiver_id"));
                msg.setSenderNickname(rs.getString("nickname"));
                msg.setMsgType(rs.getString("msg_type"));
                msg.setContent(rs.getString("content"));
                msg.setSentDate(rs.getString("sent_date"));
                msg.setDelivered(rs.getInt("delivered") == 1);
                msg.setRead(rs.getInt("read_status") == 1);
                msg.setFromMe(rs.getInt("is_from_me") == 1);
                msg.setConfirmed(rs.getInt("confirmed") == 1);
                messages.add(msg);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get messages", e);
        }
        return messages;
    }

    public List<Integer> getUnconfirmedServerIds() {
        String sql = "SELECT server_id FROM messages WHERE confirmed = 0 AND server_id > 0";
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("server_id"));
            }
        } catch (SQLException e) {
           e.printStackTrace();
        }
        return ids;
    }

    public void setMessagesConfirmed(List<Integer> serverIds) {
        if (serverIds == null || serverIds.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < serverIds.size(); i++) {
            sb.append("?");
            if (i < serverIds.size() - 1) sb.append(",");
        }
        String sql = "UPDATE messages SET confirmed = 1 WHERE server_id IN (" + sb.toString() + ")";
        try (PreparedStatement stmt = dbService.getConnection().prepareStatement(sql)) {
            for (int i = 0; i < serverIds.size(); i++) {
                stmt.setInt(i + 1, serverIds.get(i));
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set messages confirmed", e);
        }
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
