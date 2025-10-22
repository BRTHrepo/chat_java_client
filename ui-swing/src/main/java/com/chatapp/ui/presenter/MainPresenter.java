package com.chatapp.ui.presenter;

import com.chatapp.core.model.Message;
import com.chatapp.core.model.User;
import com.chatapp.core.service.ApiService;
import com.chatapp.core.service.ApiException;
import com.chatapp.core.service.AuthService;
import com.chatapp.ui.view.LoginView;
import com.chatapp.ui.view.MainView;
import com.chatapp.ui.util.ErrorMessageTranslator;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainPresenter {

    private final MainView view;
    private final AuthService authService;
    private final ApiService apiService;

    // Perzisztencia réteg
    private final com.chatapp.core.service.DBService dbService;
    private final com.chatapp.core.service.FriendDao friendDao;
    private final com.chatapp.core.service.MessageDao messageDao;
    private final com.chatapp.core.service.FriendRequestDao friendRequestDao;
    private final com.chatapp.core.service.EventLogDao eventLogDao;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean isPolling = new AtomicBoolean(false);
    private int pollingPeriodSeconds = 5; // alapértelmezett 5 mp

    public MainPresenter(MainView view, AuthService authService, ApiService apiService) {
        this.view = view;
        this.authService = authService;
        this.apiService = apiService;

        // Perzisztencia réteg példányosítása
        this.dbService = new com.chatapp.core.service.DBService();
        this.friendDao = new com.chatapp.core.service.FriendDao(dbService);
        this.messageDao = new com.chatapp.core.service.MessageDao(dbService);
        this.friendRequestDao = new com.chatapp.core.service.FriendRequestDao(dbService);
        this.eventLogDao = new com.chatapp.core.service.EventLogDao(dbService);

        attachListeners();
        addPollingListeners();
        addProfileMenuListener();
        startPolling();

        // Polling szál leállítása ablak bezárásakor is
        this.view.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                scheduler.shutdownNow();
            }
        });
    }

    private void startPolling() {
        scheduler.scheduleAtFixedRate(this::pollingTask, 0, pollingPeriodSeconds, TimeUnit.SECONDS);
    }

    public void setPollingPeriod(int seconds) {
        this.pollingPeriodSeconds = seconds;
        scheduler.shutdownNow();
        // Újraindítás új periódussal
        ScheduledExecutorService newScheduler = Executors.newSingleThreadScheduledExecutor();
        newScheduler.scheduleAtFixedRate(this::pollingTask, 0, pollingPeriodSeconds, TimeUnit.SECONDS);
    }

    public void manualPolling() {
        pollingTask();
    }

    private void pollingTask() {
        if (isPolling.getAndSet(true)) {
            return; // Már fut egy polling, ne indítsunk újat
        }
        try {
            authService.refreshTokenIfNeeded();
            loadFriends();
            loadFriendRequests();
            // Ha szükséges, loadMessages is hívható itt
        } finally {
            isPolling.set(false);
        }
    }

    private void addPollingListeners() {
        view.addManualPollingListener(e -> manualPolling());
        view.addSetPollingPeriodListener(e -> {
            String input = JOptionPane.showInputDialog(view, "Polling periódus (másodperc):", pollingPeriodSeconds);
            if (input != null) {
                try {
                    int seconds = Integer.parseInt(input.trim());
                    if (seconds > 0) {
                        setPollingPeriod(seconds);
                        JOptionPane.showMessageDialog(view, "Új polling periódus: " + seconds + " mp");
                    } else {
                        JOptionPane.showMessageDialog(view, "A periódusnak pozitív egész számnak kell lennie.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(view, "Érvénytelen számformátum.");
                }
            }
        });
    }

    private void addProfileMenuListener() {
        view.addProfileListener(e -> openProfileView());
    }

    private void openProfileView() {
        SwingUtilities.invokeLater(() -> {
            com.chatapp.ui.view.ProfileView profileView = new com.chatapp.ui.view.ProfileView();
            new com.chatapp.ui.presenter.ProfilePresenter(
                profileView,
                authService,
                apiService,
                friendDao
            );
            profileView.setVisible(true);
        });
    }

    private void attachListeners() {
        view.addFriendSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                view.setCurrentSelectedFriend(   view.getSelectedFriend());
                User selectedFriend = view.getCurrentSelectedFriend(); // Use the new getter
                if (selectedFriend != null) {
                    loadMessages(selectedFriend.getId());
                } else {
                    // If no friend is selected, clear the chat area
                    view.setChatMessages(java.util.Collections.emptyList());
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
                List<User> friendsFromServer = apiService.getFriends(token);

                // DB frissítés: törlés és mentés
                friendDao.deleteAllFriends();
                for (User user : friendsFromServer) {
                    friendDao.saveFriend(user);
                }
                // Eseménylog mentése
                eventLogDao.logEvent("friends_sync", java.time.LocalDateTime.now().toString(), "Barátlista szinkronizálva a szerverrel.");
                // Mindig DB-ből olvasunk a UI-hoz
                return friendDao.getAllFriends();
            }

            @Override
            protected void done() {
                try {
                    List<User> friends = get();
                    for (User f : friends) {
                        System.out.println("Loaded friend: " + f.toString());
                    }
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
                List<User> requestsFromServer = apiService.getFriendRequests(token); // Ez a lista tartalmazza a User objektumokat a nickname-kel

                // A barátkérések megjelenítéséhez szükséges felhasználói adatok (pl. nickname)
                // már elérhetők a requestsFromServer listában.
                // A korábbi implementáció csak az ID-kat mentette el az adatbázisba,
                // ami miatt a felhasználónév nem jelent meg.
                // A UI-nak átadott listát most közvetlenül a szerverről kapott adatokkal töltjük fel.
                // A perzisztencia réteg frissítése (ha szükséges) külön feladat lehet.

                return requestsFromServer; // Visszaadjuk a szerverről kapott User objektumokat
            }

            @Override
            protected void done() {
                try {
                    List<User> requests = get(); // Ez most már a requestsFromServer lesz
                    view.setFriendRequests(requests); // Ez most már helyesen fogja megjeleníteni a nickname-eket
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
                List<com.chatapp.core.model.Message> messagesFromServer = apiService.getMessages(token, null, friendId, "");
                // DB frissítés: opcionálisan törölhetjük a partnerhez tartozó üzeneteket, de most csak beszúrjuk az újakat
                for (com.chatapp.core.model.Message msg : messagesFromServer) {
                    messageDao.saveMessage(msg);
                }
                // Mindig DB-ből olvasunk a UI-hoz
                return messageDao.getMessagesWithFriend(friendId);
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
        User selectedFriend = view.getCurrentSelectedFriend(); // Use the new getter
        if (selectedFriend == null) {
            view.showError("Please select a friend to send a message to.");
            return;
        }
        if (content.trim().isEmpty()) {
            return; // Don't send empty messages
        }

        new SwingWorker<com.chatapp.core.model.SendMessageResponse, Void>() {
            @Override
            protected com.chatapp.core.model.SendMessageResponse doInBackground() throws Exception {
                // Először DB-be mentjük az üzenetet
                Message msg = new Message();
                msg.setSenderId(authService.getCurrentUser() != null ? authService.getCurrentUser().getId() : 0);
                msg.setReceiverId(selectedFriend.getId());
                msg.setSenderNickname(""); // opcionális, ha van
                msg.setMsgType("text");
                msg.setContent(content);
                msg.setSentDate(LocalDateTime.now().toString());
                msg.setDelivered(false);
                msg.setRead(false);
                msg.setFromMe(true);
                messageDao.saveMessage(msg);

                // Eseménylog mentése
                eventLogDao.logEvent("send_message", LocalDateTime.now().toString(), "Üzenet elküldve " + selectedFriend.getNickname() + " részére.");

                // Majd szerverre küldjük
                String token = authService.getCurrentToken();
                System.out.println("msg to send: " + msg.toString());
                return apiService.sendMessage(token, msg); // Store the response
            }

            @Override
            protected void done() {
                try {
                    com.chatapp.core.model.SendMessageResponse response = get(); // Get the response
                    if (response != null) {
                        // Handle successful response, e.g., log it or update UI if needed
                        System.out.println("Message sent successfully: " + response.toString());
                        view.clearMessageText();
                        loadMessages(selectedFriend.getId()); // Refresh messages
                    } else {
                        // This case should ideally not happen if ApiService throws ApiException on error
                        // but as a fallback, show a generic error.
                        view.showError("Failed to send message. Unknown error occurred.");
                    }
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
        scheduler.shutdownNow();
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
                if (Objects.equals( "decline",action)) {
                    apiService.deleteFriend(token, user.getId(), action);
                }
                if (Objects.equals( "accept",action)) {
                    apiService.addFriend(token, user.getId(), user.getNickname(), user.getEmail());
                }
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
        String email = view.getAddFriendNickname();
        if (email.trim().isEmpty()) {
            view.showError("Please enter a nickname to add.");
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String token = authService.getCurrentToken();
                apiService.addFriend(token,null, null,email);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    view.showSuccess("Friend request sent to " + email + ".");
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
