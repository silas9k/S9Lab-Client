package site.s9lab.s9labclient.client.module.impl.hud;

import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import site.s9lab.s9labclient.client.module.HudModule;
import site.s9lab.s9labclient.client.util.ColorUtil;
import site.s9lab.s9labclient.client.util.RenderUtil;

public class CoordinatesModule extends HudModule {
    public CoordinatesModule() {
        super("Coordinates", "Shows your current X, Y and Z position.", true);
    }

    @Override
    protected int defaultY() {
        return 18;
    }

    @Override
    public void render(DrawContext context, MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        String text = String.format(Locale.ROOT, "XYZ: %.1f / %.1f / %.1f", client.player.getX(), client.player.getY(), client.player.getZ());
        drawBackground(context, client);
        RenderUtil.drawText(context, client, text, getX(), getY(), ColorUtil.WHITE);
    }

    @Override
    public int getWidth(MinecraftClient client) {
        if (client.player == null) {
            return client.textRenderer.getWidth("XYZ: -- / -- / --");
        }

        return client.textRenderer.getWidth(String.format(Locale.ROOT, "XYZ: %.1f / %.1f / %.1f", client.player.getX(), client.player.getY(), client.player.getZ()));
    }

    @Override
    public int getHeight(MinecraftClient client) {
        return 9;
    }
}
