package com.chatapp.ui.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginView extends JFrame {
    public void setEmail(String email) {
        emailField.setText(email);
    }
    public void setPassword(String password) {
        passwordField.setText(password);
    }
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField nicknameField;
    private JButton loginButton;
    private JButton forgotPasswordButton;

    public LoginView() {
        setTitle("Chat App Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponents();
    }

    private void initComponents() {
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();
        inputPanel.add(emailLabel);
        inputPanel.add(emailField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);

        JLabel nicknameLabel = new JLabel("Nickname:");
        nicknameField = new JTextField();
        inputPanel.add(nicknameLabel);
        inputPanel.add(nicknameField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        loginButton = new JButton("Login");
        forgotPasswordButton = new JButton("Forgot Password");

        buttonPanel.add(loginButton);
        buttonPanel.add(forgotPasswordButton);

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public String getEmail() {
        return emailField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public String getNickname() {
        return nicknameField.getText();
    }

    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
    }

    public void addForgotPasswordListener(ActionListener listener) {
        forgotPasswordButton.addActionListener(listener);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void clearForm() {
        emailField.setText("");
        passwordField.setText("");
        nicknameField.setText("");
    }
}
