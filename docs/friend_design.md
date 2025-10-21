# Barátkezelés modul részletes fejlesztési terv

## Funkciók

- Barátok listázása, keresés név alapján.
- Barát hozzáadása, törlése.
- Barátkérések elfogadása/elutasítása, megjelenítése.

## Osztályok és metódusok

### FriendListPanel (extends JPanel)
- Konstruktor: inicializálja a barátok listáját, keresőmezőt, gombokat.
- Metódusok:
  - `setFriends(List<User> friends)`
  - `addFriend(String nickname)`
  - `removeFriend(int friendId)`
  - `addSearchListener(ActionListener)`
  - `addAddFriendListener(ActionListener)`
  - stb.

### FriendRequestPanel (extends JPanel)
- Konstruktor: inicializálja a barátkérések listáját, elfogadás/elutasítás gombokat.
- Metódusok:
  - `setFriendRequests(List<FriendRequest> requests)`
  - `acceptRequest(int requestId)`
  - `declineRequest(int requestId)`
  - `addAcceptListener(ActionListener)`
  - `addDeclineListener(ActionListener)`
  - stb.

### FriendPresenter
- Konstruktor: FriendListPanel, FriendRequestPanel, AuthService példányt kap.
- Metódusok:
  - `loadFriends()`: barátok betöltése API-ból.
  - `searchFriends(String query)`: keresés név alapján.
  - `handleAddFriend(String nickname)`: barát hozzáadása API-n keresztül.
  - `handleRemoveFriend(int friendId)`: barát törlése API-n keresztül.
  - `loadFriendRequests()`: barátkérések betöltése.
  - `handleAcceptRequest(int requestId)`: barátkérés elfogadása.
  - `handleDeclineRequest(int requestId)`: barátkérés elutasítása.
  - Hibakezelés minden API hívásnál.

## UI elemek

- JPanel, JList, JTextField (kereső), JButton (hozzáadás, törlés, elfogadás, elutasítás).
- Layout: BoxLayout, GridBagLayout.
- Visszajelzés: sikeres művelet, hibaüzenet.

## Adatfolyam

1. Barátok betöltése → lista frissítése.
2. Keresés → szűrt lista megjelenítése.
3. Barát hozzáadása → API hívás, lista frissítése.
4. Barát törlése → API hívás, lista frissítése.
5. Barátkérések betöltése → panel frissítése.
6. Elfogadás/elutasítás → API hívás, lista frissítése.
7. Periodikus polling: a barátkérések (és barátlista) automatikusan frissülnek meghatározott időközönként, így a felhasználó mindig naprakész adatokat lát.
8. A polling során a kliens minden ciklusban ellenőrzi a JWT token érvényességét, és ha lejárt vagy hamarosan lejár, automatikusan frissíti azt.

## Hibakezelés

- Minden API hívás try-catch ApiException és Exception.
- Hibák megjelenítése a UI-ban.

## Tesztelés

- Barát hozzáadása/törlése, keresés, barátkérések kezelése.
- Hibás adatok, szerverhiba, hálózati hiba.

## Kódolási javaslatok

- Minden UI művelet Event Dispatch Thread-en fusson.
- API hívások külön szálon (SwingWorker).
- UI frissítés thread-safe módon.

## Függőségek

- AuthService, ApiService, User, FriendRequest modellek.

## Példa workflow

```java
FriendListPanel friendListPanel = new FriendListPanel();
FriendRequestPanel friendRequestPanel = new FriendRequestPanel();
FriendPresenter presenter = new FriendPresenter(friendListPanel, friendRequestPanel, authService);
presenter.loadFriends();
presenter.loadFriendRequests();
friendListPanel.addAddFriendListener(e -> presenter.handleAddFriend(nickname));
friendListPanel.addSearchListener(e -> presenter.searchFriends(query));
friendRequestPanel.addAcceptListener(e -> presenter.handleAcceptRequest(requestId));
friendRequestPanel.addDeclineListener(e -> presenter.handleDeclineRequest(requestId));
```

## További részletek

- Minden művelet után visszajelzés a felhasználónak.
- Lista automatikus frissítése minden változás után.
