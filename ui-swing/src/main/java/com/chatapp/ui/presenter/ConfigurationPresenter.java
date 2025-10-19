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
    private static final String DEFAULT_SERVER_URL = "https://brthprog.alwaysdata.net";

    private final ConfigurationView configurationView;
    private final AuthService authService;
    private final Preferences prefs;

    public ConfigurationPresenter(ConfigurationView configurationView, AuthService authService) {
        this.configurationView = configurationView;
        this.authService = authService;
        this.prefs = Preferences.userNodeForPackage(ConfigurationPresenter.class);
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
                String serverUrl = getSavedServerUrl();
                ApiService apiService = new ApiService(serverUrl);
                authService.setApiService(apiService);

                LoginView loginView = new LoginView();
                new LoginPresenter(loginView, authService);
                loginView.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public static String getSavedServerUrl() {
        Preferences prefs = Preferences.userNodeForPackage(ConfigurationPresenter.class);
        String savedUrl = prefs.get(PREFS_SERVER_URL_KEY, null);
        // Add a trailing slash to the URL for the ApiService
        String urlWithSlash = (savedUrl != null ? savedUrl : DEFAULT_SERVER_URL);
        if (!urlWithSlash.endsWith("/")) {
            urlWithSlash += "/";
        }
        return urlWithSlash;
    }

    public static void clearSavedServerUrl() {
        Preferences prefs = Preferences.userNodeForPackage(ConfigurationPresenter.class);
        prefs.remove(PREFS_SERVER_URL_KEY);
    }
}
