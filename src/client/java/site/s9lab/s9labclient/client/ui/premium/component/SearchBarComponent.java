package site.s9lab.s9labclient.client.ui.premium.component;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.animation.AnimationManager;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;

public class SearchBarComponent extends Component {
    private static final int MAX_LENGTH = 48;
    private final Supplier<String> getter;
    private final Consumer<String> setter;
    private boolean focused;

    public SearchBarComponent(String id, Supplier<String> getter, Consumer<String> setter) {
        super(id, DEFAULT_HEIGHT);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks, ClientTheme theme, AnimationManager animations) {
        MinecraftClient client = MinecraftClient.getInstance();
        float active = animations.animate(id + ".active", focused || contains(mouseX, mouseY), deltaTicks);
        int border = ClientTheme.mix(0xAA7A7F88, ClientTheme.withAlpha(theme.accentColor(), 220), active);
        PremiumRender.shopInput(context, x, y, width, height, focused || contains(mouseX, mouseY), border);

        String value = getter.get();
        String text = value.isBlank() && !focused ? "Search..." : value + (focused && System.currentTimeMillis() / 450L % 2L == 0L ? "_" : "");
        int color = value.isBlank() && !focused ? theme.mutedTextColor() : theme.textColor();
        context.drawTextWithShadow(client.textRenderer, Text.literal(text), x + LABEL_PADDING, y + (height - client.textRenderer.fontHeight) / 2, color);
    }

    @Override
    public boolean mouseClicked(Click click) {
        focused = contains(click.x(), click.y());
        return focused;
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!focused || !input.isValidChar()) {
            return false;
        }
        String value = getter.get();
        if (value.length() < MAX_LENGTH) {
            setter.accept(value + input.asString());
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (!focused) {
            return false;
        }
        if (input.isEscape() || input.getKeycode() == GLFW.GLFW_KEY_ENTER) {
            focused = false;
            return true;
        }
        if (input.getKeycode() == GLFW.GLFW_KEY_BACKSPACE) {
            String value = getter.get();
            if (!value.isEmpty()) {
                setter.accept(value.substring(0, value.length() - 1));
            }
            return true;
        }
        return false;
    }
}
