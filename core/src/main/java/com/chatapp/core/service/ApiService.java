package com.chatapp.core.service;

import com.chatapp.core.model.ApiError;
import com.chatapp.core.model.Message;
import com.chatapp.core.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class ApiService {
    private final String baseUrl;
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public ApiService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private String getFullUrl(String endpoint) {
        String normalizedEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
        return baseUrl + (baseUrl.endsWith("/") ? "" : "/") + normalizedEndpoint;
    }

    private void handleUnsuccessfulResponse(Response response) throws IOException {
        String responseBody = Objects.requireNonNull(response.body()).string();
        ApiError apiError = null;
        try {
            apiError = gson.fromJson(responseBody, ApiError.class);
        } catch (JsonSyntaxException e) {
            // The response body was not a valid JSON or did not match ApiError structure
        }

        String errorMessage = (apiError != null && apiError.getError() != null)
                ? apiError.getError()
                : "An unexpected error occurred. HTTP status code: " + response.code();

        throw new ApiException(response.code(), errorMessage);
    }

    private <T> T executeRequest(Request request, Type returnType) {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleUnsuccessfulResponse(response);
            }
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (returnType == null) {
                return null; // For void methods
            }
            return gson.fromJson(responseBody, returnType);
        } catch (Exception e) {
            if (e instanceof ApiException) {
                throw (ApiException) e; // Re-throw the specific exception
            }
            // Wrap any other exception (like IOException) into an ApiException
            throw new ApiException(503, "Network or parsing error: " + e.getMessage());
        }
    }

    public User registerLogin(String email, String password, String nickname) {
        String url = getFullUrl("/index.php/api/registerLogin");
        JsonObject json = new JsonObject();
        json.addProperty("email", email);
        json.addProperty("password", password);
        json.addProperty("nickname", nickname);

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(url).post(body).build();
        return executeRequest(request, User.class);
    }

    public void forgotPassword(String email) {
        String url = getFullUrl("/index.php/api/forgotPassword");
        JsonObject json = new JsonObject();
        json.addProperty("email", email);

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(url).post(body).build();
        executeRequest(request, null);
    }

    public void addFriend(String token, String nickname) {
        String url = getFullUrl("/index.php/api/addFriend");
        JsonObject json = new JsonObject();
        json.addProperty("nickname", nickname);

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();
        executeRequest(request, null);
    }

    public void deleteFriend(String token, int friendId, String action) {
        String url = getFullUrl("/index.php/api/deleteFriend");
        JsonObject json = new JsonObject();
        json.addProperty("friend_id", friendId);
        json.addProperty("action", action);

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();
        executeRequest(request, null);
    }

    public List<User> getFriends(String token) {
        String url = getFullUrl("/index.php/api/getFriends");
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .post(RequestBody.create("", MediaType.get("application/json; charset=utf-8")))
                .build();
        // Special handling for nested JSON
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleUnsuccessfulResponse(response);
            }
            String responseBody = Objects.requireNonNull(response.body()).string();
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            JsonElement friendsElement = jsonObject.get("friends");
            Type type = new TypeToken<List<User>>() {}.getType();
            return gson.fromJson(friendsElement, type);
        } catch (Exception e) {
            if (e instanceof ApiException) {
                throw (ApiException) e;
            }
            throw new ApiException(503, "Network or parsing error: " + e.getMessage());
        }
    }

    public List<User> getFriendRequests(String token) {
        String url = getFullUrl("/index.php/api/getFriendRequests");
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .post(RequestBody.create("", MediaType.get("application/json; charset=utf-8")))
                .build();
        // Special handling for nested JSON
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleUnsuccessfulResponse(response);
            }
            String responseBody = Objects.requireNonNull(response.body()).string();
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            JsonElement requestsElement = jsonObject.get("requests");
            Type type = new TypeToken<List<User>>() {}.getType();
            return gson.fromJson(requestsElement, type);
        } catch (Exception e) {
            if (e instanceof ApiException) {
                throw (ApiException) e;
            }
            throw new ApiException(503, "Network or parsing error: " + e.getMessage());
        }
    }

    public void sendMessage(String token, int receiverId, String msgType, String content, File mediaFile) {
        String url = getFullUrl("/index.php/api/sendMessage");
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        builder.addFormDataPart("receiver_id", String.valueOf(receiverId));
        builder.addFormDataPart("msg_type", msgType);

        if (mediaFile != null && mediaFile.exists()) {
            builder.addFormDataPart("media", mediaFile.getName(),
                    RequestBody.create(mediaFile, MediaType.parse("application/octet-stream")));
        } else if (content != null) {
            builder.addFormDataPart("content", content);
        }

        RequestBody body = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();
        executeRequest(request, null);
    }

    public List<Message> getMessages(String token, List<Integer> confirmedMessageIds, int lastMessageId, String lastRequestDate) {
        String url = getFullUrl("/getMessages");
        JsonObject json = new JsonObject();
        if (confirmedMessageIds != null && !confirmedMessageIds.isEmpty()) {
            json.add("confirmed_message_ids", gson.toJsonTree(confirmedMessageIds).getAsJsonArray());
        }
        json.addProperty("last_message_id", lastMessageId);
        json.addProperty("last_request_date", lastRequestDate);

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();
        // Special handling for nested JSON
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleUnsuccessfulResponse(response);
            }
            String responseBody = Objects.requireNonNull(response.body()).string();
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            JsonElement messagesElement = jsonObject.get("messages");
            Type messageType = new TypeToken<List<Message>>() {}.getType();
            return gson.fromJson(messagesElement, messageType);
        } catch (Exception e) {
            if (e instanceof ApiException) {
                throw (ApiException) e;
            }
            throw new ApiException(503, "Network or parsing error: " + e.getMessage());
        }
    }
}
