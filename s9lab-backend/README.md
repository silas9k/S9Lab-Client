# S9Lab Backend

Eigenständiges Java-21-Backend fuer den S9Lab Client. Es ist kein Minecraft-Plugin und kann im Pelican Panel als normale JAR laufen.

## Build

```powershell
.\gradlew.bat :s9lab-backend:fatJar
```

Die fertige Datei liegt unter:

```txt
s9lab-backend/build/libs/s9lab-backend.jar
```

## Start

```bash
java -Xms128M -Xmx512M -jar s9lab-backend.jar
```

Der HTTP-Port kommt zuerst aus `PORT` oder `SERVER_PORT`. Wenn keine Environment Variable gesetzt ist, wird `config/config.json` genutzt.

## Admin API

Alle Admin-Requests brauchen den Header `X-Admin-Secret`.

```powershell
$secret = "CHANGE_ME_SUPER_SECRET"
$uuid = "00000000-0000-0000-0000-000000000000"
curl.exe -X POST http://127.0.0.1:8788/admin/coins/set -H "Content-Type: application/json" -H "X-Admin-Secret: $secret" -d "{\"uuid\":\"$uuid\",\"amount\":1000}"
curl.exe -X POST http://127.0.0.1:8788/admin/coins/add -H "Content-Type: application/json" -H "X-Admin-Secret: $secret" -d "{\"uuid\":\"$uuid\",\"amount\":250}"
curl.exe -X POST http://127.0.0.1:8788/admin/coins/remove -H "Content-Type: application/json" -H "X-Admin-Secret: $secret" -d "{\"uuid\":\"$uuid\",\"amount\":50}"
curl.exe -H "X-Admin-Secret: $secret" http://127.0.0.1:8788/admin/player/$uuid
```

## Pelican

Start command:

```bash
java -Xms128M -Xmx512M -jar s9lab-backend.jar
```

Files:

- Config: `config/config.json`
- Datenbank: `database/s9lab.db`
- Backups: `backups/`
