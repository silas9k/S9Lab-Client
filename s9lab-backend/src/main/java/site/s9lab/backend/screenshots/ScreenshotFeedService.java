package site.s9lab.backend.screenshots;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import site.s9lab.backend.api.dto.Dtos;
import site.s9lab.backend.storage.DatabaseManager;

public final class ScreenshotFeedService {
    private static final long MAX_UPLOAD_BYTES = 12L * 1024L * 1024L;
    private static final int MAX_DIMENSION = 8192;

    private final DatabaseManager database;
    private final Path screenshotDirectory;

    public ScreenshotFeedService(DatabaseManager database, Path screenshotDirectory) {
        this.database = database;
        this.screenshotDirectory = screenshotDirectory;
    }

    public void init() throws IOException {
        Files.createDirectories(screenshotDirectory);
        ImageIO.scanForPlugins();
    }

    public Dtos.ScreenshotPostDto upload(String uuid, String name, Dtos.ScreenshotUploadRequest request) throws IOException, SQLException {
        if (request == null) {
            throw new IllegalArgumentException("missing_body");
        }
        byte[] bytes = decodeImage(request.imageBase64());
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
            throw new IllegalArgumentException("invalid_image");
        }
        if (image.getWidth() > MAX_DIMENSION || image.getHeight() > MAX_DIMENSION) {
            throw new IllegalArgumentException("image_too_large");
        }
        if (!hasWebpWriter()) {
            throw new IllegalArgumentException("webp_writer_missing");
        }

        String id = postId();
        Path target = screenshotDirectory.resolve(id + ".webp").normalize();
        if (!target.startsWith(screenshotDirectory)) {
            throw new IllegalArgumentException("invalid_screenshot_path");
        }
        boolean written = ImageIO.write(image, "webp", target.toFile());
        if (!written || !Files.isRegularFile(target) || Files.size(target) <= 0) {
            Files.deleteIfExists(target);
            throw new IllegalArgumentException("webp_write_failed");
        }

        return database.createScreenshotPost(
                id,
                uuid,
                name,
                safe(request.fileName(), 128, "screenshot.png"),
                safe(request.caption(), 180, ""),
                safe(request.server(), 96, "Unknown"),
                safe(request.world(), 128, "Unknown"),
                safe(request.resolution(), 32, ""),
                visibility(request.visibility()),
                target.getFileName().toString(),
                uuid
        );
    }

    public List<Dtos.ScreenshotPostDto> feed(String viewerUuid) throws SQLException {
        return database.screenshotPosts(viewerUuid);
    }

    public List<Dtos.ScreenshotPostDto> contest(String viewerUuid, String period) throws SQLException {
        return database.screenshotContest(viewerUuid, period);
    }

    public Dtos.ScreenshotPostDto react(String uuid, Dtos.ScreenshotReactionRequest request) throws SQLException {
        if (request == null) {
            throw new IllegalArgumentException("missing_body");
        }
        return database.reactScreenshot(request.postId(), uuid, request.reaction());
    }

    public Dtos.ScreenshotPostDto comment(String uuid, String name, Dtos.ScreenshotCommentRequest request) throws SQLException {
        if (request == null) {
            throw new IllegalArgumentException("missing_body");
        }
        return database.addScreenshotComment(request.postId(), uuid, name, request.message());
    }

    public boolean delete(Dtos.ScreenshotDeleteRequest request) throws IOException, SQLException {
        if (request == null) {
            throw new IllegalArgumentException("missing_body");
        }
        String imagePath = database.deleteScreenshotPost(request.postId())
                .orElseThrow(() -> new IllegalArgumentException("screenshot_not_found"));
        Path target = screenshotDirectory.resolve(imagePath).normalize();
        if (target.startsWith(screenshotDirectory)) {
            Files.deleteIfExists(target);
        }
        return true;
    }

    public Path resolveWebp(String requestedName) {
        String fileName = requestedName == null ? "" : requestedName.trim().toLowerCase(Locale.ROOT);
        if (!fileName.matches("[a-z0-9][a-z0-9-]{4,80}\\.webp")) {
            return null;
        }
        Path target = screenshotDirectory.resolve(fileName).normalize();
        return target.startsWith(screenshotDirectory) ? target : null;
    }

    private static byte[] decodeImage(String imageBase64) {
        String value = imageBase64 == null ? "" : imageBase64.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("missing_image");
        }
        if (value.startsWith("data:")) {
            int separator = value.indexOf(',');
            value = separator >= 0 ? value.substring(separator + 1) : "";
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("invalid_image_base64");
        }
        if (bytes.length <= 0 || bytes.length > MAX_UPLOAD_BYTES) {
            throw new IllegalArgumentException("image_too_large");
        }
        return bytes;
    }

    private static boolean hasWebpWriter() {
        java.util.Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("webp");
        return writers.hasNext();
    }

    private static String postId() {
        return Long.toString(Instant.now().toEpochMilli(), 36) + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String safe(String value, int maxLength, String fallback) {
        String normalized = value == null ? fallback : value.trim();
        if (normalized.isBlank()) {
            normalized = fallback;
        }
        normalized = normalized.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").replace('\r', ' ').replace('\n', ' ');
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }

    private static String visibility(String value) {
        return "private".equalsIgnoreCase(value == null ? "" : value.trim()) ? "private" : "public";
    }
}
