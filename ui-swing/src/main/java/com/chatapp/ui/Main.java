package com.chatapp.ui;

import com.chatapp.core.service.ApiService;
import com.chatapp.core.service.AuthService;
import com.chatapp.ui.presenter.ConfigurationPresenter;
import com.chatapp.ui.presenter.LoginPresenter;
import com.chatapp.ui.view.ConfigurationView;
import com.chatapp.ui.view.LoginView;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.EventQueue;

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }

            // Initialize core services
            AuthService authService = new AuthService(null); // ApiService will be set after server URL is configured
            authService.loadSession(); // Load any saved session

            // Check for saved server URL
            String serverUrl = ConfigurationPresenter.getSavedServerUrl();

            // Start with the configuration view if no URL is saved
            ConfigurationView configurationView = new ConfigurationView(serverUrl);
            new ConfigurationPresenter(configurationView, authService);
            configurationView.setVisible(true);
        });
    }
}
