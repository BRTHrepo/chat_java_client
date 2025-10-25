# Programterv

## Architektúra
Az alkalmazás Model-View-Presenter (MVP) mintát követ:
- **Model**: Adatmodellek, adatbázis, DAO-k, REST API kommunikáció.
- **View**: Felhasználói felület (Java Swing), ablakok, panelek, komponensek.
- **Presenter**: UI események kezelése, workflow-k, validáció, hibakezelés.

## Fő modulok
- Bejelentkezés (LoginView, LoginPresenter)
- Főablak (MainView, MainPresenter): chat, barátlista, barátkérések
- Profilkezelés (ProfileView, ProfilePresenter)
- Beállítások (ConfigurationView, ConfigurationPresenter)
- Hibakezelés (ErrorMessageTranslator)
- Adatbázis és DAO réteg (DBService, MessageDao, FriendDao, FriendRequestDao, EventLogDao)
- REST API kommunikáció (ApiService, AuthService)

## Use-case diagramok (részletes szöveges leírás)

### 1. Bejelentkezés
- **Szereplő**: Felhasználó
- **Fő lépések**:
  1. Felhasználó megadja emailt, jelszót, becenevet.
  2. Rákattint a "Bejelentkezés" gombra.
  3. Rendszer ellenőrzi az adatokat (AuthService).
  4. Sikeres bejelentkezés esetén megnyílik a főablak (MainView).
- **Alternatívák**:
  - Hibás adatok esetén hibaüzenet jelenik meg.
  - Elfelejtett jelszó esetén a "Jelszóemlékeztető" funkció használható.
- **Eredmény**: Felhasználó belép az alkalmazásba.

### 2. Üzenetküldés
- **Szereplő**: Felhasználó
- **Fő lépések**:
  1. Felhasználó kiválaszt egy barátot a listából.
  2. Beírja az üzenetet a mezőbe.
  3. Rákattint a "Küldés" gombra.
  4. Rendszer elküldi az üzenetet (ApiService, MessageDao).
  5. Chat panel frissül, megjelenik az új üzenet.
- **Alternatívák**:
  - Média csatolása (kép, hang).
  - Hiba esetén hibaüzenet jelenik meg.
- **Eredmény**: Üzenet elküldve, megjelenik a chatben.

### 3. Barát hozzáadása/törlése
- **Szereplő**: Felhasználó
- **Fő lépések**:
  1. Felhasználó beírja a barát becenevét vagy emailjét.
  2. Rákattint a "Hozzáadás" gombra.
  3. Rendszer elküldi a barátkérést (ApiService).
  4. Barátlista frissül.
  5. Törlés esetén kiválasztja a barátot, rákattint a "Törlés" gombra.
  6. Rendszer eltávolítja a barátot (ApiService, FriendDao).
- **Alternatívák**:
  - Hiba esetén hibaüzenet jelenik meg.
- **Eredmény**: Barát hozzáadva vagy törölve a listából.

### 4. Barátkérés elfogadása/elutasítása
- **Szereplő**: Felhasználó
- **Fő lépések**:
  1. Felhasználó megnyitja a barátkérések panelt.
  2. Kiválasztja a beérkezett kérést.
  3. Rákattint az "Elfogadás" vagy "Elutasítás" gombra.
  4. Rendszer feldolgozza a választ (ApiService, FriendRequestDao).
  5. Barátlista vagy barátkérések lista frissül.
- **Alternatívák**:
  - Hiba esetén hibaüzenet jelenik meg.
- **Eredmény**: Barátkérés elfogadva vagy elutasítva.

### 5. Profil módosítása
- **Szereplő**: Felhasználó
- **Fő lépések**:
  1. Felhasználó megnyitja a profil panelt.
  2. Módosítja a becenevet, avatar URL-t.
  3. Rákattint a "Mentés" gombra.
  4. Rendszer elmenti a módosításokat (AuthService, DBService).
  5. Visszajelzés jelenik meg a felhasználónak.
- **Alternatívák**:
  - Hiba esetén hibaüzenet jelenik meg.
- **Eredmény**: Profiladatok módosítva.

### 6. Hibakezelés
- **Szereplő**: Felhasználó
- **Fő lépések**:
  1. Felhasználó műveletet hajt végre (pl. üzenetküldés, bejelentkezés).
  2. Rendszer hibát észlel (API vagy adatbázis hiba).
  3. ErrorMessageTranslator lefordítja a hibaüzenetet.
  4. Hibaüzenet jelenik meg a felhasználónak.
- **Alternatívák**:
  - Hibás adatok, hálózati hiba, szerverhiba.
- **Eredmény**: Felhasználó értesül a hibáról, javíthatja a problémát.

## UML modell (részletes szöveges leírás)

### Főbb osztályok, attribútumok és metódusok

#### User
- **Attribútumok**: id, email, nickname, avatarUrl, status, token
- **Metódusok**: getId(), getEmail(), getNickname(), getAvatarUrl(), getStatus(), getToken(), setXxx()
- **Kapcsolatok**: 
  - 1:N kapcsolat a Message (sender/receiver)
  - 1:N kapcsolat a FriendRequest (from/to)
  - N:N kapcsolat a Friend

