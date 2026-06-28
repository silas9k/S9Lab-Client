package site.s9lab.s9labclient.client.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import site.s9lab.s9labclient.S9LabClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ServerFavoritesManager {
    private static final Gson GSON = new Gson();
    private static final Type STRING_SET = new TypeToken<LinkedHashSet<String>>() { }.getType();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("s9labclient").resolve("server-favorites.json");
    private static final Set<String> FAVORITES = load();

    private ServerFavoritesManager() {
    }

    public static synchronized boolean isFavorite(String address) {
        return FAVORITES.contains(normalize(address));
    }

    public static synchronized boolean toggle(String address) {
        String key = normalize(address);
        if (key.isBlank()) return false;
        boolean favorite;
        if (FAVORITES.remove(key)) {
            favorite = false;
        } else {
            FAVORITES.add(key);
            favorite = true;
        }
        save();
        return favorite;
    }

    public static synchronized void sort(ServerList serverList) {
        List<ServerInfo> ordered = new ArrayList<>(serverList.size());
        for (int i = 0; i < serverList.size(); i++) ordered.add(serverList.get(i));
        ordered.sort((left, right) -> Boolean.compare(isFavorite(right.address), isFavorite(left.address)));
        for (int i = 0; i < ordered.size(); i++) serverList.set(i, ordered.get(i));
    }

    private static Set<String> load() {
        try {
            if (!Files.isRegularFile(FILE)) return new LinkedHashSet<>();
            Set<String> values = GSON.fromJson(Files.readString(FILE), STRING_SET);
            return values == null ? new LinkedHashSet<>() : new LinkedHashSet<>(values);
        } catch (Exception exception) {
            S9LabClient.LOGGER.warn("Could not load server favorites", exception);
            return new LinkedHashSet<>();
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Path temporary = FILE.resolveSibling(FILE.getFileName() + ".tmp");
            Files.writeString(temporary, GSON.toJson(FAVORITES));
            try {
                Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ignored) {
                Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            S9LabClient.LOGGER.warn("Could not save server favorites", exception);
        }
    }

    private static String normalize(String address) {
        return address == null ? "" : address.trim().toLowerCase(Locale.ROOT);
    }
}
