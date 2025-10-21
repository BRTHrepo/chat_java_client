package com.chatapp.core.service;

import com.chatapp.core.model.ApiError;
import com.chatapp.core.model.Message;
import com.chatapp.core.model.User;
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
