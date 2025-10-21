package com.chatapp.ui.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ProfileView extends JFrame {
    private JTextField nicknameField;
    private JTextField emailField;
    private JTextField avatarUrlField;
    private JButton saveButton;
    private JButton cancelButton;

    public ProfileView() {
        setTitle("Profil szerkesztése");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel nicknameLabel = new JLabel("Felhasználónév:");
        JLabel emailLabel = new JLabel("Email:");
        JLabel avatarUrlLabel = new JLabel("Avatar URL:");

        nicknameField = new JTextField(20);
        emailField = new JTextField(20);
        emailField.setEditable(false);
        avatarUrlField = new JTextField(20);

        saveButton = new JButton("Mentés");
        cancelButton = new JButton("Mégse");

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(nicknameLabel, gbc);
        gbc.gridx = 1;
        panel.add(nicknameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(emailLabel, gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(avatarUrlLabel, gbc);
        gbc.gridx = 1;
        panel.add(avatarUrlField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(saveButton, gbc);
        gbc.gridx = 1;
        panel.add(cancelButton, gbc);

        add(panel);
    }

    public void setNickname(String nickname) {
        nicknameField.setText(nickname);
    }

    public void setEmail(String email) {
        emailField.setText(email);
    }

    public void setAvatarUrl(String url) {
        avatarUrlField.setText(url);
    }

    public String getNickname() {
        return nicknameField.getText();
    }

    public String getAvatarUrl() {
        return avatarUrlField.getText();
    }

    public void addSaveListener(ActionListener listener) {
        saveButton.addActionListener(listener);
    }

    public void addCancelListener(ActionListener listener) {
        cancelButton.addActionListener(listener);
    }
}
