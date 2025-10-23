package com.chatapp.core.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class Message {

    /*
     {
      "message_id": 15,
      "sender_id": 1,
      "receiver_id": 2,
      "nickname": "Barát",
      "msg_type": "text", // vagy "audio", "image"
      "content": "Szia!", // csak ha text típus
      "media_info": {     // csak ha nem text típus
        "media_type": "audio",
        "file_path": "/uploads/audios/abc123.mp3",
        "file_size": 123456
      },
      "sent_date": "2025-10-23 13:10:00",
      "delivered": true,
      "read_status": false,
      "is_from_me": true // vagy false
    }
     */


    private boolean read;
    private boolean confirmed = false; // Új property: visszaigazoltuk-e a szerver felé
    // ... többi mező

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
    @JsonAlias({ "message_id", "id" })
    private int id;

    private int serverId;
    @JsonAlias({"sender_id","senderId"})
    private int senderId;
    @JsonAlias({    "nickname","senderNickname"})
    private String senderNickname;
    @JsonAlias({"receiver_id","receiverId"})
    private int receiverId;
    @JsonAlias({"msg_type","msgType"})
    private String msgType; // "text", "audio", "image"
    @JsonAlias({"content"})
    private String content; // Text content or file path for media
    @JsonAlias({"sent_date","sentDate"})
    private String sentDate;
    @JsonAlias({"delivered"})
    private boolean delivered;
    @JsonAlias({"read_status","readStatus"})
    private boolean readStatus;
    @JsonAlias({"is_from_me","isFromMe"})
    private boolean isFromMe;

    private  MediaInfo media_info;

    public Message() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public void setSenderNickname(String senderNickname) {
        this.senderNickname = senderNickname;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSentDate() {
        return sentDate;
    }

    public void setSentDate(String sentDate) {
        this.sentDate = sentDate;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public boolean isReadStatus() {
        return readStatus;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }

    public boolean isFromMe() {
        return isFromMe;
    }

    public void setFromMe(boolean fromMe) {
        isFromMe = fromMe;
    }



    public MediaInfo getMedia_info() {
        return media_info;
    }

    public void setMedia_info(MediaInfo media_info) {
        this.media_info = media_info;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "read=" + read +
                ", confirmed=" + confirmed +
                ", id=" + id +
                ", serverId=" + serverId +
                ", senderId=" + senderId +
                ", senderNickname='" + senderNickname + '\'' +
                ", receiverId=" + receiverId +
                ", msgType='" + msgType + '\'' +
                ", content='" + content + '\'' +
                ", sentDate='" + sentDate + '\'' +
                ", delivered=" + delivered +
                ", readStatus=" + readStatus +
                ", isFromMe=" + isFromMe +
                ", media_info=" + media_info +
                '}';
    }
}
