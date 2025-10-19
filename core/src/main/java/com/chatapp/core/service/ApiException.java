package com.chatapp.core.service;

public class ApiException extends RuntimeException {
    private final int statusCode;
    private final String serverMessage;

    public ApiException(int statusCode, String serverMessage) {
        super(serverMessage);
        this.statusCode = statusCode;
        this.serverMessage = serverMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getServerMessage() {
        return serverMessage;
    }
}
