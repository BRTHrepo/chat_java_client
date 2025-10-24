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
import java.awt.Component; // Explicitly import Component
import java.awt.Container;
import java.awt.Font; // Explicitly import Font
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MainPresenter {

    private final MainView view;
    // Egyedi API hívás flag-ek
    private final java.util.concurrent.atomic.AtomicBoolean isFriendsLoading = new java.util.concurrent.atomic.AtomicBoolean(false);
    private final java.util.concurrent.atomic.AtomicBoolean isFriendRequestsLoading = new java.util.concurrent.atomic.AtomicBoolean(false);
    private final java.util.concurrent.atomic.AtomicBoolean isMessagesLoading = new java.util.concurrent.atomic.AtomicBoolean(false);
    private final java.util.concurrent.atomic.AtomicBoolean isSendMessageRunning = new java.util.concurrent.atomic.AtomicBoolean(false);
    private final java.util.concurrent.atomic.AtomicBoolean isFriendRequestActionRunning = new java.util.concurrent.atomic.AtomicBoolean(false);
    private final java.util.concurrent.atomic.AtomicBoolean isAddFriendRunning = new java.util.concurrent.atomic.AtomicBoolean(false);
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

    public static final AtomicReference<MainPresenter> instance = new AtomicReference<>();

    public MainPresenter(MainView view, AuthService authService, ApiService apiService) {
        instance .set(this);
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
        addFontSizeMenuListeners(); // Add listeners for font size menu items
        // Saját user ID kiírása
        if (authService.getCurrentUser() != null) {
            view.setUserIdLabelText("Saját ID: " + authService.getCurrentUser().getId());
        }
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
            User selectedFriend = view.getCurrentSelectedFriend();
            Integer selectedFriendId = selectedFriend != null ? selectedFriend.getId() : null;
            loadMessages(selectedFriendId);

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

    public void setCurrentSelectedFriend() {
        view.setSelectedFriendInList(view.getCurrentSelectedFriend());
    }

    private void attachListeners() {
        view.addFriendSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {

                if (view.getSelectedFriend() != null) {
                    view.setCurrentSelectedFriend(view.getSelectedFriend());
                    User selectedFriend = view.getCurrentSelectedFriend(); // Use the new getter
                    if (selectedFriend != null) {
                        // Barát név/id frissítése
                        view.setFriendInfoLabelText("Barát: " + selectedFriend.getNickname() + " (ID: " + selectedFriend.getId() + ")");
                        loadMessages(selectedFriend.getId());
                    } else {
                        view.setFriendInfoLabelText("Barát: ");
                        // If no friend is selected, clear the chat area
                        // view.setChatMessages(java.util.Collections.emptyList());
                    }
                } else {
                    view.setFriendInfoLabelText("Barát: ");
                }

            }
        });

        // Jobb gombos menü barátra
        view.getFriendsList().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int index = view.getFriendsList().locationToIndex(e.getPoint());
                    if (index >= 0) {
                        view.getFriendsList().setSelectedIndex(index);
                        User selectedFriend = view.getFriendsList().getModel().getElementAt(index);
                        view.setCurrentSelectedFriendRequest(selectedFriend);
                        showFriendContextMenu(selectedFriend, e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int index = view.getFriendsList().locationToIndex(e.getPoint());
                    if (index >= 0) {
                        view.getFriendsList().setSelectedIndex(index);
                        User selectedFriend = view.getFriendsList().getModel().getElementAt(index);
                        showFriendContextMenu(selectedFriend, e.getComponent(), e.getX(), e.getY());
                    }
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

        // Friend request list click: show dialog
        /*
        view.getFriendRequestsList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                User selectedRequest = view.getSelectedFriendRequest();
                if (selectedRequest != null) {
                    int result = JOptionPane.showOptionDialog(
                        view,
                        "Elfogadod ezt a barátkérést?\n" + selectedRequest.getEmail(),
                        "Barátkérés",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new Object[]{"Elfogad", "Elutasít"},
                        "Elfogad"
                    );
                    if (result == JOptionPane.YES_OPTION) {
                        handleAcceptFriendRequest();
                    } else if (result == JOptionPane.NO_OPTION) {
                        handleDeclineFriendRequest();
                    }
                    // Visszaállítás, hogy ne maradjon kijelölve
                    view.getFriendRequestsList().clearSelection();
                }
            }
        });
*/
        view.addAddFriendListener(e -> {
            handleAddFriend();
        });

        view.addLogoutListener(e -> {
            handleLogout();
        });
    }

    private void addFontSizeMenuListeners() {
        view.getIncreaseFontSizeMenuItem().addActionListener(e -> handleIncreaseFontSize());
        view.getDecreaseFontSizeMenuItem().addActionListener(e -> handleDecreaseFontSize());
    }

    private void handleIncreaseFontSize() {
        adjustFontSize(1.1f); // Increase font size by 10%
    }

    private void handleDecreaseFontSize() {
        adjustFontSize(0.9f); // Decrease font size by 10%
    }

    private void adjustFontSize(float scaleFactor) {
        // Rekurzívan minden komponensre alkalmazzuk a fontméret-módosítást
        adjustFontRecursively(view, scaleFactor);
    }

    /**
     * Rekurzívan végigmegy minden komponensen és módosítja a fontméretet.
     */
    private void adjustFontRecursively(Component component, float scaleFactor) {
        if (component == null) return;
        Font font = component.getFont();
        if (font != null) {
            float newSize = font.getSize2D() * scaleFactor;
            component.setFont(font.deriveFont(newSize));
        }
        if (component instanceof JMenu) {
            JMenu menu = (JMenu) component;
            for (int i = 0; i < menu.getItemCount(); i++) {
                JMenuItem item = menu.getItem(i);
                if (item != null) adjustFontRecursively(item, scaleFactor);
            }
        }
        if (component instanceof JMenuBar) {
            JMenuBar bar = (JMenuBar) component;
            for (int i = 0; i < bar.getMenuCount(); i++) {
                JMenu menu = bar.getMenu(i);
                if (menu != null) adjustFontRecursively(menu, scaleFactor);
            }
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                adjustFontRecursively(child, scaleFactor);
            }
        }
    }

    private void adjustComponentFont(Component component, float scaleFactor) {
        if (component != null) {
            Font currentFont = component.getFont();
            float newSize = currentFont.getSize() * scaleFactor;
            component.setFont(currentFont.deriveFont(newSize));
        }
    }

    public void loadInitialData() {
        loadFriends();
        loadFriendRequests();
    }

    private void loadFriends() {
        if (isFriendsLoading.getAndSet(true)) return;
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
                        f.setHasUnreadMessages(false); // Default to false
                        List<Message> friendMessages = messageDao.getMessagesWithFriend(f.getId(), authService.getCurrentUser().getId());
                        for (Message msg : friendMessages) {
                            if (!msg.isRead()) {
                                f.setHasUnreadMessages(true);
                                break; // Found an unread message, no need to check further for this friend
                            }
                        }
                        // Update the selected friend if it's the one being processed
                        if (view.getCurrentSelectedFriend() != null && f.getId() == view.getCurrentSelectedFriend().getId()) {
                            view.setCurrentSelectedFriend(f);
                        }
                    }
                    view.setFriendsList(friends);
                    MainPresenter.this.setCurrentSelectedFriend();
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
                } finally {
                    isFriendsLoading.set(false);
                }
            }
        }.execute();
    }

    private void loadFriendRequests() {
        if (isFriendRequestsLoading.getAndSet(true)) return;
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                String token = authService.getCurrentToken();
                List<User> requestsFromServer = apiService.getFriendRequests(token); // Ez a lista tartalmazza a User objektumokat a nickname-kel

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
                } finally {
                    isFriendRequestsLoading.set(false);
                }
            }
        }.execute();
    }
    public boolean setMessageReadStatus(Integer messageId, boolean isRead) {
        return messageDao.setMessageReadStatus(messageId, isRead);
    }
    private void loadMessages(Integer friendId) {
        if (isMessagesLoading.getAndSet(true)) return;
        AtomicReference<List<Integer>> notUpdatedIdsRef = new AtomicReference<>(new java.util.ArrayList<>());

        new SwingWorker<List<Message>, Void>() {
            List<Integer> unconfirmedIds = null;

            @Override
            protected List<Message> doInBackground() throws Exception {
                String token = authService.getCurrentToken();
                // Lekérjük az összes unconfirmed szerverID-t
                unconfirmedIds = messageDao.getUnconfirmedServerIds();
                Object[] ret = apiService.getMessages(token, unconfirmedIds, null, "");
                List<Message> messagesFromServer = (List<Message>) ret[0];
                notUpdatedIdsRef.set((List<Integer>) ret[1]);
             //   System.out.println("Messages from server for friendId " + ": " + messagesFromServer);
                for (Message msg : messagesFromServer) {
                    messageDao.saveMessage(msg);
                }
                return messageDao.getMessagesWithFriend(friendId, authService.getCurrentUser().getId());
            }

            @Override
            protected void done() {
                try {
                    List<Message> messages = get();
                //    System.out.println("Messages to display in MainPresenter: " + messages);

                    // Sikeres getMessages után az elküldött ID-kat confirmed=true-ra állítjuk

                    List<Integer> not_updated_ids = notUpdatedIdsRef.get();
                    if (unconfirmedIds != null && !unconfirmedIds.isEmpty()) {
                        unconfirmedIds.removeAll(not_updated_ids);
                        messageDao.setMessagesConfirmed(unconfirmedIds);
                    }
                    messages = messageDao.getMessagesWithFriend(friendId, authService.getCurrentUser().getId());
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
                } finally {
                    isMessagesLoading.set(false);
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
        if (isSendMessageRunning.getAndSet(true)) return;
        AtomicReference<Message> msgA = new AtomicReference<>(new Message());
        new SwingWorker<com.chatapp.core.model.SendMessageResponse, Void>() {
            @Override
            protected com.chatapp.core.model.SendMessageResponse doInBackground() throws Exception {
                Message msg = msgA.get();
                msg.setSenderId(authService.getCurrentUser() != null ? authService.getCurrentUser().getId() : 0);
                msg.setReceiverId(selectedFriend.getId());
                msg.setSenderNickname(""); // opcionális, ha van
                msg.setMsgType("text");
                msg.setContent(content);
                msg.setSentDate(LocalDateTime.now().toString());
                msg.setDelivered(false);
                msg.setRead(false);
                msg.setFromMe(true);


                eventLogDao.logEvent("send_message", LocalDateTime.now().toString(), "Üzenet elküldve " + selectedFriend.getNickname() + " részére.");

                String token = authService.getCurrentToken();
              //  System.out.println("msg to send: " + msg.toString());
                return apiService.sendMessage(token, msg); // Store the response
            }

            @Override
            protected void done() {
                try {
                    com.chatapp.core.model.SendMessageResponse response = get(); // Get the response
                    if (response != null) {
                     //   System.out.println("Message sent successfully: " + response.toString());
                        view.clearMessageText();
                        loadMessages(selectedFriend.getId()); // Refresh messages
                    } else {
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
                } finally {
                    isSendMessageRunning.set(false);
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
            LoginPresenter.setLoginPresenter(loginView, authService);
            loginView.setVisible(true);
        });
    }

    private void handleAcceptFriendRequest() {
        User selectedRequest = view.getCurrentSelectedFriendRequest();
        if (selectedRequest == null) {
            view.showError("Please select a friend request to accept.");
            return;
        }
        handleFriendRequestAction(selectedRequest, "accept");
    }

    private void handleDeclineFriendRequest() {
        User selectedRequest = view.getCurrentSelectedFriendRequest();
        if (selectedRequest == null) {
            view.showError("Please select a friend request to decline.");
            return;
        }
        handleFriendRequestAction(selectedRequest, "decline");
    }

    private void handleFriendRequestAction(User user, String action) {
        if (isFriendRequestActionRunning.getAndSet(true)) return;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String token = authService.getCurrentToken();
                if (Objects.equals("decline", action)) {
                    apiService.deleteFriend(token, user.getId(), action);
                }
                if (Objects.equals("accept", action)) {
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
                    errorMessage = "An unexpected error occurred: " + (cause != null ? cause.getMessage() : e.getMessage());

                    view.showError(errorMessage);
                    e.printStackTrace();
                } finally {
                    isFriendRequestActionRunning.set(false);
                }
            }
        }.execute();
    }

    private void showFriendContextMenu(User friend, Component parent, int x, int y) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Név: " + friend.getNickname()));
        panel.add(new JLabel("Email: " + friend.getEmail()));
        panel.add(new JLabel("ID: " + friend.getId()));
        JButton deleteButton = new JButton("Barát törlése");
        panel.add(deleteButton);

        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(view), "Barát adatai", true);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(view);

        deleteButton.addActionListener(e -> {
            dialog.dispose();
            handleDeleteFriend(view.getCurrentSelectedFriendRequest());
        });

        dialog.setVisible(true);
    }

    private void handleDeleteFriend(User friend) {
        int confirm = JOptionPane.showConfirmDialog(
            view,
            "Biztosan törölni szeretnéd ezt a barátot?\n" + friend.getNickname() + " (" + friend.getEmail() + ")",
            "Barát törlése",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    String token = authService.getCurrentToken();
                    apiService.deleteFriend(token, friend.getId(), "delete");
                    return null;
                }
                @Override
                protected void done() {
                    try {
                        get();
                        view.showSuccess("Barát törölve.");
                        loadFriends();
                    } catch (Exception ex) {
                        view.showError("Hiba a barát törlésekor: " + ex.getMessage());
                    }
                }
            }.execute();
        }
    }

    private void handleAddFriend() {
        String email = view.getAddFriendNickname();
        if (email.trim().isEmpty()) {
            view.showError("Please enter a nickname to add.");
            return;
        }
        AtomicReference<Exception> existingException = new AtomicReference<>(null);
        if (isAddFriendRunning.getAndSet(true)) return;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String token = authService.getCurrentToken();
                try {
                    apiService.addFriend(token, null, null, email);
                }catch (Exception apiEx) {
                //    view.showError(apiEx.getMessage());
                    existingException.set(apiEx);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    if (existingException.get() != null) {
                        view.showError(existingException.get().getMessage());
                    }else {
                        view.showSuccess("Friend request sent to " + email + ".");
                        view.clearAddFriendNickname();
                    }

                }catch (ApiException apiEx) {
                    String errorMessage = ErrorMessageTranslator.translate(apiEx);
                    view.showError(errorMessage);
                }
                catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause();
                    String errorMessage;
                    if (cause instanceof ApiException) {
                        errorMessage = ErrorMessageTranslator.translate((ApiException) cause);
                    } else {
                        errorMessage = "An unexpected error occurred: " + (cause != null ? cause.getMessage() : e.getMessage());
                    }
                    view.showError(errorMessage);
                    e.printStackTrace();
                } finally {
                    isAddFriendRunning.set(false);
                }
            }
        }.execute();
    }

    public boolean updateMessageReadStatus(int id, boolean b) {
       return   this.messageDao.setMessageReadStatus(id, b);
    }
}
