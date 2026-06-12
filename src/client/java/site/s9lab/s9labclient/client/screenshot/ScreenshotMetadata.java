package site.s9lab.s9labclient.client.screenshot;

import java.nio.file.Path;
import java.time.Instant;

public record ScreenshotMetadata(
        String fileName,
        String absolutePath,
        String date,
        String server,
        String world,
        String resolution
) {
    public static ScreenshotMetadata create(Path file, Instant instant, String server, String world, String resolution) {
        return new ScreenshotMetadata(
                file.getFileName().toString(),
                file.toAbsolutePath().toString(),
                instant.toString(),
                server,
                world,
                resolution
        );
    }

    public Path path() {
        return Path.of(absolutePath);
    }
}
