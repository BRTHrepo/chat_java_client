# Chat Java Client

Ez a dokumentum a Java kliensalkalmazás felépítését és a fejlesztési folyamat lépéseit vázolja fel a **Java Swing** és **Model-View-Presenter (MVP)** architektúra alapján.
Ez a dokumentum a Java chat alkalmazás felhasználói szemszögből történő bemutatását tartalmazza.

## Áttekintés

A Chat Java Client egy modern, platformfüggetlen asztali chat alkalmazás, amely lehetővé teszi:
- gyors és biztonságos bejelentkezést,
- barátok kezelését,
- szöveges üzenetek küldését (média - kép, hang - küldése fejlesztési lehetőség, jelenleg csak szöveg támogatott),
- profiladatok megtekintését és szerkesztését,
- több klienspéldány egyidejű futtatását külön beállításokkal,
- automatikus adatmentést és offline működést.

Az alkalmazás célja, hogy egyszerű, átlátható felületen keresztül biztosítson valós idejű kommunikációt, miközben a felhasználói élmény és adatbiztonság elsődleges.

## Fő funkciók

## Javasolt Technológiai Készlet
- **Bejelentkezés:**  
  A felhasználó email-cím és jelszó megadásával jelentkezhet be. A rendszer automatikusan kezeli a tokeneket, így a bejelentkezés után a felhasználónak nem kell újra hitelesítenie magát, amíg a token érvényes.

*   **Felhasználói felület (GUI):** **Java Swing** – A Java standard, platformfüggetlen GUI könyvtára.
*   **HTTP Kommunikáció:** **OkHttp** – Hatékony és megbízható HTTP kliens.
*   **JSON Feldolgozás:** **Gson** – Google könyvtár JSON szerializációhoz/deszerializációhoz.
*   **Projektmenedzsment:** **Maven** – A projekt és függőségeinek kezelésére.
- **Barátkezelés:**  
  Barátok hozzáadása, törlése, barátkérések elfogadása/elutasítása. A barátlista mindig naprakész, a szerverrel automatikusan szinkronizálódik.

## Architektúra: Model-View-Presenter (MVP)
- **Chat:**  
  Kiválasztott baráttal folytatott beszélgetés, üzenetek listázása, új üzenet küldése (jelenleg csak szöveg, a média - kép, hang - küldése fejlesztési lehetőség).  
  A nem megerősített (confirmed=false) üzenetek piros színnel jelennek meg, a megerősítettek feketével.  
  A betűméret a View menüben állítható.

A projekt két fő modulra bomlik a tiszta kódszerkezet és a felelősségi körök szétválasztása érdekében:
- **Profil és beállítások:**  
  Profiladatok (név, avatar) megtekintése, szerkesztése.  
  Szerver URL és egyéb beállítások módosítása a beállítások menüben.

