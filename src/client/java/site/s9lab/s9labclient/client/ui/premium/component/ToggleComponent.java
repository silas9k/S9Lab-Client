package site.s9lab.s9labclient.client.ui.premium.component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.animation.AnimationManager;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;

public class ToggleComponent extends Component {
    private static final int SWITCH_WIDTH = 42;
    private static final int SWITCH_HEIGHT = 18;
    private static final int KNOB_SIZE = 14;

    private final Text label;
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;

    public ToggleComponent(String id, Text label, BooleanSupplier getter, Consumer<Boolean> setter) {
        super(id, DEFAULT_HEIGHT);
        this.label = label;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks, ClientTheme theme, AnimationManager animations) {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean hovered = contains(mouseX, mouseY);
        float hover = animations.animate(id + ".hover", hovered, deltaTicks);
        float enabled = animations.animate(id + ".enabled", getter.getAsBoolean(), deltaTicks);
        PremiumRender.card(context, x, y, width, height, 0,
                ClientTheme.mix(PremiumRender.SHOP_CARD, PremiumRender.SHOP_CARD_HOVER, hover),
                hovered ? ClientTheme.withAlpha(theme.accentColor(), 180) : PremiumRender.SHOP_SOFT_BORDER);
        context.drawTextWithShadow(client.textRenderer, label, x + LABEL_PADDING, y + (height - client.textRenderer.fontHeight) / 2, theme.textColor());

        int sx = x + width - SWITCH_WIDTH - LABEL_PADDING;
        int sy = y + (height - SWITCH_HEIGHT) / 2;
        int track = ClientTheme.mix(0xFF333746, theme.accentColor(), enabled);
        PremiumRender.roundedRect(context, sx, sy, SWITCH_WIDTH, SWITCH_HEIGHT, 0, track);
        int knobX = sx + 2 + Math.round((SWITCH_WIDTH - KNOB_SIZE - 4) * enabled);
        PremiumRender.roundedRect(context, knobX, sy + 2, KNOB_SIZE, KNOB_SIZE, 0, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(Click click) {
        if (!contains(click.x(), click.y())) {
            return false;
        }
        setter.accept(!getter.getAsBoolean());
        return true;
    }
}
