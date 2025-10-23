package com.chatapp.core.service;

import com.chatapp.core.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    private final Gson gson;

    public ApiService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.gson = new GsonBuilder().setLenient().create();
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

        if (apiError == null) {
            apiError = new ApiError();
            apiError.setError("An unexpected error occurred. HTTP status code: " + response.code());
        }

        // Próbáljuk kitölteni a route, method, userId, message mezőket, ha elérhetőek
        // route: endpoint alapján
        String requestUrl = response.request().url().encodedPath();
        String method = response.request().method();
        apiError.setRoute(requestUrl);
        apiError.setMethod(method);

        // userId keresése a kérés body-jából vagy headerből (ha van)
        String userId = null;
        Request request = response.request();
        if (request.header("Authorization") != null) {
            userId = request.header("Authorization");
        }
        apiError.setUserId(userId);

        // message: ha nincs külön, akkor az error mező tartalma
        if (apiError.getMessage() == null) {
            apiError.setMessage(apiError.getError());
        }

        throw new ApiException(response.code(), apiError);
    }

    private <T> T executeRequest(Request request, Type returnType) {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleUnsuccessfulResponse(response);
            }
            String responseBody = Objects.requireNonNull(response.body()).string();

            // If a return type is specified, try to deserialize directly into it.
            if (returnType != null) {
                try {
                    // Attempt to parse the entire response body into the specified returnType.
                    return gson.fromJson(responseBody, returnType);
                } catch (JsonSyntaxException e) {
                    // If direct deserialization fails, it might be an ApiError or an unexpected format.
                    // Try to parse it as ApiError first.
                    ApiError apiError = null;
                    try {
                        apiError = gson.fromJson(responseBody, ApiError.class);
                        // If it's an ApiError, throw it.
                        throw new ApiException(response.code(), apiError);
                    } catch (JsonSyntaxException eInner) {
                        // If it's not even a valid ApiError, throw a generic error.
                        throw new ApiException(500, "Failed to parse response into expected type: " + returnType.getTypeName() + " and not a valid ApiError. Response body: " + responseBody);
                    }
                }
            } else {
                // Original logic for when returnType is null.
                // This part is specific and assumes T is List<Message> or ApiError.
                // It needs to be robust.

                JsonElement jsonElement = gson.fromJson(responseBody, JsonElement.class);

                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    JsonElement messagesElement = jsonObject.get("messages");
                    if (messagesElement != null && messagesElement.isJsonArray()) { // Check if messagesElement exists and is an array
                        Type messageType = new TypeToken<List<Message>>() {}.getType();
                        // This cast is problematic if T is not List<Message>
                        return (T) gson.fromJson(messagesElement, messageType);
                    } else {
                        // If 'messages' key is missing or not an array, try to parse the whole object as ApiError
                        ApiError apiError = null;
                        try {
                            apiError = gson.fromJson(jsonObject, ApiError.class); // Use the whole jsonObject for ApiError
                            throw new ApiException(response.code(), apiError); // Use the actual response code
                        } catch (JsonSyntaxException eInner) {
                            throw new ApiException(500, "Unexpected JSON structure for messages: 'messages' key missing or not an array, and not a valid ApiError.");
                        }
                    }
                } else if (jsonElement.isJsonPrimitive()) {
                    System.err.println("API returned a JSON primitive instead of an object for getMessages: " + jsonElement.getAsString());
                    return null;
                } else {
                    throw new ApiException(500, "Unexpected JSON type received for messages: " + jsonElement.getClass().getSimpleName());
                }
            }
        } catch (Exception e) { // This is the outer catch block
            if (e instanceof ApiException) {
                throw (ApiException) e; // Re-throw the specific exception
            }
            // Wrap any other exception (like IOException) into an ApiException
            throw new ApiException(503, "Network or parsing error: " + e.getMessage());
        }
    }

    // Új metódus: teljes JSON válasz visszaadása Stringként
    public String registerLoginRaw(String email, String password, String nickname) {
        String url = getFullUrl("/index.php/api/registerLogin");
        JsonObject json = new JsonObject();
        json.addProperty("email", email);
        json.addProperty("password", password);
        json.addProperty("nickname", nickname);

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(url).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleUnsuccessfulResponse(response);
            }
            return response.body() != null ? response.body().string() : null;
        } catch (Exception e) {
            if (e instanceof ApiException) {
                throw (ApiException) e;
            }
            throw new ApiException(503, "Network or parsing error: " + e.getMessage());
        }
    }

    public void forgotPassword(String email) {
        String url = getFullUrl("/index.php/api/forgotPassword");
        JsonObject json = new JsonObject();
        json.addProperty("email", email);

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(url).post(body).build();
        executeRequest(request, null);
    }

    public void addFriend(String token,Integer friendId, String nickname,String email) {
        String url = getFullUrl("/index.php/api/addFriend");
        JsonObject json = new JsonObject();
        json.addProperty("nickname", nickname);
        json.addProperty("friend_id", friendId);
        json.addProperty("email", email);

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

    // TODO: Implement separate endpoints or logic for accept/decline if needed
    // For now, assuming 'action' parameter is sufficient for the server.
    // If "Friend ID is required" error persists for accept, this might need adjustment.


    public List<User> getFriends(String token) {
        String url = getFullUrl("/index.php/api/getFriends");

        // Üres JSON test, mert például nem küldünk plusz adatot a POST-ban
        RequestBody requestBody = RequestBody.create(
                "",
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .post(requestBody)  // POST metódus használata
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("getFriends Unsuccessful response code: " + response.code());
                handleUnsuccessfulResponse(response);
            }
            String responseBody = Objects.requireNonNull(response.body()).string();
            JsonElement jsonElement = gson.fromJson(responseBody, JsonElement.class);
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonElement friendsElement = jsonObject.get("friends");
                System.out.println("Friends JSON Element: " + friendsElement.toString());
                Type type = new TypeToken<List<User>>() {}.getType();
                return gson.fromJson(friendsElement, type);
            }
            // Ha a válasz nem JSON objektum, akkor üres lista visszaadása
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            if (e instanceof ApiException) {
                throw (ApiException) e;
            }
            throw new ApiException(503, "Network or parsing error: " + e.getMessage());
        }
    }


    public List<User> getFriendRequests(String token) {
        String url = getFullUrl("/index.php/api/getFriendRequests");

        // POST kéréshez létrehozunk egy üres body-t (például, ha nem kell adatot küldeni)
        RequestBody requestBody = RequestBody.create(
                "",  // Üres string, ha nincs elküldendő adat
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .post(requestBody)  // POST metódus itt van beállítva
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("getFriendRequests Unsuccessful response code: " + response.toString());
                handleUnsuccessfulResponse(response);
            }

            String responseBody = Objects.requireNonNull(response.body()).string();
            JsonElement jsonElement = gson.fromJson(responseBody, JsonElement.class);

            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonElement requestsElement = jsonObject.get("requests");
                Type type = new TypeToken<List<User>>() {}.getType();
                return gson.fromJson(requestsElement, type);
            }

            // Ha a válasz nem JSON objektum, üres lista visszaadása
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            if (e instanceof ApiException) {
                throw (ApiException) e;
            }
            throw new ApiException(503, "Network or parsing error: " + e.getMessage());
        }
    }


    public SendMessageResponse sendMessage(String token, Message message) {
        String url = getFullUrl("/index.php/api/sendMessage");
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        int receiverId = message.getReceiverId();
        builder.addFormDataPart("receiver_id", String.valueOf(receiverId));
        String msgType = message.getMsgType();
        String content = message.getContent();
        File mediaFile = null;
        if (msgType.equals("image") || msgType.equals("video") || msgType.equals("audio")) {
            mediaFile = new File(content);
        }
        builder.addFormDataPart("msg_type", msgType);

        if (mediaFile != null && mediaFile.exists()) {
            builder.addFormDataPart("media", mediaFile.getName(),
                    RequestBody.create(mediaFile, MediaType.parse("application/octet-stream")));
        } else if (content != null) {
            builder.addFormDataPart("content", content);
        }

        RequestBody body = builder.build();
        System.out.println("data to send: receiver_id=" + receiverId + ", msg_type=" + msgType + ", content=" + content + ", mediaFile=" + (mediaFile != null ? mediaFile.getName() : "null"));
        System.out.println("Body content type: " + builder.toString());
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();
        return executeRequest(request, new TypeToken<SendMessageResponse>() {}.getType());
    }

    public List<Message> getMessages(String token, List<Integer> confirmedMessageIds, Integer lastMessageId, String lastRequestDate) {
        String url = getFullUrl("/index.php/api/getMessages");
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
            System.out.println("Response received for getMessages: " + response.toString());
            if (!response.isSuccessful()) {
                handleUnsuccessfulResponse(response);
            }
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                System.out.println("Empty response body for getMessages.");
                throw new ApiException(500, "API response was empty or invalid JSON");
            }
            JsonElement jsonElement = gson.fromJson(responseBody, JsonElement.class); // Changed from JsonObject

            System.out.println("Response body for getMessages: " + responseBody);
            System.out.println("Parsed JSON Element for getMessages: " + jsonElement.toString());
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonElement messagesElement = jsonObject.get("messages");
                System.out.println("Messages JSON Element: " + (messagesElement != null ? messagesElement.toString() : "null"));
                if (messagesElement != null && messagesElement.isJsonArray()) { // Check if messagesElement exists and is an array
                    Type messageType = new TypeToken<List<GetMessageResponse>>() {}.getType();
                    List<GetMessageResponse> l = gson.fromJson(messagesElement, messageType);
                    System.out.println("Deserialized GetMessageResponse list: " + l.toString());
                    // Convert GetMessageResponse to Message
                    List<Message> messages = new java.util.ArrayList<>();
                    for (GetMessageResponse gmr : l) {
                        Message msg = new Message();
                        msg.setId(0);
                        msg.setServerId(gmr.getMessage_id()); // Assuming serverID is the same as message_id
                        msg.setSenderId(gmr.getSender_id());
                        msg.setReceiverId(gmr.getReceiver_id());
                        msg.setSenderNickname(gmr.getNickname());
                        msg.setMsgType(gmr.getMsg_type());
                        msg.setContent(gmr.getContent());
                        msg.setMedia_info(gmr.getMedia_info());
                        msg.setSentDate(gmr.getSent_date());
                        msg.setDelivered(gmr.isDelivered());
                        msg.setReadStatus(gmr.isRead_status());
                        msg.setFromMe(gmr.isIs_from_me());
                        messages.add(msg);
                    }
                    return messages;
                } else {
                    // If 'messages' key is missing or not an array, try to parse the whole object as ApiError
                    ApiError apiError = null;
                    try {
                        apiError = gson.fromJson(jsonObject, ApiError.class); // Use the whole jsonObject for ApiError
                        // If successfully parsed as ApiError, throw it
                        throw new ApiException(response.code(), apiError); // Use the actual response code
                    } catch (JsonSyntaxException e) {
                        // If it's not a valid ApiError, throw a generic error indicating unexpected structure
                        throw new ApiException(500, "Unexpected JSON structure for messages: 'messages' key missing or not an array, and not a valid ApiError.");
                    }
                }
            } else if (jsonElement.isJsonPrimitive()) {
                // Handle the case where the response is a primitive.
                System.err.println("API returned a JSON primitive instead of an object for getMessages: " + jsonElement.getAsString());
                return new java.util.ArrayList<>(); // Return empty list
            } else {
                // Handle other unexpected JSON types if necessary
                throw new ApiException(500, "Unexpected JSON type received for messages: " + jsonElement.getClass().getSimpleName());
            }
        } catch (Exception e) {
            if (e instanceof ApiException) {
                throw (ApiException) e; // Re-throw the specific exception
            }
            // Wrap any other exception (like IOException) into an ApiException
            throw new ApiException(503, "Network or parsing error: " + e.getMessage());
        }
    }
}
