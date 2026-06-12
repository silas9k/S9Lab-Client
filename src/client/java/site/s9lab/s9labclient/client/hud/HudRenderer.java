package site.s9lab.s9labclient.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.module.HudModule;
import site.s9lab.s9labclient.client.module.ModuleManager;
import site.s9lab.s9labclient.client.notification.S9ToastManager;

public final class HudRenderer {
    private HudRenderer() {
    }

    public static void register(ModuleManager moduleManager) {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of(S9LabClient.MOD_ID, "hud_modules"),
                (context, tickCounter) -> render(context, tickCounter, moduleManager)
        );
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter, ModuleManager moduleManager) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden) {
            return;
        }

        for (HudModule module : moduleManager.getHudModules()) {
            if (module.isEnabled()) {
                module.render(context, client);
            }
        }
        S9ToastManager.render(context);
    }
}
