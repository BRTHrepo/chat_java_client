package com.chatapp.ui.view;

import javax.swing.*;
import java.awt.*;

public class ConfigurationView extends JFrame {
    private JTextField serverUrlField;
    private JButton saveButton;
    private JButton cancelButton;

    public ConfigurationView(String defaultUrl) {
        setTitle("Szerver Konfiguráció");
        setSize(500, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponents(defaultUrl);
    }

    private void initComponents(String defaultUrl) {
        JPanel inputPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel serverUrlLabel = new JLabel("Szerver URL:");
        serverUrlField = new JTextField(defaultUrl);
        inputPanel.add(serverUrlLabel);
        inputPanel.add(serverUrlField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        saveButton = new JButton("Mentés");
        cancelButton = new JButton("Mégse");

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public String getServerUrl() {
        return serverUrlField.getText().trim();
    }

    public void addSaveListener(java.awt.event.ActionListener listener) {
        saveButton.addActionListener(listener);
    }

    public void addCancelListener(java.awt.event.ActionListener listener) {
        cancelButton.addActionListener(listener);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Hiba", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Sikeres", JOptionPane.INFORMATION_MESSAGE);
    }
}
