package com.chatapp.ui.view;

import com.chatapp.core.model.Message;
import com.chatapp.core.model.User;

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
    private AtomicReference<User> currentSelectedFriend = new AtomicReference<>(null); // Variable to store the currently selected friend

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

        // Left Panel - Friends List
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Friends"));
        friendsList = new JList<>();
        leftPanel.add(new JScrollPane(friendsList), BorderLayout.CENTER);

        JPanel addFriendPanel = new JPanel(new BorderLayout());
        addFriendField = new JTextField();
        addFriendButton = new JButton("Add");
        addFriendPanel.add(addFriendField, BorderLayout.CENTER);
        addFriendPanel.add(addFriendButton, BorderLayout.EAST);
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

        // Polling gomb a chat panel tetején
        JPanel pollingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        manualPollingButton = new JButton("Frissítés (Polling)");
        pollingPanel.add(manualPollingButton);
        centerPanel.add(pollingPanel, BorderLayout.NORTH);

        centerPanel.add(messageInputPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // Right Panel - Friend Requests
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Friend Requests"));
        friendRequestsList = new JList<>();
        rightPanel.add(new JScrollPane(friendRequestsList), BorderLayout.CENTER);

        JPanel requestButtonsPanel = new JPanel(new FlowLayout());
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
        if (!message.isConfirmed()) {
            StyleConstants.setForeground(attr, Color.RED);
        } else {
            StyleConstants.setForeground(attr, Color.BLACK);
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

    public User getSelectedFriendRequest() {
        return friendRequestsList.getSelectedValue();
    }

    public void setFriendRequests(List<User> requests) {
        DefaultListModel<User> model = new DefaultListModel<>();
        for (User request : requests) {
            model.addElement(request);
        }
        friendRequestsList.setModel(model);
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
}
