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

## Főbb funkciók

- Barátkérés küldése email vagy felhasználónév alapján
- Barátkérések listázása (csak email jelenik meg)
- Barátkérés elfogadása/elutasítása felugró ablakban vagy alsó gombokkal
- Barát törlése jobb gombos menüből, felugró ablakban megerősítéssel
- Minden művelet aszinkron API-hívással, UI frissítéssel

## Barátkérés folyamata

1. **Küldés:**  
   - A felhasználó megadja a barát emailjét vagy felhasználónevét.
   - Az API POST `/chat/api/addFriend` végpontot hívja.
   - Ha már van kölcsönös barátkérés, automatikusan barátokká válnak.

2. **Barátkérések listázása:**  
   - A jobb oldali panelen csak az email jelenik meg.
   - Kattintásra felugró ablak jelenik meg: "Elfogadod ezt a barátkérést?" Elfogad/Elutasít gombokkal.

3. **Elfogadás/Elutasítás:**  
   - Elfogadás: barátság létrejön, barátkérés törlődik.
   - Elutasítás: barátkérés törlődik.
   - Mindkét művelet elérhető a felugró ablakból vagy az alsó gombokkal (egymás alatt).

## Barát törlése

- A bal oldali barátlistában jobb gombbal kattintva felugró ablak jelenik meg a barát adataival és egy "Barát törlése" gombbal.
- Törlés gombra megerősítő ablak jelenik meg.
- A törlés API-hívással történik:  
  POST `/chat/api/deleteFriend`  
  ```json
  {
    "friend_id": 123,
    "action": "delete"
  }
  ```
- Az action mező lehetővé teszi, hogy ugyanaz az endpoint kezelje a törlést és a barátkérés elutasítását is.

## API paraméterek

- **Barát törlése:**  
  - `friend_id`: törlendő barát azonosítója
  - `action`: `"delete"`

- **Barátkérés elutasítása:**  
  - `friend_id`: barátkérés feladója
  - `action`: `"decline"`

## Felhasználói élmény (UX)

- Barátkérés, törlés, elfogadás/elutasítás minden esetben felugró ablakban visszaigazolható.
- Barát hozzáadása mező label, input és gomb egymás alatt.
- Barátok listája görgethető, jobb gombos menüvel.
- Saját user ID és kiválasztott barát neve/ID-ja mindig látható a chat ablakban.
- Minden művelet után automatikus UI frissítés (polling vagy manuális frissítés).

## Technikai részletek

- Az API-hívások aszinkron SwingWorker-ben futnak, a UI nem fagy le.
- A barátkérések és barátok listája mindig naprakész.
- A barát törlés és barátkérés elutasítás ugyanazt az API végpontot használja, az action mezővel különböztetve.
