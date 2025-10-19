# Chat Java Client

Ez a dokumentum a Java kliensalkalmazás felépítését és a fejlesztési folyamat lépéseit vázolja fel a **Java Swing** és **Model-View-Presenter (MVP)** architektúra alapján.

## Javasolt Technológiai Készlet

*   **Felhasználói felület (GUI):** **Java Swing** – A Java standard, platformfüggetlen GUI könyvtára.
*   **HTTP Kommunikáció:** **OkHttp** – Hatékony és megbízható HTTP kliens.
*   **JSON Feldolgozás:** **Gson** – Google könyvtár JSON szerializációhoz/deszerializációhoz.
*   **Projektmenedzsment:** **Maven** – A projekt és függőségeinek kezelésére.

## Architektúra: Model-View-Presenter (MVP)

A projekt két fő modulra bomlik a tiszta kódszerkezet és a felelősségi körök szétválasztása érdekében:

*   **`core` modul:** Tartalmazza a teljes üzleti logikát, beleértve az API-kommunikációt, adatmodelleket és a perzisztenciát. Teljesen független a felhasználói felülettől.
*   **`ui-swing` modul:** Megvalósítja a Swing alapú felhasználói felületet az MVP minta szerint. A `core` modultól függ.

## Build Folyamat

A projekt buildeléséhez a Maven szükséges. Futtassa a következő parancsot a projekt gyökérkönyvtárából:

```bash
mvn clean package
```

Ez a parancs letisztítja a korábbi buildeket, lefordítja a forráskódot, futtatja a teszteket, és becsomagolja az alkalmazást.

## Indítás

A sikeres buildelés után a `ui-swing/target/` mappában létrejön egy futtatható JAR fájl. Az alkalmazás indításához használja a következő parancsot:

```bash
java -jar ui-swing/target/ui-swing-1.0-SNAPSHOT-jar-with-dependencies.jar
