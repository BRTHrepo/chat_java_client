package com.chatapp.ui.view;

import com.chatapp.core.model.Message;
import com.chatapp.core.model.User;
import com.chatapp.ui.presenter.MainPresenter;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.StyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class MainView extends JFrame {

    private JList<User> friendsList;
private JTextPane chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JMenuBar menuBar;
    private JMenuItem logoutMenuItem;
    private JMenuItem profileMenuItem;
    private JMenuItem setPollingPeriodMenuItem;
    private JMenuItem increaseFontSizeMenuItem;
    private JMenuItem decreaseFontSizeMenuItem;
    private JButton manualPollingButton;
    private JList<User> friendRequestsList;
    private JButton acceptFriendRequestButton;
    private JButton declineFriendRequestButton;
    private JTextField addFriendField;
    private JButton addFriendButton;
    private JLabel userIdLabel;
    private JLabel friendInfoLabel;
    private AtomicReference<User> currentSelectedFriend = new AtomicReference<>(null); // Variable to store the currently selected friend
    private AtomicReference<User> currentSelectedFriendRequest = new AtomicReference<>(null); // Variable to store the currently selected friend request
    public MainView() {
        setTitle("Chat Application");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        initComponents();
        // Alapértelmezett betűméret növelése minden komponensen
        increaseDefaultFontSize(2.0f);
    }

    /**
     * Minden komponens betűméretét megnöveli a megadott értékkel (pontban).
     */
    private void increaseDefaultFontSize(float increment) {
        adjustFontRecursively(this, increment);
    }

    private void adjustFontRecursively(Component component, float increment) {
        if (component == null) return;
        Font font = component.getFont();
        if (font != null) {
            float newSize = font.getSize2D() + increment;
            component.setFont(font.deriveFont(newSize));
        }
        if (component instanceof JMenu) {
            JMenu menu = (JMenu) component;
            for (int i = 0; i < menu.getItemCount(); i++) {
                JMenuItem item = menu.getItem(i);
                if (item != null) adjustFontRecursively(item, increment);
            }
        }
        if (component instanceof JMenuBar) {
            JMenuBar bar = (JMenuBar) component;
            for (int i = 0; i < bar.getMenuCount(); i++) {
                JMenu menu = bar.getMenu(i);
                if (menu != null) adjustFontRecursively(menu, increment);
            }
        }
        if (component instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) component).getComponents()) {
                adjustFontRecursively(child, increment);
            }
        }
    }

    private void initComponents() {
        // Main layout
        setLayout(new BorderLayout());

        // Menu Bar
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        logoutMenuItem = new JMenuItem("Logout");
        profileMenuItem = new JMenuItem("Profil...");
        fileMenu.add(profileMenuItem);
        fileMenu.add(logoutMenuItem);

        JMenu pollingMenu = new JMenu("Polling");
        setPollingPeriodMenuItem = new JMenuItem("Set Polling Period...");
        pollingMenu.add(setPollingPeriodMenuItem);

        JMenu viewMenu = new JMenu("View"); // New menu for font size adjustments
        increaseFontSizeMenuItem = new JMenuItem("Increase Font Size");
        decreaseFontSizeMenuItem = new JMenuItem("Decrease Font Size");
        viewMenu.add(increaseFontSizeMenuItem);
        viewMenu.add(decreaseFontSizeMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(pollingMenu);
        menuBar.add(viewMenu); // Add the new View menu to the menu bar
        setJMenuBar(menuBar);

        // Left Panel - Friends List (keskenyebb)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(200, 0));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Friends"));
        friendsList = new JList<>();
        leftPanel.add(new JScrollPane(friendsList), BorderLayout.CENTER);

        JPanel addFriendPanel = new JPanel(new GridLayout(3, 1, 2, 2));
        JLabel addFriendLabel = new JLabel("Email :");
        addFriendField = new JTextField();
        addFriendButton = new JButton("Add");
        addFriendPanel.add(addFriendLabel);
        addFriendPanel.add(addFriendField);
        addFriendPanel.add(addFriendButton);
        leftPanel.add(addFriendPanel, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);

        // Center Panel - Chat
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Chat"));
chatArea = new JTextPane();
chatArea.setEditable(false);
// Increase default font size for chatArea
Font currentChatFont = chatArea.getFont();
chatArea.setFont(currentChatFont.deriveFont(currentChatFont.getSize() + 2.0f)); // Increase by 2 points
centerPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel messageInputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        // Increase default font size for messageField
        Font currentMessageFieldFont = messageField.getFont();
        messageField.setFont(currentMessageFieldFont.deriveFont(currentMessageFieldFont.getSize() + 2.0f)); // Increase by 2 points

        sendButton = new JButton("Send");
        messageInputPanel.add(messageField, BorderLayout.CENTER);
        messageInputPanel.add(sendButton, BorderLayout.EAST);

        // Polling gomb + user infók a chat panel tetején
        JPanel pollingPanel = new JPanel();
        pollingPanel.setLayout(new BoxLayout(pollingPanel, BoxLayout.Y_AXIS));
        JPanel pollingRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        manualPollingButton = new JButton("Frissítés (Polling)");
        pollingRow.add(manualPollingButton);
        pollingPanel.add(pollingRow);

        userIdLabel = new JLabel("Saját ID: ");
        friendInfoLabel = new JLabel("Barát: ");
        pollingPanel.add(userIdLabel);
        pollingPanel.add(friendInfoLabel);

        centerPanel.add(pollingPanel, BorderLayout.NORTH);

        centerPanel.add(messageInputPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // Right Panel - Friend Requests (keskenyebb)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(140, 0));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Friend Requests"));
        friendRequestsList = new JList<>();
        rightPanel.add(new JScrollPane(friendRequestsList), BorderLayout.CENTER);

        JPanel requestButtonsPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        acceptFriendRequestButton = new JButton("Accept");
        declineFriendRequestButton = new JButton("Decline");
        requestButtonsPanel.add(acceptFriendRequestButton);
        requestButtonsPanel.add(declineFriendRequestButton);
        rightPanel.add(requestButtonsPanel, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.EAST);
    }

    public JButton getManualPollingButton() {
        return manualPollingButton;
    }

    public JMenuItem getSetPollingPeriodMenuItem() {
        return setPollingPeriodMenuItem;
    }

    public void addManualPollingListener(ActionListener listener) {
        manualPollingButton.addActionListener(listener);
    }

    public void addSetPollingPeriodListener(ActionListener listener) {
        setPollingPeriodMenuItem.addActionListener(listener);
    }

    public void setFriendsList(List<User> friends) {
        DefaultListModel<User> model = new DefaultListModel<>();
        for (User friend : friends) {
            model.addElement(friend);
        }
        friendsList.setModel(model);

        friendsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User user) {
                    label.setText(user.getNickname()); // vagy bármilyen formátum
                    if (user.hasUnreadMessages()) {
                        label.setForeground(Color.RED); // Set text color to red for unread messages
                    } else {
                        label.setForeground(Color.BLACK); // Default color for read messages
                    }
                }
                return label;
            }
        });

    }

