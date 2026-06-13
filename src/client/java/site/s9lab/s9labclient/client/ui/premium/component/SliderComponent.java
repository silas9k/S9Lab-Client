package site.s9lab.s9labclient.client.ui.premium.component;

import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.animation.AnimationManager;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;

public class SliderComponent extends Component {
    private static final int TRACK_HEIGHT = 4;
    private static final int KNOB_SIZE = 10;

    private final Text label;
    private final double min;
    private final double max;
    private final DoubleSupplier getter;
    private final Consumer<Double> setter;
    private boolean dragging;

    public SliderComponent(String id, Text label, double min, double max, DoubleSupplier getter, Consumer<Double> setter) {
        super(id, 34);
        this.label = label;
        this.min = min;
        this.max = max;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks, ClientTheme theme, AnimationManager animations) {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean hovered = contains(mouseX, mouseY);
        float hover = animations.animate(id + ".hover", hovered || dragging, deltaTicks);
        PremiumRender.card(context, x, y, width, height, 0,
                ClientTheme.mix(PremiumRender.SHOP_CARD, PremiumRender.SHOP_CARD_HOVER, hover),
                hovered || dragging ? ClientTheme.withAlpha(theme.accentColor(), 180) : PremiumRender.SHOP_SOFT_BORDER);

        double value = clamp(getter.getAsDouble());
        String valueText = String.format("%.0f", value);
        context.drawTextWithShadow(client.textRenderer, label, x + LABEL_PADDING, y + 7, theme.textColor());
        context.drawTextWithShadow(client.textRenderer, Text.literal(valueText), x + width - LABEL_PADDING - client.textRenderer.getWidth(valueText), y + 7, theme.mutedTextColor());

        int tx = x + LABEL_PADDING;
        int ty = y + 24;
        int tw = width - LABEL_PADDING * 2;
        float progress = (float) ((value - min) / (max - min));
        PremiumRender.roundedRect(context, tx, ty, tw, TRACK_HEIGHT, 0, 0xFF2C3142);
        PremiumRender.roundedRect(context, tx, ty, Math.round(tw * progress), TRACK_HEIGHT, 0, theme.accentColor());
        int knobX = tx + Math.round(tw * progress) - KNOB_SIZE / 2;
        PremiumRender.roundedRect(context, knobX, ty - 3, KNOB_SIZE, KNOB_SIZE, 0, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(Click click) {
        if (!contains(click.x(), click.y())) {
            return false;
        }
        dragging = true;
        updateValue(click.x());
        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (!dragging) {
            return false;
        }
        updateValue(click.x());
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (!dragging) {
            return false;
        }
        dragging = false;
        return true;
    }

    private void updateValue(double mouseX) {
        int tx = x + LABEL_PADDING;
        int tw = width - LABEL_PADDING * 2;
        double progress = Math.max(0.0D, Math.min(1.0D, (mouseX - tx) / tw));
        setter.accept(clamp(min + (max - min) * progress));
    }

    private double clamp(double value) {
        return Math.max(min, Math.min(max, value));
    }
}
