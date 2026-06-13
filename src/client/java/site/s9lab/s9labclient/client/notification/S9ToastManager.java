package site.s9lab.s9labclient.client.notification;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.ui.TextLayout;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public final class S9ToastManager {
    private static final int MAX_VISIBLE = 3;
    private static final Queue<S9Toast> QUEUE = new ArrayDeque<>();
    private static final List<S9Toast> VISIBLE = new ArrayList<>();
    private static String lastToastKey = "";
    private static long lastToastAtMillis;

    private S9ToastManager() {
    }

    public static void success(String title, String message) {
        push(title, message, ThemeManager.theme().accentColor());
    }

    public static void gift(String title, String message) {
        push(title, message, 0xFFFFC857);
    }

    public static void warning(String title, String message) {
        push(title, message, 0xFFFF8F4D);
    }

    public static void push(String title, String message, int accentColor) {
        MinecraftClient client = MinecraftClient.getInstance();
        Runnable add = () -> {
            synchronized (S9ToastManager.class) {
                long now = System.currentTimeMillis();
                String key = (title + "\n" + message).toLowerCase(Locale.ROOT);
                if (key.equals(lastToastKey) && now - lastToastAtMillis < 1200L) {
                    return;
                }
                lastToastKey = key;
                lastToastAtMillis = now;
                QUEUE.add(S9Toast.of(title, message, accentColor));
            }
        };
        if (client == null || client.isOnThread()) {
            add.run();
        } else {
            client.execute(add);
        }
    }

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return;
        }

        long now = System.currentTimeMillis();
        synchronized (S9ToastManager.class) {
            VISIBLE.removeIf(toast -> toast.expired(now));
            while (VISIBLE.size() < MAX_VISIBLE && !QUEUE.isEmpty()) {
                VISIBLE.add(QUEUE.poll());
            }
        }

        if (VISIBLE.isEmpty()) {
            return;
        }

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int width = Math.min(310, Math.max(190, screenWidth / 4));
        int height = screenHeight < 260 ? 30 : 36;
        int margin = screenWidth < 420 ? 6 : 12;
        int targetX = screenWidth - width - margin;
        int y = screenHeight < 260 ? 8 : 14;
        TextRenderer textRenderer = client.textRenderer;

        int index = 0;
        Iterator<S9Toast> iterator = VISIBLE.iterator();
        while (iterator.hasNext()) {
            S9Toast toast = iterator.next();
            float progress = toast.progress(now);
            float in = easeOutCubic(Math.min(1.0F, progress / 0.16F));
            float out = progress > 0.82F ? easeOutCubic(Math.max(0.0F, (1.0F - progress) / 0.18F)) : 1.0F;
            float visible = Math.min(in, out);
            int alpha = Math.round(245.0F * visible);
            if (alpha <= 2) {
                index++;
                continue;
            }

            int slide = Math.round((1.0F - visible) * (width + 18));
            int x = targetX + slide;
            int toastY = y + index * (height + 6);
            int bg = ClientTheme.withAlpha(0xFF07080A, alpha);
            int border = ClientTheme.withAlpha(toast.accentColor(), Math.min(240, alpha));
            context.fill(x + 2, toastY + 2, x + width + 2, toastY + height + 2, ClientTheme.withAlpha(0xFF000000, Math.min(150, alpha)));
            context.fill(x, toastY, x + width, toastY + height, bg);
            context.fill(x, toastY + height - 1, x + Math.max(1, Math.round(width * (1.0F - progress))), toastY + height, ClientTheme.withAlpha(0xFFFFFFFF, Math.min(210, alpha)));
            context.fill(x, toastY, x + 2, toastY + height, border);

            String icon = toast.accentColor() == 0xFFFF8F4D ? "!" : "✓";
            int iconColor = toast.accentColor() == 0xFFFF8F4D ? 0xFFFF8F4D : 0xFF24FF4B;
            context.drawTextWithShadow(textRenderer, Text.literal(icon), x + 12, toastY + (height - 8) / 2, ClientTheme.withAlpha(iconColor, alpha));

            String title = toast.message().isBlank() ? toast.title() : toast.title() + "  ›";
            context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, title.toUpperCase(Locale.ROOT), width - 36)), x + 30, toastY + (height - 8) / 2, ClientTheme.withAlpha(0xFFFFFFFF, alpha));
            index++;
        }
    }

    private static float easeOutCubic(float value) {
        float clamped = Math.max(0.0F, Math.min(1.0F, value));
        float inverse = 1.0F - clamped;
        return 1.0F - inverse * inverse * inverse;
    }
}
