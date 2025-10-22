package com.chatapp.core.model;
import com.fasterxml.jackson.annotation.JsonAlias;
public class User {

    /**
     *  for friend requests and user lists :
     *       'from_user_id' => $request->from_user_id,
     *                     'nickname' => $request->fromUser->nickname,
     *                     'email' => $request->fromUser->email,
     */

    @JsonAlias({"from_user_id", "id", "user_id","friend_id"})
    private int id;

    private String email;

    private String nickname;
    private String avatarUrl;
    private String status;
    private String token; // JWT token field

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }



    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
