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

### 2025.10.21-23. - Egyéb fejlesztések (lásd korábbi naplóbejegyzések)

(lásd a korábbi naplóbejegyzéseket a részletekért)
