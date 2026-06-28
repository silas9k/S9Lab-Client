package site.s9lab.s9labclient.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.dev.DevModeManager;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

public class DevModeScreen extends ResponsiveScreen {
    private final Screen parent;
    private TextFieldWidget secretField;
    private String status = DevModeManager.isEnabled() ? "Dev Mode ist aktiv." : "Admin Secret eingeben.";

    public DevModeScreen(Screen parent) {
        super(Text.literal("S9Lab Dev Mode"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Panel panel = panel();
        secretField = new TextFieldWidget(textRenderer, panel.x + 24, panel.y + 96, panel.width - 48, 22, Text.literal("Admin Secret"));
        secretField.setMaxLength(256);
        secretField.setPlaceholder(Text.literal("Admin Secret..."));
        addDrawableChild(secretField);
        setInitialFocus(secretField);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ensureResponsiveLayout();
        ClientTheme theme = ThemeManager.theme();
        PremiumRender.shopBackdrop(context);
        Panel panel = panel();
        PremiumRender.shopPanel(context, panel.x, panel.y, panel.width, panel.height, 68, 0);
        context.drawTextWithShadow(textRenderer, Text.literal("DEV MODE"), panel.x + 24, panel.y + 22, theme.textColor());
        context.drawTextWithShadow(textRenderer, Text.literal("Schaltet interne Screenshot Uploads und den Dev Feed frei."), panel.x + 24, panel.y + 42, theme.mutedTextColor());
        String account = DevModeManager.currentAccountUuid();
        context.drawTextWithShadow(textRenderer, Text.literal("Account: " + (account.isBlank() ? "unknown" : account)), panel.x + 24, panel.y + 66, theme.mutedTextColor());
        context.drawTextWithShadow(textRenderer, Text.literal(status), panel.x + 24, panel.y + panel.height - 34,
                DevModeManager.isEnabled() ? 0xFF4CFF73 : 0xFFFFB54A);
        drawButton(context, panel.x + 24, panel.y + 138, 128, 28, "Verify", mouseX, mouseY);
        drawButton(context, panel.x + 160, panel.y + 138, 128, 28, "Disable", mouseX, mouseY);
        drawButton(context, panel.x + panel.width - 152, panel.y + 138, 128, 28, "Open Feed", mouseX, mouseY);
        drawButton(context, panel.x + panel.width - 54, panel.y + 18, 30, 30, "X", mouseX, mouseY);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        Panel panel = panel();
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        if (inside(mouseX, mouseY, panel.x + 24, panel.y + 138, 128, 28)) {
            status = "Pruefe Secret...";
            DevModeManager.verify(secretField.getText(), ok -> status = "Dev Mode ist aktiv.", message -> status = message);
            return true;
        }
        if (inside(mouseX, mouseY, panel.x + 160, panel.y + 138, 128, 28)) {
            DevModeManager.disable();
            status = "Dev Mode deaktiviert.";
            return true;
        }
        if (inside(mouseX, mouseY, panel.x + panel.width - 152, panel.y + 138, 128, 28)) {
            MinecraftClient.getInstance().setScreen(new ScreenshotFeedScreen(this));
            return true;
        }
        if (inside(mouseX, mouseY, panel.x + panel.width - 54, panel.y + 18, 30, 30)) {
            close();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
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

    private void drawButton(DrawContext context, int x, int y, int width, int height, String label, int mouseX, int mouseY) {
        ClientTheme theme = ThemeManager.theme();
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        PremiumRender.card(context, x, y, width, height, 0, hovered ? PremiumRender.SHOP_BUTTON_HOVER : PremiumRender.SHOP_BUTTON,
                hovered ? theme.accentColor() : 0x66505C75);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + width / 2, y + height / 2 - 4, theme.textColor());
    }

    private Panel panel() {
        ScreenLayout layout = centeredLayout(520, 230, 320, 210);
        return new Panel(layout.x(), layout.y(), layout.width(), layout.height());
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private record Panel(int x, int y, int width, int height) {
    }
}
