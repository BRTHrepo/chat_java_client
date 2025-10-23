package com.chatapp.ui.presenter;

import com.chatapp.core.service.ApiService;
import com.chatapp.core.service.AuthService;
import com.chatapp.ui.presenter.LoginPresenter;
import com.chatapp.ui.view.ConfigurationView;
import com.chatapp.ui.view.LoginView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

public class ConfigurationPresenter {
    private static final String PREFS_SERVER_URL_KEY = "chatapp_server_url";
    private static final String DEFAULT_SERVER_URL = "https://brthprog.alwaysdata.net/chat/";

    // New static method to get server URL for a specific instance
    public static String getServerUrlForInstance(String instanceId) {
        Preferences prefs = Preferences.userNodeForPackage(ConfigurationPresenter.class).node(instanceId);
        String savedUrl = prefs.get(PREFS_SERVER_URL_KEY, null);
        String urlWithSlash = (savedUrl != null ? savedUrl : DEFAULT_SERVER_URL);
        if (!urlWithSlash.endsWith("/")) {
            urlWithSlash += "/";
        }
        return urlWithSlash;
    }

    private final ConfigurationView configurationView;
private final AuthService authService;
    private final Preferences prefs; // This will now be instance-specific
    private String instanceId; // To store the unique instance identifier

    // Modified constructor to accept a unique instanceId
    public ConfigurationPresenter(ConfigurationView configurationView, AuthService authService, String instanceId) {
        this.configurationView = configurationView;
        this.authService = authService;
        this.instanceId = instanceId; // Store the instance ID
        // Create a unique preference node for this instance using the instanceId
        this.prefs = Preferences.userNodeForPackage(ConfigurationPresenter.class).node(this.instanceId);
        attachListeners();
    }

    private void attachListeners() {
        configurationView.addSaveListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSave();
            }
        });

        configurationView.addCancelListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCancel();
            }
        });
    }

    private void handleSave() {
        String serverUrl = configurationView.getServerUrl();
        if (serverUrl.isEmpty()) {
            configurationView.showError("A szerver URL nem lehet üres!");
            return;
        }

        try {
            // Normalize the server URL by removing trailing slashes
            serverUrl = serverUrl.replaceAll("/+$", "");
            
            // Save the server URL to preferences
            prefs.put(PREFS_SERVER_URL_KEY, serverUrl);
            configurationView.showSuccess("Szerver URL sikeresen mentve!");
            
            // Close the configuration window and open the login window
            configurationView.dispose();
            openLoginView();
        } catch (Exception ex) {
            configurationView.showError("Hiba a mentés során: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void handleCancel() {
        int option = JOptionPane.showConfirmDialog(
            configurationView,
            "A szerver URL mentése nélkül folytatod? Az alapértelmezett URL lesz használva.",
            "Mégse",
            JOptionPane.YES_NO_OPTION
        );

        if (option == JOptionPane.YES_OPTION) {
            configurationView.dispose();
            openLoginView();
        }
    }

private void openLoginView() {
        EventQueue.invokeLater(() -> {
            try {
                String serverUrl = getCurrentServerUrl(); // Use instance method to get URL
                ApiService apiService = new ApiService(serverUrl);
                authService.setApiService(apiService);

                LoginView loginView = new LoginView();
                // Assuming LoginPresenter can be instantiated without needing the instanceId directly,
                // as it will use the ApiService which is already configured with the correct server URL.
                new LoginPresenter(loginView, authService);
                loginView.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

// New instance method to get the server URL from this instance's preferences
    public String getCurrentServerUrl() {
        String savedUrl = this.prefs.get(PREFS_SERVER_URL_KEY, null);
        // Use the default URL if no saved URL is found for this instance
        String urlWithSlash = (savedUrl != null ? savedUrl : DEFAULT_SERVER_URL);
        // Add a trailing slash to the URL for the ApiService if it's missing
        if (!urlWithSlash.endsWith("/")) {
            urlWithSlash += "/";
        }
        return urlWithSlash;
    }

// Removed static clearSavedServerUrl() as preferences are now instance-specific.
    // If clearing preferences for a specific instance is needed, a new instance method would be required.
}
