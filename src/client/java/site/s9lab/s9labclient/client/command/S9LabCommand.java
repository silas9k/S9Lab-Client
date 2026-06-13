package site.s9lab.s9labclient.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Locale;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.account.AccountLoginHelper;
import site.s9lab.s9labclient.client.backend.BackendClient;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.emote.EmoteManager;
import site.s9lab.s9labclient.client.emote.EmoteManager.Emote;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.setting.BooleanSetting;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;
import site.s9lab.s9labclient.client.module.setting.Setting;
import site.s9lab.s9labclient.client.screenshot.ScreenshotManager;
import site.s9lab.s9labclient.client.screenshot.ScreenshotMetadata;
import site.s9lab.s9labclient.client.ui.S9LabClientScreen;
import site.s9lab.s9labclient.client.ui.ProfileScreen;
import site.s9lab.s9labclient.client.ui.SafeAccountScreen;
import site.s9lab.s9labclient.client.ui.ScreenshotGalleryScreen;
import site.s9lab.s9labclient.client.ui.premium.PremiumDemoScreen;

public final class S9LabCommand {
    private S9LabCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("s9c")
                .executes(S9LabCommand::help)
                .then(ClientCommandManager.literal("help").executes(S9LabCommand::help))
                .then(ClientCommandManager.literal("menu").executes(S9LabCommand::openMenu))
                .then(ClientCommandManager.literal("ui").executes(S9LabCommand::openPremiumUi))
                .then(ClientCommandManager.literal("screenshots").executes(S9LabCommand::openScreenshots))
                .then(ClientCommandManager.literal("profile")
                        .executes(context -> openProfile(context, "me"))
                        .then(ClientCommandManager.argument("target", StringArgumentType.word())
                                .suggests((context, builder) -> CommandSource.suggestMatching(onlinePlayerNames(), builder))
                                .executes(context -> openProfile(context, StringArgumentType.getString(context, "target")))))
                .then(ClientCommandManager.literal("screenshot")
                        .then(ClientCommandManager.literal("open")
                                .then(ClientCommandManager.argument("file", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(screenshotTargets(), builder))
                                        .executes(context -> screenshotAction(context, "open"))))
                        .then(ClientCommandManager.literal("copy")
                                .then(ClientCommandManager.argument("file", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(screenshotTargets(), builder))
                                        .executes(context -> screenshotAction(context, "copy"))))
                        .then(ClientCommandManager.literal("discord")
                                .then(ClientCommandManager.argument("file", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(screenshotTargets(), builder))
                                        .executes(context -> screenshotAction(context, "discord"))))
                        .then(ClientCommandManager.literal("delete")
                                .then(ClientCommandManager.argument("file", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(screenshotTargets(), builder))
                                        .executes(context -> screenshotAction(context, "delete")))))
                .then(ClientCommandManager.literal("shop").executes(context -> openClientTab(context, S9LabClientScreen.ClientTab.SHOP)))
                .then(ClientCommandManager.literal("cosmetics").executes(context -> openClientTab(context, S9LabClientScreen.ClientTab.COSMETICS)))
                .then(ClientCommandManager.literal("accounts").executes(S9LabCommand::openAccounts))
                .then(ClientCommandManager.literal("login").executes(S9LabCommand::openLogin))
                .then(ClientCommandManager.literal("list").executes(S9LabCommand::listModules))
                .then(ClientCommandManager.literal("discord")
                        .executes(S9LabCommand::discordStatus)
                        .then(ClientCommandManager.literal("on").executes(context -> setDiscordEnabled(context, true)))
                        .then(ClientCommandManager.literal("off").executes(context -> setDiscordEnabled(context, false)))
                        .then(ClientCommandManager.literal("showserver")
                                .then(ClientCommandManager.argument("value", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(java.util.List.of("on", "off", "true", "false"), builder))
                                        .executes(S9LabCommand::setDiscordShowServer))))
                .then(ClientCommandManager.literal("emote")
                        .then(ClientCommandManager.literal("list").executes(S9LabCommand::listEmotes))
                        .then(ClientCommandManager.literal("stop").executes(S9LabCommand::stopEmote))
                        .then(ClientCommandManager.literal("play")
                                .then(ClientCommandManager.argument("id", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(EmoteManager.ids(), builder))
                                        .executes(S9LabCommand::playEmote))))
                .then(ClientCommandManager.literal("cosmetic")
                        .then(ClientCommandManager.literal("equip")
                                .then(ClientCommandManager.argument("type", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(cosmeticTypes(), builder))
                                        .then(ClientCommandManager.argument("id", StringArgumentType.word())
                                                .suggests((context, builder) -> CommandSource.suggestMatching(cosmeticIdsForType(StringArgumentType.getString(context, "type")), builder))
                                                .executes(S9LabCommand::equipCosmetic)))))
                .then(ClientCommandManager.literal("toggle")
                        .then(ClientCommandManager.argument("module", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource.suggestMatching(moduleNames(), builder))
                                .executes(context -> setModule(context, "toggle"))))
                .then(ClientCommandManager.literal("on")
                        .then(ClientCommandManager.argument("module", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource.suggestMatching(moduleNames(), builder))
                                .executes(context -> setModule(context, "on"))))
                .then(ClientCommandManager.literal("off")
                        .then(ClientCommandManager.argument("module", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource.suggestMatching(moduleNames(), builder))
                                .executes(context -> setModule(context, "off"))))
                .then(ClientCommandManager.literal("set")
                        .then(ClientCommandManager.argument("module", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource.suggestMatching(moduleNames(), builder))
                                .then(ClientCommandManager.argument("setting", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(settingNames(StringArgumentType.getString(context, "module")), builder))
                                        .then(ClientCommandManager.argument("value", StringArgumentType.greedyString())
                                                .suggests((context, builder) -> CommandSource.suggestMatching(settingValues(
                                                        StringArgumentType.getString(context, "module"),
                                                        StringArgumentType.getString(context, "setting")
                                                ), builder))
                                                .executes(S9LabCommand::setSetting))))));
    }

    private static int help(CommandContext<FabricClientCommandSource> context) {
        feedback(context, "S9Lab commands: /s9c menu, profile <name>, ui, screenshots, shop, cosmetics, discord, emote play/list/stop, accounts, login, list, toggle/on/off <module>, set <module> <setting> <value>, cosmetic equip <type> <id>");
        return 1;
    }

    private static int openMenu(CommandContext<FabricClientCommandSource> context) {
        S9LabClientClient.openScreenNextTick(() -> new S9LabClientScreen(MinecraftClient.getInstance().currentScreen));
        feedback(context, "Opened S9Lab menu.");
        return 1;
    }

    private static int openPremiumUi(CommandContext<FabricClientCommandSource> context) {
        S9LabClientClient.openScreenNextTick(() -> new PremiumDemoScreen(MinecraftClient.getInstance().currentScreen));
        feedback(context, "Opened S9Lab premium UI demo.");
        return 1;
    }

    private static int openScreenshots(CommandContext<FabricClientCommandSource> context) {
        S9LabClientClient.openScreenNextTick(() -> new ScreenshotGalleryScreen(MinecraftClient.getInstance().currentScreen));
        feedback(context, "Opened screenshot gallery.");
        return 1;
    }

    private static int openProfile(CommandContext<FabricClientCommandSource> context, String target) {
        S9LabClientClient.openScreenNextTick(() -> new ProfileScreen(MinecraftClient.getInstance().currentScreen, target));
        feedback(context, "Opened S9Lab profile.");
        return 1;
    }

    private static int screenshotAction(CommandContext<FabricClientCommandSource> context, String action) {
        String file = StringArgumentType.getString(context, "file");
        ScreenshotMetadata metadata = ScreenshotManager.find(file).orElse(null);
        if (metadata == null) {
            feedback(context, "Screenshot not found: " + file);
            return 0;
        }

        switch (action) {
            case "open" -> {
                ScreenshotManager.openFile(metadata);
                feedback(context, "Opened screenshot.");
            }
            case "copy" -> {
                ScreenshotManager.copyToClipboard(metadata);
                feedback(context, "Copied screenshot path.");
            }
            case "discord" -> {
                ScreenshotManager.shareOnDiscord(metadata);
                feedback(context, "Copied screenshot path and opened Discord.");
            }
            case "delete" -> feedback(context, ScreenshotManager.delete(metadata) ? "Deleted screenshot." : "Delete failed.");
            default -> {
                return 0;
            }
        }
        return 1;
    }

    private static int openClientTab(CommandContext<FabricClientCommandSource> context, S9LabClientScreen.ClientTab tab) {
        S9LabClientClient.openScreenNextTick(() -> new S9LabClientScreen(MinecraftClient.getInstance().currentScreen, tab));
        feedback(context, "Opened S9Lab " + tab.name().toLowerCase(Locale.ROOT) + ".");
        return 1;
    }

    private static int openAccounts(CommandContext<FabricClientCommandSource> context) {
        S9LabClientClient.openScreenNextTick(() -> new SafeAccountScreen(MinecraftClient.getInstance().currentScreen));
        feedback(context, "Opened account screen.");
        return 1;
    }

    private static int openLogin(CommandContext<FabricClientCommandSource> context) {
        AccountLoginHelper.beginMicrosoftLogin();
        feedback(context, "Started Microsoft browser login. Open /s9c accounts to see the status.");
        return 1;
    }

    private static int listModules(CommandContext<FabricClientCommandSource> context) {
        String modules = String.join(", ", S9LabClientClient.getModuleManager().getModules().stream()
                .map(module -> module.getName() + "=" + (module.isEnabled() ? "on" : "off"))
                .toList());
        feedback(context, modules);
        return 1;
    }

    private static int discordStatus(CommandContext<FabricClientCommandSource> context) {
        feedback(context, "Discord RPC enabled=" + S9LabClientClient.getConfigManager().isDiscordRpcEnabled()
                + ", showServer=" + S9LabClientClient.getConfigManager().shouldDiscordRpcShowServer());
        return 1;
    }

    private static int setDiscordEnabled(CommandContext<FabricClientCommandSource> context, boolean enabled) {
        S9LabClientClient.getConfigManager().setDiscordRpcEnabled(enabled);
        S9LabClientClient.getConfigManager().save();
        feedback(context, "Discord RPC is now " + (enabled ? "enabled" : "disabled") + ".");
        return 1;
    }

    private static int setDiscordShowServer(CommandContext<FabricClientCommandSource> context) {
        String value = StringArgumentType.getString(context, "value").toLowerCase(Locale.ROOT);
        if (!value.equals("on") && !value.equals("true") && !value.equals("off") && !value.equals("false")) {
            feedback(context, "Use on/off or true/false.");
            return 0;
        }

        boolean showServer = value.equals("on") || value.equals("true");
        S9LabClientClient.getConfigManager().setDiscordRpcShowServer(showServer);
        S9LabClientClient.getConfigManager().save();
        feedback(context, "Discord RPC showServer is now " + showServer + ".");
        return 1;
    }

    private static int listEmotes(CommandContext<FabricClientCommandSource> context) {
        String emotes = String.join(", ", EmoteManager.all().stream()
                .map(emote -> emote.id() + "=" + emote.displayName())
                .toList());
        feedback(context, emotes);
        return 1;
    }

    private static int playEmote(CommandContext<FabricClientCommandSource> context) {
        String id = StringArgumentType.getString(context, "id");
        Emote emote = EmoteManager.byIdOrName(id);
        if (emote == null) {
            feedback(context, "Unknown emote. Use /s9c emote list.");
            return 0;
        }

        if (!EmoteManager.play(emote)) {
            feedback(context, "You do not own that emote yet. Unlock it in /s9c cosmetics.");
            return 0;
        }
        feedback(context, "Playing " + emote.displayName() + ".");
        return 1;
    }

    private static int stopEmote(CommandContext<FabricClientCommandSource> context) {
        EmoteManager.stop();
        feedback(context, "Stopped emote.");
        return 1;
    }

    private static int setModule(CommandContext<FabricClientCommandSource> context, String action) {
        Module module = getModule(StringArgumentType.getString(context, "module"));
        if (module == null) {
            feedback(context, "Unknown module. Use /s9c list.");
            return 0;
        }

        if ("toggle".equals(action)) {
            module.toggle();
        } else {
            module.setEnabled("on".equals(action));
        }

        S9LabClientClient.getConfigManager().save();
        feedback(context, module.getName() + " is now " + (module.isEnabled() ? "on" : "off") + ".");
        return 1;
    }

    private static int setSetting(CommandContext<FabricClientCommandSource> context) {
        Module module = getModule(StringArgumentType.getString(context, "module"));
        if (module == null) {
            feedback(context, "Unknown module. Use /s9c list.");
            return 0;
        }

        Setting<?> setting = getSetting(module, StringArgumentType.getString(context, "setting"));
        if (setting == null) {
            feedback(context, "Unknown setting for " + module.getName() + ".");
            return 0;
        }

        String value = StringArgumentType.getString(context, "value").trim();
        if (!applySetting(setting, value)) {
            feedback(context, "Invalid value for " + setting.getName() + ".");
            return 0;
        }

        syncSettingToCosmetic(module, setting);
        S9LabClientClient.getConfigManager().save();
        feedback(context, module.getName() + "." + setting.getName() + " = " + setting.getValue());
        return 1;
    }

    private static int equipCosmetic(CommandContext<FabricClientCommandSource> context) {
        String typeInput = StringArgumentType.getString(context, "type");
        String id = StringArgumentType.getString(context, "id");
        CosmeticType type = CosmeticType.byCommandName(typeInput).orElse(null);
        if (type == null) {
            feedback(context, "Unknown cosmetic type. Use cape, bandana, wings, hat, halo, shoulder or glint.");
            return 0;
        }

        if (!S9LabClientClient.getConfigManager().isUnlocked(id)) {
            feedback(context, "You do not own that cosmetic. Buy it in /s9c shop.");
            return 0;
        }

        if (S9LabClientClient.getCosmeticRegistry().get(id).filter(cosmetic -> cosmetic.type() == type).isEmpty()) {
            feedback(context, "That cosmetic does not exist or is not a " + type.commandName() + ".");
            return 0;
        }

        syncModuleSelection(type, id);
        BackendClient.equipCosmetic(type, id);
        feedback(context, "Requested equip for " + id + ". Backend will sync the confirmed state.");
        return 1;
    }

    private static boolean applySetting(Setting<?> setting, String value) {
        if (setting instanceof BooleanSetting booleanSetting) {
            String normalized = value.toLowerCase(Locale.ROOT);
            if (normalized.equals("true") || normalized.equals("on") || normalized.equals("1")) {
                booleanSetting.setValue(true);
                return true;
            }
            if (normalized.equals("false") || normalized.equals("off") || normalized.equals("0")) {
                booleanSetting.setValue(false);
                return true;
            }
            return false;
        }

        if (setting instanceof NumberSetting numberSetting) {
            try {
                numberSetting.setValue(Double.parseDouble(value));
                return true;
            } catch (NumberFormatException exception) {
                return false;
            }
        }

        if (setting instanceof ModeSetting modeSetting) {
            for (String mode : modeSetting.getModes()) {
                if (mode.equalsIgnoreCase(value)) {
                    modeSetting.setValue(mode);
                    return true;
                }
            }
            return false;
        }

        return false;
    }

    private static Module getModule(String name) {
        return S9LabClientClient.getModuleManager().getModule(name).orElse(null);
    }

    private static Setting<?> getSetting(Module module, String name) {
        return module.getSettings().stream()
                .filter(setting -> setting.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private static Iterable<String> moduleNames() {
        return S9LabClientClient.getModuleManager().getModules().stream()
                .map(Module::getName)
                .toList();
    }

    private static Iterable<String> settingNames(String moduleName) {
        Module module = getModule(moduleName);
        if (module == null) {
            return java.util.List.of();
        }
        return module.getSettings().stream().map(Setting::getName).toList();
    }

    private static Iterable<String> settingValues(String moduleName, String settingName) {
        Module module = getModule(moduleName);
        if (module == null) {
            return java.util.List.of();
        }

        Setting<?> setting = getSetting(module, settingName);
        if (setting instanceof BooleanSetting) {
            return java.util.List.of("on", "off", "true", "false");
        }
        if (setting instanceof ModeSetting modeSetting) {
            return modeSetting.getModes();
        }
        if (setting instanceof NumberSetting numberSetting) {
            return java.util.List.of(String.valueOf(numberSetting.getValue()), String.valueOf(numberSetting.getMin()), String.valueOf(numberSetting.getMax()));
        }
        return java.util.List.of();
    }

    private static Iterable<String> cosmeticIds() {
        return S9LabClientClient.getCosmeticRegistry().all().stream()
                .map(Cosmetic::id)
                .toList();
    }

    private static Iterable<String> cosmeticIdsForType(String typeInput) {
        return CosmeticType.byCommandName(typeInput)
                .map(type -> S9LabClientClient.getCosmeticRegistry().byType(type).stream().map(Cosmetic::id).toList())
                .orElse(java.util.List.of());
    }

    private static Iterable<String> cosmeticTypes() {
        return java.util.Arrays.stream(CosmeticType.values())
                .map(CosmeticType::commandName)
                .toList();
    }

    private static Iterable<String> screenshotTargets() {
        java.util.ArrayList<String> targets = new java.util.ArrayList<>();
        targets.add("latest");
        targets.addAll(ScreenshotManager.all().stream().map(ScreenshotMetadata::fileName).toList());
        return targets;
    }

    private static Iterable<String> onlinePlayerNames() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null) {
            return java.util.List.of();
        }
        return client.getNetworkHandler().getPlayerList().stream()
                .map(entry -> entry.getProfile().name())
                .toList();
    }

    private static void syncModuleSelection(CosmeticType type, String id) {
        String moduleName = switch (type) {
            case CAPE -> "Cape";
            case BANDANA -> "Bandana";
            case WINGS -> "Wings";
            case HAT -> "Hat";
            case HALO -> "Halo";
            case SHOULDER -> "Shoulder Buddy";
            case GLINT -> "Glint";
            case EMOTE -> "S9 Emote";
        };
        S9LabClientClient.getModuleManager().getModule(moduleName).ifPresent(module -> {
            module.setEnabled(true);
            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof ModeSetting modeSetting) {
                    modeSetting.setValue(type == CosmeticType.EMOTE ? id.replace("s9lab_emote_", "") : id);
                }
            }
        });
    }

    private static void syncSettingToCosmetic(Module module, Setting<?> setting) {
        if (!(setting instanceof ModeSetting modeSetting)) {
            return;
        }

        CosmeticType type = cosmeticTypeForModule(module.getName());
        if (type == null) {
            return;
        }

        String id = type == CosmeticType.EMOTE ? "s9lab_emote_" + modeSetting.getValue() : modeSetting.getValue();
        S9LabClientClient.getCosmeticRegistry().get(id)
                .filter(cosmetic -> cosmetic.type() == type)
                .filter(cosmetic -> S9LabClientClient.getConfigManager().isUnlocked(cosmetic.id()))
                .ifPresent(cosmetic -> {
                    BackendClient.equipCosmetic(type, cosmetic.id());
                });
    }

    private static CosmeticType cosmeticTypeForModule(String moduleName) {
        return switch (moduleName.toLowerCase(Locale.ROOT)) {
            case "cape" -> CosmeticType.CAPE;
            case "bandana" -> CosmeticType.BANDANA;
            case "wings" -> CosmeticType.WINGS;
            case "hat" -> CosmeticType.HAT;
            case "halo" -> CosmeticType.HALO;
            case "shoulder buddy" -> CosmeticType.SHOULDER;
            case "glint" -> CosmeticType.GLINT;
            case "s9 emote" -> CosmeticType.EMOTE;
            default -> null;
        };
    }

    private static void feedback(CommandContext<FabricClientCommandSource> context, String message) {
        context.getSource().sendFeedback(Text.literal("[S9Lab] " + message));
    }
}
