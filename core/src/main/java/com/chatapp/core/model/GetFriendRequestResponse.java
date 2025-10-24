package com.chatapp.core.model;

public class GetFriendRequestResponse {
    /*
    'request_id' => $request->id,
                    'from_user_id' => $request->from_user_id,
                    'nickname' => $request->fromUser->nickname,
                    'email' => $request->fromUser->email,
                    'request_date' => $request->request_date
     */

    private int request_id;
    private int from_user_id;
    private String nickname;
    private String email;
    private String request_date;

    public GetFriendRequestResponse() {
    }

    public int getRequest_id() {
        return request_id;
    }

    public void setRequest_id(int request_id) {
        this.request_id = request_id;
    }

    public int getFrom_user_id() {
        return from_user_id;
    }

    public void setFrom_user_id(int from_user_id) {
        this.from_user_id = from_user_id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRequest_date() {
        return request_date;
    }

    public void setRequest_date(String request_date) {
        this.request_date = request_date;
    }

    @Override
    public String toString() {
        return "GetFriendRequestResponse{" +
                "request_id=" + request_id +
                ", from_user_id=" + from_user_id +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", request_date='" + request_date + '\'' +
                '}';
    }
}
