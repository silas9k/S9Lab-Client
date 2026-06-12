package site.s9lab.s9labclient.client.music;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MusicManager {
    private static final long MOCK_DURATION = 213_000L;
    private static final long START_TIME = System.currentTimeMillis();
    private static final long REFRESH_INTERVAL = 2_500L;
    private static long lastScan;
    private static long cachedAt = START_TIME;
    private static final AtomicBoolean SCANNING = new AtomicBoolean();
    private static volatile MusicInfo cachedInfo = mockInfo();

    private MusicManager() {
    }

    public static MusicInfo current() {
        refreshSourceAsync();
        MusicInfo info = cachedInfo;
        if (!info.playing() || info.durationMillis() <= 0L) {
            return info;
        }
        long elapsedSinceRefresh = System.currentTimeMillis() - cachedAt;
        long position = Math.min(info.durationMillis(), Math.max(0L, info.positionMillis() + elapsedSinceRefresh));
        return new MusicInfo(info.title(), info.artist(), info.source(), position, info.durationMillis(), true);
    }

    private static void refreshSourceAsync() {
        long now = System.currentTimeMillis();
        if (now - lastScan < REFRESH_INTERVAL || !SCANNING.compareAndSet(false, true)) {
            return;
        }
        lastScan = now;
        CompletableFuture.runAsync(() -> {
            try {
                MusicInfo info = detectFromWindowsMediaSession();
                if (info == null) {
                    info = detectFallback();
                }
                cachedInfo = info;
                cachedAt = System.currentTimeMillis();
            } finally {
                SCANNING.set(false);
            }
        });
    }

    private static MusicInfo detectFallback() {
        String source = ProcessHandle.allProcesses()
                .map(process -> process.info().command().orElse("").toLowerCase(Locale.ROOT))
                .map(MusicManager::sourceFromCommand)
                .filter(candidate -> !candidate.isBlank())
                .findFirst()
                .orElse("Local Mock");
        WindowMusic windowMusic = detectFromWindowsTaskList();
        if (windowMusic != null) {
            return new MusicInfo(windowMusic.title(), windowMusic.artist(), windowMusic.source(), fallbackPosition(), MOCK_DURATION, true);
        }
        if ("Local Mock".equals(source)) {
            return mockInfo();
        }
        return new MusicInfo("Music session detected", source, source, fallbackPosition(), MOCK_DURATION, true);
    }

    private static MusicInfo detectFromWindowsMediaSession() {
        if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("windows")) {
            return null;
        }
        String script = """
                [Console]::OutputEncoding = [Text.Encoding]::UTF8
                Add-Type -AssemblyName System.Runtime.WindowsRuntime
                $asTaskGeneric = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' })[0]
                function Await($op, $type) { $asTaskGeneric.MakeGenericMethod($type).Invoke($null, @($op)).GetAwaiter().GetResult() }
                $manager = Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media.Control,ContentType=WindowsRuntime]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])
                $session = $manager.GetCurrentSession()
                if ($null -ne $session) {
                  $props = Await ($session.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])
                  $timeline = $session.GetTimelineProperties()
                  $playback = $session.GetPlaybackInfo()
                  Write-Output (($props.Title),($props.Artist),($props.AlbumTitle),([int64]$timeline.Position.TotalMilliseconds),([int64]$timeline.EndTime.TotalMilliseconds),($playback.PlaybackStatus.ToString()),($session.SourceAppUserModelId) -join "`t")
                }
                """;
        Process process = null;
        try {
            process = new ProcessBuilder("powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", script)
                    .redirectErrorStream(true)
                    .start();
            if (!process.waitFor(1_800L, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                if (line == null || line.isBlank()) {
                    return null;
                }
                String[] parts = line.split("\\t", -1);
                if (parts.length < 7 || parts[0].isBlank()) {
                    return null;
                }
                String title = clean(parts[0]);
                String artist = clean(parts[1]);
                String source = sourceFromAppId(parts[6]);
                long position = parseLong(parts[3]);
                long duration = parseLong(parts[4]);
                boolean playing = "Playing".equalsIgnoreCase(parts[5]);
                if (artist.isBlank()) {
                    artist = source;
                }
                if (duration <= 0L) {
                    duration = MOCK_DURATION;
                }
                return new MusicInfo(title, artist, source, position, duration, playing);
            }
        } catch (Exception ignored) {
            return null;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static String sourceFromCommand(String command) {
        if (command.contains("spotify")) {
            return "Spotify";
        }
        if (command.contains("applemusic") || command.contains("music.ui") || command.contains("itunes")) {
            return "Apple Music";
        }
        if (command.contains("amazon music")) {
            return "Amazon Music";
        }
        if (command.contains("soundcloud")) {
            return "SoundCloud";
        }
        if (command.contains("chrome") || command.contains("msedge") || command.contains("firefox")) {
            return "Browser Music";
        }
        return "";
    }

    private static String sourceFromAppId(String appId) {
        String normalized = appId == null ? "" : appId.toLowerCase(Locale.ROOT);
        String source = sourceFromCommand(normalized);
        if (!source.isBlank()) {
            return source;
        }
        if (normalized.contains("zunemusic")) {
            return "Apple Music";
        }
        if (normalized.contains("chrome") || normalized.contains("msedge") || normalized.contains("firefox")) {
            return "Browser Music";
        }
        return appId == null || appId.isBlank() ? "Music" : clean(appId);
    }

    private static WindowMusic detectFromWindowsTaskList() {
        if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("windows")) {
            return null;
        }
        Process process = null;
        try {
            process = new ProcessBuilder("cmd", "/c", "tasklist /v /fo csv /nh").redirectErrorStream(true).start();
            if (!process.waitFor(900L, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    List<String> columns = csv(line);
                    if (columns.size() < 9) {
                        continue;
                    }
                    String processName = columns.get(0).toLowerCase(Locale.ROOT);
                    String windowTitle = columns.get(8).trim();
                    String source = sourceFromCommand(processName);
                    if (source.isBlank()) {
                        continue;
                    }
                    return fromWindowTitle(source, windowTitle);
                }
            }
        } catch (Exception ignored) {
            return null;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return null;
    }

    private static WindowMusic fromWindowTitle(String source, String windowTitle) {
        if (windowTitle == null || windowTitle.isBlank() || "N/A".equalsIgnoreCase(windowTitle) || windowTitle.equalsIgnoreCase(source)) {
            return new WindowMusic(source, "Music session detected", source);
        }
        String cleaned = windowTitle.replace(" - Spotify", "").trim();
        int split = cleaned.indexOf(" - ");
        if (split > 0 && split < cleaned.length() - 3) {
            return new WindowMusic(source, cleaned.substring(split + 3).trim(), cleaned.substring(0, split).trim());
        }
        return new WindowMusic(source, cleaned, source);
    }

    private static List<String> csv(String line) {
        ArrayList<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                quoted = !quoted;
            } else if (c == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        return values;
    }

    private static MusicInfo mockInfo() {
        return new MusicInfo("S9Lab Radio - Midnight Build Session", "S9Lab Client", "Local Mock", fallbackPosition(), MOCK_DURATION, true);
    }

    private static long fallbackPosition() {
        return Math.floorMod(System.currentTimeMillis() - START_TIME, MOCK_DURATION);
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private static String clean(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private record WindowMusic(String source, String title, String artist) {
    }
}
