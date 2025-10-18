# Chat alkalmazás

Egy teljes körű chat alkalmazás Java kliensoldallal és PHP szerveroldallal, amely baráti kapcsolatokat, üzenetküldést (szöveg, hang, kép) és lekérdezés-alapú (polling) kommunikációt támogat.

## Funkciók

- Felhasználói regisztráció és bejelentkezés
- Token alapú hitelesítés
- Barátok kezelése (hozzáadás, törlés, kérés küldés/elfogadás)
- Üzenetküldés különböző típusokban (szöveg, hang, kép)
- Médiafájlok előfeldolgozása (átméretezés, tömörítés)
- Automatikus üzenetek és barátkérések lekérése
- Jelszóemlékeztető email küldés

## Telepítés

### Előfeltételek

- PHP 8.1 vagy újabb
- MySQL adatbázis
- Bármilyen PHP-t futtató webkiszolgáló
- Java 17 vagy újabb (a klienshez)

### Szerveroldali telepítés

1. **Klónozd a repozitóriót**
   ```bash
   git clone <repository-url>
   ```

2. **Telepítsd a függőségeket**
   ```bash
   composer install
   ```

3. **Állítsd be az adatbázis kapcsolatot**
   Szerkeszd a `.env` fájlt a következő adatokkal:
   ```
   DB_HOST=localhost
   DB_PORT=3306
   DB_DATABASE=...
   DB_USERNAME=...
   DB_PASSWORD=...
   ```

4. **Hozd létre az adatbázist**
   ```sql
   CREATE DATABASE ...;
   ```

5. **Futtasd a migrációkat**
   ```bash
   php vendor/bin/phinx migrate
   ```
   *Megjegyzés: Ha nincs telepítve a phinx, manuálisan futtasd a migrációs fájlokat.*

6. **Állítsd be az email küldést**
   Szerkeszd a `.env` fájl SMTP beállításait:
   ```
   SMTP_HOST=smtp.gmail.com
   SMTP_PORT=587
   SMTP_USERNAME=your_email@gmail.com
   SMTP_PASSWORD=your_app_password
   SMTP_FROM=noreply@chatapp.com
   ```

7. **Állítsd be a JWT titkos kulcsot**
   Módosítsd a `JWT_SECRET` értéket a `.env` fájlban.

8. **Indítsd el a szervert**
   ```bash
   composer start
   ```

A szerver a `localhost:8080` címen fog futni.

**Fontos:** Az API routing egy egyszerű, manuális mechanizmussal történik a `public/index.php` fájlban, nincs szükség `mod_rewrite`-re vagy speciális `.htaccess` konfigurációra. A projekt tartalmazza a `Slim` frameworköt mint függőséget, de az jelenleg nincs használatban.

## API dokumentáció

### Authentikáció

#### Regisztráció / Bejelentkezés
```
POST /chat/index.php/api/registerLogin
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "password123",
    "nickname": "nickname"
}
```

Válasz (sikeres bejelentkezés):
```json
{
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "user_id": 1,
    "nickname": "nickname",
    "email": "user@example.com"
}
```

#### Jelszóemlékeztető
```
POST /chat/index.php/api/forgotPassword
Content-Type: application/json

{
    "email": "user@example.com"
}
```

Válasz:
```json
{
    "message": "If email exists, reset link has been sent"
}
```

#### Adatok módosítása
```
POST /chat/index.php/api/changeCredentials
Authorization: Bearer <token>
Content-Type: application/json

{
    "nickname": "new_nickname",
    "email": "new_email@example.com",
    "password": "new_password123"
}
```

### Barátkezelés

#### Barát hozzáadása
```
POST /chat/index.php/api/addFriend
Authorization: Bearer <token>
Content-Type: application/json

{
    "nickname": "friend_nickname"
}
```

Válasz:
```json
{
    "message": "Friend request sent",
    "friend_id": 2,
    "nickname": "friend_nickname"
}
```

#### Barát törlése / Kérés elutasítása
```
POST /chat/index.php/api/deleteFriend
Authorization: Bearer <token>
Content-Type: application/json

{
    "friend_id": 2,
    "action": "delete" // vagy "decline"
}
```

Válasz:
```json
{
    "message": "Friend removed successfully"
}
```

#### Barátok listája
```
POST /chat/index.php/api/getFriends
Authorization: Bearer <token>
```

Válasz:
```json
{
    "friends": [
        {
            "friend_id": 2,
            "nickname": "friend1",
            "email": "friend1@example.com",
            "status": 1
        }
    ]
}
```

#### Barátkérések listája
```
POST /chat/index.php/api/getFriendRequests
Authorization: Bearer <token>
```

Válasz:
```json
{
    "requests": [
        {
            "request_id": 1,
            "from_user_id": 3,
            "nickname": "friend2",
            "email": "friend2@example.com",
            "request_date": "2024-01-01 12:00:00"
        }
    ]
}
```

