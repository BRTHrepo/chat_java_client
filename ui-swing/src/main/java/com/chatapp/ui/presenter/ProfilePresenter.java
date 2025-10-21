package com.chatapp.ui.presenter;

import com.chatapp.core.model.User;
import com.chatapp.core.service.ApiService;
import com.chatapp.core.service.AuthService;
import com.chatapp.core.service.FriendDao;
import com.chatapp.ui.view.ProfileView;

import javax.swing.*;

public class ProfilePresenter {
    private final ProfileView view;
    private final AuthService authService;
    private final ApiService apiService;
    private final FriendDao friendDao;

    public ProfilePresenter(ProfileView view, AuthService authService, ApiService apiService, FriendDao friendDao) {
        this.view = view;
        this.authService = authService;
        this.apiService = apiService;
        this.friendDao = friendDao;
        init();
    }

    private void init() {
        User user = authService.getCurrentUser();
        if (user != null) {
            view.setNickname(user.getNickname());
            view.setEmail(user.getEmail());
            view.setAvatarUrl(user.getAvatarUrl());
        }

        view.addSaveListener(e -> handleSave());
        view.addCancelListener(e -> view.dispose());
    }

    private void handleSave() {
        String newNickname = view.getNickname();
        String newAvatarUrl = view.getAvatarUrl();

        User user = authService.getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(view, "Nincs bejelentkezett felhasználó!", "Hiba", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Frissítés helyben
        user.setNickname(newNickname);
        user.setAvatarUrl(newAvatarUrl);

        // DB update (ha szükséges)
        friendDao.saveFriend(user);

        // Szerver update (ha van ilyen API)
        // Példa: apiService.updateProfile(token, newNickname, newAvatarUrl);
        // Itt csak szimuláljuk:
        JOptionPane.showMessageDialog(view, "Profil mentve (szimulált szerver update)", "Siker", JOptionPane.INFORMATION_MESSAGE);

        view.dispose();
    }
}
