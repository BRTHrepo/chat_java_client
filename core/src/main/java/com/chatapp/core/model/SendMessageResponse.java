package com.chatapp.core.model;

public class SendMessageResponse {
    private int message_id;
    private int receiver_id;
    private String msg_type;
    private String sent_date;

    // Getters
    public int getMessage_id() {
        return message_id;
    }

    public int getReceiver_id() {
        return receiver_id;
    }

    public String getMsg_type() {
        return msg_type;
    }

    public String getSent_date() {
        return sent_date;
    }

    // Setters
    public void setMessage_id(int message_id) {
        this.message_id = message_id;
    }

    public void setReceiver_id(int receiver_id) {
        this.receiver_id = receiver_id;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public void setSent_date(String sent_date) {
        this.sent_date = sent_date;
    }

    @Override
    public String toString() {
        return "SendMessageResponse{" +
               "message_id=" + message_id +
               ", receiver_id=" + receiver_id +
               ", msg_type='" + msg_type + '\'' +
               ", sent_date='" + sent_date + '\'' +
               '}';
    }
}
