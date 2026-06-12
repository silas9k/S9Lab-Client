package site.s9lab.s9labclient.client.module.impl.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import site.s9lab.s9labclient.client.module.HudModule;
import site.s9lab.s9labclient.client.util.RenderUtil;

public class KeystrokesModule extends HudModule {
    private static final int KEY = 22;
    private static final int GAP = 3;

    public KeystrokesModule() {
        super("Keystrokes", "Shows movement and mouse input on the HUD.", false);
    }

    @Override
    protected int defaultY() {
        return 48;
    }

    @Override
    public void render(DrawContext context, MinecraftClient client) {
        int x = getX();
        int y = getY();
        drawBackground(context, client);
        RenderUtil.drawKey(context, client, "W", x + KEY + GAP, y, KEY, KEY, client.options.forwardKey.isPressed());
        RenderUtil.drawKey(context, client, "A", x, y + KEY + GAP, KEY, KEY, client.options.leftKey.isPressed());
        RenderUtil.drawKey(context, client, "S", x + KEY + GAP, y + KEY + GAP, KEY, KEY, client.options.backKey.isPressed());
        RenderUtil.drawKey(context, client, "D", x + (KEY + GAP) * 2, y + KEY + GAP, KEY, KEY, client.options.rightKey.isPressed());
        RenderUtil.drawKey(context, client, "SPACE", x, y + (KEY + GAP) * 2, KEY * 3 + GAP * 2, 14, client.options.jumpKey.isPressed());
        RenderUtil.drawKey(context, client, "LMB", x, y + (KEY + GAP) * 2 + 17, 34, 16, client.options.attackKey.isPressed());
        RenderUtil.drawKey(context, client, "RMB", x + 37, y + (KEY + GAP) * 2 + 17, 34, 16, client.options.useKey.isPressed());
    }

    @Override
    public int getWidth(MinecraftClient client) {
        return KEY * 3 + GAP * 2;
    }

    @Override
    public int getHeight(MinecraftClient client) {
        return (KEY + GAP) * 2 + 33;
    }
}
