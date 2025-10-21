# Üzenetkezelés (Chat) modul részletes fejlesztési terv

**Lokális SQLite adatbázis:**  
Az üzenetkezelés modul minden üzenetet, státuszt, média csatolást egy beágyazott SQLite adatbázisban tárol a core modulban.  
Ez lehetővé teszi az üzenet-előzmények gyors elérését, offline működést, keresést, naplózást, és multiplatform támogatást (Java, Android, .NET/C#).  
A Presenter csak a modellel/DAO-val kommunikál, a UI közvetlenül nem használja az adatbázist.

## Funkciók

- Üzenetek listázása (kiválasztott baráttal).
- Új üzenet küldése (szöveg, kép, hang).
- Chat ablak automatikus/polling frissítése.
- Média csatolása (fájl kiválasztás, ellenőrzés).

## Osztályok és metódusok

### ChatPanel (extends JPanel)
- Konstruktor: inicializálja az üzenetek paneljét, szövegmezőt, küldés/média gombokat.
- Metódusok:
  - `setMessages(List<Message> messages)`
  - `addSendMessageListener(ActionListener)`
  - `addAttachMediaListener(ActionListener)`
  - `showError(String message)`
  - `showSuccess(String message)`
  - stb.

### MessagePresenter
- Konstruktor: ChatPanel, AuthService példányt kap.
- Metódusok:
  - `loadMessages(friendId)`: üzenetek betöltése API-ból.
  - `handleSendMessage(friendId, content, mediaFile)`: üzenetküldés API-n keresztül.
  - `handleAttachMedia(File file)`: média csatolása, validáció.
  - Polling: időzített lekérdezés új üzenetekre.
  - Hibakezelés minden API hívásnál.

### Message (model)
- Attribútumok: messageId, senderId, receiverId, nickname, msgType, content, sentDate, delivered, readStatus, isFromMe, stb.

## UI elemek

- JPanel, JTextArea (üzenetek), JTextField (új üzenet), JButton (küldés, média csatolás), JFileChooser.
- Layout: BorderLayout, BoxLayout.
- Visszajelzés: sikeres küldés, hibaüzenet.

## Adatfolyam

1. Barát kiválasztása → üzenetek betöltése.
2. Új üzenet írása → küldés gomb → API hívás → chat panel frissítése.
3. Média csatolása → fájl kiválasztása → validáció → küldés.
4. Polling: időzített lekérdezés új üzenetekre.

## Hibakezelés

- Minden API hívás try-catch ApiException és Exception.
- Hibák megjelenítése a UI-ban.
- Média csatolásnál fájlméret, típus ellenőrzése.

## Tesztelés

- Üzenet küldése/fogadása, média csatolás, polling működése.
- Hibás adatok, szerverhiba, hálózati hiba.

## Kódolási javaslatok

- Minden UI művelet Event Dispatch Thread-en fusson.
- API hívások külön szálon (SwingWorker).
- Polling időzítővel (pl. Timer).
- UI frissítés thread-safe módon.

## Függőségek

- AuthService, ApiService, Message model.

## Példa workflow

```java
ChatPanel chatPanel = new ChatPanel();
MessagePresenter presenter = new MessagePresenter(chatPanel, authService);
presenter.loadMessages(friendId);
chatPanel.addSendMessageListener(e -> presenter.handleSendMessage(friendId, content, mediaFile));
chatPanel.addAttachMediaListener(e -> presenter.handleAttachMedia(file));
```

## További részletek

- Chat panel automatikus görgetése új üzenetnél.
- Média csatolásnál előnézet, validáció.
- Minden művelet után visszajelzés a felhasználónak.
