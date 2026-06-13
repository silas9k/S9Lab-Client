# S9Lab Client Microsoft Login Setup

Der Account-Switcher im Minecraft-Client nutzt einen echten Microsoft Browser-Login mit PKCE.
Es wird keine fremde Client-ID, kein Client Secret und keine Passwortabfrage verwendet.

## Microsoft App Registration

1. Öffne das Microsoft Entra/Azure App Registration Portal.
2. Erstelle eine neue App Registration für `S9Lab Client`.
3. Unterstützte Kontotypen: persönliche Microsoft-Konten müssen erlaubt sein.
4. Die App ist eine Public Client / Native Desktop App.
5. Trage exakt diese Redirect URI ein:

```text
http://localhost:25585/callback
```

6. Aktiviere Public Client Flow, falls das Portal diese Option für deine App anzeigt.
7. Verwende kein Client Secret im Minecraft-Client.
8. Kopiere die Application/Client ID.

## Client-ID konfigurieren

Für Development kannst du eine Umgebungsvariable setzen:

```powershell
$env:S9LAB_MICROSOFT_CLIENT_ID="DEINE-CLIENT-ID-UUID"
.\gradlew.bat runClient
```

Alternativ geht eine JVM Property:

```powershell
.\gradlew.bat runClient -Ds9lab.microsoftClientId=DEINE-CLIENT-ID-UUID
```

Der Client akzeptiert außerdem `MICROSOFT_CLIENT_ID`.

## Login testen

1. Starte Minecraft mit dem S9Lab Client.
2. Öffne `/s9c accounts` oder den Accounts-Button im Menü.
3. Klicke `Mit Microsoft anmelden`.
4. Der Standardbrowser öffnet Microsoft.
5. Nach erfolgreichem Login zeigt der Browser eine S9Lab-Erfolgsmeldung.
6. Im Client erscheint der Account und wird direkt als Minecraft-Session gesetzt.

## Wichtige Hinweise

- Tokens werden nicht in Chat oder Logs ausgegeben.
- Account-Metadaten werden in `config/s9labclient-accounts.json` gespeichert.
- Sensitive Tokens bleiben aktuell nur im Arbeitsspeicher dieser Minecraft-Session.
- Nach einem Minecraft-Neustart muss für gespeicherte Accounts erneut eingeloggt werden.
- Wenn Minecraft Services `Invalid app registration` meldet, ist die externe Microsoft-App-Registrierung nicht für den Flow akzeptiert. Das kann nicht durch Code sauber umgangen werden.

## Typische Fehler

- `AUTH_CONFIG_MISSING`: Client-ID fehlt. Setze `S9LAB_MICROSOFT_CLIENT_ID`.
- `AUTH_REDIRECT_INVALID`: Redirect URI muss `http://localhost:25585/callback` sein.
- `AUTH_CALLBACK_PORT_BUSY`: Port 25585 ist belegt.
- `AUTH_STATE_MISMATCH`: Sicherheitsprüfung fehlgeschlagen, Login neu starten.
- `AUTH_MINECRAFT_NOT_OWNED`: Der Microsoft Account besitzt Minecraft Java nicht.
- `AUTH_MINECRAFT_PROFILE_FAILED`: Für den Account existiert kein Java-Profil.
