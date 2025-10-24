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

---

## 2025.10.23. - Chat UI újdonságok

- A chatArea JTextPane-re lett cserélve, így támogatott a soronkénti színezés és formázás (StyledDocument).
- A setChatMessages metódus minden üzenetet a confirmed státusz alapján színez: ha confirmed=false, piros színnel jelenik meg, egyébként fekete.
- A View menüben a betűméret növelése/csökkentése mostantól a chatArea-ra is érvényes, a színezés és a méret együtt működik.
- A Message osztály confirmed státusza alapján történik a színezés, így a felhasználó azonnal látja, mely üzenetek státusza bizonytalan.
- A not_updated_ids logika pontosítása: csak a fogadó fél tudja confirmed=true-ra állítani az üzenetet, a küldő nem.

---

## 2025.10.23. - Példány-azonosítás és preferences logika

- A kliens minden példánya a futtatási könyvtár (`System.getProperty("user.dir")`) alapján kap egyedi preferences node-ot.
- Az instanceId az abszolút elérési út, amely base64 kódolva kerül a Preferences node nevébe.
- Így ugyanabból a könyvtárból indítva a beállítások megmaradnak, más könyvtárból külön példányként viselkedik az alkalmazás.
- Az UUID alapú példány-azonosítás megszűnt, helyette könyvtárfüggő azonosítás van.


## Főbb komponensek

- **Bal panel (Friends):**
  - Görgethető barátlista (JList)
  - Barát hozzáadása mező: label, input, gomb egymás alatt (GridLayout)
  - Jobb gombos kattintás egy barátra: felugró ablak a barát adataival és "Barát törlése" gombbal

- **Középső panel (Chat):**
  - ChatArea (JTextPane, színezett sorok)
  - Saját user ID és kiválasztott barát neve/ID-ja a chat ablak tetején
  - Üzenetküldő mező és gomb
  - Polling/Frissítés gomb

- **Jobb panel (Friend Requests):**
  - Görgethető barátkérések lista (csak email jelenik meg)
  - Kattintásra felugró ablak: elfogadás/elutasítás
  - Accept/Decline gombok egymás alatt (GridLayout)

## Interakciók

- **Barát hozzáadása:**  
  - Felhasználó megadja az emailt vagy felhasználónevet, majd Add gomb.
  - Sikeres barátkérés után visszajelzés, mező törlése.

- **Barátkérés elfogadása/elutasítása:**  
  - Kattintás a barátkérésre: felugró ablakban döntés (Elfogad/Elutasít).
  - Ugyanaz a logika, mint az alsó Accept/Decline gomboknál.

- **Barát törlése:**  
  - Barátlistán jobb gombbal kattintva felugró ablak: barát adatai, "Barát törlése" gomb.
  - Törlés gombra megerősítő dialog, majd API-hívás.

- **UI frissítés:**  
  - Minden művelet után automatikus vagy manuális frissítés (polling).

## UX részletek

- Minden fontosabb művelet felugró ablakban visszaigazolható.
- A barátlista, barátkérések lista, chat panel mindig naprakész.
- A felhasználó azonnal látja saját ID-ját, a kiválasztott barát adatait.
- A bal/jobb panelek keskenyebbek, a chat panel domináns.

## Technikai megjegyzések

- Minden aszinkron API-hívás SwingWorker-ben fut.
- A barát törlés és barátkérés elutasítás ugyanazt az API végpontot használja, az action mezővel különböztetve.
- A barátkérések listájában csak az email jelenik meg (cellRenderer).
- A barátlistában a barát neve jelenik meg (cellRenderer).
