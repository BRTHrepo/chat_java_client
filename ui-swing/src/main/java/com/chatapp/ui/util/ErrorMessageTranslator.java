package com.chatapp.ui.util;

import com.chatapp.core.service.ApiException;

public class ErrorMessageTranslator {

    public static String translate(ApiException e) {
        String serverMessage = e.getServerMessage();
        int statusCode = e.getStatusCode();

        // Handle login/registration specific errors
        if (serverMessage.contains("Invalid credentials")) {
            return "Hibás e-mail cím vagy jelszó.";
        }
        if (serverMessage.contains("Email and password are required")) {
            return "Az e-mail cím és a jelszó megadása kötelező.";
        }
        if (serverMessage.contains("Nickname is required for registration")) {
            return "A becenév megadása kötelező a regisztrációhoz.";
        }
        if (serverMessage.contains("Registration failed")) {
            return "A regisztráció egy szerverhiba miatt sikertelen volt.";
        }

        // Handle other generic errors based on status code
        switch (statusCode) {
            case 400:
                return "Hibás kérés: " + serverMessage;
            case 401:
                return "Hitelesítési hiba: " + serverMessage;
            case 403:
                return "Hozzáférés megtagadva: " + serverMessage;
            case 404:
                return "A kért erőforrás nem található.";
            case 500:
                return "Belső szerverhiba történt. Kérjük, próbálja meg később.";
            default:
                return "Ismeretlen hiba történt (" + statusCode + "): " + serverMessage;
        }
    }
}
