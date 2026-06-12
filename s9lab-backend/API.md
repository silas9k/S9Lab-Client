# S9Lab Backend API

Base URL:

```text
http://host:port/api/v1
```

Admin header:

```text
X-Admin-Secret: your-secret
```

Client session header after handshake:

```text
X-S9Lab-Session: session-token
```

## Public

### GET /health

No auth.

### POST /api/v1/handshake

Request:

```json
{"uuid":"player-uuid","name":"Player","clientVersion":"1.0.0"}
```

Returns profile, catalog and `sessionToken`.

### POST /api/v1/heartbeat

Requires `X-S9Lab-Session`.

```json
{"uuid":"player-uuid","name":"Player","playtimeSeconds":120,"status":"Singleplayer"}
```

### GET /api/v1/cosmetics

Returns backend cosmetic catalog.

## User Settings And Profiles

### GET /api/v1/settings

Requires `X-S9Lab-Session`.

Returns account-bound UI/module/emote-wheel settings from SQLite.

### POST /api/v1/settings

Requires `X-S9Lab-Session`.

```json
{"uuid":"player-uuid","settings":{"modules":{},"ui":{"accentColor":4284382434,"blurEnabled":true},"emoteWheelSlots":["t_pose","","",""]}}
```

The session token decides the UUID. A mismatching body UUID is rejected.

### GET /api/v1/profile/{uuid}

Requires `X-S9Lab-Session`.

Returns public profile data only: name, UUID, coins, first/last seen, playtime, online state, active emote and owned cosmetic count.

### GET /api/v1/profile/name/{name}

Requires `X-S9Lab-Session`.

Looks up the newest known player with that name and returns the same public profile DTO.

## Shop And Ownership

### POST /api/v1/shop/buy

Requires `X-S9Lab-Session`.

```json
{"uuid":"player-uuid","cosmeticId":"s9lab_blue_energy_wings","type":"wings"}
```

Checks coins, enabled catalog entry and duplicate ownership.

### POST /api/v1/cosmetics/equip

Requires `X-S9Lab-Session`.

```json
{"uuid":"player-uuid","cosmeticId":"s9lab_blue_energy_wings","type":"wings"}
```

Server requires ownership before equip.

### POST /api/v1/cosmetics/gift

Requires `X-S9Lab-Session`.

```json
{"senderUuid":"player-uuid","receiverUuid":"","receiverName":"Friend","cosmeticId":"s9lab_blue_energy_wings"}
```

Server validates sender ownership, receiver existence, duplicate ownership and enabled cosmetic status. The gift is written to `cosmetic_gifts` and the receiver receives ownership.

### POST /api/v1/cosmetics/unequip

Requires `X-S9Lab-Session`.

```json
{"uuid":"player-uuid","cosmeticId":"","type":"wings"}
```

Supported types:

```text
cape, bandana, wings, hat, halo, shoulder, glint, emote
```

## Emotes

### POST /api/v1/emotes/start

Requires `X-S9Lab-Session`.

```json
{"uuid":"player-uuid","emoteId":"t_pose"}
```

The matching cosmetic must be owned, for example `s9lab_emote_t_pose`.

### POST /api/v1/emotes/stop

Requires `X-S9Lab-Session`.

```json
{"uuid":"player-uuid","emoteId":""}
```

## Admin API

### GET /admin/player/{uuid}

Requires `X-Admin-Secret`.

### POST /admin/coins/set

```json
{"uuid":"player-uuid","amount":1000}
```

### POST /admin/coins/add

```json
{"uuid":"player-uuid","amount":250}
```

### POST /admin/coins/remove

```json
{"uuid":"player-uuid","amount":50}
```

Coin amount range:

```text
0 to 1,000,000,000,000
```

## WebSocket

URL:

```text
ws://host:websocketPort?uuid=player-uuid&name=Player&token=session-token
```

Events:

```json
{"event":"CosmeticStateUpdate","uuid":"...","equippedCosmetics":{"wings":"s9lab_blue_energy_wings"},"emoteId":"","online":true}
{"event":"PlayerStatusUpdate","uuid":"...","equippedCosmetics":{},"emoteId":"","online":false}
{"event":"EmoteStateUpdate","uuid":"...","equippedCosmetics":{},"emoteId":"t_pose","online":true}
{"event":"ProfileUpdate","ok":true,"uuid":"...","ownedCosmetics":["..."],"equippedCosmetics":{},"coins":100}
```

One active WebSocket per UUID is kept. New valid connections replace old ones.
