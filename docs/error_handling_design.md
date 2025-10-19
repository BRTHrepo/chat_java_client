# Hibakezelés modul részletes fejlesztési terv

## Funkciók

- Minden szerverhívásnál egységes hibakezelés.
- ApiException: szerver által visszaadott hibaüzenet.
- Exception: általános, váratlan hibák.
- Hibák felhasználóbarát megjelenítése.

## Osztályok és metódusok

### ErrorMessageTranslator
- Statikus metódusok:
  - `translate(ApiException e)`: szerver hibaüzenet fordítása magyar nyelvű, felhasználóbarát üzenetre.
  - `translate(Exception e)`: általános hibaüzenet fordítása.

### Hibakezelési logika a Presenter osztályokban
- Minden API hívás try-catch blokkban:
  - `catch (ApiException ex)`: szerver hibaüzenet fordítása és megjelenítése.
  - `catch (Exception ex)`: általános hibaüzenet megjelenítése.

### UI hibamegjelenítés
- Minden hibaüzenet a megfelelő panelen jelenik meg (pl. showError metódus).

## Adatfolyam

1. API hívás → try-catch blokk.
2. ApiException esetén: ErrorMessageTranslator.translate(ex) → showError.
3. Exception esetén: ErrorMessageTranslator.translate(ex) → showError.

## Tesztelés

- Hibás adatok, szerverhiba, hálózati hiba, Exception szimuláció.
- Hibák helyes megjelenítése a UI-ban.

## Kódolási javaslatok

- Minden szerverhívás try-catch ApiException és Exception.
- ErrorMessageTranslator minden presenterben használható.
- Hibák logolása fejlesztési módban (ex.printStackTrace).

## Példa workflow

```java
try {
    // API hívás
} catch (ApiException ex) {
    String msg = ErrorMessageTranslator.translate(ex);
    panel.showError(msg);
    ex.printStackTrace();
} catch (Exception ex) {
    String msg = ErrorMessageTranslator.translate(ex);
    panel.showError(msg);
    ex.printStackTrace();
}
```

## További részletek

- Hibák magyar nyelvű, érthető megjelenítése.
- Tesztelés: minden presenterben hibás esetek szimulálása.
