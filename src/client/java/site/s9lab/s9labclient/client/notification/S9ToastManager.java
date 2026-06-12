package site.s9lab.s9labclient.client.notification;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.ui.TextLayout;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public final class S9ToastManager {
    private static final int MAX_VISIBLE = 3;
    private static final Queue<S9Toast> QUEUE = new ArrayDeque<>();
    private static final List<S9Toast> VISIBLE = new ArrayList<>();

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

        int width = Math.min(260, Math.max(188, context.getScaledWindowWidth() / 4));
        int x = context.getScaledWindowWidth() - width - 14;
        int y = 16;
        TextRenderer textRenderer = client.textRenderer;
        ClientTheme theme = ThemeManager.theme();

        int index = 0;
        Iterator<S9Toast> iterator = VISIBLE.iterator();
        while (iterator.hasNext()) {
            S9Toast toast = iterator.next();
            float progress = toast.progress(now);
            float fadeIn = Math.min(1.0F, progress / 0.12F);
            float fadeOut = progress > 0.82F ? Math.max(0.0F, (1.0F - progress) / 0.18F) : 1.0F;
            int alpha = Math.round(235.0F * Math.min(fadeIn, fadeOut));
            if (alpha <= 2) {
                index++;
                continue;
            }

            int toastY = y + index * 58;
            int bg = ClientTheme.withAlpha(0xFF080B12, alpha);
            int border = ClientTheme.withAlpha(toast.accentColor(), Math.min(240, alpha));
            PremiumRender.roundedRect(context, x, toastY, width, 48, 9, bg);
            PremiumRender.outline(context, x, toastY, width, 48, 9, border);
            context.fill(x, toastY, x + 3, toastY + 48, border);
            PremiumRender.roundedRect(context, x + 12, toastY + 12, 24, 24, 7, ClientTheme.withAlpha(toast.accentColor(), Math.min(190, alpha)));
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("S9"), x + 24, toastY + 20, ClientTheme.withAlpha(0xFFFFFFFF, alpha));
            context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, toast.title(), width - 54)), x + 44, toastY + 11, ClientTheme.withAlpha(theme.textColor(), alpha));
            if (!toast.message().isBlank()) {
                context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, toast.message(), width - 54)), x + 44, toastY + 26, ClientTheme.withAlpha(theme.mutedTextColor(), alpha));
            }
            index++;
        }
    }
}
