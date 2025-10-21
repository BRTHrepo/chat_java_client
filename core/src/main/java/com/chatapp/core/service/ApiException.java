package com.chatapp.core.service;

import com.chatapp.core.model.ApiError;

public class ApiException extends RuntimeException {
    private final int statusCode;
    private final String serverMessage;
    private final ApiError apiError;

    public ApiException(int statusCode, String serverMessage) {
        super(serverMessage);
        this.statusCode = statusCode;
        this.serverMessage = serverMessage;
        this.apiError = null;
    }

    public ApiException(int statusCode, ApiError apiError) {
        super(apiError != null ? apiError.getError() : null);
        this.statusCode = statusCode;
        this.serverMessage = apiError != null ? apiError.getError() : null;
        this.apiError = apiError;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getServerMessage() {
        return serverMessage;
    }

    public ApiError getApiError() {
        return apiError;
    }
}
