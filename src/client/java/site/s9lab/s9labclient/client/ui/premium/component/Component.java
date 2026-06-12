package site.s9lab.s9labclient.client.ui.premium.component;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.animation.AnimationManager;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;

public abstract class Component {
    protected static final int DEFAULT_HEIGHT = 26;
    protected static final int LABEL_PADDING = 9;

    protected final String id;
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected boolean visible = true;

    protected Component(String id, int height) {
        this.id = id;
        this.height = height;
    }

    public void bounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(DrawContext context, int mouseX, int mouseY, float deltaTicks, ClientTheme theme, AnimationManager animations);

    public boolean mouseClicked(Click click) {
        return false;
    }

    public boolean mouseReleased(Click click) {
        return false;
    }

    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        return false;
    }

    public boolean charTyped(CharInput input) {
        return false;
    }

    public boolean keyPressed(KeyInput input) {
        return false;
    }

    public boolean contains(double mouseX, double mouseY) {
        return visible && PremiumRender.inside(mouseX, mouseY, x, y, width, height);
    }

    public int preferredHeight() {
        return height;
    }

    public boolean visible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
