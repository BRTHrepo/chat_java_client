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
import java.util.UUID; // Import UUID

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }

            // Use the working directory as instance key for preferences
            String instanceId = System.getProperty("user.dir");
            AuthService authService = new AuthService(null, instanceId); // ApiService will be set after server URL is configured
            authService.loadSession(); // Load any saved session

            // Get the server URL using the instance ID (now the working directory)
            String serverUrl = ConfigurationPresenter.getServerUrlForInstance(instanceId);

            // Start with the configuration view if no URL is saved
            ConfigurationView configurationView = new ConfigurationView(serverUrl);
            // Pass the working directory as the instance ID to the ConfigurationPresenter
            new ConfigurationPresenter(configurationView, authService, instanceId);
            configurationView.setVisible(true);
        });
    }
}
