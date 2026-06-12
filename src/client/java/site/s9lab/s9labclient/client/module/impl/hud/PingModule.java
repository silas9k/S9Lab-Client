package site.s9lab.s9labclient.client.module.impl.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import site.s9lab.s9labclient.client.module.HudModule;
import site.s9lab.s9labclient.client.util.ColorUtil;
import site.s9lab.s9labclient.client.util.RenderUtil;

public class PingModule extends HudModule {
    public PingModule() {
        super("Ping", "Shows your current server latency.", true);
    }

    @Override
    protected int defaultY() {
        return 30;
    }

    @Override
    public void render(DrawContext context, MinecraftClient client) {
        String text = getText(client);
        drawBackground(context, client);
        RenderUtil.drawText(context, client, text, getX(), getY(), ColorUtil.WHITE);
    }

    @Override
    public int getWidth(MinecraftClient client) {
        return client.textRenderer.getWidth(getText(client));
    }

    @Override
    public int getHeight(MinecraftClient client) {
        return 9;
    }

    private String getText(MinecraftClient client) {
        if (client.player != null && client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (entry != null) {
                return "Ping: " + entry.getLatency() + " ms";
            }
        }

        return "Ping: -- ms";
    }
}
