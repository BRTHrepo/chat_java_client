# Projekt Haladási Napló

Ez a dokumentum a Java chat kliens fejlesztésének aktuális állapotát és a következő lépéseket rögzíti.

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

### 2025.10.21. - Login tokenkezelés javítása

- A szerver a login válaszban a JWT tokent külön kulcsként (`"token"`) küldi.
- A Java kliens most már a teljes JSON választ feldolgozza: a tokent külön eltárolja, a user adatokat (`user_id`, `nickname`, `email`) külön tölti be.
- Új metódus: `registerLoginRaw` az ApiService-ben, amely a teljes JSON választ visszaadja.
- Így a login után a token nem lesz null, és minden védett API hívásnál helyesen átadásra kerül az Authorization headerben.

### 2025.10.21. - JWT token automatikus frissítés polling során

- A kliens minden polling ciklusban ellenőrzi a JWT token érvényességét.
- Ha a token lejárt vagy hamarosan lejár, automatikusan frissíti (új login vagy refresh).
- Így a felhasználónak nem kell manuálisan újra bejelentkeznie, ha a token lejár.

### 2025.10.21. - Lokális SQLite adatbázis architektúra dokumentálása

- A core modulban beágyazott SQLite adatbázis kezeli az üzenet-előzményeket, barátlistát, barátkéréseket és eseménylogokat.
- A perzisztencia réteg DAO-kon keresztül érhető el, a Presenter csak a modellel kommunikál.
- A dokumentációkban (README.md, chat_client_execution_plan.md, chat_design.md, stb.) részletezve lett az MVP-integráció és a multiplatform támogatás (Java, Android, .NET/C#).

## Következő lépések

1.  **Barátkezelés funkciók:**
    - Barátkérések megjelenítése és kezelése (elfogadás, elutasítás).
    - Új barát hozzáadása.
2.  **Profil nézet:**
    - Felhasználói profil adatainak megjelenítése.
    - Profil szerkesztésének lehetősége.
3.  **Kijelentkezés:**
    - A `handleLogout` metódus implementálása.
    - A fő nézet bezárása és a bejelentkezési ablak újbóli megjelenítése.
4.  **Valós idejű frissítés:**
    - Periodikus üzenetlekérdezés (nincs WebSocket) implementálása az azonnali üzenetfrissítéshez.
