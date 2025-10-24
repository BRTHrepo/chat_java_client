# Chat Java Client

Ez a projekt egy többplatformos chat alkalmazás Java kliens oldali implementációja (Swing UI), amely REST API-n keresztül kommunikál a szerverrel.

## Fő funkciók

- Felhasználói bejelentkezés, regisztráció, jelszóemlékeztető
- JWT token alapú authentikáció, automatikus token frissítés
- Barátlista, barátkérések kezelése (küldés, elfogadás, elutasítás, törlés)
- Üzenetküldés, üzenet-előzmények, státusz színezés
- Profilnézet, profil szerkesztés (helyi szinten)
- Több klienspéldány támogatása (könyvtárfüggő beállítások)
- Lokális SQLite adatbázisban történő perzisztencia (üzenetek, barátok, barátkérések, eseménylog)
- Aszinkron API hívások, responsive UI

## Barátkezelés és UX fejlesztések (2025.10.24.)

- **Barátkérések listája:** csak az email jelenik meg.
- **Barátkérésre kattintva:** felugró ablakban lehet elfogadni/elutasítani, ugyanazt a logikát használva, mint az alsó gombok.
- **Accept/Decline gombok:** egymás alatt jelennek meg.
- **Barát hozzáadása mező:** label, input és gomb egymás alatt.
- **Barátok listája:** görgethető, jobb gombos kattintásra felugró ablak a barát adataival és "Barát törlése" gombbal.
- **Barát törlése:** API-hívással (`deleteFriend`), action mezővel (`"action": "delete"`).
- **Chat panel:** domináns, bal/jobb oldali panelek keskenyebbek.
- **Saját user ID és kiválasztott barát neve/ID-ja:** mindig frissül a chat ablakban.

## API endpointok (részlet)

- **Barát törlése:**  
  POST `/chat/api/deleteFriend`  
  ```json
  {
    "friend_id": 123,
    "action": "delete"
  }
  ```
  Az `action` mező lehetővé teszi, hogy ugyanaz az endpoint kezelje a törlést és a barátkérés elutasítását is.

- **Barátkérés elutasítása:**  
  POST `/chat/api/deleteFriend`  
  ```json
  {
    "friend_id": 123,
    "action": "decline"
  }
  ```

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

## Fejlesztők

- BRTHrepo, 2023-2025
