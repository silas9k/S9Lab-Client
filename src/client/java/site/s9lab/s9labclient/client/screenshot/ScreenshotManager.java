package site.s9lab.s9labclient.client.screenshot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Util;
import site.s9lab.s9labclient.S9LabClient;

public final class ScreenshotManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<ScreenshotMetadata>>() {}.getType();
    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final List<ScreenshotMetadata> SCREENSHOTS = new ArrayList<>();
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private static Path metadataPath;

    private ScreenshotManager() {}

    // ─────────────────────────────────────────────────────────────
    //  Init
    // ─────────────────────────────────────────────────────────────

    public static void init(MinecraftClient client) {
        metadataPath = client.runDirectory.toPath()
                .resolve("screenshots")
                .resolve("s9lab_metadata.json");
        load();
    }

    // ─────────────────────────────────────────────────────────────
    //  File naming
    // ─────────────────────────────────────────────────────────────

    public static String customFileName(MinecraftClient client) {
        String server = safeFilePart(serverName(client));
        String stamp  = LocalDateTime.now().format(FILE_FORMAT);
        return stamp + "_" + server + ".png";
    }

    // ─────────────────────────────────────────────────────────────
    //  Screenshot callback — registers metadata + shows action bar
    // ─────────────────────────────────────────────────────────────

    public static Consumer<Text> wrapScreenshotCallback(
            MinecraftClient client, String fileName, Consumer<Text> vanillaCallback) {
        return message -> {
            Path screenshot = client.runDirectory.toPath()
                    .resolve("screenshots").resolve(fileName);
            if (!Files.exists(screenshot)) {
                screenshot = findLatestScreenshot(client).orElse(screenshot);
            }
            if (Files.exists(screenshot)) {
                ScreenshotMetadata metadata = register(client, screenshot);
                vanillaCallback.accept(actionMessage(metadata));
            } else {
                vanillaCallback.accept(message);
            }
        };
    }

    // ─────────────────────────────────────────────────────────────
    //  Query helpers
    // ─────────────────────────────────────────────────────────────

    public static List<ScreenshotMetadata> all() {
        pruneMissing();
        return SCREENSHOTS.stream()
                .sorted(Comparator.comparing(ScreenshotMetadata::date).reversed())
                .toList();
    }

    public static List<ScreenshotMetadata> search(String query) {
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) return all();
        return all().stream()
                .filter(m -> m.server().toLowerCase(Locale.ROOT).contains(normalized)
                          || m.world().toLowerCase(Locale.ROOT).contains(normalized)
                          || m.date().toLowerCase(Locale.ROOT).contains(normalized)
                          || m.fileName().toLowerCase(Locale.ROOT).contains(normalized))
                .toList();
    }

    public static Optional<ScreenshotMetadata> latest() {
        return all().stream().findFirst();
    }

    public static Optional<ScreenshotMetadata> find(String fileName) {
        if ("latest".equalsIgnoreCase(fileName)) return latest();
        return all().stream()
                .filter(m -> m.fileName().equalsIgnoreCase(fileName)
                          || m.absolutePath().equalsIgnoreCase(fileName))
                .findFirst();
    }

    // ─────────────────────────────────────────────────────────────
    //  Actions
    // ─────────────────────────────────────────────────────────────

    public static void openFile(ScreenshotMetadata metadata) {
        try {
            Util.getOperatingSystem().open(metadata.path());
        } catch (Exception e) {
            S9LabClient.LOGGER.warn("Failed to open screenshot {}.", metadata.absolutePath(), e);
        }
    }

    public static void openFolder() {
        Path folder = MinecraftClient.getInstance().runDirectory.toPath().resolve("screenshots");
        try {
            Files.createDirectories(folder);
            Util.getOperatingSystem().open(folder);
        } catch (Exception e) {
            S9LabClient.LOGGER.warn("Failed to open screenshot folder.", e);
        }
    }

    /**
     * Copies the actual image pixels into the system clipboard — not the file path.
     * Falls back to path string if AWT image clipboard is unavailable (headless env).
     */
    public static void copyToClipboard(ScreenshotMetadata metadata) {
        try {
            BufferedImage image = ImageIO.read(metadata.path().toFile());
            if (image == null) throw new IOException("ImageIO returned null");

            Transferable transferable = new Transferable() {
                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[]{DataFlavor.imageFlavor};
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return DataFlavor.imageFlavor.equals(flavor);
                }

                @Override
                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                    if (!DataFlavor.imageFlavor.equals(flavor)) throw new UnsupportedFlavorException(flavor);
                    return image;
                }
            };

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
            S9LabClient.LOGGER.info("Copied image to clipboard: {}", metadata.fileName());

        } catch (Exception e) {
            S9LabClient.LOGGER.warn("Image clipboard failed, falling back to Minecraft clipboard path string.", e);
            copyPathToMinecraftClipboard(metadata);
        }
    }

    private static void copyPathToMinecraftClipboard(ScreenshotMetadata metadata) {
        MinecraftClient client = MinecraftClient.getInstance();
        Runnable copy = () -> {
            try {
                client.keyboard.setClipboard(metadata.absolutePath());
                S9LabClient.LOGGER.info("Copied screenshot path to clipboard: {}", metadata.absolutePath());
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§8[§as9lab§8] §fCopied screenshot path."), false);
                }
            } catch (Exception clipboardException) {
                S9LabClient.LOGGER.warn("Failed to copy screenshot path to Minecraft clipboard: {}",
                        metadata.absolutePath(), clipboardException);
            }
        };

        if (client.isOnThread()) {
            copy.run();
        } else {
            client.execute(copy);
        }
    }

    /**
     * Opens the Discord recipient picker screen.
     * The actual upload happens inside {@link DiscordShareScreen} after the user
     * selects a target (DM / server channel via webhook).
     */
    public static void shareOnDiscord(ScreenshotMetadata metadata) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() ->
                client.setScreen(new DiscordShareScreen(client.currentScreen, metadata)));
    }

    /**
     * Uploads the screenshot as a multipart file to a Discord webhook URL.
     * Called from {@link DiscordShareScreen} after the user confirms the target.
     *
     * @param webhookUrl  full Discord webhook URL
     * @param metadata    screenshot to upload
     * @param message     optional caption (may be empty)
     * @param onDone      callback on the main thread: true = success
     */
    public static void uploadToDiscordWebhook(
            String webhookUrl,
            ScreenshotMetadata metadata,
            String message,
            Consumer<Boolean> onDone) {

        CompletableFuture.supplyAsync(() -> {
            try {
                byte[] imageBytes = Files.readAllBytes(metadata.path());
                String boundary  = "----S9LabBoundary" + UUID.randomUUID().toString().replace("-", "");

                ByteArrayOutputStream body = new ByteArrayOutputStream();
                // -- content field (optional caption)
                if (!message.isBlank()) {
                    writeMultipartField(body, boundary, "content", message);
                }
                // -- file field
                writeMultipartFile(body, boundary, "file", metadata.fileName(), imageBytes);
                body.write(("--" + boundary + "--\r\n").getBytes());

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(webhookUrl))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                        .build();

                HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
                return response.statusCode() >= 200 && response.statusCode() < 300;

            } catch (Exception e) {
                S9LabClient.LOGGER.warn("Discord webhook upload failed.", e);
                return false;
            }
        }).thenAcceptAsync(success ->
                MinecraftClient.getInstance().execute(() -> onDone.accept(success)));
    }

    // ─────────────────────────────────────────────────────────────
    //  Delete
    // ─────────────────────────────────────────────────────────────

    public static boolean delete(ScreenshotMetadata metadata) {
        try {
            Files.deleteIfExists(metadata.path());
            SCREENSHOTS.removeIf(e -> e.absolutePath().equals(metadata.absolutePath()));
            save();
            return true;
        } catch (IOException e) {
            S9LabClient.LOGGER.warn("Failed to delete screenshot {}.", metadata.absolutePath(), e);
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Chat message
    // ─────────────────────────────────────────────────────────────

    private static Text actionMessage(ScreenshotMetadata metadata) {
        return Text.literal("§8[§as9lab§8] §fScreenshot saved! ")
                .append(action("[OPEN]",    0x55FF77, "/s9c screenshot open "    + metadata.fileName()))
                .append(Text.literal(" "))
                .append(action("[COPY]",    0xFFD84D, "/s9c screenshot copy "    + metadata.fileName()))
                .append(Text.literal(" "))
                .append(action("[DISCORD]", 0x5865F2, "/s9c screenshot discord " + metadata.fileName()))
                .append(Text.literal(" "))
                .append(action("[DELETE]",  0xFF4B55, "/s9c screenshot delete "  + metadata.fileName()));
    }

    private static MutableText action(String label, int color, String command) {
        return Text.literal(label).setStyle(Style.EMPTY
                .withColor(TextColor.fromRgb(color))
                .withBold(true)
                .withClickEvent(new ClickEvent.RunCommand(command)));
    }

    // ─────────────────────────────────────────────────────────────
    //  Internals
    // ─────────────────────────────────────────────────────────────

    private static ScreenshotMetadata register(MinecraftClient client, Path screenshot) {
        ScreenshotMetadata metadata = ScreenshotMetadata.create(
                screenshot,
                java.time.Instant.now(),
                serverName(client),
                worldName(client),
                client.getWindow().getFramebufferWidth() + "x" + client.getWindow().getFramebufferHeight()
        );
        SCREENSHOTS.removeIf(e -> e.absolutePath().equals(metadata.absolutePath()));
        SCREENSHOTS.add(metadata);
        save();
        return metadata;
    }

    private static Optional<Path> findLatestScreenshot(MinecraftClient client) {
        Path folder = client.runDirectory.toPath().resolve("screenshots");
        if (!Files.isDirectory(folder)) return Optional.empty();
        try (var stream = Files.list(folder)) {
            return stream.filter(p -> p.getFileName().toString().endsWith(".png"))
                    .max(Comparator.comparingLong(ScreenshotManager::lastModified));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static void load() {
        SCREENSHOTS.clear();
        if (metadataPath == null || !Files.exists(metadataPath)) return;
        try (Reader reader = Files.newBufferedReader(metadataPath)) {
            List<ScreenshotMetadata> loaded = GSON.fromJson(reader, LIST_TYPE);
            if (loaded != null) SCREENSHOTS.addAll(loaded);
            pruneMissing();
        } catch (IOException e) {
            S9LabClient.LOGGER.warn("Failed to load screenshot metadata.", e);
        }
    }

    private static void save() {
        if (metadataPath == null) return;
        try {
            Files.createDirectories(metadataPath.getParent());
            try (Writer writer = Files.newBufferedWriter(metadataPath)) {
                GSON.toJson(SCREENSHOTS, LIST_TYPE, writer);
            }
        } catch (IOException e) {
            S9LabClient.LOGGER.warn("Failed to save screenshot metadata.", e);
        }
    }

    private static void pruneMissing() {
        if (SCREENSHOTS.removeIf(m -> !Files.exists(m.path()))) save();
    }

    private static long lastModified(Path path) {
        try { return Files.getLastModifiedTime(path).toMillis(); }
        catch (IOException e) { return 0L; }
    }

    private static String serverName(MinecraftClient client) {
        if (client.isInSingleplayer()) return "Singleplayer";
        ServerInfo entry = client.getCurrentServerEntry();
        return entry == null ? "Menu" : entry.address;
    }

    private static String worldName(MinecraftClient client) {
        if (client.world == null) return "Menu";
        return client.world.getRegistryKey().getValue().toString();
    }

    private static String safeFilePart(String value) {
        String cleaned = value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]+", "-")
                .replaceAll("-+", "-");
        if (cleaned.isBlank()) return "minecraft";
        return cleaned.length() > 48 ? cleaned.substring(0, 48) : cleaned;
    }

    // ─────────────────────────────────────────────────────────────
    //  Multipart helpers
    // ─────────────────────────────────────────────────────────────

    private static void writeMultipartField(
            ByteArrayOutputStream out, String boundary, String name, String value) throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes());
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes());
        out.write((value + "\r\n").getBytes());
    }

    private static void writeMultipartFile(
            ByteArrayOutputStream out, String boundary,
            String fieldName, String fileName, byte[] data) throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes());
        out.write(("Content-Disposition: form-data; name=\"" + fieldName
                + "\"; filename=\"" + fileName + "\"\r\n").getBytes());
        out.write("Content-Type: image/png\r\n\r\n".getBytes());
        out.write(data);
        out.write("\r\n".getBytes());
    }
}
