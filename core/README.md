# Java Chat Alkalmazás

## Résztvevők

### Projektvezető: Bartha Szabolcs Lajos - O5XWGB

### 1. Tag: Orbán Gábor - DRJE7Y

### 2. Tag: Pataki György - K2WMGH

# Core Modul

Ez a modul tartalmazza a chat kliens alkalmazás teljes üzleti logikáját és a szerverrel való kommunikációt. A tervezésének kulcsfontosságú szempontja, hogy teljesen független legyen bármilyen konkrét felhasználói felülettől (UI), így a logika könnyen újrahasznosítható más felületekkel is (pl. webes, mobil).

## Felelősségi Körök

- **API Kommunikáció:** A szerver API végpontjainak hívása, a kérések összeállítása és a válaszok feldolgozása.
- **Adatmodellek:** Az alkalmazás által használt főbb entitások (pl. `User`, `Message`, `ApiError`) definíciója.
- **Hitelesítés és Munkamenet-kezelés:** A felhasználói bejelentkezés, regisztráció, token-kezelés és a munkamenet életciklusának menedzselése.
- **Perzisztencia:** A felhasználói adatok (pl. bejelentkezési információk, token) helyi tárolása és betöltése.

## Technológiai Készlet

- **HTTP Kliens:** [OkHttp](https://square.github.io/okhttp/) - Egy hatékony és megbízható HTTP kliens a hálózati kérések kezelésére.
- **JSON Feldolgozás:** [Gson](https://github.com/google/gson) - A Google könyvtára a Java objektumok és a JSON formátum közötti szerializációra és deszerializációra.

## Build, használat és integráció

A core modul önállóan is buildelhető Maven-nel:

```bash
cd core
mvn clean package
```

A build után a `core/target/` mappában jön létre a JAR, amelyet más Java projektekből is felhasználhatsz dependency-ként.

- Java 17 vagy újabb szükséges.
- A modul nem tartalmaz futtatható main metódust, csak library-ként használható.
- A tesztek futtatásához:
  ```bash
  mvn test
  ```
  > **Megjegyzés:** Jelenleg nincsenek automatikus (JUnit) tesztek ebben a modulban, de a Maven build és tesztfázis támogatott, így később bővíthető.

### Dependency használata más Maven projektben

Ha csak a core modult szeretnéd használni egy másik Java projektben, add hozzá a következőt a saját `pom.xml`-edhez (feltételezve, hogy a core JAR elérhető a lokális Maven repository-ban):

```xml
<dependency>
    <groupId>com.chatapp</groupId>
    <artifactId>core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Példakód: ApiService és AuthService használata

```java
import com.chatapp.core.service.ApiService;
import com.chatapp.core.service.AuthService;

public class Example {
    public static void main(String[] args) {
        String serverUrl = "https://brthprog.alwaysdata.net/chat/";
        ApiService apiService = new ApiService(serverUrl);
        AuthService authService = new AuthService(apiService, "my-instance-id");

        // Példa: bejelentkezés
        // authService.login("email@example.com", "jelszo");

        // További műveletek: apiService.getFriends(), apiService.sendMessage(), stb.
    }
}
```

### Licenc és szerző

- Licenc: MIT (vagy amit a projekt ténylegesen használ)
- Szerző: [Fejlesztő neve vagy csapat]

### Roadmap / TODO

- Automatikus tesztek hozzáadása (JUnit)
- Swagger/OpenAPI dokumentáció generálása
- További perzisztencia rétegek támogatása (pl. NoSQL)
- További API végpontok támogatása


## Főbb Komponensek

- `service/ApiService.java`: Közvetlenül felelős a HTTP kérések összeállításáért és a szerver végpontjainak meghívásáért. Itt történik a JSON adatok küldése és fogadása.
- `service/AuthService.java`: Az `ApiService`-re épülve kezeli a hitelesítési folyamatokat, a felhasználói munkamenetet és a tokenek tárolását.
- `model/`: Ez a csomag tartalmazza az adatmodelleket, amelyek a szerverrel váltott adatokat reprezentálják.
