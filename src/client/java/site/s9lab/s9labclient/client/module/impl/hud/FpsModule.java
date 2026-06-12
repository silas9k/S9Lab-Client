package site.s9lab.s9labclient.client.module.impl.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import site.s9lab.s9labclient.client.module.HudModule;
import site.s9lab.s9labclient.client.util.ColorUtil;
import site.s9lab.s9labclient.client.util.RenderUtil;

public class FpsModule extends HudModule {
    public FpsModule() {
        super("FPS", "Shows the current client FPS.", true);
    }

    @Override
    public void render(DrawContext context, MinecraftClient client) {
        drawBackground(context, client);
        RenderUtil.drawText(context, client, "FPS: " + client.getCurrentFps(), getX(), getY(), ColorUtil.WHITE);
    }

    @Override
    public int getWidth(MinecraftClient client) {
        return client.textRenderer.getWidth("FPS: " + client.getCurrentFps());
    }

    @Override
    public int getHeight(MinecraftClient client) {
        return 9;
    }
}
