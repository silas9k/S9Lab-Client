package site.s9lab.s9labclient.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.account.AccountLoginHelper;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public class SafeAccountScreen extends ResponsiveScreen {
    private final Screen parent;
    private String statusMessage = "Using the account from your Minecraft Launcher session.";

    public SafeAccountScreen(Screen parent) {
        super(Text.literal("Select Account"));
        this.parent = parent;
    }

    @Override
    protected void init() {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ensureResponsiveLayout();
        S9LabClientScreen.renderDarkBackground(context);
        renderWindow(context, mouseX, mouseY);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();

        if (inside(mouseX, mouseY, closeX(), closeY(), 28, 28)) {
            close();
            return true;
        }

        if (inside(mouseX, mouseY, refreshX(), refreshY(), refreshWidth(), 34)) {
            Session session = MinecraftClient.getInstance().getSession();
            statusMessage = "Refreshed launcher session: " + session.getUsername();
            return true;
        }

        if (inside(mouseX, mouseY, addX(), addY(), addWidth(), 34)) {
            AccountLoginHelper.openBrowserLogin();
            statusMessage = "Opened minecraft.net login. Relaunch through your launcher after switching accounts.";
            return true;
        }

        if (inside(mouseX, mouseY, rowX(), rowY(), rowWidth(), 54)) {
            statusMessage = "This launcher account is already active.";
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    private void renderWindow(DrawContext context, int mouseX, int mouseY) {
        Session session = MinecraftClient.getInstance().getSession();
        int x = windowX();
        int y = windowY();
        int width = windowWidth();
        int height = windowHeight();
        ClientTheme theme = ThemeManager.theme();

        PremiumRender.card(context, x, y, width, height, theme.radius(), 0xF0121620, theme.borderColor());
        PremiumRender.roundedRect(context, x + 2, y + 2, width - 4, 68, theme.radius() - 2, 0xEE171D29);
        PremiumRender.roundedRect(context, x + 14, y + 67, width - 28, 1, 0, theme.borderColor());

        context.drawTextWithShadow(this.textRenderer, Text.literal("SELECT ACCOUNT"), x + 30, y + 30, 0xFFFFFFFF);
        renderClose(context, mouseX, mouseY);
        renderAccountRow(context, session, mouseX, mouseY);
        renderLoginInfo(context);
        renderFooter(context, session, mouseX, mouseY);
    }

    private void renderClose(DrawContext context, int mouseX, int mouseY) {
        int x = closeX();
        int y = closeY();
        boolean hovered = inside(mouseX, mouseY, x, y, 28, 28);
        ClientTheme theme = ThemeManager.theme();
        PremiumRender.card(context, x, y, 28, 28, theme.radius(), hovered ? 0xCC2A3650 : 0x8824334E, hovered ? theme.accentColor() : theme.borderColor());
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("X"), x + 14, y + 9, hovered ? 0xFFFFFFFF : 0xFFB8C0CC);
    }

    private void renderAccountRow(DrawContext context, Session session, int mouseX, int mouseY) {
        int x = rowX();
        int y = rowY();
        int width = rowWidth();
        boolean hovered = inside(mouseX, mouseY, x, y, width, 54);
        ClientTheme theme = ThemeManager.theme();

        PremiumRender.card(context, x, y, width, 54, theme.radius(), hovered ? 0xDD24304A : 0xCC1B2232, theme.accentColor());

        renderAvatar(context, x + 16, y + 7);
        context.drawTextWithShadow(this.textRenderer, Text.literal(session.getUsername().toUpperCase()), x + 66, y + 12, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Signed in - launcher account"), x + 66, y + 30, 0xFFB8C0CC);

        int badgeWidth = 58;
        int badgeX = x + width - badgeWidth - 18;
        int badgeY = y + 17;
        PremiumRender.roundedRect(context, badgeX, badgeY, badgeWidth, 20, 6, 0x662E5BFF);
        PremiumRender.outline(context, badgeX, badgeY, badgeWidth, 20, 6, theme.accentColor());
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Active"), badgeX + badgeWidth / 2, badgeY + 6, 0xFFBFD0FF);
    }

    private void renderLoginInfo(DrawContext context) {
        int x = rowX();
        int y = rowY() + 68;
        int width = rowWidth();
        int height = Math.max(70, footerY() - y - 42);
        ClientTheme theme = ThemeManager.theme();
        PremiumRender.card(context, x, y, width, height, theme.radius(), 0x99151D2A, theme.borderColor());
        PremiumRender.roundedRect(context, x, y, 3, height, 2, theme.accentColor());
        context.drawTextWithShadow(this.textRenderer, Text.literal("Secure browser login"), x + 18, y + 16, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("S9Lab never stores passwords or Microsoft/Minecraft tokens."), x + 18, y + 34, 0xFFB8C0CC);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Use the browser page and then relaunch with your launcher account."), x + 18, y + 50, 0xFF8D96A8);
    }

    private void renderAvatar(DrawContext context, int x, int y) {
        PremiumRender.roundedRect(context, x, y, 40, 40, 8, 0xFF111111);
        context.fill(x + 3, y + 3, x + 37, y + 37, 0xFFE4B48D);
        context.fill(x + 3, y + 3, x + 37, y + 13, 0xFF5B3524);
        context.fill(x + 9, y + 20, x + 15, y + 26, 0xFF191919);
        context.fill(x + 25, y + 20, x + 31, y + 26, 0xFF191919);
        context.fill(x + 16, y + 31, x + 24, y + 34, 0xFFB97868);
    }

    private void renderFooter(DrawContext context, Session session, int mouseX, int mouseY) {
        int y = footerY();
        int refreshX = refreshX();
        int addX = addX();
        int refreshWidth = refreshWidth();
        int addWidth = addWidth();
        context.drawTextWithShadow(this.textRenderer, Text.literal("Launcher-controlled session"), windowX() + 30, y + 12, 0xFF9DA7B8);
        renderActionButton(context, refreshX, refreshY(), refreshWidth, 34, "Refresh", inside(mouseX, mouseY, refreshX, refreshY(), refreshWidth, 34));
        renderActionButton(context, addX, addY(), addWidth, 34, "Browser Login", inside(mouseX, mouseY, addX, addY(), addWidth, 34));

        String mode = MinecraftClient.getInstance().isInSingleplayer() ? "Singleplayer" : "Menu";
        context.drawTextWithShadow(this.textRenderer, Text.literal(statusMessage), windowX() + 30, y - 31, 0xFFB8C0CC);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Current: " + session.getUsername() + " | " + mode), windowX() + 30, y - 15, 0xFF6F7C91);
    }

    private void renderActionButton(DrawContext context, int x, int y, int width, int height, String label, boolean hovered) {
        ClientTheme theme = ThemeManager.theme();
        PremiumRender.card(context, x, y, width, height, theme.radius(), hovered ? 0xCC293653 : 0x99212A3D, hovered ? theme.accentColor() : theme.borderColor());
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(label), x + width / 2, y + 12, hovered ? 0xFFFFFFFF : 0xFFD9E2FF);
    }

    private int windowWidth() {
        return centeredLayout(760, 430, 260, 220).width();
    }

    private int windowHeight() {
        return centeredLayout(760, 430, 260, 220).height();
    }

    private int windowX() {
        return centeredLayout(760, 430, 260, 220).x();
    }

    private int windowY() {
        return centeredLayout(760, 430, 260, 220).y();
    }

    private int rowX() {
        return windowX() + padding();
    }

    private int rowY() {
        return windowY() + (windowHeight() < 270 ? 72 : 84);
    }

    private int rowWidth() {
        return windowWidth() - padding() * 2;
    }

    private int closeX() {
        return windowX() + windowWidth() - padding() - 28;
    }

    private int closeY() {
        return windowY() + 30;
    }

    private int footerY() {
        return windowY() + windowHeight() - (windowWidth() < 430 ? 112 : 70);
    }

    private int refreshY() {
        return footerY() + (windowWidth() < 430 ? 28 : 0);
    }

    private int addY() {
        return footerY() + (windowWidth() < 430 ? 66 : 0);
    }

    private int refreshX() {
        if (windowWidth() < 430) {
            return rowX();
        }
        return addX() - 12 - refreshWidth();
    }

    private int addX() {
        if (windowWidth() < 430) {
            return rowX();
        }
        return windowX() + windowWidth() - padding() - addWidth();
    }

    private int refreshWidth() {
        if (windowWidth() < 430) {
            return rowWidth();
        }
        return Math.max(88, Math.min(150, windowWidth() / 5));
    }

    private int addWidth() {
        if (windowWidth() < 430) {
            return rowWidth();
        }
        return Math.max(130, Math.min(230, windowWidth() - padding() * 2 - 12 - refreshWidth()));
    }

    private int padding() {
        return Math.max(10, centeredLayout(760, 430, 260, 220).padding() + (windowWidth() < 430 ? 4 : 12));
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