#### Message
- **Attribútumok**: id, serverId, senderId, senderNickname, receiverId, msgType, content, sentDate, delivered, read, confirmed, isFromMe, mediaInfo
- **Metódusok**: getXxx(), setXxx()
- **Kapcsolatok**: 
  - Asszociáció User-rel (senderId, receiverId)
  - Aggregáció MediaInfo-val

#### MediaInfo
- **Attribútumok**: mediaType, filePath, fileSize
- **Metódusok**: getXxx(), setXxx()
- **Kapcsolatok**: 
  - Aggregáció Message-sel

#### FriendRequest
- **Attribútumok**: id, fromUserId, toUserId, requestDate
- **Metódusok**: getXxx(), setXxx()
- **Kapcsolatok**: 
  - Asszociáció User-rel (fromUserId, toUserId)

#### ApiError
- **Attribútumok**: error, route, method, userId, message
- **Metódusok**: getXxx(), setXxx()
- **Kapcsolatok**: 
  - Használja: ApiException, ErrorMessageTranslator

#### DBService
- **Attribútumok**: connection
- **Metódusok**: getConnection(), initializeTables(), getDatabasePath()
- **Kapcsolatok**: 
  - Aggregáció DAO osztályokkal

#### MessageDao, FriendDao, FriendRequestDao, EventLogDao
- **Attribútumok**: dbService
- **Metódusok**: CRUD műveletek (save, get, delete, update)
- **Kapcsolatok**: 
  - Aggregáció DBService-szel
  - Asszociáció Model osztályokkal (User, Message, stb.)

#### ApiService
- **Attribútumok**: baseUrl, client
- **Metódusok**: API hívások (registerLoginRaw, forgotPassword, addFriend, deleteFriend, getFriends, getFriendRequests, sendMessage, getMessages)
- **Kapcsolatok**: 
  - Aggregáció AuthService-szel
  - Asszociáció Model osztályokkal

#### AuthService
- **Attribútumok**: currentUser, token, apiService
- **Metódusok**: login, logout, token kezelés, session kezelés
- **Kapcsolatok**: 
  - Aggregáció ApiService-szel
  - Asszociáció User-rel

#### Presenter osztályok (LoginPresenter, MainPresenter, ProfilePresenter, ConfigurationPresenter)
- **Attribútumok**: view, service/dao réteg példányai
- **Metódusok**: attachListeners, handleXxx, workflow-k
- **Kapcsolatok**: 
  - Aggregáció View és Service/DAO réteggel

#### View osztályok (LoginView, MainView, ProfileView, ConfigurationView)
- **Attribútumok**: UI komponensek (mezők, gombok, panelek)
- **Metódusok**: getXxx(), setXxx(), addListener(), showError(), showSuccess()
- **Kapcsolatok**: 
  - Aggregáció Presenter-rel

#### ErrorMessageTranslator
- **Attribútumok**: nincs
- **Metódusok**: translate(error)
- **Kapcsolatok**: 
  - Asszociáció ApiError-ral

### Kapcsolattípusok és irányok

- **Asszociáció**: osztályok között, pl. User és Message, Presenter és View
- **Aggregáció**: pl. Presenter tartalmaz View-t, DAO tartalmaz DBService-t
- **Öröklés**: nincs jelentős öröklés, minden osztály önálló
- **Kapcsolatok iránya**: Presenter → View, Presenter → Service/DAO, DAO → DBService, Service → Model, View → Presenter (események)

### Példa diagramleírás (szövegesen)

- A MainPresenter tartalmaz egy MainView példányt, egy AuthService példányt, egy ApiService példányt, egy MessageDao példányt, egy FriendDao példányt, egy FriendRequestDao példányt.
- A MainView tartalmaz UI komponenseket (JList<User>, JTextPane, JButton, stb.).
- A MessageDao aggregálja a DBService-t, és asszociál a Message modellel.
- Az ApiService aggregálja az AuthService-t, és asszociál a User, Message, FriendRequest modellekkel.
- A User osztály asszociál a Message és FriendRequest osztályokkal.
- Az ErrorMessageTranslator asszociál az ApiError osztállyal.

Ezek alapján egy képalkotó eszköz (pl. draw.io, PlantUML) pontos UML osztálydiagramot tud készíteni, a kapcsolatok, attribútumok és metódusok megadásával.

## Workflow példa
1. Bejelentkezés → AuthService ellenőrzi a felhasználót → siker esetén MainView megnyílik.
2. MainPresenter betölti a barátokat, üzeneteket, barátkéréseket.
3. Felhasználó üzenetet küld → ApiService elküldi → MessageDao menti → chat panel frissül.
4. Barátkérés elfogadása → ApiService → FriendRequestDao frissít → barátlista frissül.
5. Profil módosítása → AuthService → adatbázis frissül → visszajelzés a felhasználónak.

## Tesztelés
- Funkcionális tesztek: bejelentkezés, üzenetküldés, barátkezelés, profil módosítás, hibakezelés.
- Automatizálható tesztesetek (JUnit).

## Verzió
2025. október
