## Résztvevők

### Projektvezető: Bartha Szabolcs Lajos - O5XWGB

### 1. Tag: Orbán Gábor - DRJE7Y

### 2. Tag: Pataki György - K2WMGH

# UI Swing Modul

Ez a modul valósítja meg a chat kliens alkalmazás grafikus felhasználói felületét (GUI) a Java Swing keretrendszer segítségével. A modul a `core` modulra támaszkodik az üzleti logika és a szerverkommunikáció eléréséhez.

## Architektúra

A modul a **Model-View-Presenter (MVP)** tervezési mintát követi, ami segít szétválasztani a felhasználói felületet (View), az üzleti logikát (Model, amit a `core` modul biztosít) és a kettő közötti vezérlési logikát (Presenter).

- **View:** A `view` csomagban található Swing komponensek (`JFrame`, `JPanel`, stb.), amelyek a felhasználói felület megjelenítéséért felelősek. A `View` réteg passzív, nem tartalmaz üzleti logikát, csupán eseményeket vált ki (pl. gombnyomás) és adatokat jelenít meg, amiket a `Presenter`-től kap.
- **Presenter:** A `presenter` csomagban található osztályok, amelyek a `View`-tól kapott felhasználói interakciókat kezelik. A `Presenter` hívja meg a `core` modul megfelelő `Service` metódusait, feldolgozza az eredményeket, és frissíti a `View`-t.
- **Model:** Az adatmodelleket és az üzleti logikát a `core` modul biztosítja.

## Főbb Komponensek

- `Main.java`: Az alkalmazás belépési pontja. Felelős az alapvető szolgáltatások inicializálásáért és az első ablak (konfigurációs vagy bejelentkezési nézet) megjelenítéséért.
- `view/`: Ebben a csomagban találhatóak a különböző ablakok, mint például a `LoginView` és a `ConfigurationView`.
- `presenter/`: Itt helyezkednek el a nézetekhez tartozó `Presenter`-ek, mint a `LoginPresenter` és a `ConfigurationPresenter`, amelyek a nézetek logikáját vezérlik.
- `util/ErrorMessageTranslator.java`: Egy segédosztály, amely a `core` modulból érkező `ApiException`-eket fordítja le felhasználóbarát, magyar nyelvű hibaüzenetekké.

## Build és futtatás

A modul önmagában nem futtatható, hanem a projekt gyökeréből kell buildelni a teljes alkalmazást:

```bash
mvn clean package
```

A build után a futtatható JAR a `ui-swing/target/` mappában jön létre:

```bash
java -jar ui-swing/target/ui-swing-1.0-SNAPSHOT-jar-with-dependencies.jar
```

A modul fejlesztése során a következőkre figyelj:
- A Swing UI fejlesztéséhez Java 17 vagy újabb szükséges.
- A `core` modulnak sikeresen buildelhetőnek kell lennie, mert a `ui-swing` erre épül.
- A fejlesztéshez ajánlott IDE: IntelliJ IDEA, Eclipse vagy VS Code Java plugin.

**Tesztek futtatása:**  
```bash
mvn test
```
> **Megjegyzés:** Jelenleg nincsenek automatikus (JUnit) tesztek ebben a modulban, de a Maven build és tesztfázis támogatott, így később bővíthető.

## Függőségek

Ez a modul közvetlenül függ a `core` modultól, ahogy az a `pom.xml` fájlban is deklarálva van.
