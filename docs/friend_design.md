# Barátkezelés és Barátkérés - Részletes Dokumentáció

## Főbb funkciók

- Barátkérés küldése email vagy felhasználónév alapján
- Barátkérések listázása (csak email jelenik meg)
- Barátkérés elfogadása/elutasítása felugró ablakban vagy alsó gombokkal
- Barát törlése jobb gombos menüből, felugró ablakban megerősítéssel
- Minden művelet aszinkron API-hívással, UI frissítéssel

## Barátkérés folyamata

1. **Küldés:**  
   - A felhasználó megadja a barát emailjét vagy felhasználónevét.
   - Az API POST `/chat/api/addFriend` végpontot hívja.
   - Ha már van kölcsönös barátkérés, automatikusan barátokká válnak.

2. **Barátkérések listázása:**  
   - A jobb oldali panelen csak az email jelenik meg.
   - Kattintásra felugró ablak jelenik meg: "Elfogadod ezt a barátkérést?" Elfogad/Elutasít gombokkal.

3. **Elfogadás/Elutasítás:**  
   - Elfogadás: barátság létrejön, barátkérés törlődik.
   - Elutasítás: barátkérés törlődik.
   - Mindkét művelet elérhető a felugró ablakból vagy az alsó gombokkal (egymás alatt).

## Barát törlése

- A bal oldali barátlistában jobb gombbal kattintva felugró ablak jelenik meg a barát adataival és egy "Barát törlése" gombbal.
- Törlés gombra megerősítő ablak jelenik meg.
- A törlés API-hívással történik:  
  POST `/chat/api/deleteFriend`  
  ```json
  {
    "friend_id": 123,
    "action": "delete"
  }
  ```
- Az action mező lehetővé teszi, hogy ugyanaz az endpoint kezelje a törlést és a barátkérés elutasítását is.

## API paraméterek

- **Barát törlése:**  
  - `friend_id`: törlendő barát azonosítója
  - `action`: `"delete"`

- **Barátkérés elutasítása:**  
  - `friend_id`: barátkérés feladója
  - `action`: `"decline"`

## Felhasználói élmény (UX)

- Barátkérés, törlés, elfogadás/elutasítás minden esetben felugró ablakban visszaigazolható.
- Barát hozzáadása mező label, input és gomb egymás alatt.
- Barátok listája görgethető, jobb gombos menüvel.
- Saját user ID és kiválasztott barát neve/ID-ja mindig látható a chat ablakban.
- Minden művelet után automatikus UI frissítés (polling vagy manuális frissítés).

## Technikai részletek

- Az API-hívások aszinkron SwingWorker-ben futnak, a UI nem fagy le.
- A barátkérések és barátok listája mindig naprakész.
- A barát törlés és barátkérés elutasítás ugyanazt az API végpontot használja, az action mezővel különböztetve.
