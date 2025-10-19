# MainView és MainPresenter részletes fejlesztési terv

## Funkciók

- Főablak megnyitása sikeres bejelentkezés után.
- Bal oldali panel: barátok listája, keresőmező.
- Középső panel: chat ablak, üzenetek megjelenítése, új üzenet küldése.
- Jobb oldali panel: barátkérések, profil információk.
- Menü: kijelentkezés, profil szerkesztése, beállítások.

## Osztályok és metódusok

### MainView (extends JFrame)
- Konstruktor: inicializálja a paneleket, menüt, layoutot.
- Metódusok:
  - `setFriendsList(List<User> friends)`
  - `setChatMessages(List<Message> messages)`
  - `setFriendRequests(List<FriendRequest> requests)`
  - `showError(String message)`
  - `showSuccess(String message)`
  - `addFriendSelectionListener(ActionListener)`
  - `addSendMessageListener(ActionListener)`
  - stb.

### MainPresenter
- Konstruktor: MainView és AuthService példányt kap.
- Metódusok:
  - `loadFriends()`: betölti a barátokat az API-ból.
  - `loadMessages(friendId)`: betölti a kiválasztott baráttal folytatott üzeneteket.
  - `loadFriendRequests()`: betölti a barátkéréseket.
  - `handleSendMessage(content, mediaFile)`: üzenetküldés API-n keresztül.
  - `handleLogout()`: kijelentkezteti a felhasználót.
  - Hibakezelés minden API hívásnál.

## UI elemek

- JFrame, JPanel, JList, JTextArea, JTextField, JButton, JMenuBar, JFileChooser.
- Layout: BorderLayout, BoxLayout, GridBagLayout.
- Reszponzív elrendezés, színek, ikonok.

## Adatfolyam

1. Bejelentkezés után MainView példányosítása.
2. MainPresenter betölti a barátokat, barátkéréseket, üzeneteket.
3. Felhasználó barátot választ → chat panel frissül.
4. Üzenet küldése → API hívás, chat panel frissül.
5. Barátkérés elfogadása/elutasítása → lista frissül.

## Hibakezelés

- Minden API hívás try-catch ApiException és Exception.
- Hibák megjelenítése a UI-ban.

## Tesztelés

- Főablak megnyitása, navigáció, adatok betöltése.
- Barát kiválasztása, üzenetküldés, barátkérések kezelése.
- Hibás esetek (pl. szerverhiba, hálózati hiba).

## Kódolási javaslatok

- Minden UI művelet Event Dispatch Thread-en fusson (SwingUtilities.invokeLater).
- API hívások külön szálon (pl. SwingWorker).
- UI frissítés thread-safe módon.
- Minden adatváltozás után a megfelelő panel frissítése.

## Függőségek

- AuthService, ApiService, User, Message, FriendRequest modellek.

## Példa workflow

```java
MainView mainView = new MainView();
MainPresenter presenter = new MainPresenter(mainView, authService);
presenter.loadFriends();
presenter.loadFriendRequests();
mainView.addFriendSelectionListener(e -> presenter.loadMessages(selectedFriendId));
mainView.addSendMessageListener(e -> presenter.handleSendMessage(content, mediaFile));
```

## További részletek

- Profil szerkesztés, kijelentkezés, beállítások külön panelen.
- Minden művelet után visszajelzés a felhasználónak.
