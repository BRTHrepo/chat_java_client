# Java Kliens Projektterv (Swing & MVP)

Ez a dokumentum a Java kliensalkalmazás felépítését és a fejlesztési folyamat lépéseit vázolja fel a **Java Swing** és **Model-View-Presenter (MVP)** architektúra alapján.

### 1. Javasolt Technológiai Készlet

*   **Felhasználói felület (GUI):** **Java Swing** – A Java standard, platformfüggetlen GUI könyvtára.
*   **HTTP Kommunikáció:** **OkHttp** – Hatékony és megbízható HTTP kliens.
*   **JSON Feldolgozás:** **Gson** – Google könyvtár JSON szerializációhoz/deszerializációhoz.
*   **Projektmenedzsment:** **Maven** – A projekt és függőségeinek kezelésére.

### 2. Architektúra: Model-View-Presenter (MVP)

A projekt két fő modulra bomlik a tiszta kódszerkezet és a felelősségi körök szétválasztása érdekében:

*   **`core` modul:** Tartalmazza a teljes üzleti logikát, beleértve az API-kommunikációt, adatmodelleket és a perzisztenciát. Teljesen független a felhasználói felülettől.
*   **`ui-swing` modul:** Megvalósítja a Swing alapú felhasználói felületet az MVP minta szerint. A `core` modultól függ.

### 3. Mappa- és Fájlstruktúra

```
chat_java_client/
├── pom.xml                 // Fő Maven projektfájl, amely a modulokat definiálja
├── core/
│   ├── pom.xml
│   └── src/main/java/com/chatapp/core/
│       ├── model/          // Adatmodellek (User, Message, stb.)
│       ├── service/
│       │   ├── ApiService.java // API hívások
│       │   └── AuthService.java // Token- és munkamenet-kezelés
│       └── repository/     // Adattárolás (pl. beállítások mentése)
└── ui-swing/
    ├── pom.xml
    └── src/main/java/com/chatapp/ui/
        ├── Main.java       // Alkalmazás belépési pontja
        ├── view/           // Swing ablakok (JFrame, JPanel) - a "View"
        │   ├── LoginView.java
        │   └── MainView.java
        └── presenter/      // A "Presenter" réteg
            ├── LoginPresenter.java
            └── MainPresenter.java
```

### 4. Fejlesztési Lépések

1.  **Projekt Struktúra Átalakítása:**
    *   A jelenlegi `pom.xml` átalakítása többmodulos (parent) POM-má.
    *   A `core` és `ui-swing` modulok létrehozása a megfelelő `pom.xml` fájlokkal.

2.  **`core` Modul Felépítése:**
    *   Függőségek hozzáadása: OkHttp, Gson.
    *   Az `ApiService` és `AuthService` megvalósítása a szerverrel való kommunikációra.
    *   Az adatmodellek (`User`, `Message` stb.) létrehozása.

3.  **`ui-swing` Modul Felépítése:**
    *   Függőség hozzáadása a `core` modulhoz.
    *   A **View** réteg létrehozása: `JFrame`-ek és `JPanel`-ek a bejelentkezési és főablakhoz.
    *   A **Presenter** réteg létrehozása: A `LoginPresenter` és `MainPresenter` osztályok fogják kezelni a felhasználói interakciókat, meghívni a `core` modul szolgáltatásait, és frissíteni a View-t.

4.  **Integráció és Tesztelés:**
    *   A modulok összekapcsolása és a teljes funkcionalitás tesztelése.
