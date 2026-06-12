package site.s9lab.s9labclient.client.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import net.minecraft.util.Util;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.mixin.client.MinecraftClientAccessor;

public final class AccountLoginHelper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final URI MINECRAFT_LOGIN = URI.create("https://www.minecraft.net/login");

    private AccountLoginHelper() {
    }

    public static void openBrowserLogin() {
        Util.getOperatingSystem().open(MINECRAFT_LOGIN);
    }

    public static List<StoredAccount> loadAccounts() {
        Path path = accountsPath();
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            AccountStore store = GSON.fromJson(reader, AccountStore.class);
            if (store == null || store.accounts == null) {
                return new ArrayList<>();
            }

            return new ArrayList<>(store.accounts);
        } catch (IOException | JsonSyntaxException exception) {
            return new ArrayList<>();
        }
    }

    public static StoredAccount addOfflineAccount() {
        List<StoredAccount> accounts = loadAccounts();
        int index = accounts.size() + 1;
        StoredAccount account = new StoredAccount("S9Alt" + index, UUID.nameUUIDFromBytes(("S9Alt" + index).getBytes(StandardCharsets.UTF_8)).toString());
        accounts.add(account);
        saveAccounts(accounts);
        return account;
    }

    public static void switchTo(StoredAccount account) {
        UUID uuid = UUID.fromString(account.uuid);
        Session session = new Session(account.username, uuid, "0", Optional.empty(), Optional.empty());
        ((MinecraftClientAccessor) MinecraftClient.getInstance()).s9labclient$setSession(session);
    }

    private static void saveAccounts(List<StoredAccount> accounts) {
        try {
            Path path = accountsPath();
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                AccountStore store = new AccountStore();
                store.accounts = accounts;
                GSON.toJson(store, writer);
            }
        } catch (IOException ignored) {
        }
    }

    private static Path accountsPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(S9LabClient.MOD_ID + "-accounts.json");
    }

    public record StoredAccount(String username, String uuid) {
    }

    private static class AccountStore {
        List<StoredAccount> accounts = new ArrayList<>();
    }
}
