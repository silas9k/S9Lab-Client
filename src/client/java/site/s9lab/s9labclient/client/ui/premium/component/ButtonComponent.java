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
        int color = ClientTheme.mix(0xFF17191D, 0xFF25282E, hover);
        int border = ClientTheme.mix(0xFF3C414B, ClientTheme.withAlpha(theme.accentColor(), 230), hover);
        context.fill(x + 1, y + 1, x + width + 1, y + height + 1, 0x66000000);
        context.fill(x, y, x + width, y + height, color);
        PremiumRender.outline(context, x, y, width, height, 0, border);
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
