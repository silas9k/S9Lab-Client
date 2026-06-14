# S9C+ completion patch

Implemented in this patch:

- Blocks repeat S9C+ purchases in both backend and client UI while Plus is active.
- Adds a separate Gift button for every Plus plan.
- Adds `BackendClient.giftPlus(...)` with player-name or UUID support.
- Reuses the existing gift dialog for Plus gifts and cosmetics.
- Keeps server-side checks for self-gifting, insufficient coins, invalid plans and recipients that already have active Plus.
- Keeps server-authoritative storage and WebSocket distribution of up to three allowlisted name effects.
- Adds `Show Other Plus Name Effects` to the Tablist Badge module. Disabling it hides remote animated name effects but keeps badges.
- Applies synchronized name effects to the tab list and player nametag rendering paths already used by the project.

Effect IDs currently accepted by the client/backend implementation:

- `none`
- `rainbow`
- `wave`
- `shake`
- `spin`
- `bounce`
- `pulse`
- `italic_wave` (backend/client state allowlist; not currently exposed in the module selector)

Important limitation:

The current renderer uses TheSalt-style text-color markers. A single marker color can only trigger combinations supported by the installed text-effects shader/resource pack. Arbitrary stacking of any three independent animations requires bundling/adapting a compatible shader implementation rather than only selecting a marker color.

Build status in this environment:

A full Gradle build could not be executed because the project requests Gradle 9.4.0 from `services.gradle.org`, while the execution environment has no outbound network and no cached Gradle distribution. Source-level structure and brace checks were completed. Run on the development PC with:

```powershell
.\gradlew.bat clean build
.\gradlew.bat :s9lab-backend:build
```

If the backend is not configured as a Gradle subproject, run from `s9lab-backend` using its configured wrapper/project command.
