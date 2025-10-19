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

## Főbb Komponensek

- `service/ApiService.java`: Közvetlenül felelős a HTTP kérések összeállításáért és a szerver végpontjainak meghívásáért. Itt történik a JSON adatok küldése és fogadása.
- `service/AuthService.java`: Az `ApiService`-re épülve kezeli a hitelesítési folyamatokat, a felhasználói munkamenetet és a tokenek tárolását.
- `model/`: Ez a csomag tartalmazza az adatmodelleket, amelyek a szerverrel váltott adatokat reprezentálják.
