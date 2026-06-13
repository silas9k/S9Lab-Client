package site.s9lab.s9labclient.client.ui.premium.component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.animation.AnimationManager;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;

public class DropdownComponent extends Component {
    private static final int OPTION_HEIGHT = 22;
    private static final int ARROW_PADDING = 18;

    private final Text label;
    private final List<String> options;
    private final Supplier<String> getter;
    private final Consumer<String> setter;
    private boolean open;

    public DropdownComponent(String id, Text label, List<String> options, Supplier<String> getter, Consumer<String> setter) {
        super(id, DEFAULT_HEIGHT);
        this.label = label;
        this.options = options;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks, ClientTheme theme, AnimationManager animations) {
        MinecraftClient client = MinecraftClient.getInstance();
        float hover = animations.animate(id + ".hover", contains(mouseX, mouseY), deltaTicks);
        PremiumRender.card(context, x, y, width, height, 0,
                ClientTheme.mix(PremiumRender.SHOP_CARD, PremiumRender.SHOP_CARD_HOVER, hover),
                open ? ClientTheme.withAlpha(theme.accentColor(), 190) : PremiumRender.SHOP_SOFT_BORDER);
        context.drawTextWithShadow(client.textRenderer, label, x + LABEL_PADDING, y + (height - client.textRenderer.fontHeight) / 2, theme.textColor());

        String selected = getter.get();
        int textX = x + width - LABEL_PADDING - ARROW_PADDING - client.textRenderer.getWidth(selected);
        context.drawTextWithShadow(client.textRenderer, Text.literal(selected), textX, y + (height - client.textRenderer.fontHeight) / 2, theme.mutedTextColor());
        context.drawTextWithShadow(client.textRenderer, Text.literal(open ? "^" : "v"), x + width - LABEL_PADDING - 7, y + 8, theme.accentColor());

        if (open) {
            renderOptions(context, mouseX, mouseY, theme);
        }
    }

    @Override
    public boolean mouseClicked(Click click) {
        if (open) {
            for (int i = 0; i < options.size(); i++) {
                int oy = y + height + 4 + i * OPTION_HEIGHT;
                if (PremiumRender.inside(click.x(), click.y(), x, oy, width, OPTION_HEIGHT)) {
                    setter.accept(options.get(i));
                    open = false;
                    return true;
                }
            }
        }
        if (contains(click.x(), click.y())) {
            open = !open;
            return true;
        }
        open = false;
        return false;
    }

    @Override
    public int preferredHeight() {
        return open ? height + 4 + options.size() * OPTION_HEIGHT : height;
    }

    private void renderOptions(DrawContext context, int mouseX, int mouseY, ClientTheme theme) {
        MinecraftClient client = MinecraftClient.getInstance();
        int panelY = y + height + 4;
        PremiumRender.card(context, x, panelY, width, options.size() * OPTION_HEIGHT, 0, PremiumRender.SHOP_HEADER, PremiumRender.SHOP_SOFT_BORDER);
        for (int i = 0; i < options.size(); i++) {
            int oy = panelY + i * OPTION_HEIGHT;
            boolean hovered = PremiumRender.inside(mouseX, mouseY, x, oy, width, OPTION_HEIGHT);
            if (hovered) {
                PremiumRender.roundedRect(context, x + 3, oy + 2, width - 6, OPTION_HEIGHT - 4, 0, ClientTheme.withAlpha(theme.accentColor(), 70));
            }
            context.drawTextWithShadow(client.textRenderer, Text.literal(options.get(i)), x + LABEL_PADDING, oy + 7, theme.textColor());
        }
    }
}
