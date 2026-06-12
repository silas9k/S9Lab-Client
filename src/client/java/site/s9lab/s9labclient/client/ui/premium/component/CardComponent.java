package site.s9lab.s9labclient.client.ui.premium.component;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.animation.AnimationManager;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;

public class CardComponent extends Component {
    private static final int CARD_HEIGHT = 70;
    private final Text title;
    private final Text description;
    private final Runnable action;

    public CardComponent(String id, Text title, Text description, Runnable action) {
        super(id, CARD_HEIGHT);
        this.title = title;
        this.description = description;
        this.action = action;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks, ClientTheme theme, AnimationManager animations) {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean hovered = contains(mouseX, mouseY);
        float hover = animations.animate(id + ".hover", hovered, deltaTicks);
        int color = ClientTheme.mix(theme.cardColor(), theme.cardHoverColor(), hover);
        int border = ClientTheme.mix(theme.borderColor(), ClientTheme.withAlpha(theme.accentColor(), 160), hover);
        PremiumRender.card(context, x, y, width, height, theme.radius(), color, border);
        PremiumRender.roundedRect(context, x + 9, y + 11, 34, 34, theme.radius(), ClientTheme.withAlpha(theme.accentColor(), 80));
        context.drawTextWithShadow(client.textRenderer, Text.literal("*"), x + 23, y + 22, theme.textColor());
        context.drawTextWithShadow(client.textRenderer, title, x + 54, y + 13, theme.textColor());
        context.drawTextWithShadow(client.textRenderer, description, x + 54, y + 31, theme.mutedTextColor());
        if (hovered) {
            context.drawTextWithShadow(client.textRenderer, Text.literal("Open"), x + width - 36, y + height - 18, theme.accentColor());
        }
    }

    @Override
    public boolean mouseClicked(Click click) {
        if (!contains(click.x(), click.y())) {
            return false;
        }
        if (action != null) {
            action.run();
        }
        return true;
    }
}