public void setChatMessages(List<Message> messages) {
    chatArea.setText("");
    if (messages == null) return;
    StyledDocument doc = chatArea.getStyledDocument();
    Font font = chatArea.getFont();
    for (Message message : messages) {
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attr, font.getFamily());
        StyleConstants.setFontSize(attr, font.getSize());
        StyleConstants.setForeground(attr, Color.BLACK);
        if (!message.isConfirmed()) {
            StyleConstants.setForeground(attr, Color.RED);
        }
        if (!message.isRead())
        {
            StyleConstants.setForeground(attr, Color.YELLOW);
        }
        if ( !message.isRead()) {
            message.setRead(true); //   Mark message as read when displaying
            boolean ok = MainPresenter.instance.get().updateMessageReadStatus(message.getId(), true);
        }
        String line = String.format("%s: %s\n", message.getSenderNickname(), message.getContent());
        try {
            doc.insertString(doc.getLength(), line, attr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void addFriendSelectionListener(ListSelectionListener listener) {
        friendsList.addListSelectionListener(listener);
    }
    public void addFriendRequestsSelectionListener(ListSelectionListener o) {
        friendRequestsList.addListSelectionListener( o);
    }

    public void addSendMessageListener(ActionListener listener) {
        sendButton.addActionListener(listener);
    }

    public String getMessageText() {
        return messageField.getText();
    }

    public void clearMessageText() {
        messageField.setText("");
    }

    public User getSelectedFriend() {
        return friendsList.getSelectedValue();
    }


    public void setFriendRequests(List<User> requests) {
        DefaultListModel<User> model = new DefaultListModel<>();
        for (User request : requests) {
            model.addElement(request);
        }
        friendRequestsList.setModel(model);
        friendRequestsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User user) {
                    label.setText(user.getEmail());
                }
                return label;
            }
        });
        friendRequestsList.addListSelectionListener(e -> {
                User selectedRequest = friendRequestsList.getSelectedValue();
            System.out.println("Selected friend request: " + selectedRequest);
                if (selectedRequest == null  &&      currentSelectedFriendRequest.get() != null){
                     friendRequestsList.setSelectedValue(currentSelectedFriendRequest.get(),  true);
                }
                selectedRequest = friendRequestsList.getSelectedValue();
                setCurrentSelectedFriendRequest(selectedRequest);
        });
    }

    public void addAcceptFriendRequestListener(ActionListener listener) {
        acceptFriendRequestButton.addActionListener(listener);
    }

    public void addDeclineFriendRequestListener(ActionListener listener) {
        declineFriendRequestButton.addActionListener(listener);
    }

    public String getAddFriendNickname() {
        return addFriendField.getText();
    }

    public void clearAddFriendNickname() {
        addFriendField.setText("");
    }

    public void addAddFriendListener(ActionListener listener) {
        addFriendButton.addActionListener(listener);
    }

    public void addProfileListener(ActionListener listener) {
        profileMenuItem.addActionListener(listener);
    }

    public void addLogoutListener(ActionListener listener) {
        logoutMenuItem.addActionListener(listener);
    }

    /**
     * Returns the currently selected friend.
     * @return The selected User object, or null if none is selected.
     */
    public User getCurrentSelectedFriend() {
        return currentSelectedFriend.get();
    }

    public void setCurrentSelectedFriend(User selectedFriend) {
        currentSelectedFriend.set(selectedFriend);
    }
    public void setCurrentSelectedFriendRequest(User selectedFriendRequest){
        currentSelectedFriendRequest.set(selectedFriendRequest);
    }
    public JMenuItem getIncreaseFontSizeMenuItem() {
        return increaseFontSizeMenuItem;
    }

    public JMenuItem getDecreaseFontSizeMenuItem() {
        return decreaseFontSizeMenuItem;
    }

public JTextPane getChatArea() {
    return chatArea;
}

    public JTextField getMessageField() {
        return messageField;
    }

    public JList<User> getFriendsList() {
        return friendsList;
    }

    public JList<User> getFriendRequestsList() {
        return friendRequestsList;
    }

    public JMenuBar getMainMenuBar() { // Renamed to avoid clash with Frame.getMenuBar()
        return menuBar;
    }

    public void setSelectedFriendInList(User user) {
        friendsList.setSelectedValue(user, true);
    }

    public void setUserIdLabelText(String text) {
        userIdLabel.setText(text);
    }

    public void setFriendInfoLabelText(String text) {
        friendInfoLabel.setText(text);
    }

    public User getCurrentSelectedFriendRequest() {
        return currentSelectedFriendRequest.get();
    }
}
