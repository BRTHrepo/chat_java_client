package com.chatapp.core.service;

import com.chatapp.core.model.User;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.prefs.Preferences;

public class AuthService {
    private static final String PREFS_USER_KEY = "chatapp_user";
    private static final String PREFS_TOKEN_KEY = "chatapp_token";
    private static final String PREFS_EMAIL_KEY = "chatapp_email";
    private static final String PREFS_PASSWORD_KEY = "chatapp_password";
    private static final String PREFS_SERVER_URL_KEY = "chatapp_server_url";

    private final OkHttpClient client;
    private final Gson gson;
    private final Preferences prefs;

    private User currentUser;
    private String currentToken;
    private ApiService apiService;

    public AuthService(ApiService apiService) {
        this.apiService = apiService;
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.prefs = Preferences.userNodeForPackage(AuthService.class);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getCurrentToken() {
        return currentToken;
    }

    public boolean isLoggedIn() {
        return currentUser != null && currentToken != null;
    }

    public void saveCredentials(String email, String password) {
        prefs.put(PREFS_EMAIL_KEY, email);
        prefs.put(PREFS_PASSWORD_KEY, password);
    }

    public String getSavedEmail() {
        return prefs.get(PREFS_EMAIL_KEY, null);
    }

    public String getSavedPassword() {
        return prefs.get(PREFS_PASSWORD_KEY, null);
    }

    public void clearCredentials() {
        prefs.remove(PREFS_EMAIL_KEY);
        prefs.remove(PREFS_PASSWORD_KEY);
    }

    public void logout() {
        currentUser = null;
        currentToken = null;
        clearCredentials();
    }

    public User login(String email, String password, String nickname) {
        User user = apiService.registerLogin(email, password, nickname);
        if (user != null && user.getId() > 0) {
            this.currentUser = user;
        this.currentToken = user.getToken(); // Extract token from API response
            saveCredentials(email, password);
            saveSession();
        }
        return user;
    }

    public boolean checkTokenValidity() {
        if (currentToken == null) {
            return false;
        }
        // In a real implementation, you would decode the JWT and check its expiration date
        // For now, we'll just check if the token is not null
        return true;
    }

    public void refreshTokenIfNeeded() {
        if (!checkTokenValidity() && currentUser != null) {
            String email = getSavedEmail();
            String password = getSavedPassword();
            if (email != null && password != null) {
                login(email, password, currentUser.getNickname());
            }
        }
    }

    private void saveSession() {
        if (currentUser != null && currentToken != null) {
            String userJson = gson.toJson(currentUser);
            prefs.put(PREFS_USER_KEY, userJson);
            prefs.put(PREFS_TOKEN_KEY, currentToken);
        }
    }

    public void loadSession() {
        String userJson = prefs.get(PREFS_USER_KEY, null);
        String token = prefs.get(PREFS_TOKEN_KEY, null);
        if (userJson != null && token != null) {
            try {
                this.currentUser = gson.fromJson(userJson, User.class);
                this.currentToken = token;
            } catch (Exception e) {
                clearSession();
            }
        }
    }

    public void clearSession() {
        prefs.remove(PREFS_USER_KEY);
        prefs.remove(PREFS_TOKEN_KEY);
        logout();
    }

    public void forgotPassword(String email) {
        apiService.forgotPassword(email);
    }

    public String getServerUrl() {
        String savedUrl = prefs.get(PREFS_SERVER_URL_KEY, null);
        return savedUrl != null ? savedUrl : "https://brthprog.alwaysdata.net/chat/";
    }

    public void setApiService(ApiService apiService) {
        this.apiService = apiService;
    }

    public ApiService getApiService() {
        return apiService;
    }
}
