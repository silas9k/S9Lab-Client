package site.s9lab.s9labclient.client;

import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.function.Supplier;
import site.s9lab.s9labclient.S9LabClient;
import site.s9lab.s9labclient.client.backend.BackendClient;
import site.s9lab.s9labclient.client.command.S9LabCommand;
import site.s9lab.s9labclient.client.config.ConfigManager;
import site.s9lab.s9labclient.client.cosmetics.CosmeticRegistry;
import site.s9lab.s9labclient.client.discord.DiscordRPCManager;
import site.s9lab.s9labclient.client.emote.EmoteManager;
import site.s9lab.s9labclient.client.hud.HudRenderer;
import site.s9lab.s9labclient.client.module.ModuleManager;
import site.s9lab.s9labclient.client.resource.S9BuiltinResourcePacks;
import site.s9lab.s9labclient.client.screenshot.ScreenshotManager;
import site.s9lab.s9labclient.client.ui.EmoteWheelScreen;
import site.s9lab.s9labclient.client.ui.S9LabClientScreen;
import site.s9lab.s9labclient.client.ui.SafeAccountScreen;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;
import site.s9lab.s9labclient.client.zoom.ZoomController;

public class S9LabClientClient implements ClientModInitializer {
    private static ModuleManager moduleManager;
    private static ConfigManager configManager;
    private static CosmeticRegistry cosmeticRegistry;
    private static KeyBinding openMenuKey;
    private static KeyBinding openEmoteWheelKey;
    private static KeyBinding zoomKey;
    private static Supplier<Screen> queuedScreen;

    @Override
    public void onInitializeClient() {
        S9BuiltinResourcePacks.register();
        moduleManager = new ModuleManager();
        cosmeticRegistry = new CosmeticRegistry();
        configManager = new ConfigManager(moduleManager, cosmeticRegistry);

        cosmeticRegistry.registerDefaults();
        moduleManager.registerDefaults(cosmeticRegistry);
        configManager.load();
        ScreenshotManager.init(MinecraftClient.getInstance());
        ThemeManager.loadFromConfig();
        HudRenderer.register(moduleManager);
        registerKeybinds();
        registerScreenButtons();
        S9LabCommand.register();
        DiscordRPCManager.start();
        BackendClient.start();

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            BackendClient.stop();
            DiscordRPCManager.stop();
            configManager.save();
        });
    }

    private static void registerKeybinds() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(S9LabClient.MOD_ID, "client"));
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.s9labclient.open_menu",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_RIGHT_SHIFT,
                category
        ));
        openEmoteWheelKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.s9labclient.open_emote_wheel",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_B,
                category
        ));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.s9labclient.zoom",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_C,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            EmoteManager.tick(client);
            ZoomController.setZooming(zoomKey.isPressed() && client.currentScreen == null);
            while (openMenuKey.wasPressed()) {
                MinecraftClient minecraftClient = MinecraftClient.getInstance();
                if (minecraftClient.currentScreen instanceof S9LabClientScreen) {
                    minecraftClient.setScreen(null);
                } else {
                    minecraftClient.setScreen(new S9LabClientScreen(minecraftClient.currentScreen));
                }
            }
            while (openEmoteWheelKey.wasPressed()) {
                MinecraftClient minecraftClient = MinecraftClient.getInstance();
                if (minecraftClient.currentScreen instanceof EmoteWheelScreen) {
                    minecraftClient.setScreen(null);
                } else if (minecraftClient.currentScreen == null || minecraftClient.currentScreen instanceof S9LabClientScreen) {
                    minecraftClient.setScreen(new EmoteWheelScreen(minecraftClient.currentScreen));
                }
            }
            if (queuedScreen != null) {
                Supplier<Screen> screenSupplier = queuedScreen;
                queuedScreen = null;
                client.setScreen(screenSupplier.get());
            }
        });
    }

    private static void registerScreenButtons() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                configureTitleScreenButtons(client, screen, scaledWidth, scaledHeight);
            }

            if (screen instanceof MultiplayerScreen) {
                Screens.getButtons(screen).add(ButtonWidget.builder(Text.literal("Accounts"), button ->
                        client.setScreen(new SafeAccountScreen(screen))
                ).dimensions(16, 16, 112, 24).build());
            }
        });
    }

    private static void configureTitleScreenButtons(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
        Screens.getButtons(screen).removeIf(widget -> "Mods".equals(widget.getMessage().getString()));
        int width = Math.min(98, Math.max(72, scaledWidth / 5));
        int x = scaledWidth / 2 + 104;
        if (x + width > scaledWidth - 8) {
            x = scaledWidth / 2 - width / 2;
        }
        int y = scaledHeight / 4 + 72;
        if (scaledHeight < 260) {
            y = Math.min(scaledHeight - 58, scaledHeight / 4 + 96);
        }
        Screens.getButtons(screen).add(ButtonWidget.builder(Text.literal("Mods"), button ->
                client.setScreen(ModMenuApi.createModsScreen(screen))
        ).dimensions(x, y, width, 20).build());
    }

    public static ModuleManager getModuleManager() {
        return moduleManager;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static CosmeticRegistry getCosmeticRegistry() {
        return cosmeticRegistry;
    }

    public static void openScreenNextTick(Supplier<Screen> screenSupplier) {
        queuedScreen = screenSupplier;
    }
}
