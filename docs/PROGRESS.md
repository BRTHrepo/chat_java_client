# Projekt Haladási Napló

Ez a dokumentum a Java kliensalkalmazás felépítését és a fejlesztési folyamat lépéseit rögzíti.

## Elkészült funkciók

- [x] Alap projektstruktúra kialakítása (core, ui-swing modulok)
- [x] API kommunikációs réteg alapjai (`ApiService`)
- [x] Authentikációs logika (`AuthService`)
- [x] Bejelentkezési felület és a hozzá tartozó prezenter (`LoginView`, `LoginPresenter`)
- [x] Központi hibakezelési mechanizmus (`ApiException`, `ApiError`, `ErrorMessageTranslator`)
- [x] Fő nézet (Main View) alapvető implementációja:
    - Felhasználói felület vázának létrehozása (`MainView`)
    - Prezentációs logika vázának létrehozása (`MainPresenter`)
    - Bejelentkezés után a fő nézet megnyitása
    - Barátok listájának betöltése és megjelenítése
    - Üzenetek betöltése és megjelenítése barát kiválasztásakor
    - Üzenetküldés funkció implementálása
- [x] Kijelentkezés funkció implementálása
- [x] Barátkezelési funkciók (barátkérés elfogadása/elutasítása, új barát hozzáadása)
- [x] JSON feldolgozási hiba javítása az `ApiService`-ben

## Jelenlegi állapot

A fő nézet alapvető funkciói, a barátkezelés és a kijelentkezés is működik. A felhasználó sikeres bejelentkezés után látja a barátainak és a barátkéréseinek listáját, tud üzenetet küldeni. A hálózati kérések aszinkron módon, a UI blokkolása nélkül futnak. A JSON feldolgozási hibák javítva lettek.

A MainPresenter-ben minden API hívás külön AtomicBoolean flag-et használ, így a különböző műveletek egymástól függetlenül, párhuzamosan is futhatnak.

### 2025.10.21. - Login tokenkezelés javítása

- A szerver a login válaszban a JWT tokent külön kulcsként (`"token"`) küldi.
- A Java kliens most már a teljes JSON választ feldolgozza: a tokent külön eltárolja, a user adatokat (`user_id`, `nickname`, `email`) külön tölti be.
- Új metódus: `registerLoginRaw` az ApiService-ben, amely a teljes JSON választ visszaadja.
- Így a login után a token nem lesz null, és minden védett API hívásnál helyesen átadásra kerül az Authorization headerben.

### 2025.10.21. - JWT token automatikus frissítés polling során

- A kliens minden polling ciklusban ellenőrzi a JWT token érvényességét.
- Ha a token lejárt vagy hamarosan lejár, automatikusan frissíti (új login vagy refresh).
- Így a felhasználónak nem kell manuálisan újra bejelentkeznie, ha a token lejár.

### 2025.10.21. - Login folyamat javítása

- Minden programinduláskor tényleges login történik a Preferences-ben tárolt email/jelszóval (a mezők automatikusan kitöltődnek).
- Sikertelen login esetén csak a token nullázódik, a mezők kitöltve maradnak, és a felhasználó újra próbálkozhat.
- A pooling során a token érvényességét folyamatosan figyeljük, és ha lejár vagy érvénytelen, automatikusan újra loginolunk (vagy visszairányítjuk a felhasználót a login képernyőre).

### 2025.10.21. - Lokális SQLite adatbázis architektúra dokumentálása

