# Server banners

Place PNG server-list banners in this directory. The running backend creates the
same `banners` directory next to its JAR and serves the files at `/banners/...`.

The default filename is generated from the configured server address:

- `play.example.net:25565` -> `play-example-net.png`

PNG files are limited to 5 MB. Use a wide image, for example 1200 x 240 pixels.