### Üzenetkezelés

#### Üzenet küldése
```
POST /chat/index.php/api/sendMessage
Authorization: Bearer <token>
Content-Type: application/json

{
    "receiver_id": 2,
    "msg_type": "text",
    "content": "Hello!"
}
```

Médiaüzenet küldése:
```
POST /chat/index.php/api/sendMessage
Authorization: Bearer <token>
Content-Type: multipart/form-data

receiver_id: 2
msg_type: image
media: <file>
```

Válasz:
```json
{
    "message_id": 1,
    "receiver_id": 2,
    "msg_type": "text",
    "sent_date": "2024-01-01 12:00:00"
}
```

#### Üzenetek lekérése
```
POST /chat/index.php/api/getMessages
Authorization: Bearer <token>
Content-Type: application/json

{
    "confirmed_message_ids": [1, 2],
    "last_message_id": 10,
    "last_request_date": "2024-01-01 12:00:00"
}
```

Válasz:
```json
{
    "messages": [
        {
            "message_id": 11,
            "sender_id": 1,
            "receiver_id": 2,
            "nickname": "friend1",
            "msg_type": "text",
            "content": "How are you?",
            "sent_date": "2024-01-01 12:05:00",
            "delivered": true,
            "read_status": false,
            "is_from_me": false
        }
    ],
    "friend_requests": [
        {
            "request_id": 2,
            "from_user_id": 4,
            "nickname": "friend3",
            "email": "friend3@example.com",
            "request_date": "2024-01-01 12:10:00"
        }
    ]
}
```

## Adatbázis séma

### users tábla
- id (PK, AI)
- nickname (string, unique)
- email (string, unique)
- password_hash (string)
- reg_date (timestamp)
- status (tinyint)
- reset_token (string, nullable)
- reset_token_expires (timestamp, nullable)

### friends tábla
- id (PK, AI)
- user_id (FK, users.id)
- friend_id (FK, users.id)
- created_at (timestamp)
- updated_at (timestamp, nullable)
- *Indexek:* `(user_id, friend_id)` - unique

### friend_requests tábla
- id (PK, AI)
- from_user_id (FK, users.id)
- to_user_id (FK, users.id)
- request_date (timestamp)
- *Indexek:* `(from_user_id, to_user_id)` - unique

### messages tábla
- id (PK, AI)
- sender_id (FK, users.id)
- receiver_id (FK, users.id)
- msg_type (enum: text, audio, image)
- content (text)
- sent_date (timestamp)
- delivered (boolean)
- read_status (boolean)
- *Indexek:* `(sender_id, receiver_id, sent_date)`, `(receiver_id, sent_date)`

### media_files tábla
- id (PK, AI)
- owner_id (FK, users.id)
- media_type (enum: audio, image)
- file_path (string)
- file_size (bigint)
- created_at (timestamp)
- *Indexek:* `(owner_id, media_type)`, `(created_at)`

## Kliensoldal telepítése

A Java kliens terve megtalálható a `client/java-client-plan.md` fájlban.

## Biztonsági megfontolások

- Minden kommunikáció titkosított (HTTPS)
- JWT tokenek lejárati idővel rendelkeznek
- Jelszavak soha nem tárolhatók egyszerű szövegként
- Feltöltött fájlok validálása méret és formátum szerint
- SQL injekció elleni védelem

## Hibaelhárítás

### Gyakori problémák

1. **Adatbázis kapcsolat hiba**
   - Ellenőrizd a `.env` fájl adatbázis beállításait
   - Győződj meg róla, hogy az adatbázis szerver fut
   - Ellenőrizd, hogy a felhasználó rendelkezik-e megfelelő jogosultságokkal

2. **Token hiba**
   - Ellenőrizd, hogy a JWT_SECRET be van-e állítva
   - Ellenőrizd, hogy a token érvényes és le nem járt

3. **Email küldés hiba**
   - Ellenőrizd az SMTP beállításokat
   - Győződj meg róla, hogy a felhasználói jelszó (alkalmazás jelszó) helyes

4. **Fájl feltöltés hiba**
   - Ellenőrizd a fájl méretét (max 10MB)
   - Ellenőrizd a fájl formátumát
   - Győződj meg róla, hogy az `uploads` könyvtár írható

## Fejlesztés

### Lokális fejlesztés

1. Állítsd be a PHP fejlesztői környezetet
2. Telepítsd a Composer függőségeket
3. Indítsd el a szervert a `composer start` paranccsal
4. Teszteld az API végpontokat egy REST klienssel (pl. Postman, curl)

### Tesztelés

1. Teszteld az API végpontokat manuálisan
2. Írj automatizált teszteket a funkcionalitás ellenőrzéséhez
3. Teszteld a különböző helyzeteket (hibák, határesetek)

## Licenc

Ez a projekt MIT licenc alatt áll.
