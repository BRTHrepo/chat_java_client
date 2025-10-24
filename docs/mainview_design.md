# MainView - Fő UI elrendezés és interakciók

## Főbb komponensek

- **Bal panel (Friends):**
  - Görgethető barátlista (JList)
  - Barát hozzáadása mező: label, input, gomb egymás alatt (GridLayout)
  - Jobb gombos kattintás egy barátra: felugró ablak a barát adataival és "Barát törlése" gombbal

- **Középső panel (Chat):**
  - ChatArea (JTextPane, színezett sorok)
  - Saját user ID és kiválasztott barát neve/ID-ja a chat ablak tetején
  - Üzenetküldő mező és gomb
  - Polling/Frissítés gomb

- **Jobb panel (Friend Requests):**
  - Görgethető barátkérések lista (csak email jelenik meg)
  - Kattintásra felugró ablak: elfogadás/elutasítás
  - Accept/Decline gombok egymás alatt (GridLayout)

## Interakciók

- **Barát hozzáadása:**  
  - Felhasználó megadja az emailt vagy felhasználónevet, majd Add gomb.
  - Sikeres barátkérés után visszajelzés, mező törlése.

- **Barátkérés elfogadása/elutasítása:**  
  - Kattintás a barátkérésre: felugró ablakban döntés (Elfogad/Elutasít).
  - Ugyanaz a logika, mint az alsó Accept/Decline gomboknál.

- **Barát törlése:**  
  - Barátlistán jobb gombbal kattintva felugró ablak: barát adatai, "Barát törlése" gomb.
  - Törlés gombra megerősítő dialog, majd API-hívás.

- **UI frissítés:**  
  - Minden művelet után automatikus vagy manuális frissítés (polling).

## UX részletek

- Minden fontosabb művelet felugró ablakban visszaigazolható.
- A barátlista, barátkérések lista, chat panel mindig naprakész.
- A felhasználó azonnal látja saját ID-ját, a kiválasztott barát adatait.
- A bal/jobb panelek keskenyebbek, a chat panel domináns.

## Technikai megjegyzések

- Minden aszinkron API-hívás SwingWorker-ben fut.
- A barát törlés és barátkérés elutasítás ugyanazt az API végpontot használja, az action mezővel különböztetve.
- A barátkérések listájában csak az email jelenik meg (cellRenderer).
- A barátlistában a barát neve jelenik meg (cellRenderer).
