# Chat kliens fejlesztési ütemterv (részletes kidolgozás)

## 1. Bevezetés

Ez a dokumentum részletes végrehajtási ütemtervet tartalmaz a Java Swing alapú chat kliens fejlesztéséhez. A cél egy stabil, felhasználóbarát alkalmazás, amely támogatja a bejelentkezést, barátkezelést, üzenetküldést (szöveg és média), valamint a profil és beállítások kezelését.

**Lokális SQLite adatbázis:**  
A kliens core moduljában egy beágyazott SQLite adatbázis kezeli az üzenet-előzményeket, barátlistát, barátkéréseket és eseménylogokat. Ez lehetővé teszi az offline működést, gyors keresést, naplózást, és multiplatform támogatást (Java, Android, .NET/C#).  
A perzisztencia réteg DAO-kon keresztül érhető el, a Presenter csak a modellel kommunikál.

---

## 2. Modulok és részletes fejlesztési lépések

### 2.1. Főablak (MainView, MainPresenter)

Részletes fejlesztési terv: lásd [docs/mainview_design.md](mainview_design.md)

---

### 2.2. Barátkezelés

Részletes fejlesztési terv: lásd [docs/friend_design.md](friend_design.md)

---

### 2.3. Üzenetkezelés (Chat)

Részletes fejlesztési terv: lásd [docs/chat_design.md](chat_design.md)

---

### 2.4. Profil és beállítások

Részletes fejlesztési terv: lásd [docs/profile_design.md](profile_design.md)

---

### 2.5. Hibakezelés

Részletes fejlesztési terv: lásd [docs/error_handling_design.md](error_handling_design.md)

---

### 2.6. UI fejlesztés

**Logikai terv:**
- Modern, reszponzív felület.
- Könnyű navigáció, áttekinthető elrendezés.
- Felhasználói élmény maximalizálása.

**Technikai terv:**
- Swing komponensek: layout menedzserek, színek, ikonok, tooltip-ek.
- Ablakméret, skálázhatóság, accessibility.
- Tesztelés: UI elemek működése, kinézet.

---

## 3. Ütemezés (javasolt sorrend)

- 1. nap: MainView/MainPresenter (alap ablak, navigáció)
- 2-3. nap: Barátkezelés (lista, hozzáadás, törlés, kérések)
- 4-6. nap: Üzenetkezelés (chat ablak, üzenetküldés, polling, média)
    - A periodikus lekérdezés (polling) nemcsak az üzeneteket, hanem a barátkéréseket is automatikusan frissíti, így a felhasználó mindig naprakész adatokat lát.
    - **A polling során a kliens minden ciklusban ellenőrzi a JWT token érvényességét, és ha lejárt vagy hamarosan lejár, automatikusan frissíti azt.**
- 7. nap: Profil és beállítások (adatmódosítás, jelszóemlékeztető, szerver URL)
- 8. nap: Hibakezelés finomítása (egységes try-catch, ErrorMessageTranslator)
- 9-10. nap: UI fejlesztés, tesztelés (reszponzív design, felhasználói élmény)

---

## 4. Kockázatok, minőségbiztosítás

- API változások, szerveroldali hibák kezelése.
- Hálózati problémák, inkompatibilitás.
- Felhasználói élmény, UI hibák.
- Tesztelés: minden fő funkcióra manuális és automatizált tesztek.
- Hibakezelés: minden hiba felhasználóbarát módon jelenjen meg.

---

## 5. Záró megjegyzések

Az ütemterv rugalmas, a fejlesztés során felmerülő problémák és visszajelzések alapján módosítható. A cél egy stabil, jól használható chat kliens elkészítése, amely minden funkciót és hibakezelést átláthatóan, egységesen valósít meg.

---

## 2025.10.23. - Chat UI és státusz újdonságok

- A chat ablakban a nem megerősített (confirmed=false) üzenetek piros színnel jelennek meg, a megerősítettek feketével.
- A betűméret állítása (View menü) mostantól a chatArea-ra is érvényes, a színezés és a méret együtt működik.
- A Message osztály confirmed státusza alapján történik a színezés, így a felhasználó azonnal látja, mely üzenetek státusza bizonytalan.
- A not_updated_ids logika pontosítása: csak a fogadó fél tudja confirmed=true-ra állítani az üzenetet, a küldő nem.
- Technikai háttér: a chatArea JTextPane-re lett cserélve, StyledDocument-et használ, így támogatott a soronkénti színezés és formázás.
