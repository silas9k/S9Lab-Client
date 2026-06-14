package site.s9lab.s9labclient.client.ui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.account.AccountLoginHelper;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public class SafeAccountScreen extends ResponsiveScreen {
    private final Screen parent;
    private int scroll;

    public SafeAccountScreen(Screen parent) {
        super(Text.literal("S9Lab Accounts"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ensureResponsiveLayout();
        PremiumRender.shopBackdrop(context);
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
        if (inside(mouseX, mouseY, loginX(), actionY(), loginW(), 32)) {
            if (AccountLoginHelper.loginRunning()) {
                AccountLoginHelper.cancelMicrosoftLogin();
            } else {
                AccountLoginHelper.beginMicrosoftLogin();
            }
            return true;
        }

        List<AccountLoginHelper.StoredAccount> accounts = rows();
        int y = listY() - scroll;
        for (AccountLoginHelper.StoredAccount account : accounts) {
            if (inside(mouseX, mouseY, rowX(), y, rowWidth(), 56)) {
                int removeX = rowX() + rowWidth() - 76;
                if (!account.launcherSession() && inside(mouseX, mouseY, removeX, y + 17, 58, 22)) {
                    AccountLoginHelper.removeAccount(account.uuid());
                    return true;
                }
                if (!account.launcherSession()) {
                    AccountLoginHelper.switchTo(account);
                }
                return true;
            }
            y += 64;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (inside((int) mouseX, (int) mouseY, rowX(), listY(), rowWidth(), listH())) {
            int max = Math.max(0, rows().size() * 64 - listH());
            scroll = Math.max(0, Math.min(max, scroll - (int) Math.round(verticalAmount * 24.0D)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    private void renderWindow(DrawContext context, int mouseX, int mouseY) {
        int x = windowX();
        int y = windowY();
        int width = windowWidth();
        int height = windowHeight();
        ClientTheme theme = ThemeManager.theme();
        PremiumRender.shopPanel(context, x, y, width, height, 64, 48);
        context.drawTextWithShadow(textRenderer, Text.literal("ACCOUNTS"), x + 26, y + 20, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal("Microsoft browser login inside S9Lab Client"), x + 26, y + 36, 0xFF9AA1B2);
        renderClose(context, mouseX, mouseY);

        int listX = rowX();
        int listY = listY();
        context.enableScissor(listX, listY, listX + rowWidth(), listY + listH());
        int rowY = listY - scroll;
        for (AccountLoginHelper.StoredAccount account : rows()) {
            renderAccountRow(context, account, rowY, mouseX, mouseY, theme.accentColor());
            rowY += 64;
        }
        context.disableScissor();

        String status = AccountLoginHelper.status();
        if (!AccountLoginHelper.lastError().isBlank()) {
            status = AccountLoginHelper.lastError();
        }
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, status, width - 52)), x + 26, y + height - 39, AccountLoginHelper.lastError().isBlank() ? 0xFF9AA1B2 : 0xFFFFB454);
        String label = AccountLoginHelper.loginRunning() ? "Cancel Login" : "Mit Microsoft anmelden";
        renderActionButton(context, loginX(), actionY(), loginW(), 32, label, inside(mouseX, mouseY, loginX(), actionY(), loginW(), 32), theme.accentColor());
    }

    private void renderAccountRow(DrawContext context, AccountLoginHelper.StoredAccount account, int y, int mouseX, int mouseY, int accent) {
        int x = rowX();
        int width = rowWidth();
        boolean hovered = inside(mouseX, mouseY, x, y, width, 56);
        PremiumRender.card(context, x, y, width, 56, 0, hovered ? PremiumRender.SHOP_CARD_HOVER : PremiumRender.SHOP_CARD, account.launcherSession() ? accent : PremiumRender.SHOP_SOFT_BORDER);
        renderAvatar(context, x + 12, y + 8, account.launcherSession() ? 0xFF2E5BFF : 0xFF111111);
        context.drawTextWithShadow(textRenderer, Text.literal(account.username().toUpperCase()), x + 62, y + 12, 0xFFFFFFFF);
        String sub = account.launcherSession()
                ? "Current launcher session"
                : account.status().isBlank() ? "Microsoft account - click to switch" : account.status();
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, sub, width - 160)), x + 62, y + 31, account.reauthRequired() ? 0xFFFFB454 : 0xFF9AA1B2);
        if (account.launcherSession()) {
            renderBadge(context, x + width - 70, y + 18, 52, 20, "Active", accent);
        } else {
            renderActionButton(context, x + width - 76, y + 17, 58, 22, "Remove", inside(mouseX, mouseY, x + width - 76, y + 17, 58, 22), accent);
        }
    }

    private List<AccountLoginHelper.StoredAccount> rows() {
        List<AccountLoginHelper.StoredAccount> rows = new ArrayList<>();
        rows.add(AccountLoginHelper.currentLauncherAccount());
        rows.addAll(AccountLoginHelper.loadAccounts());
        return rows;
    }

    private void renderClose(DrawContext context, int mouseX, int mouseY) {
        boolean hovered = inside(mouseX, mouseY, closeX(), closeY(), 28, 28);
        renderActionButton(context, closeX(), closeY(), 28, 28, "X", hovered, ThemeManager.theme().accentColor());
    }

    private void renderAvatar(DrawContext context, int x, int y, int bg) {
        PremiumRender.roundedRect(context, x, y, 40, 40, 0, bg);
        context.fill(x + 5, y + 5, x + 35, y + 35, 0xFFE4B48D);
        context.fill(x + 5, y + 5, x + 35, y + 15, 0xFF5B3524);
        context.fill(x + 11, y + 21, x + 16, y + 26, 0xFF191919);
        context.fill(x + 24, y + 21, x + 29, y + 26, 0xFF191919);
    }

    private void renderBadge(DrawContext context, int x, int y, int width, int height, String label, int accent) {
        PremiumRender.roundedRect(context, x, y, width, height, 0, 0x662E5BFF);
        PremiumRender.outline(context, x, y, width, height, 0, accent);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + width / 2, y + 6, 0xFFBFD0FF);
    }

    private void renderActionButton(DrawContext context, int x, int y, int width, int height, String label, boolean hovered, int accent) {
        PremiumRender.card(context, x, y, width, height, 0, hovered ? PremiumRender.SHOP_BUTTON_HOVER : PremiumRender.SHOP_BUTTON, hovered ? accent : PremiumRender.SHOP_SOFT_BORDER);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, label, width - 8)), x + width / 2, y + (height - 8) / 2, hovered ? 0xFFFFFFFF : 0xFFD9E2FF);
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

    private int rowWidth() {
        return windowWidth() - padding() * 2;
    }

    private int listY() {
        return windowY() + 76;
    }

    private int listH() {
        return Math.max(80, windowHeight() - 144);
    }

    private int closeX() {
        return windowX() + windowWidth() - padding() - 28;
    }

    private int closeY() {
        return windowY() + 18;
    }

    private int actionY() {
        return windowY() + windowHeight() - 40;
    }

    private int loginX() {
        return windowX() + windowWidth() - padding() - loginW();
    }

    private int loginW() {
        return Math.min(210, Math.max(150, rowWidth() / 3));
    }

    private int padding() {
        return Math.max(12, centeredLayout(760, 430, 260, 220).padding() + 8);
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
