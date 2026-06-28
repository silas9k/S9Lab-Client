package site.s9lab.s9labclient.client.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import site.s9lab.s9labclient.S9LabClient;

/** Atomic, metadata-only repository. OAuth credentials never enter this JSON file. */
public final class AccountRepository {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path path;

    public AccountRepository(Path path) { this.path = path; }

    public synchronized List<StoredAccount> load() {
        if (!Files.isRegularFile(path)) return new ArrayList<>();
        List<StoredAccount> accounts;
        try (Reader reader = Files.newBufferedReader(path)) {
            Store store = GSON.fromJson(reader, Store.class);
            accounts = store == null || store.accounts == null ? new ArrayList<>() : new ArrayList<>(store.accounts);
            accounts.replaceAll(StoredAccount::normalized);
            accounts.removeIf(account -> !valid(account));
            accounts.sort(Comparator.comparingLong(StoredAccount::lastUsedAt).reversed()
                    .thenComparing(StoredAccount::username, String.CASE_INSENSITIVE_ORDER));
        } catch (Exception exception) {
            quarantine();
            S9LabClient.LOGGER.warn("Ignored damaged S9Lab account metadata", exception);
            return new ArrayList<>();
        }
        save(accounts); // Reader is closed; rewrite only the allow-listed StoredAccount fields.
        return accounts;
    }

    public synchronized void save(List<StoredAccount> accounts) {
        try {
            Files.createDirectories(path.getParent());
            Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                GSON.toJson(new Store(2, accounts.stream().map(StoredAccount::normalized).filter(AccountRepository::valid)
                        .sorted(Comparator.comparingLong(StoredAccount::lastUsedAt).reversed()).toList()), writer);
            }
            try { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (IOException ignored) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException exception) {
            S9LabClient.LOGGER.warn("Could not save S9Lab account metadata", exception);
        }
    }

    public synchronized void upsert(StoredAccount account) {
        List<StoredAccount> accounts = loadRaw();
        accounts.removeIf(existing -> key(existing).equals(key(account)));
        accounts.add(account.normalized());
        save(accounts);
    }

    public synchronized void remove(String uuid) {
        List<StoredAccount> accounts = loadRaw();
        accounts.removeIf(account -> normalize(account.uuid()).equals(normalize(uuid)));
        save(accounts);
    }

    private List<StoredAccount> loadRaw() {
        if (!Files.isRegularFile(path)) return new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(path)) {
            Store store = GSON.fromJson(reader, Store.class);
            return store == null || store.accounts == null ? new ArrayList<>() : new ArrayList<>(store.accounts);
        } catch (Exception ignored) { return new ArrayList<>(); }
    }

    private static boolean valid(StoredAccount account) {
        if (account == null || account.username().isBlank() || account.uuid().isBlank()) return false;
        try { UUID.fromString(account.uuid()); return true; } catch (IllegalArgumentException ignored) { return false; }
    }

    private static String key(StoredAccount account) { return normalize(account.uuid()); }
    private static String normalize(String value) { return value == null ? "" : value.replace("-", "").toLowerCase(Locale.ROOT); }
    private void quarantine() { try { Files.move(path, path.resolveSibling(path.getFileName() + ".damaged-" + System.currentTimeMillis())); } catch (IOException ignored) { } }
    private record Store(int version, List<StoredAccount> accounts) { }
}