- A core modulban beágyazott SQLite adatbázis kezeli az üzenet-előzményeket, barátlistát, barátkéréseket és eseménylogokat.
- A perzisztencia réteg DAO-kon keresztül érhető el, a Presenter csak a modellel kommunikál.
- A dokumentációkban (README.md, chat_client_execution_plan.md, chat_design.md, stb.) részletezve lett az MVP-integráció és a multiplatform támogatás (Java, Android, .NET/C#).

### 2025.10.21. - Presenter-DAO integráció, barátlista és üzenetek perzisztencia

- A MainPresenter-ben integrálva lett a perzisztencia réteg (DBService, FriendDao, MessageDao).
- A szerverről letöltött barátokat és üzeneteket először az adatbázisba menti, majd a UI-t mindig a DB-ből frissíti.
- Az üzenetküldésnél először az adatbázisba menti az üzenetet, majd elküldi a szerverre.
- A pooling során a szerverről letöltött adatok a DB-be kerülnek, a UI minden esetben a DB-ből olvas.
- Az adatbázis tartalma az alkalmazás leállítása után is megmarad a `chatapp.db` fájlban.

### 2025.10.21. - SQLite perzisztencia réteg implementációja

- Elkészült a DBService (adatbázis inicializálás, tábla létrehozás).
- Elkészült a MessageDao (üzenetek CRUD), FriendDao (barátok CRUD), FriendRequestDao (barátkérések CRUD), EventLogDao (eseménylog CRUD).
- A DAO-k MVP architektúrában a model réteg részei, a Presenter ezeken keresztül éri el a helyi adatokat.
- A projekt mostantól támogatja az üzenet-előzmények, barátlista, barátkérések és események tartós, lokális tárolását.

### 2025.10.21. - Profilnézet és szerkesztés

- Elkészült a ProfileView (profilnézet) és ProfilePresenter (logika).
- A felhasználó megtekintheti és szerkesztheti a nevét és avatar URL-jét.
- A módosítások csak a helyi adatbázisban és memóriában frissülnek, mert a szerveroldali API jelenleg nem támogatja a profiladatok módosítását.
- A főmenüben elérhető a "Profil..." menüpont, amely megnyitja a szerkesztő nézetet.

### 2025.10.23. - Több klienspéldány támogatása könyvtárfüggő beállításokkal

A korábbi implementációban a `java.util.prefs.Preferences` osztály alapértelmezetten a felhasználó operációs rendszerén tárolta a beállításokat. A `ConfigurationPresenter` osztályban a `Preferences.userNodeForPackage(ConfigurationPresenter.class)` hívás minden futó példány számára ugyanazt a preferencia csomópontot hozta létre. Ez megakadályozta több klienspéldány egyidejű futtatását külön felhasználókkal, mivel mindegyik ugyanazokat a beállításokat (pl. szerver URL) használta.

A probléma megoldására az alkalmazás módosításra került, hogy minden klienspéldány a futtatási könyvtár (`System.getProperty("user.dir")`) alapján kap egyedi `Preferences` csomópontot. Ez a következőképpen valósul meg:
- A `ConfigurationPresenter` konstruktora most egyedi `instanceId` paramétert fogad el, amely a futtatási könyvtár abszolút elérési útja.
- Ezt az `instanceId`-t base64 kódolva használja a `Preferences.userNodeForPackage(ConfigurationPresenter.class).node(...)` hívásban, így minden könyvtárból indított példány külön beállításokat kap.
- A `Main` osztályban az instanceId a `System.getProperty("user.dir")`, így ugyanabból a könyvtárból indítva a beállítások megmaradnak, más könyvtárból külön példányként viselkedik az alkalmazás.
- A szerver URL lekérdezése most már az új `ConfigurationPresenter.getServerUrlForInstance(instanceId)` statikus metódussal történik, amely az adott könyvtárhoz tartozó beállításokat használja.

Ezek a módosítások lehetővé teszik több klienspéldány egyidejű futtatását, mindegyik saját, elkülönített beállításokkal, de ugyanabból a könyvtárból indítva a beállítások tartósak maradnak.

A customPrefsNode változó eltávolítható, mert a preferences node kezeléséhez már nincs rá szükség.

## Következő lépések

A korábbi "Következő lépések" szekcióban felsorolt feladatok (Barátkezelés funkciók, Profil nézet, Kijelentkezés) már implementálva lettek, és a fenti "Jelenlegi állapot" részben dokumentálva. A legutóbbi fejlesztés, a több klienspéldány egyedi beállításokkal történő támogatása is elkészült.

Jelenleg nincsenek további tervezett lépések.

### 2025.10.23. - Chat UI fejlesztések: státusz színezés, betűméret, státuszlogika

- A MainView chatArea komponense JTextPane-re lett cserélve, így támogatott a soronkénti színezés és formázás.
- A setChatMessages metódus StyledDocument-et használ: ahol az üzenet confirmed=false, ott a szöveg piros színnel jelenik meg, egyébként fekete.
- A betűméret állítása (View menü) mostantól a chatArea-ra is érvényes, a színezés és a méret együtt működik.
- A Message osztály confirmed státusza alapján történik a színezés, így a felhasználó azonnal látja, mely üzenetek státusza bizonytalan.
- A not_updated_ids logika pontosítása: csak a fogadó fél tudja confirmed=true-ra állítani az üzenetet, a küldő nem.
- A dokumentációk (README.md, mainview_design.md, chat_design.md, chat_client_execution_plan.md) is frissülnek a fenti változásokkal.

### 2025.10.24. - Barátkérés és barát törlés UX fejlesztések

- Barátkérések listájában csak az email jelenik meg.
- Barátkérésre kattintva felugró ablakban lehet elfogadni/elutasítani, ugyanazt a logikát használva, mint az alsó gombok.
- Az Accept/Decline gombok egymás alatt jelennek meg.
- Barát hozzáadása mező label, input és gomb egymás alatt jelenik meg.
- Barátok listája görgethető maradt.
- Bal/jobb oldali panelek keskenyebbek, chat panel domináns.
- Saját user ID és kiválasztott barát neve/ID-ja mindig frissül a chat ablakban.
- Jobb gombos kattintás a barátlistán: felugró ablak a barát adataival és "Barát törlése" gombbal.
- Barát törlése API-hívással (`deleteFriend`), action mezővel (`"action": "delete"`).
- Az action mező lehetővé teszi, hogy ugyanaz az endpoint kezelje a törlést és az elutasítást is.
- Dokumentációk (README.md, friend_design.md, mainview_design.md) frissítése folyamatban.