*   **`core` modul:** Tartalmazza a teljes üzleti logikát, beleértve az API-kommunikációt, adatmodelleket és a perzisztenciát.
    - **Lokális SQLite adatbázis:** A core modulban egy beágyazott SQLite adatbázis kezeli az üzenet-előzményeket, barátlistát, barátkéréseket és eseménylogokat. Ez lehetővé teszi az offline működést, gyors keresést, naplózást, és multiplatform támogatást (Java, Android, .NET/C#).
    - A perzisztencia réteg DAO-kon keresztül érhető el, a Presenter csak a modellel kommunikál.
*   **`ui-swing` modul:** Megvalósítja a Swing alapú felhasználói felületet az MVP minta szerint. A `core` modultól függ.
- **Több példány támogatása:**  
  Az alkalmazás minden példánya a futtatási könyvtár (`System.getProperty("user.dir")`) alapján kap egyedi beállításokat. Így ugyanabból a könyvtárból indítva a beállítások megmaradnak, más könyvtárból külön példányként viselkedik az alkalmazás.

## Build Folyamat
- **Offline működés:**  
  Az üzenetek, barátlista és események helyi SQLite adatbázisban is tárolódnak, így internetkapcsolat nélkül is elérhetők.

A projekt buildeléséhez a Maven szükséges. Futtassa a következő parancsot a projekt gyökérkönyvtárából:
## Használat lépései

### 1. **Buildelés**

A projekt buildeléséhez szükséges:
- Java 17 vagy újabb (ellenőrizd: `java -version`)
- Maven (ellenőrizd: `mvn -version`)

A buildelés lépései:
```bash
mvn clean package
```

Ez a parancs letisztítja a korábbi buildeket, lefordítja a forráskódot, futtatja a teszteket, és becsomagolja az alkalmazást.

## Login és token kezelés (2025. október 21.)

- A szerver a login válaszban a JWT tokent külön kulcsként (`"token"`) küldi, nem a user objektum részeként.
- A Java kliens a teljes JSON választ feldolgozza: a tokent külön eltárolja, a user adatokat (`user_id`, `nickname`, `email`) külön tölti be.
- Minden programinduláskor tényleges login történik a Preferences-ben tárolt email/jelszóval (a mezők automatikusan kitöltődnek).
- Sikertelen login esetén csak a token nullázódik, a mezők kitöltve maradnak, és a felhasználó újra próbálkozhat.
- A pooling során a token érvényességét folyamatosan figyeljük, és ha lejár vagy érvénytelen, automatikusan újra loginolunk (vagy visszairányítjuk a felhasználót a login képernyőre).
- **Újdonság:** A JWT token érvényességét a kliens periodikusan ellenőrzi, és ha lejárt vagy hamarosan lejár, automatikusan frissíti (új login vagy refresh). Így a felhasználónak nem kell manuálisan újra bejelentkeznie.

## Több Klienspéldány Támogatása Könyvtárfüggő Beállításokkal

A korábbi implementációban a `java.util.prefs.Preferences` osztály alapértelmezetten a felhasználó operációs rendszerén tárolta a beállításokat. A `ConfigurationPresenter` osztályban a `Preferences.userNodeForPackage(ConfigurationPresenter.class)` hívás minden futó példány számára ugyanazt a preferencia csomópontot hozta létre. Ez megakadályozta több klienspéldány egyidejű futtatását külön felhasználókkal, mivel mindegyik ugyanazokat a beállításokat (pl. szerver URL) használta.

A probléma megoldására az alkalmazás módosításra került, hogy minden klienspéldány a futtatási könyvtár (System.getProperty("user.dir")) alapján kap egyedi `Preferences` csomópontot. Ez a következőképpen valósul meg:
- A `ConfigurationPresenter` konstruktora most egyedi `instanceId` paramétert fogad el, amely a futtatási könyvtár abszolút elérési útja.
- Ezt az `instanceId`-t base64 kódolva használja a `Preferences.userNodeForPackage(ConfigurationPresenter.class).node(...)` hívásban, így minden könyvtárból indított példány külön beállításokat kap.
- A `Main` osztályban az instanceId a `System.getProperty("user.dir")`, így ugyanabból a könyvtárból indítva a beállítások megmaradnak, más könyvtárból külön példányként viselkedik az alkalmazás.
- Az `AuthService` példányosításakor is ezt az instanceId-t adjuk át.
- A szerver URL lekérdezése most már az új `ConfigurationPresenter.getServerUrlForInstance(instanceId)` statikus metódussal történik, amely az adott könyvtárhoz tartozó beállításokat használja.
# 1. Lépj a projekt gyökerébe
cd /elérési/út/a/chat_java_client

Ezek a módosítások lehetővé teszik több klienspéldány egyidejű futtatását, mindegyik saját, elkülönített beállításokkal, de ugyanabból a könyvtárból indítva a beállítások tartósak maradnak.
# 2. Tisztítsd a korábbi buildeket (opcionális)
mvn clean

## Indítás
# 3. Fordítsd le és csomagold a projektet
mvn package
```

A sikeres buildelés után a `ui-swing/target/` mappában létrejön egy futtatható JAR fájl. Az alkalmazás indításához használja a következő parancsot:
A sikeres build után a futtatható JAR a `ui-swing/target/` mappában jön létre:

```bash
java -jar ui-swing/target/ui-swing-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Újdonságok és fejlesztések (2025.10.23.)
**Tesztek futtatása:**
```bash
mvn test
```
> **Megjegyzés:** Jelenleg nincsenek automatikus (JUnit) tesztek a projektben, de a Maven build és tesztfázis támogatott, így később bővíthető.

**Lehetséges hibák:**
- Ha a Java vagy Maven nincs telepítve, telepítsd a hivatalos oldalról.
- Ha dependency hibát kapsz, próbáld újra a `mvn clean package` parancsot.

### 2. **Bejelentkezés:**
Add meg az email-címed és jelszavad. Sikeres bejelentkezés után a főablak jelenik meg.
3. **Barát hozzáadása:**  
   Írd be a barátod email-címét vagy becenevét, majd kattints az "Add" gombra.
4. **Üzenetküldés:**  
   Válassz ki egy barátot a listából, írj üzenetet, majd kattints a "Send" gombra.  
   (Jelenleg csak szöveges üzenet támogatott, a média csatolás fejlesztési lehetőség.)
5. **Profil szerkesztése:**  
   A menüben válaszd a "Profil..." opciót, módosítsd az adatokat, majd mentsd el.
6. **Beállítások:**  
   A szerver URL és egyéb beállítások a beállítások menüben módosíthatók.
   > **Fontos:** A szerver címe bármikor, forráskód módosítása vagy újrafordítás nélkül átírható a programban, és a beállítás elmentésre kerül.
7. **Kijelentkezés:**  
   A "Logout" menüponttal bármikor kijelentkezhetsz.

## Platformkövetelmények

- Java 17 vagy újabb
- Windows, Linux, macOS támogatott
- Internetkapcsolat szükséges a szerverrel való szinkronizációhoz

## Adatvédelem és biztonság

- A bejelentkezési adatok és tokenek titkosítva, csak helyben tárolódnak.
- Minden kommunikáció HTTPS-en keresztül történik.
- A helyi SQLite adatbázis minden példányhoz külön jön létre.

- **Chat UI színezés:** A chat ablakban a nem megerősített (confirmed=false) üzenetek piros színnel jelennek meg, a megerősítettek feketével.
- **Betűméret állítás:** A View menüben a betűméret növelése/csökkentése mostantól a chatArea-ra is érvényes, a színezés és a méret együtt működik.
- **Üzenet státusz logika:** A Message osztály confirmed státusza alapján történik a színezés, így a felhasználó azonnal látja, mely üzenetek státusza bizonytalan.
- **not_updated_ids kezelés:** A szerver oldali logika pontosítása: csak a fogadó fél tudja confirmed=true-ra állítani az üzenetet, a küldő nem.
- **Technikai háttér:** A MainView chatArea komponense JTextPane-re lett cserélve, StyledDocument-et használ, így támogatott a soronkénti színezés és formázás.
## Tipikus workflow

*Megjegyzés: A kliensoldali tervdokumentáció (`docs/client/java-client-plan.md`) jelenleg nem található.*
1. Indítsd el az alkalmazást.
2. Jelentkezz be vagy regisztrálj.
3. Adj hozzá barátokat, fogadj el barátkéréseket.
4. Chatelj, küldj és fogadj szöveges üzeneteket. (A média csatolás jelenleg nem támogatott, fejlesztési lehetőség.)
5. Szerkeszd a profilod, módosítsd a beállításokat.
6. Használd az alkalmazást akár több példányban, különböző könyvtárakból.

## GYIK

- **Elvesznek az adataim, ha újraindítom az appot?**  
  Nem, minden beállítás és adat megmarad ugyanabból a könyvtárból indítva.
- **Futhat egyszerre több példány?**  
  Igen, minden példány külön beállításokat és adatbázist használ, ha más könyvtárból indítod.
- **Miért piros egy üzenet?**  
  Az üzenet még nem lett megerősítve a szerver által (pl. nem ért célba vagy nincs visszaigazolva).

## Hibabejelentés, támogatás

Ha hibát találsz vagy kérdésed van, írj a projekt fejlesztőinek vagy nyiss issue-t a repository-ban.

## Felhasználói élmény

- Minden fontosabb művelet (barátkérés, törlés, elfogadás/elutasítás) felugró ablakban visszaigazolható.
- A barátlista és barátkérések listája mindig naprakész, a polling és manuális frissítés révén.
- A felhasználó azonnal látja saját ID-ját, a kiválasztott barát adatait, és minden művelethez egyértelmű visszajelzést kap.

## Dokumentációk

- [docs/PROGRESS.md](docs/PROGRESS.md) – fejlesztési napló, főbb mérföldkövek
- [docs/friend_design.md](docs/friend_design.md) – barátkezelés, API, UX részletek
- [docs/mainview_design.md](docs/mainview_design.md) – fő UI elrendezés, komponensek
- [docs/chat_design.md](docs/chat_design.md) – chat logika, státusz, színezés
- [ui-swing/README.md](ui-swing/README.md) – Swing UI specifikus részletek
- [core/README.md](core/README.md) – core modul, perzisztencia, DAO-k


