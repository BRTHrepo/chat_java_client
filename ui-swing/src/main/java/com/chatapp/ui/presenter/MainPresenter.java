package com.chatapp.ui.presenter;

import com.chatapp.core.model.User;
import com.chatapp.core.service.ApiService;
import com.chatapp.core.service.ApiException;
import com.chatapp.core.service.AuthService;
import com.chatapp.ui.view.LoginView;
import com.chatapp.ui.view.MainView;
import com.chatapp.ui.util.ErrorMessageTranslator;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainPresenter {

    private final MainView view;
    private final AuthService authService;
    private final ApiService apiService;

    public MainPresenter(MainView view, AuthService authService, ApiService apiService) {
        this.view = view;
        this.authService = authService;
        this.apiService = apiService;
        attachListeners();
    }

    private void attachListeners() {
        view.addFriendSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                User selectedFriend = view.getSelectedFriend();
                if (selectedFriend != null) {
                    loadMessages(selectedFriend.getId());
                }
            }
        });

        view.addSendMessageListener(e -> {
            handleSendMessage(view.getMessageText());
        });

        view.addAcceptFriendRequestListener(e -> {
            handleAcceptFriendRequest();
        });

        view.addDeclineFriendRequestListener(e -> {
            handleDeclineFriendRequest();
        });

        view.addAddFriendListener(e -> {
            handleAddFriend();
        });

        view.addLogoutListener(e -> {
            handleLogout();
        });
    }

    public void loadInitialData() {
        loadFriends();
        loadFriendRequests();
    }

    private void loadFriends() {
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                String token = authService.getCurrentToken();
                System.out.println("Loading friends with token: " + token);
                return apiService.getFriends(token);
            }

            @Override
            protected void done() {
                try {
                    List<User> friends = get();
                    view.setFriendsList(friends);
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage;
                    if (cause instanceof ApiException) {
                        errorMessage = ErrorMessageTranslator.translate((ApiException) cause);
                    } else {
                        errorMessage = "An unexpected error occurred: " + (cause != null ? cause.getMessage() : e.getMessage());
                    }
                    view.showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void loadFriendRequests() {
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                String token = authService.getCurrentToken();
                return apiService.getFriendRequests(token);
            }

            @Override
            protected void done() {
                try {
                    List<User> requests = get();
                    view.setFriendRequests(requests);
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage;
                    if (cause instanceof ApiException) {
                        errorMessage = ErrorMessageTranslator.translate((ApiException) cause);
                    } else {
                        errorMessage = "An unexpected error occurred while loading friend requests: " + (cause != null ? cause.getMessage() : e.getMessage());
                    }
                    view.showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public void loadMessages(int friendId) {
        new SwingWorker<List<com.chatapp.core.model.Message>, Void>() {
            @Override
            protected List<com.chatapp.core.model.Message> doInBackground() throws Exception {
                String token = authService.getCurrentToken();
                // The getMessages method in ApiService seems complex,
                // for now I will pass null and default values.
                // This needs to be revisited based on actual API requirements.
                return apiService.getMessages(token, null, 0, "");
            }

            @Override
            protected void done() {
                try {
                    List<com.chatapp.core.model.Message> messages = get();
                    view.setChatMessages(messages);
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage;
                    if (cause instanceof ApiException) {
                        errorMessage = ErrorMessageTranslator.translate((ApiException) cause);
                    } else {
                        errorMessage = "An unexpected error occurred: " + (cause != null ? cause.getMessage() : e.getMessage());
                    }
                    view.showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public void handleSendMessage(String content) {
        User selectedFriend = view.getSelectedFriend();
        if (selectedFriend == null) {
            view.showError("Please select a friend to send a message to.");
            return;
        }
        if (content.trim().isEmpty()) {
            return; // Don't send empty messages
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String token = authService.getCurrentToken();
                apiService.sendMessage(token, selectedFriend.getId(), "text", content, null);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    view.clearMessageText();
                    loadMessages(selectedFriend.getId()); // Refresh messages
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage;
                    if (cause instanceof ApiException) {
                        errorMessage = ErrorMessageTranslator.translate((ApiException) cause);
                    } else {
                        errorMessage = "An unexpected error occurred: " + (cause != null ? cause.getMessage() : e.getMessage());
                    }
                    view.showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public void handleLogout() {
        authService.logout();
        openLoginView();
    }

    private void openLoginView() {
        SwingUtilities.invokeLater(() -> {
            view.dispose();
            LoginView loginView = new LoginView();
            new LoginPresenter(loginView, authService);
            loginView.setVisible(true);
        });
    }

    private void handleAcceptFriendRequest() {
        User selectedRequest = view.getSelectedFriendRequest();
        if (selectedRequest == null) {
            view.showError("Please select a friend request to accept.");
            return;
        }
        handleFriendRequestAction(selectedRequest, "accept");
    }

    private void handleDeclineFriendRequest() {
        User selectedRequest = view.getSelectedFriendRequest();
        if (selectedRequest == null) {
            view.showError("Please select a friend request to decline.");
            return;
        }
        handleFriendRequestAction(selectedRequest, "decline");
    }

    private void handleFriendRequestAction(User user, String action) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String token = authService.getCurrentToken();
                apiService.deleteFriend(token, user.getId(), action);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    view.showSuccess("Friend request " + action + "ed successfully.");
                    loadFriends(); // Refresh both lists
                    loadFriendRequests();
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage;
                    if (cause instanceof ApiException) {
                        errorMessage = ErrorMessageTranslator.translate((ApiException) cause);
                    } else {
                        errorMessage = "An unexpected error occurred: " + (cause != null ? cause.getMessage() : e.getMessage());
                    }
                    view.showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void handleAddFriend() {
        String nickname = view.getAddFriendNickname();
        if (nickname.trim().isEmpty()) {
            view.showError("Please enter a nickname to add.");
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String token = authService.getCurrentToken();
                apiService.addFriend(token, nickname);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    view.showSuccess("Friend request sent to " + nickname + ".");
                    view.clearAddFriendNickname();
                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage;
                    if (cause instanceof ApiException) {
                        errorMessage = ErrorMessageTranslator.translate((ApiException) cause);
                    } else {
                        errorMessage = "An unexpected error occurred: " + (cause != null ? cause.getMessage() : e.getMessage());
                    }
                    view.showError(errorMessage);
                    e.printStackTrace();
                }
            }
        }.execute();
    }
}
