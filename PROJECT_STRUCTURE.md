# S9Lab Client - Projektstruktur

Dieses Dokument ist als kleine Landkarte für das Projekt gedacht. Es erklärt die wichtigsten
Bereiche und welche Ordner man im Alltag eher anfasst und welche eher nur Build-/Laufzeit-Artefakte sind.

## Was ist was?

- `src/client/java/...`  
  Der eigentliche Fabric-Client. Hier liegen GUI, Module, Cosmetics, Emotes, Commands, Screenshots
  und die Client-seitige Backend-Anbindung.

- `src/main/java/...`  
  Gemeinsame Klassen oder sehr kleine Hilfsstellen, die sowohl vom Client als auch von anderen
  Modulen gebraucht werden.

- `src/client/resources/...`  
  Texturen, Fonts, Assets, JSON-Dateien für Cosmetics, Emotes und UI-Ressourcen.

- `s9lab-backend/...`  
  Das Server-Backend mit API, Datenbank, WebSocket, Profilen, Shops und Deployment-Code.

- `backups/`  
  Laufzeit-Backups des Backends. Wichtig, aber normalerweise nicht manuell bearbeiten.

- `run/`  
  Laufzeitdaten von Minecraft/Fabric. Das ist eher ein Arbeitsverzeichnis als Quellcode.

- `build/` und `bin/`  
  Generierte Dateien. Diese Ordner werden beim Bauen wieder erzeugt und müssen nicht im Code gepflegt werden.

## Wo sollte man Änderungen meistens machen?

### Client
- GUI: `src/client/java/site/s9lab/s9labclient/client/ui`
- Cosmetics: `src/client/java/site/s9lab/s9labclient/client/cosmetics`
- Emotes: `src/client/java/site/s9lab/s9labclient/client/emote`
- Module: `src/client/java/site/s9lab/s9labclient/client/module`
- Commands: `src/client/java/site/s9lab/s9labclient/client/command`
- Backend-Sync: `src/client/java/site/s9lab/s9labclient/client/backend`

### Backend
- API: `s9lab-backend/src/main/java/site/s9lab/backend/api`
- Datenbank/Migrationen: `s9lab-backend/src/main/java/site/s9lab/backend/storage`
- Profile/Shop/Owned-Logik: `s9lab-backend/src/main/java/site/s9lab/backend/profiles`
- WebSocket: `s9lab-backend/src/main/java/site/s9lab/backend/websocket`

## Welche Dateien sind oft nur Hilfsdateien?

- `.gradle/`, `build/`, `bin/`, `run/`
- temporäre Logs
- lokale Secrets und Config-Dateien im Backend

## Praktische Regel

Wenn du etwas an der Nutzererfahrung ändern willst, dann fast immer zuerst:
1. Screen/UI
2. Module oder Cosmetics
3. Config/Backend-State
4. Command zum schnellen Testen

So bleibt der Code lesbar und du findest Änderungen später schneller wieder.
