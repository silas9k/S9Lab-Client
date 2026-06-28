package site.s9lab.s9labclient.client.account;

import com.sun.jna.platform.win32.Crypt32Util;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import site.s9lab.s9labclient.S9LabClient;

/** Windows user-scoped DPAPI store. The encrypted files cannot be decrypted by another Windows user. */
public final class WindowsDpapiCredentialStore implements CredentialStore {
    private final Path directory;
    private final boolean available;

    public WindowsDpapiCredentialStore(Path directory) {
        this.directory = directory;
        this.available = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    @Override
    public Optional<String> load(String accountKey) {
        if (!available || accountKey == null || accountKey.isBlank()) return Optional.empty();
        Path file = file(accountKey);
        if (!Files.isRegularFile(file)) return Optional.empty();
        try {
            String value = new String(Crypt32Util.cryptUnprotectData(Files.readAllBytes(file)), StandardCharsets.UTF_8);
            return value.isBlank() ? Optional.empty() : Optional.of(value);
        } catch (Throwable exception) {
            S9LabClient.LOGGER.warn("Could not decrypt stored Microsoft credentials for account {}", safeKey(accountKey));
            return Optional.empty();
        }
    }

    @Override
    public boolean save(String accountKey, String secret) {
        if (!available || accountKey == null || accountKey.isBlank() || secret == null || secret.isBlank()) return false;
        try {
            Files.createDirectories(directory);
            byte[] encrypted = Crypt32Util.cryptProtectData(secret.getBytes(StandardCharsets.UTF_8));
            Files.write(file(accountKey), encrypted, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            return true;
        } catch (Throwable exception) {
            S9LabClient.LOGGER.warn("Could not store Microsoft credentials securely for account {}", safeKey(accountKey));
            return false;
        }
    }

    @Override
    public void remove(String accountKey) {
        try { Files.deleteIfExists(file(accountKey)); }
        catch (IOException exception) { S9LabClient.LOGGER.debug("Could not remove encrypted Microsoft credential", exception); }
    }

    @Override public boolean available() { return available; }
    @Override public String displayName() { return available ? "Windows DPAPI" : "Unavailable"; }

    private Path file(String accountKey) { return directory.resolve(hash(accountKey) + ".bin"); }

    private static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            return Integer.toHexString(value.hashCode());
        }
    }

    private static String safeKey(String value) {
        String hash = hash(value);
        return hash.substring(0, Math.min(8, hash.length()));
    }
}
