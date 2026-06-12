package site.s9lab.s9labclient.client.ui.premium.component;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.animation.AnimationManager;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;

public class ButtonComponent extends Component {
    private final Text label;
    private final Runnable action;

    public ButtonComponent(String id, Text label, Runnable action) {
        super(id, DEFAULT_HEIGHT);
        this.label = label;
        this.action = action;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks, ClientTheme theme, AnimationManager animations) {
        boolean hovered = contains(mouseX, mouseY);
        float hover = animations.animate(id + ".hover", hovered, deltaTicks);
        int color = ClientTheme.mix(theme.cardColor(), theme.cardHoverColor(), hover);
        int border = ClientTheme.mix(theme.borderColor(), ClientTheme.withAlpha(theme.accentColor(), 170), hover);
        PremiumRender.card(context, x, y, width, height, theme.radius(), color, border);
        PremiumRender.centeredText(context, label, x + width / 2, y + (height - MinecraftClient.getInstance().textRenderer.fontHeight) / 2, theme.textColor());
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
