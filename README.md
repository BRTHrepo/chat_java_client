# Chat Java Client

Ez a dokumentum a Java kliensalkalmazás felépítését és a fejlesztési folyamat lépéseit vázolja fel a **Java Swing** és **Model-View-Presenter (MVP)** architektúra alapján.

## Javasolt Technológiai Készlet

*   **Felhasználói felület (GUI):** **Java Swing** – A Java standard, platformfüggetlen GUI könyvtára.
*   **HTTP Kommunikáció:** **OkHttp** – Hatékony és megbízható HTTP kliens.
*   **JSON Feldolgozás:** **Gson** – Google könyvtár JSON szerializációhoz/deszerializációhoz.
*   **Projektmenedzsment:** **Maven** – A projekt és függőségeinek kezelésére.

## Architektúra: Model-View-Presenter (MVP)

A projekt két fő modulra bomlik a tiszta kódszerkezet és a felelősségi körök szétválasztása érdekében:

*   **`core` modul:** Tartalmazza a teljes üzleti logikát, beleértve az API-kommunikációt, adatmodelleket és a perzisztenciát.
    - **Lokális SQLite adatbázis:** A core modulban egy beágyazott SQLite adatbázis kezeli az üzenet-előzményeket, barátlistát, barátkéréseket és eseménylogokat. Ez lehetővé teszi az offline működést, gyors keresést, naplózást, és multiplatform támogatást (Java, Android, .NET/C#).
    - A perzisztencia réteg DAO-kon keresztül érhető el, a Presenter csak a modellel kommunikál.
*   **`ui-swing` modul:** Megvalósítja a Swing alapú felhasználói felületet az MVP minta szerint. A `core` modultól függ.

## Build Folyamat

A projekt buildeléséhez a Maven szükséges. Futtassa a következő parancsot a projekt gyökérkönyvtárából:

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

## Indítás

A sikeres buildelés után a `ui-swing/target/` mappában létrejön egy futtatható JAR fájl. Az alkalmazás indításához használja a következő parancsot:

```bash
java -jar ui-swing/target/ui-swing-1.0-SNAPSHOT-jar-with-dependencies.jar
