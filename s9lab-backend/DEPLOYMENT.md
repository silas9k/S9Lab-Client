# S9Lab Backend Deployment

## Build

From the project root:

```powershell
.\gradlew.bat :s9lab-backend:build
```

Upload this file:

```text
s9lab-backend/build/libs/s9lab-backend.jar
```

## Pelican Setup

Recommended runtime:

```text
Java 21 JRE
```

Recommended Docker image if your egg allows custom images:

```text
eclipse-temurin:21-jre
```

If your Pelican/Pterodactyl egg already provides a Java 21 image, use that image.

Startup command:

```bash
java -Xms128M -Xmx512M -jar s9lab-backend.jar
```

Minimum resources:

```text
CPU: 1 core
RAM: 256 MB minimum, 512 MB recommended
Disk: 1 GB minimum
Network: expose HTTP port and WebSocket port
```

Ports:

```text
HTTP: PORT or SERVER_PORT, fallback 8788
WebSocket: WEBSOCKET_PORT or WS_PORT, fallback 8789
```

Working directory layout in the container:

```text
/home/container/s9lab-backend.jar
/home/container/config/config.json
/home/container/database/s9lab.db
/home/container/backups/
```

## Config

The backend creates this file on first start:

```text
config/config.json
```

Important fields:

```json
{
  "host": "0.0.0.0",
  "port": 8788,
  "websocketPort": 8789,
  "databasePath": "database/s9lab.db",
  "backupDirectory": "backups",
  "adminSecret": "CHANGE_ME_TO_A_LONG_RANDOM_SECRET",
  "apiVersion": "v1",
  "enableWebsocket": true,
  "rateLimitPerMinute": 120
}
```

Change the admin secret either in `config/config.json` or via environment variable:

```text
S9LAB_ADMIN_SECRET=your-long-random-secret
```

## Updates

1. Stop the server.
2. Download/copy `database/s9lab.db` and the latest `backups/*.sqlite`.
3. Replace `s9lab-backend.jar`.
4. Start the server.
5. Check `/health`.

## Backups

The backend creates a SQLite backup on startup:

```text
backups/s9lab-db-yyyy-MM-dd_HH-mm-ss.sqlite
```

Before every update, manually copy:

```text
database/s9lab.db
backups/
config/config.json
```

## Health Check

```bash
curl http://YOUR_HOST:PORT/health
```

Expected:

```json
{"ok":true,"service":"s9lab-backend"}
```

## Connect Client

Set the client config to:

```json
{
  "backend": {
    "enabled": true,
    "baseUrl": "http://YOUR_HOST:PORT/api/v1",
    "websocketUrl": "ws://YOUR_HOST:WEBSOCKET_PORT",
    "showOfflineWarnings": true
  }
}
```

Use `https` and `wss` behind a reverse proxy for public production.
