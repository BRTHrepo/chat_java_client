# Profil és beállítások modul részletes fejlesztési terv

## Funkciók

- Felhasználói adatok megjelenítése és módosítása (nickname, email, jelszó).
- Jelszóemlékeztető küldése.
- Szerver URL módosítása, mentése Preferences-be.

## Osztályok és metódusok

### ProfilePanel (extends JPanel)
- Konstruktor: inicializálja a profil mezőket, mentés gombot.
- Metódusok:
  - `setProfileData(User user)`
  - `addSaveListener(ActionListener)`
  - `showError(String message)`
  - `showSuccess(String message)`
  - stb.

### SettingsPanel (extends JPanel)
- Konstruktor: szerver URL mező, mentés gomb.
- Metódusok:
  - `setServerUrl(String url)`
  - `addSaveListener(ActionListener)`
  - `showError(String message)`
  - `showSuccess(String message)`
  - stb.

### ProfilePresenter
- Konstruktor: ProfilePanel, SettingsPanel, AuthService példányt kap.
- Metódusok:
  - `loadProfile()`: profil adatok betöltése.
  - `handleSaveProfile(nickname, email, password)`: adatmódosítás API-n keresztül.
  - `handleForgotPassword(email)`: jelszóemlékeztető API hívás.
  - `loadServerUrl()`: szerver URL betöltése Preferences-ből.
  - `handleSaveServerUrl(url)`: szerver URL mentése Preferences-be.
  - Hibakezelés minden API hívásnál.

## UI elemek

- JPanel, JTextField, JPasswordField, JButton, JLabel.
- Layout: BoxLayout, GridBagLayout.
- Visszajelzés: sikeres mentés, hibaüzenet.

## Adatfolyam

1. Profil adatok betöltése → mezők kitöltése.
2. Adatmódosítás → mentés gomb → API hívás → visszajelzés.
3. Jelszóemlékeztető → email megadása → API hívás → visszajelzés.
4. Szerver URL betöltése/mentése → Preferences API.

## Hibakezelés

- Minden API hívás try-catch ApiException és Exception.
- Validáció: email, jelszó, nickname ellenőrzése.
- Hibák megjelenítése a UI-ban.

## Tesztelés

- Adatmódosítás, jelszóemlékeztető, szerver URL mentése.
- Hibás adatok, szerverhiba, hálózati hiba.

## Kódolási javaslatok

- Minden UI művelet Event Dispatch Thread-en fusson.
- API hívások külön szálon (SwingWorker).
- UI frissítés thread-safe módon.

## Függőségek

- AuthService, ApiService, User model.

## Példa workflow

```java
ProfilePanel profilePanel = new ProfilePanel();
SettingsPanel settingsPanel = new SettingsPanel();
ProfilePresenter presenter = new ProfilePresenter(profilePanel, settingsPanel, authService);
presenter.loadProfile();
presenter.loadServerUrl();
profilePanel.addSaveListener(e -> presenter.handleSaveProfile(nickname, email, password));
profilePanel.addForgotPasswordListener(e -> presenter.handleForgotPassword(email));
settingsPanel.addSaveListener(e -> presenter.handleSaveServerUrl(url));
```

## További részletek

- Mentés után visszajelzés a felhasználónak.
- Validációs hibák részletes megjelenítése.
- Szerver URL módosítás után újraindítás lehetősége.
