# Chat Java Client

Ez a dokumentum a Java chat alkalmazás felhasználói szemszögből történő bemutatását tartalmazza.

## Áttekintés

A Chat Java Client egy modern, platformfüggetlen asztali chat alkalmazás, amely lehetővé teszi:
- gyors és biztonságos bejelentkezést,
- barátok kezelését,
- szöveges üzenetek küldését (média - kép, hang - küldése fejlesztési lehetőség, jelenleg csak szöveg támogatott),
- profiladatok megtekintését és szerkesztését,
- több klienspéldány egyidejű futtatását külön beállításokkal,
- automatikus adatmentést és offline működést.

Az alkalmazás célja, hogy egyszerű, átlátható felületen keresztül biztosítson valós idejű kommunikációt, miközben a felhasználói élmény és adatbiztonság elsődleges.

## Fő funkciók

- **Bejelentkezés:**  
  A felhasználó email-cím és jelszó megadásával jelentkezhet be. A rendszer automatikusan kezeli a tokeneket, így a bejelentkezés után a felhasználónak nem kell újra hitelesítenie magát, amíg a token érvényes.

- **Barátkezelés:**  
  Barátok hozzáadása, törlése, barátkérések elfogadása/elutasítása. A barátlista mindig naprakész, a szerverrel automatikusan szinkronizálódik.

- **Chat:**  
  Kiválasztott baráttal folytatott beszélgetés, üzenetek listázása, új üzenet küldése (jelenleg csak szöveg, a média - kép, hang - küldése fejlesztési lehetőség).  
  A nem megerősített (confirmed=false) üzenetek piros színnel jelennek meg, a megerősítettek feketével.  
  A betűméret a View menüben állítható.

- **Profil és beállítások:**  
  Profiladatok (név, avatar) megtekintése, szerkesztése.  
  Szerver URL és egyéb beállítások módosítása a beállítások menüben.

- **Több példány támogatása:**  
  Az alkalmazás minden példánya a futtatási könyvtár (`System.getProperty("user.dir")`) alapján kap egyedi beállításokat. Így ugyanabból a könyvtárból indítva a beállítások megmaradnak, más könyvtárból külön példányként viselkedik az alkalmazás.

- **Offline működés:**  
  Az üzenetek, barátlista és események helyi SQLite adatbázisban is tárolódnak, így internetkapcsolat nélkül is elérhetők.

## Használat lépései

### 1. **Buildelés**

A projekt buildeléséhez szükséges:
- Java 17 vagy újabb (ellenőrizd: `java -version`)
- Maven (ellenőrizd: `mvn -version`)

A buildelés lépései:
```bash
# 1. Lépj a projekt gyökerébe
cd /elérési/út/a/chat_java_client

# 2. Tisztítsd a korábbi buildeket (opcionális)
mvn clean

# 3. Fordítsd le és csomagold a projektet
mvn package
```

A sikeres build után a futtatható JAR a `ui-swing/target/` mappában jön létre:

```bash
java -jar ui-swing/target/ui-swing-1.0-SNAPSHOT-jar-with-dependencies.jar
```

**Tesztek futtatása:**
```bash
mvn test
```
> **Megjegyzés:** Jelenleg nincsenek automatikus (JUnit) tesztek a projektben, de a Maven build és tesztfázis támogatott, így később bővíthető.

**Lehetséges hibák:**
- Ha a Java vagy Maven nincs telepítve, telepítsd a hivatalos oldalról.
- Ha dependency hibát kapsz, próbáld újra a `mvn clean package` parancsot.

### 2. **Bejelentkezés:**  
Add meg az email-címed és jelszavad. Sikeres bejelentkezés után a főablak jelenik meg.
3. **Barát hozzáadása:**  
   Írd be a barátod email-címét vagy becenevét, majd kattints az "Add" gombra.
4. **Üzenetküldés:**  
   Válassz ki egy barátot a listából, írj üzenetet, majd kattints a "Send" gombra.  
   (Jelenleg csak szöveges üzenet támogatott, a média csatolás fejlesztési lehetőség.)
5. **Profil szerkesztése:**  
   A menüben válaszd a "Profil..." opciót, módosítsd az adatokat, majd mentsd el.
6. **Beállítások:**  
   A szerver URL és egyéb beállítások a beállítások menüben módosíthatók.  
   > **Fontos:** A szerver címe bármikor, forráskód módosítása vagy újrafordítás nélkül átírható a programban, és a beállítás elmentésre kerül.
7. **Kijelentkezés:**  
   A "Logout" menüponttal bármikor kijelentkezhetsz.

## Platformkövetelmények

- Java 17 vagy újabb
- Windows, Linux, macOS támogatott
- Internetkapcsolat szükséges a szerverrel való szinkronizációhoz

## Adatvédelem és biztonság

- A bejelentkezési adatok és tokenek titkosítva, csak helyben tárolódnak.
- Minden kommunikáció HTTPS-en keresztül történik.
- A helyi SQLite adatbázis minden példányhoz külön jön létre.

## Tipikus workflow

1. Indítsd el az alkalmazást.
2. Jelentkezz be vagy regisztrálj.
3. Adj hozzá barátokat, fogadj el barátkéréseket.
4. Chatelj, küldj és fogadj szöveges üzeneteket. (A média csatolás jelenleg nem támogatott, fejlesztési lehetőség.)
5. Szerkeszd a profilod, módosítsd a beállításokat.
6. Használd az alkalmazást akár több példányban, különböző könyvtárakból.

## GYIK

- **Elvesznek az adataim, ha újraindítom az appot?**  
  Nem, minden beállítás és adat megmarad ugyanabból a könyvtárból indítva.
- **Futhat egyszerre több példány?**  
  Igen, minden példány külön beállításokat és adatbázist használ, ha más könyvtárból indítod.
- **Miért piros egy üzenet?**  
  Az üzenet még nem lett megerősítve a szerver által (pl. nem ért célba vagy nincs visszaigazolva).

## Hibabejelentés, támogatás

Ha hibát találsz vagy kérdésed van, írj a projekt fejlesztőinek vagy nyiss issue-t a repository-ban.
