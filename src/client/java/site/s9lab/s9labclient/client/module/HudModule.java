package site.s9lab.s9labclient.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import site.s9lab.s9labclient.client.module.setting.BooleanSetting;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;

public abstract class HudModule extends Module {
    private final NumberSetting x;
    private final NumberSetting y;
    private final BooleanSetting background;

    protected HudModule(String name, String description, boolean enabled) {
        super(name, description, ModuleCategory.HUD, enabled);
        this.x = addSetting(new NumberSetting("X", defaultX(), 0, 2000, 1));
        this.y = addSetting(new NumberSetting("Y", defaultY(), 0, 1200, 1));
        this.background = addSetting(new BooleanSetting("Background", true));
    }

    public abstract void render(DrawContext context, MinecraftClient client);

    public abstract int getWidth(MinecraftClient client);

    public abstract int getHeight(MinecraftClient client);

    protected int defaultX() {
        return 6;
    }

    protected int defaultY() {
        return 6;
    }

    public int getX() {
        return x.getValue().intValue();
    }

    public int getY() {
        return y.getValue().intValue();
    }

    public void setPosition(int x, int y) {
        this.x.setValue((double) Math.max(0, x));
        this.y.setValue((double) Math.max(0, y));
    }

    public boolean hasBackground() {
        return background.getValue();
    }

    protected void drawBackground(DrawContext context, MinecraftClient client) {
        if (!hasBackground()) {
            return;
        }

        int x = getX();
        int y = getY();
        context.fill(x - 3, y - 3, x + getWidth(client) + 3, y + getHeight(client) + 3, 0xAA101722);
    }
}
