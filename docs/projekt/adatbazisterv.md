# Adatbázisterv

## Adatbázis típusa
SQLite

## Táblák és mezők

### users
- id INTEGER PRIMARY KEY
- email TEXT
- nickname TEXT
- avatar_url TEXT
- status TEXT
- token TEXT

### messages
- id INTEGER PRIMARY KEY
- sender_id INTEGER
- receiver_id INTEGER
- nickname TEXT
- msg_type TEXT
- content TEXT
- sent_date TEXT
- delivered BOOLEAN
- read BOOLEAN
- confirmed BOOLEAN
- is_from_me BOOLEAN
- media_type TEXT
- file_path TEXT
- file_size INTEGER
- server_id INTEGER
- FOREIGN KEY(sender_id) REFERENCES users(id)
- FOREIGN KEY(receiver_id) REFERENCES users(id)

### friends
- id INTEGER PRIMARY KEY
- email TEXT
- nickname TEXT
- avatar_url TEXT
- status TEXT

### friend_requests
- id INTEGER PRIMARY KEY AUTOINCREMENT
- from_user_id INTEGER
- to_user_id INTEGER
- request_date TEXT
- FOREIGN KEY(from_user_id) REFERENCES users(id)
- FOREIGN KEY(to_user_id) REFERENCES users(id)

### event_logs
- id INTEGER PRIMARY KEY AUTOINCREMENT
- event_type TEXT
- event_time TEXT
- details TEXT

## Kapcsolatok
- Egy user több üzenetet küldhet/fogadhat (messages.sender_id, messages.receiver_id)
- Egy user több barátkérést küldhet/fogadhat (friend_requests.from_user_id, to_user_id)
- Barátság: a friends tábla csak a bejelentkezett felhasználó barátlistáját tartalmazza

## Sémák (példa)
```sql
CREATE TABLE users (
  id INTEGER PRIMARY KEY,
  email TEXT,
  nickname TEXT,
  avatar_url TEXT,
  status TEXT,
  token TEXT
);

CREATE TABLE messages (
  id INTEGER PRIMARY KEY,
  sender_id INTEGER,
  receiver_id INTEGER,
  nickname TEXT,
  msg_type TEXT,
  content TEXT,
  sent_date TEXT,
  delivered BOOLEAN,
  read BOOLEAN,
  confirmed BOOLEAN,
  is_from_me BOOLEAN,
  media_type TEXT,
  file_path TEXT,
  file_size INTEGER,
  server_id INTEGER,
  FOREIGN KEY(sender_id) REFERENCES users(id),
  FOREIGN KEY(receiver_id) REFERENCES users(id)
);

CREATE TABLE friends (
  id INTEGER PRIMARY KEY,
  email TEXT,
  nickname TEXT,
  avatar_url TEXT,
  status TEXT
);

CREATE TABLE friend_requests (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  from_user_id INTEGER,
  to_user_id INTEGER,
  request_date TEXT,
  FOREIGN KEY(from_user_id) REFERENCES users(id),
  FOREIGN KEY(to_user_id) REFERENCES users(id)
);

CREATE TABLE event_logs (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  event_type TEXT,
  event_time TEXT,
  details TEXT
);
