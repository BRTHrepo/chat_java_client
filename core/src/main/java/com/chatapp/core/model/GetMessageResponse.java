package com.chatapp.core.model;

public class GetMessageResponse {
    private int message_id;
    private int sender_id;
    private int receiver_id;
    private String nickname;
    private String msg_type; // "text", "audio", "image"
    private String content; // only if text type
    private MediaInfo media_info; // only if not text type
    private String sent_date;
    private boolean delivered;
    private boolean read_status;
    private boolean is_from_me; // or false

    // Getters and Setters

    public int getMessage_id() {
        return message_id;
    }

    public void setMessage_id(int message_id) {
        this.message_id = message_id;
    }

    public int getSender_id() {
        return sender_id;
    }

    public void setSender_id(int sender_id) {
        this.sender_id = sender_id;
    }

    public int getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(int receiver_id) {
        this.receiver_id = receiver_id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MediaInfo getMedia_info() {
        return media_info;
    }

    public void setMedia_info(MediaInfo media_info) {
        this.media_info = media_info;
    }

    public String getSent_date() {
        return sent_date;
    }

    public void setSent_date(String sent_date) {
        this.sent_date = sent_date;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public boolean isRead_status() {
        return read_status;
    }

    public void setRead_status(boolean read_status) {
        this.read_status = read_status;
    }

    public boolean isIs_from_me() {
        return is_from_me;
    }

    public void setIs_from_me(boolean is_from_me) {
        this.is_from_me = is_from_me;
    }

    @Override
    public String toString() {
        return "GetrMessageResponse{" +
               "message_id=" + message_id +
               ", sender_id=" + sender_id +
               ", receiver_id=" + receiver_id +
               ", nickname='" + nickname + '\'' +
               ", msg_type='" + msg_type + '\'' +
               ", content='" + content + '\'' +
               ", media_info=" + media_info +
               ", sent_date='" + sent_date + '\'' +
               ", delivered=" + delivered +
               ", read_status=" + read_status +
               ", is_from_me=" + is_from_me +
               '}';
    }
}
