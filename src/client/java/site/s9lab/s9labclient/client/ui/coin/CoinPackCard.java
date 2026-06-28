package site.s9lab.s9labclient.client.ui.coin;

import java.util.Locale;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.ui.TextLayout;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.animation.AnimationManager;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;

/** Reusable RSHIFT-themed product card with cached, time-based hover animation. */
public final class CoinPackCard {
    private static final int INNER_PADDING = 10;
    private static final int PRICE_BUTTON_HEIGHT = 27;
    private static final int PRICE_BUTTON_MARGIN = 9;
    private final CoinPack pack;
    private int x;
    private int y;
    private int width;
    private int height;

    public CoinPackCard(CoinPack pack) {
        this.pack = pack;
    }

    public CoinPack pack() { return pack; }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float deltaTicks,
                       boolean focused, ClientTheme theme, AnimationManager animations) {
        boolean hovered = contains(mouseX, mouseY);
        float hover = animations.animate("coin-pack-" + pack.baseCoins(), hovered || focused, deltaTicks);
        int accent = pack.accentColor();
        int fill = ClientTheme.mix(PremiumRender.SHOP_CARD, PremiumRender.SHOP_CARD_HOVER, hover);
        int border = focused ? accent : ClientTheme.mix(PremiumRender.SHOP_SOFT_BORDER, ClientTheme.withAlpha(accent, 220), hover);
        PremiumRender.card(context, x, y, width, height, 0, fill, border);
        if (hover > 0.02F) {
            context.fill(x + 1, y + 1, x + 3, y + height - 1, ClientTheme.withAlpha(accent, Math.round(90 + hover * 150)));
            context.fill(x + 3, y + 1, x + width - 1, y + 2, ClientTheme.withAlpha(accent, Math.round(40 + hover * 100)));
        }

        String amount = format(pack.baseCoins()) + " COINS";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(amount), x + width / 2, y + 10, 0xFFFFFFFF);
        if (pack.bonusCoins() > 0) {
            String bonus = "BONUS! +" + format(pack.bonusCoins());
            int bonusW = Math.min(width - 18, textRenderer.getWidth(bonus) + 12);
            int bonusX = x + (width - bonusW) / 2;
            context.fill(bonusX, y + 26, bonusX + bonusW, y + 42, ClientTheme.withAlpha(accent, 100));
            PremiumRender.outline(context, bonusX, y + 26, bonusW, 16, 0, ClientTheme.withAlpha(accent, 210));
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(bonus), x + width / 2, y + 30, 0xFFFFFFFF);
        }

        int buttonY = priceButtonY();
        int mediaTop = y + (pack.bonusCoins() > 0 ? 47 : 29);
        int mediaHeight = Math.max(20, buttonY - mediaTop - 7);
        int baseSize = Math.max(20, Math.min(width - INNER_PADDING * 2, mediaHeight));
        int imageSize = Math.min(Math.min(width - 8, mediaHeight + 4), Math.round(baseSize * (1.0F + hover * 0.035F)));
        int imageX = x + (width - imageSize) / 2;
        int imageY = mediaTop + (mediaHeight - imageSize) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, pack.texture(), imageX, imageY, 0.0F, 0.0F,
                imageSize, imageSize, imageSize, imageSize);

        boolean priceHovered = priceContains(mouseX, mouseY);
        int buttonFill = priceHovered ? ClientTheme.withAlpha(accent, 235) : ClientTheme.withAlpha(accent, 185);
        PremiumRender.card(context, x + PRICE_BUTTON_MARGIN, buttonY, width - PRICE_BUTTON_MARGIN * 2,
                PRICE_BUTTON_HEIGHT, 0, buttonFill, accent);
        String price = TextLayout.ellipsize(textRenderer, pack.price(), width - PRICE_BUTTON_MARGIN * 2 - 10);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(price), x + width / 2,
                buttonY + (PRICE_BUTTON_HEIGHT - textRenderer.fontHeight) / 2, 0xFFFFFFFF);
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean priceContains(double mouseX, double mouseY) {
        int buttonY = priceButtonY();
        return mouseX >= x + PRICE_BUTTON_MARGIN && mouseX <= x + width - PRICE_BUTTON_MARGIN
                && mouseY >= buttonY && mouseY <= buttonY + PRICE_BUTTON_HEIGHT;
    }

    private int priceButtonY() { return y + height - PRICE_BUTTON_HEIGHT - PRICE_BUTTON_MARGIN; }
    private static String format(int value) { return String.format(Locale.GERMANY, "%,d", value); }
}
