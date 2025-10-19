package com.chatapp.ui.presenter;

import com.chatapp.core.model.User;
import com.chatapp.core.service.ApiException;
import com.chatapp.core.service.AuthService;
import com.chatapp.ui.util.ErrorMessageTranslator;
import com.chatapp.ui.view.LoginView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPresenter {
    private final LoginView loginView;
    private final AuthService authService;

    public LoginPresenter(LoginView loginView, AuthService authService) {
        this.loginView = loginView;
        this.authService = authService;
        attachListeners();
        
        // If a session is already active, auto-login
        if (authService.isLoggedIn()) {
            openMainView();
        }
    }

    private void attachListeners() {
        loginView.addLoginListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        loginView.addForgotPasswordListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleForgotPassword();
            }
        });
    }

    private void handleLogin() {
        String email = loginView.getEmail();
        String password = loginView.getPassword();
        String nickname = loginView.getNickname();

        if (email.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
            loginView.showError("All fields are required!");
            return;
        }

        try {
            // Show loading indicator (optional)
            loginView.setEnabled(false);
            loginView.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            User user = authService.login(email, password, nickname);

            if (user != null) {
                loginView.showSuccess("Login successful!");
                openMainView();
            }
            // This else block is now effectively handled by the ApiException catch block
            // This else block is now effectively handled by the ApiException catch block
            // for specific error messages. We can keep a generic message here as a fallback.
            else {
                loginView.showError("Login failed due to an unknown error.");
            }
        } catch (ApiException ex) {
            String friendlyMessage = ErrorMessageTranslator.translate(ex);
            loginView.showError(friendlyMessage);
            ex.printStackTrace();
        } finally {
            // Reset UI
            loginView.setEnabled(true);
            loginView.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void handleForgotPassword() {
        String email = loginView.getEmail();
        if (email.isEmpty()) {
            loginView.showError("Please enter your email address.");
            return;
        }

        try {
            // Show loading indicator
            loginView.setEnabled(false);
            loginView.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            authService.forgotPassword(email);
            loginView.showSuccess("If an account with this email exists, a reset link has been sent.");
            loginView.clearForm();
        } catch (ApiException ex) {
            String friendlyMessage = ErrorMessageTranslator.translate(ex);
            loginView.showError(friendlyMessage);
            ex.printStackTrace();
        } finally {
            // Reset UI
            loginView.setEnabled(true);
            loginView.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void openMainView() {
        // TODO: Implement MainView and MainPresenter
        // For now, just show a success message and close the login window
        loginView.showSuccess("Login successful! Main window will be implemented later.");
        loginView.dispose();
    }
}
