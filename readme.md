# FloodMonitor REST (DEZSYS_GK71)

Simuliertes Hochwasser-Frühwarnsystem mit drei Messstationen entlang der Donau. Die Messwerte werden über eine REST-Schnittstelle bereitgestellt.

## Status

Umgesetzt:
- Spring-Boot-Projekt mit Gradle
- Datenmodell: Station, Location, Measurement, Enums StationStatus und WarningLevel
- Simulation der Messwerte mit Vorwerten, Regeln und konfigurierbarem Intervall
- Serverseitige Warnstufenberechnung (NORMAL, WARNING, CRITICAL)
- Endpunkte für Stationen und den aktuellsten Messwert

Offen:
- Historische Messwerte mit Filtern (from, to, limit, warningLevel)
- Warnungen (`/alerts`) und Statistik
- Filter für Stationen (river, isActive, warningLevel)
- Zentrale Fehlerbehandlung im JSON- oder XML-Format
- Webclient
- Automatisierte Tests
- OpenAPI-Dokumentation mit Annotationen

## Voraussetzungen

- Java 17 oder höher
- Gradle (Wrapper `gradlew` ist im Projekt enthalten)
- Git

## Starten

Windows (PowerShell):

```powershell
.\gradlew bootRun
```

Linux / macOS:

```bash
./gradlew bootRun
```

Die Anwendung läuft danach auf `http://localhost:8080`.

## Konfiguration

Datei: `src/main/resources/application.properties`

| Schlüssel | Wert | Bedeutung |
|---|---|---|
| `server.port` | 8080 | Port des Servers |
| `simulation.enabled` | true | Schaltet die Simulation ein oder aus |
| `simulation.interval` | 10000 | Abstand zwischen zwei Simulationsschritten in ms |
| `spring.jackson.serialization.write-dates-as-timestamps` | false | Zeitstempel als ISO-8601-Text |

## Architektur

| Paket | Inhalt |
|---|---|
| `model` | Fachklassen und Enums |
| `repository` | In-Memory-Speicher für Stationen (`StationRepository`) und Messwerte (`MeasurementRepository`) |
| `service` | `SimulationService` (Messwerte) und `WarningService` (Warnstufe) |
| `scheduler` | `SimulationScheduler` ruft die Simulation regelmäßig auf |
| `controller` | `StationController`, nimmt HTTP-Anfragen entgegen |

Die Controller enthalten keine Berechnungen. Die Daten liegen nur im Arbeitsspeicher und gehen beim Neustart verloren.

## Simulationsregeln

Die Simulation erzeugt für jede aktive Station im Abstand von `simulation.interval` einen neuen Messwert, der aus dem vorherigen berechnet wird.

- **Niederschlag:** klingt pro Schritt ab (Faktor 0,7) und erhält mit kleiner Wahrscheinlichkeit einen Schauer. Begrenzt auf 0 bis 80 mm/h.
- **Wasserstand:** steigt mit dem Niederschlag, kehrt langsam zum Normalwert der Station zurück und erhält eine kleine Zufallsschwankung. Nie negativ.
- **Durchfluss:** Funktion des Wasserstands, `Q = 0,13 · h^1,5`.
- **Temperatur:** kleine Schwankungen, begrenzt auf -5 bis
