# S9Lab Cosmetics hinzufügen

Neue Cosmetic-Styles werden zentral hier eingetragen:

`src/main/resources/assets/s9labclient/s9lab_cosmetics.json`

## Neuer Style

1. PNG-Texture in den passenden Ordner legen, zum Beispiel:
   `src/main/resources/assets/s9labclient/textures/cosmetics/wings/mein_wing.png`

2. Einen Eintrag in `s9lab_cosmetics.json` hinzufügen:

```json
{
  "id": "s9lab_mein_wing",
  "name": "Mein Wing",
  "type": "wings",
  "texture": "textures/cosmetics/wings/mein_wing.png",
  "animated": true
}
```

3. Build/Client neu starten.

Der Style erscheint automatisch im RSHIFT-Menü, in der Suche, in den Cosmetic-Cards, in den Settings und bei `/s9c` Cosmetic-Commands.

## Types

Erlaubte `type` Werte:

- `cape`
- `bandana`
- `wings`
- `hat`
- `halo`
- `shoulder`

Für `cape` und `bandana` kannst du optional eigene GeckoLib-Dateien angeben:

```json
{
  "id": "s9lab_custom_cape",
  "name": "Custom Cape",
  "type": "cape",
  "texture": "textures/cosmetics/capes/custom_cape.png",
  "model": "geo/s9lab_cape.geo.json",
  "animation": "animations/s9lab_cape.animation.json",
  "animated": true
}
```

Wenn `model` oder `animation` fehlen, nutzt der Client die Standard-Dateien.
