package site.s9lab.s9labclient.client.discord;

import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.S9LabClientClient;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class DiscordRPCManager {
    private static final String APPLICATION_ID = "1196490847982718986";

    private static final String CLIENT_NAME = "S9Lab Client";
    private static final String LARGE_IMAGE_KEY = "large";

    private static final String WEBSITE_URL = "https://s9lab.site";
    private static final String DISCORD_URL = "https://discord.s9lab.site";

    private static final long UPDATE_INTERVAL_SECONDS = 15L;

    private static ScheduledExecutorService executor;
    private static DiscordIpcClient ipcClient;
    private static long startedAtEpochSeconds;
    private static boolean unavailableLogged;

    private DiscordRPCManager() {
    }

    public static synchronized void start() {
        if (executor != null) {
            return;
        }

        startedAtEpochSeconds = Instant.now().getEpochSecond();

        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "S9Lab Discord RPC");
            thread.setDaemon(true);
            return thread;
        });

        executor.scheduleWithFixedDelay(
                DiscordRPCManager::safeUpdate,
                2L,
                UPDATE_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );

        S9LabClient.LOGGER.info("Discord Rich Presence manager started.");
    }

    public static synchronized void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        closeClient();
        S9LabClient.LOGGER.info("Discord Rich Presence manager stopped.");
    }

    private static void safeUpdate() {
        try {
            update();
        } catch (Exception exception) {
            S9LabClient.LOGGER.warn("Failed to update Discord Rich Presence.", exception);
            closeClient();
        }
    }

    private static void update() throws IOException {
        if (S9LabClientClient.getConfigManager() == null) {
            closeClient();
            return;
        }

        if (!S9LabClientClient.getConfigManager().isDiscordRpcEnabled()) {
            closeClient();
            return;
        }

        if (APPLICATION_ID.isBlank() || APPLICATION_ID.equals("1234567890123456789")) {
            if (!unavailableLogged) {
                S9LabClient.LOGGER.warn("Discord Rich Presence has no real application id.");
                unavailableLogged = true;
            }
            return;
        }

        DiscordIpcClient client = getOrConnect();
        if (client == null) {
            return;
        }

        client.send(1, buildActivityPayload());

        unavailableLogged = false;
        S9LabClient.LOGGER.debug("Discord Rich Presence activity sent.");
    }

    private static DiscordIpcClient getOrConnect() {
        if (ipcClient != null && ipcClient.isOpen()) {
            return ipcClient;
        }

        closeClient();

        try {
            ipcClient = DiscordIpcClient.connect();

            String handshakePayload = "{"
                    + "\"v\":1,"
                    + "\"client_id\":\"" + escape(APPLICATION_ID) + "\""
                    + "}";

            ipcClient.send(0, handshakePayload);

            String response = ipcClient.receive();

            if (response.contains("\"cmd\":\"ERROR\"") || response.contains("\"code\"")) {
                S9LabClient.LOGGER.warn("Discord Rich Presence handshake failed: {}", response);
                closeClient();
                return null;
            }

            S9LabClient.LOGGER.info("Connected to Discord Rich Presence IPC.");
            S9LabClient.LOGGER.debug("Discord Rich Presence handshake response: {}", response);

            return ipcClient;
        } catch (IOException exception) {
            if (!unavailableLogged) {
                S9LabClient.LOGGER.info(
                        "Discord Rich Presence unavailable. Discord may not be running: {}",
                        exception.getMessage()
                );
                unavailableLogged = true;
            }

            closeClient();
            return null;
        }
    }

    private static void closeClient() {
        if (ipcClient == null) {
            return;
        }

        try {
            ipcClient.close();
        } catch (IOException exception) {
            S9LabClient.LOGGER.debug("Failed to close Discord Rich Presence IPC.", exception);
        }

        ipcClient = null;
    }

    private static String buildActivityPayload() {
        MinecraftClient minecraft = MinecraftClient.getInstance();

        String version = minecraftVersion();
        String location = currentLocation(minecraft);

        boolean showServer = S9LabClientClient.getConfigManager() != null
                && S9LabClientClient.getConfigManager().shouldDiscordRpcShowServer();

        String details = "Playing " + CLIENT_NAME;
        String state = showServer ? location : "Minecraft " + version;

        return "{"
                + "\"cmd\":\"SET_ACTIVITY\","
                + "\"args\":{"
                + "\"pid\":" + processId() + ","
                + "\"activity\":{"
                + "\"type\":0,"
                + "\"details\":\"" + escape(details) + "\","
                + "\"state\":\"" + escape(state) + "\","
                + "\"timestamps\":{"
                + "\"start\":" + startedAtEpochSeconds
                + "},"
                + "\"assets\":{"
                + "\"large_image\":\"" + escape(LARGE_IMAGE_KEY) + "\","
                + "\"large_text\":\"" + escape(CLIENT_NAME + " • Minecraft " + version) + "\""
                + "},"
                + "\"buttons\":["
                + "{"
                + "\"label\":\"Website\","
                + "\"url\":\"" + escape(WEBSITE_URL) + "\""
                + "},"
                + "{"
                + "\"label\":\"Join Discord\","
                + "\"url\":\"" + escape(DISCORD_URL) + "\""
                + "}"
                + "],"
                + "\"instance\":false"
                + "}"
                + "},"
                + "\"nonce\":\"" + UUID.randomUUID() + "\""
                + "}";
    }

    private static String currentLocation(MinecraftClient minecraft) {
        if (minecraft == null || minecraft.world == null) {
            return "In the Menus";
        }

        if (minecraft.isInSingleplayer()) {
            return "Playing Singleplayer";
        }

        ServerInfo serverInfo = minecraft.getCurrentServerEntry();
        if (serverInfo == null) {
            return "Playing Multiplayer";
        }

        if (serverInfo.name != null && !serverInfo.name.isBlank()) {
            return "On " + serverInfo.name;
        }

        if (serverInfo.address != null && !serverInfo.address.isBlank()) {
            return "On " + serverInfo.address;
        }

        return "Playing Multiplayer";
    }

    private static String minecraftVersion() {
        try {
            return SharedConstants.getGameVersion().name();
        } catch (Exception ignored) {
            return "1.21.11";
        }
    }

    private static long processId() {
        try {
            return ProcessHandle.current().pid();
        } catch (Throwable ignored) {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            int separator = name.indexOf('@');

            if (separator > 0) {
                try {
                    return Long.parseLong(name.substring(0, separator));
                } catch (NumberFormatException ignoredAgain) {
                    return 0L;
                }
            }

            return 0L;
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder(value.length() + 8);

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            switch (c) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (c < 0x20) {
                        builder.append(String.format("\\u%04x", (int) c));
                    } else {
                        builder.append(c);
                    }
                }
            }
        }

        return builder.toString();
    }

    private interface DiscordIpcClient extends AutoCloseable {
        void send(int opcode, String payload) throws IOException;

        String receive() throws IOException;

        boolean isOpen();

        @Override
        void close() throws IOException;

        static DiscordIpcClient connect() throws IOException {
            String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

            if (osName.contains("win")) {
                return WindowsPipeClient.connect();
            }

            return UnixSocketClient.connect();
        }
    }

    private static final class WindowsPipeClient implements DiscordIpcClient {
        private final RandomAccessFile pipe;

        private WindowsPipeClient(RandomAccessFile pipe) {
            this.pipe = pipe;
        }

        static WindowsPipeClient connect() throws IOException {
            IOException lastException = null;

            for (int i = 0; i < 10; i++) {
                String pipePath = "\\\\.\\pipe\\discord-ipc-" + i;

                try {
                    return new WindowsPipeClient(new RandomAccessFile(pipePath, "rw"));
                } catch (IOException exception) {
                    lastException = exception;
                }
            }

            throw lastException == null
                    ? new IOException("No Discord IPC pipe found.")
                    : lastException;
        }

        @Override
        public void send(int opcode, String payload) throws IOException {
            byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);

            ByteBuffer header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            header.putInt(opcode);
            header.putInt(bytes.length);

            pipe.write(header.array());
            pipe.write(bytes);
        }

        @Override
        public String receive() throws IOException {
            byte[] headerBytes = new byte[8];
            pipe.readFully(headerBytes);

            ByteBuffer header = ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN);
            header.getInt();

            int length = header.getInt();
            if (length < 0 || length > 1024 * 1024) {
                throw new IOException("Invalid Discord IPC packet size: " + length);
            }

            byte[] payload = new byte[length];
            pipe.readFully(payload);

            return new String(payload, StandardCharsets.UTF_8);
        }

        @Override
        public boolean isOpen() {
            return pipe.getChannel().isOpen();
        }

        @Override
        public void close() throws IOException {
            pipe.close();
        }
    }

    private static final class UnixSocketClient implements DiscordIpcClient {
        private final SocketChannel channel;

        private UnixSocketClient(SocketChannel channel) {
            this.channel = channel;
        }

        static UnixSocketClient connect() throws IOException {
            IOException lastException = null;

            for (String basePath : unixBasePaths()) {
                if (basePath == null || basePath.isBlank()) {
                    continue;
                }

                for (int i = 0; i < 10; i++) {
                    Path path = Path.of(basePath, "discord-ipc-" + i);

                    if (!Files.exists(path)) {
                        continue;
                    }

                    try {
                        SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX);
                        channel.connect(UnixDomainSocketAddress.of(path));
                        return new UnixSocketClient(channel);
                    } catch (IOException exception) {
                        lastException = exception;
                    }
                }
            }

            throw lastException == null
                    ? new IOException("No Discord IPC socket found.")
                    : lastException;
        }

        private static String[] unixBasePaths() {
            return new String[] {
                    System.getenv("XDG_RUNTIME_DIR"),
                    System.getenv("TMPDIR"),
                    System.getenv("TMP"),
                    System.getenv("TEMP"),
                    "/tmp"
            };
        }

        @Override
        public void send(int opcode, String payload) throws IOException {
            byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);

            ByteBuffer buffer = ByteBuffer.allocate(8 + bytes.length).order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(opcode);
            buffer.putInt(bytes.length);
            buffer.put(bytes);
            buffer.flip();

            while (buffer.hasRemaining()) {
                if (channel.write(buffer) < 0) {
                    throw new EOFException("Discord IPC socket closed.");
                }
            }
        }

        @Override
        public String receive() throws IOException {
            ByteBuffer header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            readFully(header);
            header.flip();

            header.getInt();

            int length = header.getInt();
            if (length < 0 || length > 1024 * 1024) {
                throw new IOException("Invalid Discord IPC packet size: " + length);
            }

            ByteBuffer payload = ByteBuffer.allocate(length);
            readFully(payload);
            payload.flip();

            return StandardCharsets.UTF_8.decode(payload).toString();
        }

        private void readFully(ByteBuffer buffer) throws IOException {
            while (buffer.hasRemaining()) {
                if (channel.read(buffer) < 0) {
                    throw new EOFException("Discord IPC socket closed.");
                }
            }
        }

        @Override
        public boolean isOpen() {
            return channel.isOpen();
        }

        @Override
        public void close() throws IOException {
            channel.close();
        }
    }
}