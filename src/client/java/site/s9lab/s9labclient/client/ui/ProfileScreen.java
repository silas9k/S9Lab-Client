package site.s9lab.s9labclient.client.ui;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.backend.BackendClient;
import site.s9lab.s9labclient.client.backend.BackendClient.ProfileInfo;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public class ProfileScreen extends ResponsiveScreen {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            .withZone(ZoneId.systemDefault());
    private static final int WHITE = 0xFFFFFFFF;
    private static final int TEXT = 0xFFE7EAF2;
    private static final int MUTED = 0xFF9AA1B2;
    private static final int DIM = 0xFF687083;
    private static final int GREEN = 0xFF49F26F;
    private static final int WARN = 0xFFFFB454;

    private final Screen parent;
    private final String target;
    private ProfileInfo profile;
    private String error = "";
    private boolean requested;

    public ProfileScreen(Screen parent, String target) {
        super(Text.literal("S9Lab Profile"));
        this.parent = parent;
        this.target = target == null || target.isBlank() ? "me" : target.trim();
    }

    @Override
    protected void init() {
        if (!requested) {
            requested = true;
            BackendClient.requestProfile(resolveTarget(), loaded -> this.profile = loaded, message -> this.error = message);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ensureResponsiveLayout();
        ClientTheme theme = ThemeManager.theme();
        int accent = theme.accentColor();
        PremiumRender.shopBackdrop(context);

        ScreenLayout layout = centeredLayout(560, 320, 310, 230);
        PremiumRender.shopPanel(context, layout.x(), layout.y(), layout.width(), layout.height(), 46, 0);
        context.drawTextWithShadow(textRenderer, Text.literal("S9Lab Profile"), layout.x() + 18, layout.y() + 15, WHITE);
        drawClose(context, layout, mouseX, mouseY);

        if (profile == null) {
            String line = error.isBlank() ? "Loading profile..." : "Profile unavailable: " + error;
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, line, layout.width() - 36)),
                    layout.x() + layout.width() / 2, layout.y() + layout.height() / 2, error.isBlank() ? MUTED : WARN);
            return;
        }

        int leftX = layout.x() + 22;
        int top = layout.y() + 66;
        // PremiumRender.roundedRect(context, leftX, top, 92, 92, 12, 0xAA111824);
        // PremiumRender.outline(context, leftX, top, 92, 92, 12, profile.s9labUser() ? accent : 0x66333B4C);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("S9"), leftX + 46, top + 36, WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(profile.online() ? "ONLINE" : "OFFLINE"), leftX + 46, top + 68, profile.online() ? GREEN : DIM);

        int infoX = leftX + 118;
        context.drawTextWithShadow(textRenderer, Text.literal(profile.name()), infoX, top + 4, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal(profile.uuid()), infoX, top + 20, MUTED);
        context.drawTextWithShadow(textRenderer, Text.literal(profile.s9labUser() ? "S9Lab User" : "Minecraft User"), infoX, top + 36, profile.s9labUser() ? accent : DIM);

        int gridY = top + 72;
        int colW = Math.max(130, (layout.x() + layout.width() - infoX - 28) / 2);
        stat(context, "Coins", String.valueOf(profile.coins()), infoX, gridY, colW, accent);
        stat(context, "Cosmetics", String.valueOf(profile.ownedCosmeticsCount()), infoX + colW + 12, gridY, colW, accent);
        stat(context, "First Seen", date(profile.firstSeen()), infoX, gridY + 50, colW, accent);
        stat(context, "Last Seen", date(profile.lastSeen()), infoX + colW + 12, gridY + 50, colW, accent);
        stat(context, "Playtime", playtime(profile.totalPlaytimeSeconds()), infoX, gridY + 100, colW, accent);
        // stat(context, "Active Emote", profile.activeEmote().isBlank() ? "None" : profile.activeEmote(), infoX + colW + 12, gridY + 100, colW, accent);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        ScreenLayout layout = centeredLayout(560, 320, 310, 230);
        if (inside(click.x(), click.y(), layout.x() + layout.width() - 34, layout.y() + 12, 22, 22)) {
            close();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        if (input.isEscape()) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    private String resolveTarget() {
        if (!"me".equalsIgnoreCase(target)) {
            return target;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        return client.getSession() == null ? target : client.getSession().getUsername();
    }

    private void stat(DrawContext context, String label, String value, int x, int y, int width, int accent) {
        PremiumRender.card(context, x, y, width, 40, 0, PremiumRender.SHOP_CARD, 0x55FFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal(label), x + 10, y + 8, DIM);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, value, width - 20)), x + 10, y + 23, label.equals("Coins") ? accent : TEXT);
    }

    private void drawClose(DrawContext context, ScreenLayout layout, int mouseX, int mouseY) {
        boolean hovered = inside(mouseX, mouseY, layout.x() + layout.width() - 34, layout.y() + 12, 22, 22);
        PremiumRender.roundedRect(context, layout.x() + layout.width() - 34, layout.y() + 12, 22, 22, 0, hovered ? 0xFF2B1A22 : PremiumRender.SHOP_BUTTON);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("×"), layout.x() + layout.width() - 23, layout.y() + 18, hovered ? 0xFFFF9CA3 : MUTED);
    }

    private static String date(long epochSeconds) {
        return epochSeconds <= 0 ? "Unknown" : DATE_FORMAT.format(Instant.ofEpochSecond(epochSeconds));
    }

    private static String playtime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return hours > 0 ? hours + "h " + minutes + "m" : minutes + "m";
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
