# Teljes Applikáció – Tartalmi, Logikai és Technikai Dokumentáció (C# portolhatósággal)

## 1. Bevezetés

Ez a dokumentum a chat alkalmazás teljes tartalmi, logikai és technikai működését írja le, a ténylegesen megvalósított Java kód alapján, de úgy, hogy C# környezetben is implementálható legyen. Minden főbb modul, workflow, UI logika, eseménykezelés, adatfolyam, architektúra, adatmodell, API, hibakezelés, tesztelés részletesen bemutatásra kerül.

## 2. Architektúra és fő komponensek

- **MVP (Model-View-Presenter) minta**: UI (View), logika (Presenter), adatkezelés (Model/Service/DAO) szétválasztva.
- **Rétegek**:
  - UI réteg: Swing (Java) /  WPF (C#)
  - Presenter réteg: eseménykezelés, workflow, logika
  - Service/DAO réteg: adatbázis, REST API, adatmodellek
  - Model réteg: entitások (User, Message, stb.)
- **Adatbázis**: SQLite (Java: JDBC, C#: System.Data.SQLite vagy Entity Framework)
- **REST API**: HTTP kommunikáció, JSON adatmodellek

## 3. Főbb modulok és workflow-k

### 3.1 Bejelentkezés és főablak

- **LoginView**: email, jelszó, nickname mezők, bejelentkezés és jelszóemlékeztető gomb.
- **LoginPresenter**: handleLogin(), handleForgotPassword(), openMainView().
- **Workflow**:
  1. Felhasználó beírja adatait, Login gomb → handleLogin() → AuthService.login() → siker esetén openMainView().
  2. Jelszóemlékeztető gomb → handleForgotPassword() → AuthService.forgotPassword().

### 3.2 Főablak (MainView, MainPresenter)

- **MainView**: három panel (bal: barátlista, közép: chat, jobb: barátkérések/profil), menü (profil, kijelentkezés, beállítások), polling gomb, fontméret állítás.
- **MainPresenter**: loadFriends(), loadMessages(), loadFriendRequests(), handleSendMessage(), handleLogout(), pollingTask(), handleAddFriend(), handleDeleteFriend(), handleAcceptFriendRequest(), handleDeclineFriendRequest(), event listener-ek.
- **Workflow**:
  1. Sikeres bejelentkezés után MainView példányosítása.
  2. Presenter betölti a barátokat, barátkéréseket, üzeneteket.
  3. Barát kiválasztása → chat panel frissül.
  4. Üzenet küldése → API hívás, chat panel frissül.
  5. Barátkérés elfogadása/elutasítása → lista frissül.
  6. Polling: időzített vagy manuális frissítés minden panelen.

### 3.3 Chat modul

- **Chat panel**: üzenetek listája (JTextPane), új üzenet mező, küldés/média gomb, automatikus görgetés, státusz színezés (confirmed).
- **MessagePresenter**: loadMessages(), handleSendMessage(), handleAttachMedia(), polling, hibakezelés.
- **Logika**:
  - Üzenetek betöltése, küldése, státuszkezelés (delivered, read, confirmed).
  - Média csatolás, validáció, előnézet.
  - Polling: új üzenetek lekérdezése időzítővel.

### 3.4 Barátkezelés modul

- **FriendListPanel, FriendRequestPanel**: barátok listája, kereső, hozzáadás/törlés gomb, barátkérések elfogadása/elutasítása.
- **FriendPresenter**: loadFriends(), searchFriends(), handleAddFriend(), handleRemoveFriend(), loadFriendRequests(), handleAcceptRequest(), handleDeclineRequest(), polling.
- **Logika**:
  - Barátok listázása, keresés, hozzáadás, törlés.
  - Barátkérések kezelése, elfogadás/elutasítás, automatikus UI frissítés.

### 3.5 Profil és beállítások modul

- **ProfileView, SettingsPanel**: profil adatok (nickname, email, avatar), szerver URL, mentés gomb.
- **ProfilePresenter**: loadProfile(), handleSaveProfile(), handleForgotPassword(), loadServerUrl(), handleSaveServerUrl().
- **Logika**:
  - Profil adatok betöltése, módosítása, validáció.
  - Szerver URL módosítása, Preferences/Settings kezelés.

### 3.6 Hibakezelés

- **ErrorMessageTranslator**: szerver és általános hibák magyar, felhasználóbarát fordítása.
- **Presenter logika**: minden API hívás try-catch blokkban, hibaüzenet UI-ban jelenik meg.

## 3/A. Implementációs technikai és logikai részletek

### Kliens példány-azonosítás

- Az alkalmazás minden példánya egyedi azonosítót kap, amely a futtatási könyvtár abszolút elérési útjának base64 kódolásából képződik.
- Ez az azonosító a Java Preferences API-ban (vagy C#-ban Settings) node neveként szolgál, így minden példány külön beállításokat tárol.
- Az instanceId nem UUID, hanem könyvtárfüggő, így ugyanabból a könyvtárból indítva a beállítások megmaradnak, más könyvtárból külön példányként viselkedik az alkalmazás.

### Felhasználó mentése az adatbázisba

- A felhasználó (User) adatai az adatbázis users táblájába kerülnek mentésre.
- A mentett user rekord id mezője pontosan megegyezik a szervertől kapott user id-val (nincs lokális külön id generálás).
- A login vagy regisztráció után a szerver által visszaadott User objektum minden mezője (id, email, nickname, avatarUrl, status, token) bekerül az adatbázisba.
- A felhasználó adatai frissítéskor (pl. profil módosítás) is mindig a szerver által visszaadott értékekkel frissülnek.

### Adatbázis és szerver azonosítók szinkronja

- Minden entitás (User, Message, Friend, stb.) id mezője az adatbázisban megegyezik a szerver által visszaadott id-val.
- Nincs külön lokális id generálás, minden rekord az API válaszban kapott id-val kerül mentésre.
- Üzenetek esetén a szerver által visszaadott message_id a messages tábla id mezőjébe kerül.
- Barátok, barátkérések, event logok szintén a szerver id-jával kerülnek mentésre.

### Preferences és beállítások logika

- A bejelentkezett felhasználó emailje, jelszava, tokenje, szerver URL-je a Preferences-ben (Java) vagy Settings-ben (C#) kerül tárolásra.
- A Preferences node neve az instanceId (lásd fent), így minden példány külön beállításokat tárol.
- A szerver URL módosítása Preferences-ben történik, és újraindítás után lép érvénybe.

### User/session kezelés

- A bejelentkezett felhasználó adatai memóriában (AuthService._currentUser) és az adatbázisban is tárolódnak.
- A session token minden API hívás előtt ellenőrzésre kerül (lejárat, érvényesség), szükség esetén automatikusan frissül.
- Kijelentkezéskor minden session adat törlődik a memóriából és a Preferences-ből.

### Adatbázis mentési szabályok

- Minden szerverről érkező entitás (User, Message, Friend, FriendRequest) teljes adattartalommal, szerver id-val kerül mentésre.
- Frissítéskor (pl. profil módosítás, barátlista frissítés) a meglévő rekordok szerver id alapján felülíródnak.
- Törléskor a szerverről törölt rekordok az adatbázisból is törlésre kerülnek.

### Üzenet státuszok és szinkronizáció

- Minden üzenet státusz mezői: delivered, read, confirmed.
- Az üzenet confirmed státuszát csak a fogadó fél tudja true-ra állítani, a küldő nem.
- A nem megerősített (confirmed=false) üzenetek piros színnel jelennek meg a chat UI-ban.
- A kliens polling során minden új vagy státuszban változott üzenetet lekér a szervertől, és az adatbázisban frissíti.

### Példány-specifikus beállítások

- Minden példány saját Preferences node-ot használ, így több példány párhuzamosan is futhat különböző beállításokkal.
- Az instanceId logika biztosítja, hogy a beállítások ne keveredjenek különböző könyvtárakból indított példányok között.

### Technikai összefoglaló

- Minden adatbázisban tárolt id megegyezik a szerver által visszaadott id-val.
- Preferences-ben minden példány külön node-ot használ (instanceId).
- A felhasználó, barát, üzenet, barátkérés, event log minden mezője a szerver által visszaadott értékekkel kerül mentésre/frissítésre.
- Nincs példakód, minden deklaratívan, a tényleges implementáció szerint van leírva.


### 4.1 Entitások, attribútumok, kapcsolatok

#### User

- **Implementációs részletek:**
  - A User osztály a felhasználó minden lényeges adatát tartalmazza: id, email, nickname, avatarUrl, status, token.
  - Az id mezőhöz többféle JSON kulcs is hozzárendelhető a @JsonAlias annotációval: "from_user_id", "id", "user_id", "friend_id". Ez biztosítja, hogy a szerver különböző válaszaiban (pl. barátkérés, user lista, barát lista) mindig a megfelelő mező töltődik fel.
  - Az id mező minden esetben a szervertől kapott azonosító, nincs lokális id generálás.
  - A token mező a JWT session token, amely minden sikeres bejelentkezés vagy regisztráció után a szerver válaszából kerül beállításra.
  - A User objektum minden mezője getter/setter metódusokkal elérhető.
  - A toString() metódus a főbb mezőket (id, email, nickname, avatarUrl, status) szövegesen jeleníti meg, a token nem kerül kiírásra.
  - A User osztályt a barátkérések, barátlista, user lista, bejelentkezés, regisztráció, profil módosítás, minden API válasz és adatbázis művelet használja.
  - A felhasználó adatai minden esetben a szerver által visszaadott értékekkel kerülnek mentésre/frissítésre az adatbázisban és a memóriában.
  - A User osztály nem tartalmaz jelszót, az csak a login/registration API hívás paramétere.

- **Mezők:**
  - int id – szerver által adott azonosító (bármelyik alias néven jöhet)
  - String email – felhasználó email címe
  - String nickname – felhasználó beceneve
  - String avatarUrl – profilkép URL
  - String status – státusz (pl. online/offline)
  - String token – JWT session token

- **Logikai kapcsolatok:**
  - Egy User több üzenet feladója/címzettje lehet (Message.senderId/receiverId).
  - Egy User lehet barát (Friend), barátkérés feladója/címzettje (FriendRequest).
  - A User id mindenhol a szerver által adott érték, nincs duplikáció vagy külön lokális id.
  - A User objektum minden API válaszban és adatbázisban egységesen ugyanazt az id-t használja.

- **Technikai összefoglaló:**
  - A User osztály minden mezője getter/setterrel elérhető.
  - Az id mezőhöz többféle JSON kulcs is hozzárendelhető, így minden API válaszban automatikusan a megfelelő érték töltődik.
  - A User adatok mindenhol a szerver által visszaadott értékekkel kerülnek mentésre és frissítésre.
  - A User osztály nem tartalmaz jelszót, csak a session token-t.

#### Message

- **Implementációs részletek:**
  - A Message osztály minden üzenet összes adatát tartalmazza, beleértve a státuszokat, szerver id-ket, média információkat.
  - Az id mezőhöz többféle JSON kulcs is hozzárendelhető: "message_id", "id". Ez biztosítja, hogy a szerver különböző válaszaiban mindig a megfelelő mező töltődik fel.
  - A senderId, receiverId, senderNickname, msgType, content, sentDate, delivered, readStatus, isFromMe, media_info mezők mind @JsonAlias annotációval többféle szerver oldali kulcsról is beolvashatók.
  - A confirmed mező azt jelzi, hogy az üzenetet a fogadó fél visszaigazolta-e a szerver felé. Ez a státusz csak a fogadó által állítható true-ra, a küldő nem módosíthatja.
  - A read és readStatus mezők az olvasottságot jelzik, delivered a kézbesítést, isFromMe azt, hogy az üzenet a jelenlegi felhasználótól származik-e.
  - A media_info mező csak akkor van kitöltve, ha az üzenet típusa nem "text" (pl. "audio", "image"), ilyenkor tartalmazza a média típusát, elérési útját, méretét.
  - A serverId mező a szerver által adott egyedi azonosító, amely minden esetben megegyezik az adatbázisban tárolt id-val.
  - A Message objektum minden mezője getter/setter metódusokkal elérhető.
  - A toString() metódus minden főbb mezőt szövegesen jelenít meg.
  - A Message osztályt minden üzenetküldés, üzenetlekérdezés, adatbázis mentés, API kommunikáció, státusz szinkronizáció használja.
  - Az üzenet státusz mezői (delivered, read, confirmed) alapján történik a chat UI-ban a színezés, visszajelzés, státuszkezelés.
  - Az üzenet confirmed státuszát csak a fogadó fél tudja true-ra állítani, a küldő nem.
  - Az üzenet minden mezője a szerver által visszaadott értékekkel kerül mentésre/frissítésre az adatbázisban.

- **Mezők:**
  - int id – szerver által adott azonosító ("message_id" vagy "id")
  - int serverId – szerver által adott egyedi azonosító (duplikáció elkerülésére)
  - int senderId – feladó user id
  - String senderNickname – feladó beceneve
  - int receiverId – címzett user id
  - String msgType – üzenet típusa ("text", "audio", "image")
  - String content – szöveges tartalom vagy média elérési út
  - String sentDate – küldés dátuma/időpontja
  - boolean delivered – kézbesítve-e
  - boolean read – olvasott-e
  - boolean readStatus – olvasottsági státusz (külön mező a szerver válasz miatt)
  - boolean confirmed – visszaigazolt-e a szerver felé (csak a fogadó állíthatja true-ra)
  - boolean isFromMe – az üzenet a jelenlegi felhasználótól származik-e
  - MediaInfo media_info – média információk (csak ha nem text típus)

- **Logikai kapcsolatok:**
  - Egy Message egy feladótól egy címzettnek szól.
  - Egy Message tartalmazhat médiát (audio, image), ilyenkor a media_info mező kitöltött.
  - Az üzenet státusz mezői alapján történik a chat UI-ban a színezés, státusz visszajelzés.
  - Az id és serverId mindenhol a szerver által adott érték, nincs lokális id generálás.
  - Az üzenet minden mezője a szerver által visszaadott értékekkel kerül mentésre/frissítésre.

- **Technikai összefoglaló:**
  - A Message osztály minden mezője getter/setterrel elérhető.
  - Az összes státusz mező (delivered, read, confirmed, readStatus, isFromMe) a szerver válaszából töltődik.
  - Az üzenet confirmed státuszát csak a fogadó fél tudja true-ra állítani, a küldő nem.
  - A media_info mező csak média típusú üzenetnél van kitöltve.
  - Az id és serverId mindenhol a szerver által adott érték, nincs duplikáció.

#### MediaInfo

- **Implementációs részletek:**
  - A MediaInfo osztály minden média típusú üzenethez tartozó információt tartalmaz.
  - A media_type mező az üzenet típusát adja meg: "audio", "image", "video".
  - A file_path mező a médiafájl szerver oldali elérési útját tartalmazza (pl. "/uploads/audios/abc123.mp3").
  - A file_size mező a médiafájl méretét tartalmazza bájtban.
  - Minden mező getter/setter metódusokkal elérhető.
  - A toString() metódus minden mezőt szövegesen jelenít meg.
  - A MediaInfo objektum csak akkor van kitöltve, ha az üzenet típusa nem "text".
  - A MediaInfo minden mezője a szerver által visszaadott értékekkel kerül mentésre/frissítésre az adatbázisban.
  - A MediaInfo-t a Message.media_info mező tartalmazza, minden média típusú üzenetnél.

- **Mezők:**
  - String media_type – média típusa ("audio", "image", "video")
  - String file_path – médiafájl szerver oldali elérési útja
  - long file_size – médiafájl mérete bájtban

- **Technikai összefoglaló:**
  - A MediaInfo minden mezője getter/setterrel elérhető.
  - A media_type, file_path, file_size mezők minden média típusú üzenetnél a szerver válaszából töltődnek.
  - A MediaInfo csak akkor van kitöltve, ha az üzenet nem "text" típusú.

#### FriendRequest

- **Implementációs részletek:**
  - A FriendRequest adatbázisban minden barátkérés egy rekord a friend_requests táblában: id, from_user_id, to_user_id, request_date.
  - A FriendRequestDao.getAllFriendRequests() minden barátkérést visszaad, nincs user_id szerinti szűrés (a szűrés a Presenter/Service rétegben történik).
  - A FriendRequestRecord POJO tartalmazza az összes mezőt: id, fromUserId, toUserId, requestDate.
  - Barátkérés mentése: saveFriendRequest(fromUserId, toUserId, requestDate).
  - Barátkérések törlése: deleteAllFriendRequests().
  - Barátkérés elfogadása/elutasítása nem a DAO-ban, hanem a Presenter/Service rétegben történik (API hívás után frissül az adatbázis).

- **Attribútumok (Java POJO):**
  - int id
  - int fromUserId
  - int toUserId
  - String requestDate

- **Példa (Java-ban):**
```java
public class FriendRequestRecord {
    public int id;
    public int fromUserId;
    public int toUserId;
    public String requestDate;
}
```

#### ApiError

- **Implementációs részletek:**
  - Az ApiError osztály minden szerver oldali vagy API hiba részletes leírását tartalmazza.
  - Az error mező a hiba típusát vagy kódját tartalmazza (pl. "invalid_token", "user_not_found").
  - A route mező az érintett API végpontot tartalmazza (pl. "/chat/api/login").
  - A method mező az érintett HTTP metódust tartalmazza (pl. "POST", "GET").
  - A userId mező a hibát okozó vagy érintett felhasználó azonosítóját tartalmazza (ha van ilyen).
  - A message mező a hiba részletes, emberi olvasásra szánt leírását tartalmazza.
  - Minden mező getter/setter metódusokkal elérhető.
  - Az ApiError objektum minden API hívás hibás válaszában, valamint az ApiException kivételben is szerepel.
  - Az ErrorMessageTranslator osztály ezt az objektumot használja a felhasználóbarát hibaüzenetek fordításához.
  - Az ApiError minden mezője a szerver által visszaadott értékekkel kerül kitöltésre.

- **Mezők:**
  - String error – hiba típusa vagy kódja
  - String route – érintett API végpont
  - String method – HTTP metódus
  - String userId – érintett felhasználó azonosítója (ha van)
  - String message – részletes hibaüzenet

- **Technikai összefoglaló:**
  - Az ApiError minden mezője getter/setterrel elérhető.
  - Az ApiError objektum minden API hívás hibás válaszában szerepel, és a hibakezelés minden szintjén (service, presenter, view) felhasználásra kerül.

### 4.2 Adatbázis séma (SQLite)

- **Táblák**: users, messages, friends, friend_requests, event_logs
- **Kapcsolatok**:
  - users (1) --- (N) messages (küldő/fogadó)
  - users (1) --- (N) friend_requests (from/to)
  - users (N) --- (N) friends (kapcsolótábla)
- **Példa (C#-ban is alkalmazható):**
```sql
CREATE TABLE users (
  id INTEGER PRIMARY KEY,
  email TEXT,
  nickname TEXT,
  avatar_url TEXT,
  status TEXT,
  token TEXT
);

CREATE TABLE messages (
  id INTEGER PRIMARY KEY,
  sender_id INTEGER,
  receiver_id INTEGER,
  nickname TEXT,
  msg_type TEXT,
  content TEXT,
  sent_date TEXT,
  delivered BOOLEAN,
  read BOOLEAN,
  confirmed BOOLEAN,
  is_from_me BOOLEAN,
  media_type TEXT,
  file_path TEXT,
  file_size INTEGER,
  server_id INTEGER,
  FOREIGN KEY(sender_id) REFERENCES users(id),
  FOREIGN KEY(receiver_id) REFERENCES users(id)
);

CREATE TABLE friend_requests (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  from_user_id INTEGER,
  to_user_id INTEGER,
  request_date TEXT,
  FOREIGN KEY(from_user_id) REFERENCES users(id),
  FOREIGN KEY(to_user_id) REFERENCES users(id)
);

CREATE TABLE friends (
  id INTEGER PRIMARY KEY,
  email TEXT,
  nickname TEXT,
  avatar_url TEXT,
  status TEXT
);

CREATE TABLE event_logs (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  event_type TEXT,
  event_time TEXT,
  details TEXT
);
```

### 4.3 Példák entitások közötti kapcsolatokra

- Egy user több üzenetet küldhet/fogadhat (messages.sender_id, messages.receiver_id)
- Egy user több barátkérést küldhet/fogadhat (friend_requests.from_user_id, to_user_id)
- Barátság: a friends tábla csak a bejelentkezett felhasználó barátlistáját tartalmazza (nincs user_id, csak barát rekordok)

### 4.4 JSON szerializáció (API kommunikációhoz)

- Minden entitás JSON formátumban kerül küldésre/fogadásra REST API-n keresztül.
- Példa (Message):
```json
{
  "id": 1,
  "senderId": 2,
  "senderNickname": "Anna",
  "receiverId": 3,
  "msgType": "text",
  "content": "Szia!",
  "sentDate": "2025-10-24T12:00:00",
  "delivered": true,
  "read": false,
  "confirmed": true,
  "isFromMe": true,
  "mediaInfo": null,
  "serverId": 42
}
```

## 5. Service/DAO réteg – Részletes kidolgozás

### 5.1 ApiService

REST API hívások minden fő funkcióhoz, a válaszok típusbiztos deszerializálásával.  
**Főbb metódusok (Java → C#):**
- `registerLoginRaw(email, password, nickname)` – regisztráció/bejelentkezés (nyers JSON válasz)
- `forgotPassword(email)` – jelszóemlékeztető (DeleteFriendResponse-t vár)
- `addFriend(token, friendId, nickname, email)` – barát hozzáadása (DeleteFriendResponse-t vár)
- `deleteFriend(token, friendId, action)` – barát törlése/barátkérés elutasítása (DeleteFriendResponse-t vár)
- `getFriends(token)` – barátok lekérdezése (List<User>)
- `getFriendRequests(token)` – barátkérések lekérdezése (List<GetFriendRequestResponse> → List<User>)
- `sendMessage(token, message)` – üzenetküldés (SendMessageResponse-t vár)
- `getMessages(token, confirmedMessageIds, lastMessageId, lastRequestDate)` – üzenetek lekérdezése (List<GetMessageResponse> → List<Message> + not_updated_ids)

**Válaszmodellek:**
- **DeleteFriendResponse**: minden barátművelet (addFriend, deleteFriend, forgotPassword) ezt a típust várja vissza, siker/hiba státusszal.
- **GetFriendRequestResponse**: barátkérések lekérdezésekor a szerver ezt a típust adja vissza, amelyből User lista készül.
- **SendMessageResponse**: üzenetküldés válasza.
- **GetMessageResponse**: üzenetek lekérdezésekor a szerver ezt a típust adja vissza, amelyből Message lista készül.
- **ApiError**: minden hiba esetén a szerver ezt a típust adja vissza, amit az executeRequest automatikusan felismer és ApiException-t dob.

**Központi végrehajtás:**
- Minden metódus a `private <T> T executeRequest(Request request, Type returnType)` metódust használja, amely a megadott returnType alapján deszerializálja a választ (pl. DeleteFriendResponse, GetFriendRequestResponse, stb.).
- Ha a válasz nem felel meg a várt típusnak, vagy ApiError-t kap, automatikusan ApiException dobódik.
- A getMessages és getFriendRequests metódusok speciális logikát tartalmaznak a válaszok feldolgozására (pl. GetFriendRequestResponse → User).

**Példa (C#-ban):**
```csharp
public class ApiService {
    private readonly HttpClient _client;
    public ApiService(string baseUrl) {
        _client = new HttpClient { BaseAddress = new Uri(baseUrl) };
    }

    public async Task<string> RegisterLoginRaw(string email, string password, string nickname) { /* ... */ }
    public async Task<DeleteFriendResponse> ForgotPassword(string email) { /* ... */ }
    public async Task<DeleteFriendResponse> AddFriend(string token, int friendId, string nickname, string email) { /* ... */ }
    public async Task<DeleteFriendResponse> DeleteFriend(string token, int friendId, string action) { /* ... */ }
    public async Task<List<User>> GetFriends(string token) { /* ... */ }
    public async Task<List<GetFriendRequestResponse>> GetFriendRequests(string token) { /* ... */ }
    public async Task<SendMessageResponse> SendMessage(string token, Message message) { /* ... */ }
    public async Task<List<GetMessageResponse>> GetMessages(string token, List<int> confirmedMessageIds, int? lastMessageId, string lastRequestDate) { /* ... */ }
}
```
**Megjegyzés:**  
- Minden metódus HTTP kérést indít, a válaszokat a returnType alapján deszerializálja.
- Az executeRequest metódus minden hibát ApiError alapján ApiException-né alakít.

### 5.2 AuthService

Felhasználói session, token, login/logout, credentials, szerver URL kezelés.
**Főbb metódusok:**
- `GetCurrentUser()`, `GetCurrentToken()`, `SetCurrentToken(token)`
- `IsLoggedIn()`, `SaveCredentials(email, password)`, `GetSavedEmail()`, `GetSavedPassword()`, `ClearCredentials()`
- `Logout()`, `Login(email, password, nickname)`
- `CheckTokenValidity()`, `RefreshTokenIfNeeded()`
- `LoadSession()`, `ClearSession()`
- `ForgotPassword(email)`
- `GetServerUrl()`, `SetApiService(apiService)`, `GetApiService()`

**Példa (C#-ban):**
```csharp
public class AuthService {
    private User _currentUser;
    private string _token;
    private ApiService _apiService;
    // ... konstruktor, property-k

    public User GetCurrentUser() => _currentUser;
    public string GetCurrentToken() => _token;
    public void SetCurrentToken(string token) => _token = token;
    public bool IsLoggedIn() => _currentUser != null;
    public void SaveCredentials(string email, string password) { /* ... */ }
    public string GetSavedEmail() { /* ... */ }
    public string GetSavedPassword() { /* ... */ }
    public void ClearCredentials() { /* ... */ }
    public void Logout() { _currentUser = null; _token = null; }
    public async Task<User> Login(string email, string password, string nickname) { /* ... */ }
    public bool CheckTokenValidity() { /* ... */ }
    public async Task RefreshTokenIfNeeded() { /* ... */ }
    public void LoadSession() { /* ... */ }
    public void ClearSession() { /* ... */ }
    public async Task ForgotPassword(string email) { /* ... */ }
    public string GetServerUrl() { /* ... */ }
    public void SetApiService(ApiService apiService) { _apiService = apiService; }
    public ApiService GetApiService() => _apiService;
}
```
**Megjegyzés:** A session és token kezelés Preferences/Settings-ben történik, minden API hívás előtt token érvényesség ellenőrzés.

### 5.3 DBService

SQLite adatbázis kapcsolat, táblák inicializálása.
**Főbb metódusok:**
- `GetConnection()`
- `InitializeTables()`
- `GetDatabasePath()`

**Példa (C#-ban):**
```csharp
public class DBService {
    private string _dbPath;
    public DBService(string dbPath) { _dbPath = dbPath; }
    public SQLiteConnection GetConnection() => new SQLiteConnection($"Data Source={_dbPath}");
    public void InitializeTables() { /* CREATE TABLE IF NOT EXISTS ... */ }
    public string GetDatabasePath() => _dbPath;
}
```

### 5.4 DAO-k (Data Access Object)

#### MessageDao

- `tableExists(tableName)` – tábla létezésének ellenőrzése
- `saveMessage(message)` – üzenet mentése, duplikáció ellenőrzéssel (server_id alapján)
- `getMessagesWithFriend(friendId, currentUserId)` – csak azokat az üzeneteket adja vissza, ahol a két fél pontosan a bejelentkezett felhasználó és a barát (nem enged át mások üzeneteit)
- `getUnconfirmedServerIds()` – nem megerősített üzenetek server_id-jai
- `setMessagesConfirmed(serverIds)` – üzenetek confirmed státuszának beállítása
- `deleteAllMessages()` – összes üzenet törlése
- `setMessageReadStatus(messageId, isRead)` – olvasottság állítása

**Megjegyzés:**  
A getMessagesWithFriend() helyes paraméterezése: mindkét fél id-jét figyelembe veszi (currentUserId, friendId).  
Az üzenetek beszúrása előtt ellenőrzi, hogy a server_id már szerepel-e az adatbázisban (duplikáció elkerülése).  
Az olvasottság (read_status) explicit metódussal állítható.

**Példa (Java-ban):**
```java
public class MessageDao {
    private final DBService dbService;
    public MessageDao(DBService dbService) { this.dbService = dbService; }
    public boolean tableExists(String tableName) { /* ... */ }
    public void saveMessage(Message message) { /* ... */ }
    public List<Message> getMessagesWithFriend(Integer friendId, Integer currentUserId) { /* ... */ }
    public List<Integer> getUnconfirmedServerIds() { /* ... */ }
    public void setMessagesConfirmed(List<Integer> serverIds) { /* ... */ }
    public void deleteAllMessages() { /* ... */ }
    public boolean setMessageReadStatus(Integer messageId, boolean isRead) { /* ... */ }
}
```

#### FriendDao

- `getFriendByIdStatic(friendId)` – barát lekérdezése id alapján (user_id nincs, csak barát id)
- `saveFriend(user)` – barát adatainak mentése (id, email, nickname, avatar_url, status)
- `getAllFriends()` – összes barát lekérdezése (nincs user_id szerinti szűrés, csak a bejelentkezett felhasználó barátlistája van az adatbázisban)
- `deleteAllFriends()` – összes barát törlése

**Megjegyzés:**  
A friends tábla nem kapcsolótábla (nincs user_id, friend_id), hanem minden barát egy külön rekord (id, email, nickname, avatar_url, status).  
A getAllFriends() minden barátot visszaad, nincs user_id szerinti szűrés.

**Példa (Java-ban):**
```java
public class FriendDao {
    private final DBService dbService;
    public FriendDao(DBService dbService) { this.dbService = dbService; }
    public User getFriendByIdStatic(Integer friendId) { /* ... */ }
    public void saveFriend(User user) { /* ... */ }
    public List<User> getAllFriends() { /* ... */ }
    public void deleteAllFriends() { /* ... */ }
}
```

#### FriendRequestDao

- `SaveFriendRequest(fromUserId, toUserId, requestDate)`
- `GetAllFriendRequests()`
- `DeleteAllFriendRequests()`

**Példa (C#-ban):**
```csharp
public class FriendRequestDao {
    private DBService _db;
    public FriendRequestDao(DBService db) { _db = db; }
    public void SaveFriendRequest(int fromUserId, int toUserId, string requestDate) { /* ... */ }
    public List<FriendRequest> GetAllFriendRequests() { /* ... */ }
    public void DeleteAllFriendRequests() { /* ... */ }
}
```

#### EventLogDao

- `LogEvent(eventType, eventTime, details)`
- `GetAllEvents()`
- `DeleteAllEvents()`

**Példa (C#-ban):**
```csharp
public class EventLogDao {
    private DBService _db;
    public EventLogDao(DBService db) { _db = db; }
    public void LogEvent(string eventType, string eventTime, string details) { /* ... */ }
    public List<EventLogRecord> GetAllEvents() { /* ... */ }
    public void DeleteAllEvents() { /* ... */ }
}
public class EventLogRecord {
    public string EventType { get; set; }
    public string EventTime { get; set; }
    public string Details { get; set; }
}
```

### 5.5 Service/DAO workflow-k és kapcsolatok

- Az ApiService minden REST hívás előtt AuthService-től kéri a tokent.
- A DAO-k minden adatbázis művelethez DBService-t használnak.
- A Presenter réteg minden adat- vagy API-műveletet Service/DAO-n keresztül végez.
- Minden hibát try-catch blokkal kezelnek, ApiException vagy Exception dobásával, amit a Presenter kezel.

### 5.6 Példák Service/DAO használatra

```csharp
// Barátok lekérdezése
var friends = await apiService.GetFriends(authService.GetCurrentToken());

// Üzenet mentése adatbázisba
messageDao.SaveMessage(message);

// Barátkérés mentése
friendRequestDao.SaveFriendRequest(fromUserId, toUserId, DateTime.Now.ToString("s"));
```

## 6. Presenter réteg – Részletes kidolgozás

A Presenter réteg felelős a UI események kezeléséért, workflow-kért, adatbetöltésért, validációért, hibakezelésért, Service/DAO réteg meghívásáért.

### 6.1 LoginPresenter

**Feladat:** Bejelentkezés, jelszóemlékeztető, főablak megnyitása.

**Főbb mezők:**
- LoginView _view
- AuthService _authService

**Főbb metódusok:**
- `AttachListeners()` – minden gombhoz listener-t rendel
- `HandleLogin()` – bejelentkezési workflow
- `HandleForgotPassword()` – jelszóemlékeztető workflow
- `OpenMainView()` – sikeres login után főablak megnyitása

**Workflow példa:**
```csharp
public void AttachListeners() {
    view.LoginButton.Click += (s, e) => HandleLogin();
    view.ForgotPasswordButton.Click += (s, e) => HandleForgotPassword();
}

public async void HandleLogin() {
    try {
        var user = await authService.Login(view.GetEmail(), view.GetPassword(), view.GetNickname());
        OpenMainView();
    } catch (ApiException ex) {
        view.ShowError(ErrorMessageTranslator.Translate(ex));
    }
}
```

### 6.2 MainPresenter

**Feladat:** Főablak workflow, barátok, üzenetek, barátkérések betöltése, polling, eseménykezelés, logout, profilmenü, fontméret, barát hozzáadás/törlés, barátkérés elfogadás/elutasítás.

**Főbb mezők:**
- MainView _view
- AuthService _authService
- ApiService _apiService
- MessageDao _messageDao
- FriendDao _friendDao
- FriendRequestDao _friendRequestDao

**Főbb metódusok:**
- `LoadInitialData()` – barátok, barátkérések, üzenetek betöltése
- `SetCurrentSelectedFriend()` – kiválasztott barát beállítása
- `LoadFriends()`, `LoadFriendRequests()`, `LoadMessages(friendId)`
- `HandleSendMessage(content)` – üzenetküldés workflow
- `HandleAddFriend()`, `HandleDeleteFriend(friend)` – barát hozzáadás/törlés
- `HandleAcceptFriendRequest()`, `HandleDeclineFriendRequest()` – barátkérés elfogadás/elutasítás
- `PollingTask()` – időzített frissítés
- `HandleLogout()`, `OpenProfileView()`, `HandleIncreaseFontSize()`, `HandleDecreaseFontSize()`
- `AttachListeners()` – minden UI eseményhez listener

**Workflow példa:**
```csharp
public void AttachListeners() {
    view.AddFriendSelectionListener((s, e) => SetCurrentSelectedFriend());
    view.AddSendMessageListener((s, e) => HandleSendMessage(view.GetMessageText()));
    view.AddAddFriendListener((s, e) => HandleAddFriend());
    view.AddAcceptFriendRequestListener((s, e) => HandleAcceptFriendRequest());
    view.AddDeclineFriendRequestListener((s, e) => HandleDeclineFriendRequest());
    view.AddProfileListener((s, e) => OpenProfileView());
    view.AddLogoutListener((s, e) => HandleLogout());
    view.GetIncreaseFontSizeMenuItem().Click += (s, e) => HandleIncreaseFontSize();
    view.GetDecreaseFontSizeMenuItem().Click += (s, e) => HandleDecreaseFontSize();
    view.AddManualPollingListener((s, e) => ManualPolling());
}

public async void LoadFriends() {
    try {
        var friends = await apiService.GetFriends(authService.GetCurrentToken());
        view.SetFriendsList(friends);
    } catch (ApiException ex) {
        view.ShowError(ErrorMessageTranslator.Translate(ex));
    }
}
```

**Polling logika:**
- Timer vagy Task időzítővel hívja a LoadFriends, LoadMessages, LoadFriendRequests metódusokat.
- Manuális polling gomb is elérhető.

### 6.3 ProfilePresenter

**Feladat:** Profil betöltése, mentés, jelszóemlékeztető, szerver URL kezelés.

**Főbb mezők:**
- ProfileView _view
- AuthService _authService

**Főbb metódusok:**
- `LoadProfile()`
- `HandleSaveProfile(nickname, email, password)`
- `HandleForgotPassword(email)`
- `LoadServerUrl()`
- `HandleSaveServerUrl(url)`
- `AttachListeners()`

**Workflow példa:**
```csharp
public void AttachListeners() {
    view.AddSaveListener((s, e) => HandleSaveProfile(view.GetNickname(), view.GetEmail(), view.GetPassword()));
    view.AddForgotPasswordListener((s, e) => HandleForgotPassword(view.GetEmail()));
}

public async void HandleSaveProfile(string nickname, string email, string password) {
    try {
        // Validáció
        await authService.UpdateProfile(nickname, email, password);
        view.ShowSuccess("Profil mentve.");
    } catch (ApiException ex) {
        view.ShowError(ErrorMessageTranslator.Translate(ex));
    }
}
```

### 6.4 ConfigurationPresenter

**Feladat:** Szerver URL beállítás, mentés, cancel.

**Főbb mezők:**
- ConfigurationView _view
- AuthService _authService

**Főbb metódusok:**
- `GetServerUrlForInstance(instanceId)`
- `GetCurrentServerUrl()`
- `HandleSave()`
- `HandleCancel()`
- `AttachListeners()`

**Workflow példa:**
```csharp
public void AttachListeners() {
    view.AddSaveListener((s, e) => HandleSave());
    view.AddCancelListener((s, e) => HandleCancel());
}

public void HandleSave() {
    var url = view.GetServerUrl();
    authService.SetServerUrl(url);
    view.ShowSuccess("Szerver URL mentve.");
}
```

### 6.5 Presenter workflow-k és kapcsolatok

- Minden UI eseményhez tartozik egy listener, amely a Presenter megfelelő metódusát hívja.
- A Presenter minden adat- vagy API-műveletet Service/DAO-n keresztül végez.
- Hibakezelés: minden try-catch blokkban, ErrorMessageTranslator-ral.
- Minden UI frissítés thread-safe módon történik (C#-ban pl. InvokeRequired/BeginInvoke).

### 6.6 Példák Presenter használatra

```csharp
// Bejelentkezés
loginPresenter.AttachListeners();

// Főablak inicializálás
mainPresenter.AttachListeners();
mainPresenter.LoadInitialData();

// Profil mentése
profilePresenter.AttachListeners();
```

## 7. View réteg és UI logika – Részletes kidolgozás

A View réteg felelős a felhasználói felület megjelenítéséért, komponensek kezeléséért, események továbbításáért a Presenter felé.

### 7.1 LoginView

**Feladat:** Bejelentkezési ablak, mezők, gombok, visszajelzés.

**Implementációs részletek:**
- A LoginView egy JFrame, amely tartalmazza a bejelentkezéshez szükséges mezőket és gombokat.
- A fő komponensek: emailField (JTextField), passwordField (JPasswordField), nicknameField (JTextField), loginButton (JButton), forgotPasswordButton (JButton).
- A mezők elrendezése GridLayout-ban történik, a gombok FlowLayout-ban.
- A getEmail(), getPassword(), getNickname() metódusok visszaadják a mezők aktuális értékét.
- A setEmail(), setPassword() metódusokkal előre kitölthető a mező (pl. preferences-ből).
- Az addLoginListener() és addForgotPasswordListener() metódusokkal ActionListener adható a gombokhoz.
- A showError() és showSuccess() metódusok JOptionPane dialógusban jelenítik meg az üzenetet.
- A clearForm() metódus minden mezőt töröl.
- Az ablak mérete fix (400x300), középre igazított, bezáráskor kilép az alkalmazásból.
- A LoginView csak a UI-t jeleníti meg, minden logika a Presenter-ben van.

**Főbb mezők/komponensek:**
- JTextField emailField – email cím beviteli mező
- JPasswordField passwordField – jelszó beviteli mező
- JTextField nicknameField – becenév beviteli mező
- JButton loginButton – bejelentkezés gomb
- JButton forgotPasswordButton – jelszóemlékeztető gomb

**Főbb metódusok:**
- setEmail(String email), setPassword(String password)
- getEmail(), getPassword(), getNickname()
- addLoginListener(ActionListener listener)
- addForgotPasswordListener(ActionListener listener)
- showError(String message), showSuccess(String message)
- clearForm()

**Technikai összefoglaló:**
- A LoginView minden mezője és gombja csak a UI-t jeleníti meg, minden esemény a Presenter-hez van továbbítva.
- A mezők értékei getterrel lekérhetők, setterrel beállíthatók.
- A hibák és sikeres műveletek dialógusablakban jelennek meg.
- A mezők törlése a clearForm() metódussal történik.

### 7.2 MainView

**Feladat:** Főablak, barátlista, chat panel, barátkérések, menü, polling, fontméret, visszajelzés.

**Implementációs részletek:**
- A MainView egy JFrame, amely három fő panelből áll: bal oldalon barátlista, középen chat panel, jobb oldalon barátkérések.
- A menüsorban található: File (Profil, Logout), Polling (időzítés), View (betűméret állítás).
- A barátlista (JList<User>) bal oldalon, hozzáadás mezővel és gombbal.
- A chat panel középen: chatArea (JTextPane, nem szerkeszthető), messageField (JTextField), sendButton (JButton).
- A chatArea minden üzenetet színezve jelenít meg: confirmed=false piros, confirmed=true fekete.
- **Olvasott/olvasatlan üzenetek jelölése a chatben:**  
  - A Message objektum read mezője (boolean) jelzi, hogy az adott üzenet olvasott-e.
  - A chatArea-ban jelenleg csak a confirmed státusz alapján történik színezés (piros/fekete), de a read státusz alapján is bővíthető: pl. olvasatlan üzenetnél félkövér vagy kék szín.
  - A jelenlegi implementációban a read státusz explicit színezése nincs, de a Message.read mező elérhető, így a setChatMessages() metódusban bővíthető.
- **Barátlista – olvasatlan üzenetek jelölése:**  
  - A User osztályban van egy hasUnreadMessages() metódus, amely azt jelzi, hogy az adott baráttól van-e olvasatlan üzenet.
  - A setFriendsList() metódus cellRenderer-e a User.hasUnreadMessages() alapján piros színnel jeleníti meg a barát nevét, ha van olvasatlan üzenet tőle, különben feketével.
  - Ez a logika biztosítja, hogy a barátlista vizuálisan kiemeli azokat a barátokat, akiktől olvasatlan üzenet van.
- A jobb oldali panelen barátkérések listája (JList<User>), elfogadás/elutasítás gombokkal.
- Polling gomb a chat panel tetején, userIdLabel és friendInfoLabel információs címkékkel.
- Betűméret minden komponensen növelhető/csökkenthető (rekurzív font állítás).
- Minden fő komponens getterrel elérhető, minden eseményhez külön addXListener metódus.
- A setFriendsList() és setFriendRequests() metódusok DefaultListModel-t használnak, cellRenderer-rel formázzák a megjelenítést.
- A setChatMessages() metódus StyledDocument-et használ, minden üzenet státusz szerint színezve jelenik meg (confirmed, illetve bővíthető read alapján is).
- A getMessageText(), clearMessageText(), getAddFriendNickname(), clearAddFriendNickname() metódusok a mezők kezelésére szolgálnak.
- Hibák és sikeres műveletek JOptionPane dialógusban jelennek meg.
- A menüpontokhoz, gombokhoz, listákhoz minden esemény külön metódussal köthető.
- A MainView csak a UI-t jeleníti meg, minden logika a Presenter-ben van.

**Főbb mezők/komponensek:**
- JList<User> friendsList – barátlista
- JTextPane chatArea – chat üzenetek megjelenítése
- JTextField messageField – üzenet beviteli mező
- JButton sendButton – üzenetküldés gomb
- JMenuBar menuBar – főmenü
- JMenuItem logoutMenuItem, profileMenuItem, setPollingPeriodMenuItem, increaseFontSizeMenuItem, decreaseFontSizeMenuItem – menüpontok
- JButton manualPollingButton – polling gomb
- JList<User> friendRequestsList – barátkérések listája
- JButton acceptFriendRequestButton, declineFriendRequestButton – barátkérés elfogadás/elutasítás
- JTextField addFriendField – barát hozzáadás mező
- JButton addFriendButton – barát hozzáadás gomb
- JLabel userIdLabel, friendInfoLabel – információs címkék

**Főbb metódusok:**
- setFriendsList(List<User> friends)
- setChatMessages(List<Message> messages)
- setFriendRequests(List<User> requests)
- addFriendSelectionListener(ListSelectionListener listener)
- addSendMessageListener(ActionListener listener)
- addAddFriendListener(ActionListener listener)
- addAcceptFriendRequestListener(ActionListener listener)
- addDeclineFriendRequestListener(ActionListener listener)
- addProfileListener(ActionListener listener)
- addLogoutListener(ActionListener listener)
- addManualPollingListener(ActionListener listener)
- getMessageText(), clearMessageText()
- getAddFriendNickname(), clearAddFriendNickname()
- showError(String message), showSuccess(String message)
- getIncreaseFontSizeMenuItem(), getDecreaseFontSizeMenuItem()
- getSetPollingPeriodMenuItem(), addSetPollingPeriodListener(ActionListener listener)
- setUserIdLabelText(String text), setFriendInfoLabelText(String text)
- setSelectedFriendInList(User user)
- getCurrentSelectedFriend(), setCurrentSelectedFriend(User user)

**UI logika és workflow:**
- Barát kiválasztása: friendsList.addListSelectionListener → Presenter.SetCurrentSelectedFriend()
- Üzenet küldése: sendButton.addActionListener → Presenter.HandleSendMessage()
- Barát hozzáadása: addFriendButton.addActionListener → Presenter.HandleAddFriend()
- Barátkérés elfogadása/elutasítása: acceptFriendRequestButton/declineFriendRequestButton.addActionListener → Presenter.HandleAcceptFriendRequest()/HandleDeclineFriendRequest()
- Profil menü: profileMenuItem.addActionListener → Presenter.OpenProfileView()
- Kijelentkezés: logoutMenuItem.addActionListener → Presenter.HandleLogout()
- Polling: manualPollingButton.addActionListener → Presenter.ManualPolling()
- Polling időzítés: setPollingPeriodMenuItem.addActionListener → Presenter.SetPollingPeriod()
- Fontméret: increaseFontSizeMenuItem/decreaseFontSizeMenuItem.addActionListener → Presenter.HandleIncreaseFontSize()/HandleDecreaseFontSize()
- Hibák, visszajelzések: showError(), showSuccess() dialógusban

**Technikai összefoglaló:**
- A MainView minden mezője és gombja csak a UI-t jeleníti meg, minden esemény a Presenter-hez van továbbítva.
- A chatArea StyledDocument-et használ, minden üzenet státusz szerint színezve jelenik meg.
- A barátlista és barátkérések lista cellRenderer-rel formázott.
- Minden fő komponens getterrel elérhető, minden eseményhez külön addXListener metódus.
- A betűméret minden komponensen rekurzívan növelhető/csökkenthető.
- A MainView csak a UI-t jeleníti meg, minden logika a Presenter-ben van.

### 7.3 ProfileView

**Feladat:** Profil adatok megjelenítése, módosítása, mentés, visszajelzés.

**Implementációs részletek:**
- A ProfileView egy JFrame, amely a felhasználó profiladatainak szerkesztésére szolgál.
- A fő komponensek: nicknameField (JTextField), emailField (JTextField, csak olvasható), avatarUrlField (JTextField), saveButton (JButton), cancelButton (JButton).
- A mezők GridBagLayout-ban helyezkednek el, minden mezőhöz tartozik címke.
- A getNickname(), getAvatarUrl() metódusok visszaadják a mezők aktuális értékét.
- A setNickname(), setEmail(), setAvatarUrl() metódusokkal előre kitölthető a mező (pl. adatbázisból).
- Az email mező nem szerkeszthető.
- Az addSaveListener() és addCancelListener() metódusokkal ActionListener adható a gombokhoz.
- Az ablak mérete fix (400x250), középre igazított, bezáráskor csak az ablak záródik (DISPOSE_ON_CLOSE).
- A ProfileView csak a UI-t jeleníti meg, minden logika a Presenter-ben van.

**Főbb mezők/komponensek:**
- JTextField nicknameField – felhasználónév mező
- JTextField emailField – email mező (nem szerkeszthető)
- JTextField avatarUrlField – avatar URL mező
- JButton saveButton – mentés gomb
- JButton cancelButton – mégse gomb

**Főbb metódusok:**
- setNickname(String nickname), setEmail(String email), setAvatarUrl(String url)
- getNickname(), getAvatarUrl()
- addSaveListener(ActionListener listener)
- addCancelListener(ActionListener listener)

**Technikai összefoglaló:**
- A ProfileView minden mezője és gombja csak a UI-t jeleníti meg, minden esemény a Presenter-hez van továbbítva.
- A mezők értékei getterrel lekérhetők, setterrel beállíthatók.
- Az email mező nem szerkeszthető.
- Az ablak bezárása nem lépteti ki az alkalmazást, csak a profilt zárja be.

### 7.4 ConfigurationView

**Feladat:** Szerver URL beállítása, mentés, cancel, visszajelzés.

**Implementációs részletek:**
- A ConfigurationView egy JFrame, amely a szerver URL beállítására szolgál.
- A fő komponensek: serverUrlField (JTextField), saveButton (JButton), cancelButton (JButton).
- A mezők GridLayout-ban helyezkednek el, minden mezőhöz tartozik címke.
- A getServerUrl() metódus visszaadja a mező aktuális értékét (trim-elve).
- Az addSaveListener() és addCancelListener() metódusokkal ActionListener adható a gombokhoz.
- A showError() és showSuccess() metódusok JOptionPane dialógusban jelenítik meg az üzenetet.
- Az ablak mérete fix (500x150), középre igazított, bezáráskor kilép az alkalmazásból.
- A ConfigurationView csak a UI-t jeleníti meg, minden logika a Presenter-ben van.

**Főbb mezők/komponensek:**
- JTextField serverUrlField – szerver URL mező
- JButton saveButton – mentés gomb
- JButton cancelButton – mégse gomb

**Főbb metódusok:**
- getServerUrl()
- addSaveListener(ActionListener listener)
- addCancelListener(ActionListener listener)
- showError(String message), showSuccess(String message)

**Technikai összefoglaló:**
- A ConfigurationView minden mezője és gombja csak a UI-t jeleníti meg, minden esemény a Presenter-hez van továbbítva.
- A mező értéke getterrel lekérhető.
- Hibák és sikeres műveletek dialógusablakban jelennek meg.
- Az ablak bezárása kilépteti az alkalmazást.

### 7.5 View workflow-k és kapcsolatok

- Minden UI komponenshez tartozik listener, amely a Presenter megfelelő metódusát hívja.
- A View csak adatot jelenít meg, minden logika a Presenter-ben van.
- Minden visszajelzés (hiba, siker) közvetlenül a View-ban jelenik meg.
- Minden UI művelet thread-safe módon történik (C#-ban InvokeRequired/BeginInvoke).

### 7.6 Példák View használatra

```csharp
// LoginView inicializálás és eseménykezelés
var loginView = new LoginView();
loginView.AddLoginListener((s, e) => presenter.HandleLogin());
loginView.AddForgotPasswordListener((s, e) => presenter.HandleForgotPassword());

// MainView események
mainView.AddSendMessageListener((s, e) => presenter.HandleSendMessage(mainView.GetMessageText()));
mainView.AddAddFriendListener((s, e) => presenter.HandleAddFriend());
mainView.AddProfileListener((s, e) => presenter.OpenProfileView());
```

## 8. API-k és végpontok

- **/chat/api/login**: POST, email, password, nickname
- **/chat/api/addFriend**: POST, token, friendId, nickname, email
- **/chat/api/deleteFriend**: POST, token, friendId, action
- **/chat/api/getFriends**: GET, token
- **/chat/api/getFriendRequests**: GET, token
- **/chat/api/sendMessage**: POST, token, message
- **/chat/api/getMessages**: GET, token, confirmedMessageIds, lastMessageId, lastRequestDate
- **/chat/api/forgotPassword**: POST, email

## 9. Hibakezelés

- Minden API hívás try-catch ApiException és Exception.
- ErrorMessageTranslator: szerver hibaüzenet magyar, felhasználóbarát fordítása.
- Hibák megjelenítése a megfelelő UI panelen (showError).
- Tesztelés: hibás adatok, szerverhiba, hálózati hiba, Exception szimuláció.

## 10. Tesztelés

- Funkcionális tesztek: bejelentkezés, üzenetküldés, barátkezelés, profilmódosítás, hibakezelés.
- Hibás adatok, szerverhiba, hálózati hiba, Exception szimuláció.
- Automatizálható tesztesetek (JUnit, NUnit).

## 11. Platformfüggetlen és C# portolási szempontok

- **MVP/MVVM**: Java Swing → C# WinForms/WPF (UserControl, DataBinding, ICommand)
- **Adatbázis**: JDBC → System.Data.SQLite vagy Entity Framework
- **REST API**: OkHttp/Retrofit → HttpClient/RestSharp
- **Async**: SwingWorker → async/await, Task
- **Preferences**: Java Preferences API → C# Settings vagy Registry
- **Exception kezelés**: try-catch, saját Exception osztályok
- **UI komponensek**: JPanel, JFrame, JTextPane → UserControl, Form, RichTextBox stb.

## 12. Példakódok és sémaábrák

### Példa: Üzenetküldés workflow (Java → C#)

```java
// Java
mainView.addSendMessageListener(e -> presenter.handleSendMessage(content, mediaFile));
```
```csharp
// C#
sendMessageButton.Click += (s, e) => presenter.HandleSendMessage(content, mediaFile);
```

### Példa: API hívás (Java → C#)

```java
// Java
try {
        apiService.sendMessage(token, message);
} catch (ApiException ex) {
        view.showError(ErrorMessageTranslator.translate(ex));
        }

```
```csharp
// C#
try {
    apiService.SendMessage(token, message);
} catch (ApiException ex) {
    view.ShowError(ErrorMessageTranslator.Translate(ex));
}
```

---

## 13. Összefoglalás

Ez a dokumentáció minden főbb logikai kapcsolatot, UI-t, eseménykezelést, workflow-t, panelt, menüt, gombot, listener-t, callback-et, thread-et, pollingot, validációt, hibakezelést, visszajelzést, minden UI és backend kapcsolatot részletesen, példákkal, kódrészletekkel, C#-ban is értelmezhető módon tartalmaz. A dokumentum alapján a teljes alkalmazás C# környezetben is újraimplementálható.
