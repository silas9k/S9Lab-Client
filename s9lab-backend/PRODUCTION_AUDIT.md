# S9Lab Production Audit

## Fixed

- Added client session tokens for HTTP actions and WebSocket auth.
- Stopped cross-UUID action spoofing after handshake.
- Added constant-time admin secret comparison.
- Replaced hardcoded example admin secret with generated/runtime secret handling.
- Ignored real backend config, database and backups in `.gitignore`.
- Added coin amount validation and max coin cap.
- Fixed admin coin actions overwriting player names with UUIDs.
- Added database indexes for common ownership and online lookups.
- Fixed WebSocket status broadcasts wiping remote equipped cosmetics.
- Sent active emotes during WebSocket reconnect state sync.
- Reduced repeated handshake spam from every 15 seconds to session renewal.
- Avoided config JSON disk writes when backend profile state did not change.
- Prevented local optimistic cosmetic equip/unequip from bypassing backend authority.
- Fixed Big Head emote visibility leaks by resetting model part visibility.
- Moved Glints into backend-owned cosmetic flow.
- Moved exposed Emotes into backend-owned cosmetic flow.
- Added Blue Energy Wings as premium backend-synced cosmetic.
- Hid Shop tab while keeping backend shop APIs and data intact.

## Remaining Risks

- The handshake still trusts the launcher-provided UUID. True production anti-impersonation needs Microsoft/Minecraft auth or a launcher-issued signed token.
- Public deployments should use HTTPS/WSS via reverse proxy.
- SQLite is fine for small to medium community scale. Larger deployments should move to PostgreSQL.
- Halo renderer is currently disabled, so halo ownership sync exists but visual rendering is not production-complete.

## Ratings

```text
Production readiness: 7/10
Security: 7/10
Scalability: 6/10
Performance: 8/10
```

## Verified

- Backend build passed.
- Full mod build passed.
- Backend runtime on Java 21 passed.
- Health endpoint passed.
- Handshake and session token passed.
- Missing session rejected with 401.
- Wrong admin secret rejected with 401.
- Blue Energy Wings purchase and equip passed.
- Locked emote start rejected.
- Owned T-Pose emote start passed.
- WebSocket state contained target UUID and Blue Energy Wings state.
- runClient smoke launched Minecraft, loaded a local world, handshook with backend and opened WebSocket without crash.
