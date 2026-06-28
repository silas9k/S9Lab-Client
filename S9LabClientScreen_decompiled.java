// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package site.s9lab.s9labclient.client.ui;

import java.awt.Color;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.backend.BackendClient;
import site.s9lab.s9labclient.client.backend.BackendState;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticIdAliases;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext.BaseVisibility;
import site.s9lab.s9labclient.client.cosmetics.preview.CosmeticPreviewRenderer;
import site.s9lab.s9labclient.client.cosmetics.preview.PreviewPose;
import site.s9lab.s9labclient.client.module.HudModule;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;
import site.s9lab.s9labclient.client.module.impl.utility.TablistBadgeModule;
import site.s9lab.s9labclient.client.module.setting.BooleanSetting;
import site.s9lab.s9labclient.client.module.setting.ColorSetting;
import site.s9lab.s9labclient.client.module.setting.KeybindSetting;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;
import site.s9lab.s9labclient.client.module.setting.NumberSetting;
import site.s9lab.s9labclient.client.module.setting.Setting;
import site.s9lab.s9labclient.client.notification.S9ToastManager;
import site.s9lab.s9labclient.client.performance.ModDetectionManager;
import site.s9lab.s9labclient.client.performance.PerformanceManager;
import site.s9lab.s9labclient.client.performance.PerformanceManager.PerformancePreset;
import site.s9lab.s9labclient.client.ui.coin.CoinPack;
import site.s9lab.s9labclient.client.ui.coin.CoinPackCard;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.animation.AnimationManager;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;
import site.s9lab.s9labclient.client.util.S9TextEffects;

@Environment(EnvType.CLIENT)
public class S9LabClientScreen extends ResponsiveScreen {
    private static final class_2960 CLIENT_ICON = class_2960.method_60655("s9labclient", "textures/font/s9_icon.png");
    private static final Map<class_2960, Boolean> MODULE_ICON_CACHE = new HashMap();
    private static final int BG = -268105971;
    private static final int PANEL = -653652716;
    private static final int PANEL_2 = -1240460516;
    private static final int CARD = -1189865438;
    private static final int CARD_HOVER = -652599248;
    private static final int CARD_ACTIVE = -635236037;
    private static final int LINE = 1714172740;
    private static final int LINE_SOFT = 859325013;
    private static final int WHITE = -1;
    private static final int TEXT = -1578254;
    private static final int MUTED = -6643278;
    private static final int DIM = -9932669;
    private static final int GREEN = -11931025;
    private static final int WARN = -19372;
    private static final String PLUS_PLAN_1M = "plus_1m";
    private static final String PLUS_PLAN_3M = "plus_3m";
    private static final long PLUS_PRICE_1M = 750L;
    private static final long PLUS_PRICE_3M = 1900L;
    private static final List<CoinPack> COIN_PACKS = List.of(new CoinPack(1000, 0, "9,99 €", class_2960.method_60655("s9labclient", "textures/gui/coin_shop/s9c_coin_pack_1000.png"), -2575766), new CoinPack(2000, 500, "19,99 €", class_2960.method_60655("s9labclient", "textures/gui/coin_shop/s9c_coin_pack_2000_bonus_500.png"), -16222611), new CoinPack(5000, 1000, "39,99 €", class_2960.method_60655("s9labclient", "textures/gui/coin_shop/s9c_coin_pack_5000_bonus_1000.png"), -1612013), new CoinPack(10000, 2000, "64,99 €", class_2960.method_60655("s9labclient", "textures/gui/coin_shop/s9c_coin_pack_10000_bonus_2000.png"), -1891829));
    private final class_437 parent;
    private final AnimationManager uiAnimations;
    private final List<CoinPackCard> coinPackCards;
    private ClientTab selectedTab;
    private ModuleCategory selectedCategory;
    private Module selectedModule;
    private CosmeticType selectedCosmeticType;
    private Cosmetic selectedCosmetic;
    private HudModule draggingModule;
    private int dragOffsetX;
    private int dragOffsetY;
    private int scroll;
    private int cosmeticSideScroll;
    private boolean showAllCosmetics;
    private boolean plusShopOpen;
    private boolean showAllModules;
    private boolean moduleDetailsOpen;
    private boolean cosmeticDetailsOpen;
    private boolean previewDragging;
    private float previewYaw;
    private float previewPitch;
    private int previewZoom;
    private PreviewPose previewPose;
    private boolean previewTryOn;
    private boolean searchFocused;
    private String search;
    private boolean sortAscending;
    private boolean giftDialogOpen;
    private String plusGiftPlan;
    private String giftReceiver;
    private String giftStatus;
    private boolean coinShopOpen;
    private int coinShopScroll;
    private int focusedCoinPack;
    private ColorSetting colorPickerSetting;
    private Module colorPickerModule;
    private int colorPickerDrag;

    public S9LabClientScreen(class_437 parent) {
        this(parent, S9LabClientScreen.ClientTab.MODS);
    }

    public S9LabClientScreen(class_437 parent, ClientTab selectedTab) {
        super(class_2561.method_43470("S9Lab Client"));
        this.uiAnimations = new AnimationManager();
        this.coinPackCards = COIN_PACKS.stream().map(CoinPackCard::new).toList();
        this.selectedCategory = ModuleCategory.HUD;
        this.selectedCosmeticType = CosmeticType.CAPE;
        this.previewYaw = 180.0F;
        this.previewPitch = 8.0F;
        this.previewZoom = 78;
        this.previewPose = PreviewPose.IDLE;
        this.search = "";
        this.sortAscending = true;
        this.plusGiftPlan = "";
        this.giftReceiver = "";
        this.giftStatus = "";
        this.parent = parent;
        this.selectedTab = selectedTab;
    }

    public S9LabClientScreen(class_437 parent, ClientTab selectedTab, ModuleCategory selectedCategory) {
        this(parent, selectedTab);
        this.selectedCategory = selectedCategory;
        this.showAllModules = false;
    }

    protected void method_25426() {
        this.clampScroll();
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        this.ensureResponsiveLayout();
        Layout layout = this.layout();
        ClientTheme theme = ThemeManager.theme();
        int accent = theme.accentColor();
        if (this.coinShopOpen) {
            this.renderShopBackdrop(context);
            this.renderCoinShop(context, layout, mouseX, mouseY, deltaTicks, theme);
            super.method_25394(context, mouseX, mouseY, deltaTicks);
        } else if (this.isShopLike()) {
            this.renderShopBackdrop(context);
            this.renderCosmetics(context, layout, mouseX, mouseY, accent);
            if (this.giftDialogOpen) {
                this.renderGiftDialog(context, layout, mouseX, mouseY, accent);
            }

            super.method_25394(context, mouseX, mouseY, deltaTicks);
        } else {
            this.renderShopBackdrop(context);
            this.renderClientShell(context, layout, mouseX, mouseY, accent);
            this.renderNotificationBanner(context, layout, mouseX, mouseY, accent);
            switch (this.selectedTab.ordinal()) {
                case 0 -> this.renderModsCatalog(context, layout, mouseX, mouseY, accent);
                case 1 -> this.renderSettingsCatalog(context, layout, mouseX, mouseY, accent);
                case 2 -> this.renderCosmetics(context, layout, mouseX, mouseY, accent);
                case 3 -> this.renderCosmetics(context, layout, mouseX, mouseY, accent);
            }

            if (this.giftDialogOpen) {
                this.renderGiftDialog(context, layout, mouseX, mouseY, accent);
            }

            if (this.colorPickerSetting != null) {
                this.renderColorPicker(context, mouseX, mouseY, accent);
            }

            super.method_25394(context, mouseX, mouseY, deltaTicks);
        }
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        int mouseX = (int)click.comp_4798();
        int mouseY = (int)click.comp_4799();
        Layout layout = this.layout();
        if (this.colorPickerSetting != null) {
            return this.handleColorPickerClick(mouseX, mouseY);
        } else if (this.giftDialogOpen) {
            return this.handleGiftDialogClick(layout, mouseX, mouseY);
        } else if (this.coinShopOpen) {
            return this.handleFooterTabsClick(layout, mouseX, mouseY) ? true : this.handleCoinShopClick(layout, mouseX, mouseY);
        } else if (doubled && this.previewBounds(layout).contains((double)mouseX, (double)mouseY)) {
            this.resetPreviewCamera();
            return true;
        } else if (this.handleNotificationBannerClick(layout, mouseX, mouseY)) {
            return true;
        } else if (this.handleFooterTabsClick(layout, mouseX, mouseY)) {
            return true;
        } else if (!this.isShopLike() && this.handleClientShellClick(layout, mouseX, mouseY)) {
            return true;
        } else if (this.handleHudDragStart(mouseX, mouseY)) {
            return true;
        } else {
            boolean var10000;
            switch (this.selectedTab.ordinal()) {
                case 0 -> var10000 = this.handleModsCatalogClick(layout, mouseX, mouseY);
                case 1 -> var10000 = this.handleSettingsCatalogClick(layout, mouseX, mouseY);
                case 2 -> var10000 = this.handleCosmeticClick(layout, mouseX, mouseY);
                case 3 -> var10000 = this.handleCosmeticClick(layout, mouseX, mouseY);
                default -> throw new MatchException((String)null, (Throwable)null);
            }

            boolean handled = var10000;
            if (handled) {
                return true;
            } else {
                this.searchFocused = false;
                return super.method_25402(click, doubled);
            }
        }
    }

    public boolean method_25403(class_11909 click, double offsetX, double offsetY) {
        if (this.colorPickerSetting != null && this.colorPickerDrag != 0) {
            this.updateColorPicker((int)click.comp_4798(), (int)click.comp_4799(), this.colorPickerDrag);
            return true;
        } else if (this.previewDragging) {
            this.previewYaw += (float)offsetX * 1.9F;
            this.previewPitch = (float)clamp(Math.round(this.previewPitch - (float)offsetY * 1.2F), -80, 80);
            return true;
        } else if (this.draggingModule != null) {
            this.draggingModule.setPosition((int)click.comp_4798() - this.dragOffsetX, (int)click.comp_4799() - this.dragOffsetY);
            return true;
        } else {
            return super.method_25403(click, offsetX, offsetY);
        }
    }

    public boolean method_25406(class_11909 click) {
        if (this.colorPickerDrag != 0) {
            this.colorPickerDrag = 0;
            S9LabClientClient.getConfigManager().save();
            return true;
        } else if (this.previewDragging) {
            this.previewDragging = false;
            return true;
        } else if (this.draggingModule != null) {
            this.draggingModule = null;
            S9LabClientClient.getConfigManager().save();
            return true;
        } else {
            return super.method_25406(click);
        }
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        Layout layout = this.layout();
        if (this.coinShopOpen) {
            CoinShopLayout shop = this.coinShopLayout(layout);
            if (inside(mouseX, mouseY, shop.contentX, shop.contentY, shop.contentW, shop.contentH)) {
                this.coinShopScroll = ResponsiveLayout.scroll(this.coinShopScroll, verticalAmount, shop.maxScroll);
                return true;
            } else {
                return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
            }
        } else if (this.previewBounds(layout).contains(mouseX, mouseY)) {
            this.previewZoom = clamp(this.previewZoom + (int)Math.round(verticalAmount * (double)7.0F), 42, 140);
            return true;
        } else {
            if (this.isShopLike()) {
                CosmeticLayout parts = this.cosmeticLayout(layout);
                if (inside(mouseX, mouseY, parts.sideX, parts.contentY, parts.sideW, parts.contentH)) {
                    this.cosmeticSideScroll = ResponsiveLayout.scroll(this.cosmeticSideScroll, verticalAmount, this.maxCosmeticSideScroll(parts.contentH));
                    return true;
                }

                if (inside(mouseX, mouseY, parts.gridX, parts.gridY, parts.gridW, parts.gridH)) {
                    this.scroll = ResponsiveLayout.scroll(this.scroll, verticalAmount, this.maxScroll(layout));
                    return true;
                }
            }

            if (!inside(mouseX, mouseY, layout.bodyX(), layout.bodyY(), layout.bodyWidth(), layout.bodyHeight())) {
                return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
            } else {
                this.scroll = ResponsiveLayout.scroll(this.scroll, verticalAmount, this.maxScroll(layout));
                return true;
            }
        }
    }

    public boolean method_25400(class_11905 input) {
        if (this.giftDialogOpen && input.method_74227()) {
            this.giftReceiver = TextLayout.ellipsize(this.field_22793, this.giftReceiver + input.method_74226(), 190);
            return true;
        } else if (this.searchFocused && input.method_74227()) {
            this.search = TextLayout.ellipsize(this.field_22793, this.search + input.method_74226(), 180);
            this.scroll = 0;
            return true;
        } else {
            return super.method_25400(input);
        }
    }

    public boolean method_25404(class_11908 input) {
        if (this.giftDialogOpen) {
            if (input.method_74231()) {
                this.giftDialogOpen = false;
                this.plusGiftPlan = "";
                return true;
            }

            if (input.method_74228() == 259 && !this.giftReceiver.isEmpty()) {
                this.giftReceiver = this.giftReceiver.substring(0, this.giftReceiver.length() - 1);
                return true;
            }

            if (input.method_74228() == 257) {
                this.confirmGift();
                return true;
            }
        }

        if (input.method_74231()) {
            if (this.coinShopOpen) {
                this.coinShopOpen = false;
                this.coinShopScroll = 0;
                return true;
            } else if (!this.moduleDetailsOpen && !this.cosmeticDetailsOpen) {
                if (this.searchFocused) {
                    this.searchFocused = false;
                    return true;
                } else {
                    this.method_25419();
                    return true;
                }
            } else {
                this.moduleDetailsOpen = false;
                this.cosmeticDetailsOpen = false;
                this.previewDragging = false;
                return true;
            }
        } else {
            if (this.coinShopOpen) {
                if (input.method_74228() == 263 || input.method_74228() == 265) {
                    this.focusedCoinPack = Math.floorMod(this.focusedCoinPack - 1, this.coinPackCards.size());
                    return true;
                }

                if (input.method_74228() == 262 || input.method_74228() == 264) {
                    this.focusedCoinPack = Math.floorMod(this.focusedCoinPack + 1, this.coinPackCards.size());
                    return true;
                }

                if (input.method_74228() == 257) {
                    this.onCoinPackSelected(((CoinPackCard)this.coinPackCards.get(this.focusedCoinPack)).pack());
                    return true;
                }
            }

            if (this.searchFocused) {
                if (input.method_74228() == 259 && !this.search.isEmpty()) {
                    this.search = this.search.substring(0, this.search.length() - 1);
                    this.scroll = 0;
                    return true;
                }

                if (input.method_74228() == 257) {
                    this.searchFocused = false;
                    return true;
                }
            }

            return super.method_25404(input);
        }
    }

    protected void onResponsiveResize() {
        this.clampScroll();
        if (this.coinShopOpen) {
            CoinShopLayout shop = this.coinShopLayout(this.layout());
            this.coinShopScroll = clamp(this.coinShopScroll, 0, shop.maxScroll);
        }

        int maxX = Math.max(0, this.field_22789 - 20);
        int maxY = Math.max(0, this.field_22790 - 20);
        S9LabClientClient.getModuleManager().getHudModules().forEach((module) -> module.setPosition(clamp(module.getX(), 0, maxX), clamp(module.getY(), 0, maxY)));
    }

    public void method_25419() {
        if (this.giftDialogOpen) {
            this.giftDialogOpen = false;
            this.plusGiftPlan = "";
        } else {
            S9LabClientClient.getConfigManager().save();
            class_310.method_1551().method_1507(this.parent);
        }
    }

    public static void renderDarkBackground(class_332 context) {
        PremiumRender.shopBackdrop(context);
    }

    private void renderShopBackdrop(class_332 context) {
        PremiumRender.shopBackdrop(context);
    }

    private boolean isShopLike() {
        return this.selectedTab == S9LabClientScreen.ClientTab.COSMETICS || this.selectedTab == S9LabClientScreen.ClientTab.SHOP;
    }

    private String shopTitle() {
        return this.showAllCosmetics ? "COSMETICS" : cosmeticMenuLabel(this.selectedCosmeticType).toUpperCase(Locale.ROOT);
    }

    private void renderHeader(class_332 context, Layout layout, int mouseX, int mouseY, int accent) {
        int y = layout.y + 14;
        int logoX = layout.x + 22;
        context.method_25302(class_10799.field_56883, CLIENT_ICON, layout.x + 5, layout.y + 16, 2.0F, 2.0F, 16, 16, 256, 256, 256, 256);
        context.method_27535(this.field_22793, class_2561.method_43470("S9Lab"), logoX, y + 2, -1);
        context.method_27535(this.field_22793, class_2561.method_43470("Client"), logoX, y + 14, ClientTheme.withAlpha(accent, 210));
        int tabX = this.tabStartX(layout);

        for(ClientTab tab : visibleTabs()) {
            int w = this.tabWidth(tab);
            boolean active = tab == this.selectedTab;
            boolean hovered = inside((double)mouseX, (double)mouseY, tabX, y, w, 24);
            context.method_27534(this.field_22793, class_2561.method_43470(tab.label), tabX + w / 2, y + 7, active ? accent : (hovered ? -1 : -6643278));
            if (active) {
                context.method_25294(tabX + 10, y + 26, tabX + w - 10, y + 28, accent);
            }

            tabX += w + 12;
        }

        int coinsBoxW = 92;
        int coinsBoxX = layout.x + layout.width - coinsBoxW - 28;
        rect(context, coinsBoxX, y + 1, coinsBoxW, 20, 4, 1712330016);
        outline(context, coinsBoxX, y + 1, coinsBoxW, 20, 4, -12960443);
        String formattedCoins = formatCoins(BackendState.coins());
        context.method_27535(this.field_22793, class_2561.method_43470(formattedCoins), coinsBoxX + 10, y + 7, -1);
        context.method_25294(layout.x + 14, layout.y + layout.headerHeight - 1, layout.x + layout.width - 14, layout.y + layout.headerHeight, -14342098);
    }

    private void renderNotificationBanner(class_332 context, Layout layout, int mouseX, int mouseY, int accent) {
        List<BackendState.Notification> notifications = BackendState.unreadNotificationsSnapshot();
        if (!notifications.isEmpty()) {
            Rect bounds = this.notificationBannerBounds(layout);
            if (bounds.width > 0) {
                BackendState.Notification latest = (BackendState.Notification)notifications.get(0);
                boolean hovered = bounds.contains((double)mouseX, (double)mouseY);
                rect(context, bounds.x, bounds.y, bounds.width, bounds.height, 9, hovered ? -266854870 : -435087838);
                outline(context, bounds.x, bounds.y, bounds.width, bounds.height, 9, ClientTheme.withAlpha(-14249, hovered ? 240 : 185));
                context.method_27535(this.field_22793, class_2561.method_43470("Gift"), bounds.x + 12, bounds.y + 8, -14249);
                String message = notifications.size() == 1 ? latest.cosmeticName() + " from " + latest.senderName() : notifications.size() + " unread gifts";
                context.method_27535(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, message, bounds.width - 128)), bounds.x + 42, bounds.y + 8, -1);
                int readX = bounds.x + bounds.width - 64;
                rect(context, readX, bounds.y + 5, 54, bounds.height - 10, 6, inside((double)mouseX, (double)mouseY, readX, bounds.y + 5, 54, bounds.height - 10) ? ClientTheme.withAlpha(accent, 210) : 1998793270);
                context.method_27534(this.field_22793, class_2561.method_43470("Read"), readX + 27, bounds.y + 10, -1);
            }
        }
    }

    private void renderMods(class_332 context, Layout layout, int mouseX, int mouseY, int accent) {
        int x = layout.x + 20;
        int y = layout.bodyY() + 12;
        int width = layout.width - 40;
        int chipY = y;
        int chipX = x;

        for(ModuleCategory category : ModuleCategory.values()) {
            String label = titleCase(category.name());
            int chipW = Math.max(54, this.field_22793.method_1727(label) + 22);
            boolean active = category == this.selectedCategory;
            boolean hovered = inside((double)mouseX, (double)mouseY, chipX, chipY, chipW, 20);
            rect(context, chipX, chipY, chipW, 20, 3, active ? accent : (hovered ? -13618374 : -14671063));
            context.method_27534(this.field_22793, class_2561.method_43470(label), chipX + chipW / 2, chipY + 6, active ? -1 : -6643278);
            chipX += chipW + 8;
        }

        int searchW = 130;
        this.renderSearch(context, x + width - searchW, y, searchW, mouseX, mouseY, accent, "Search");
        int azX = x + width - searchW - 56;
        rect(context, azX, y, 23, 20, 3, this.sortAscending ? ClientTheme.withAlpha(accent, 210) : -14539219);
        context.method_27534(this.field_22793, class_2561.method_43470("A↓"), azX + 11, y + 6, this.sortAscending ? -1 : -6643278);
        rect(context, azX + 28, y, 23, 20, 3, !this.sortAscending ? ClientTheme.withAlpha(accent, 210) : -14539219);
        context.method_27534(this.field_22793, class_2561.method_43470("Z↓"), azX + 39, y + 6, !this.sortAscending ? -1 : -6643278);
        int gridY = y + 36;
        int gridH = layout.y + layout.height - gridY - 20;
        context.method_44379(x, gridY, x + width, gridY + gridH);
        Grid grid = this.grid(width, gridH, 132, 84, 5);
        List<Module> modules = this.filteredModules();
        int baseY = gridY - this.scroll;

        for(int i = 0; i < modules.size(); ++i) {
            Module module = (Module)modules.get(i);
            int cardX = x + i % grid.columns * (grid.cardW + grid.gap);
            int cardY = baseY + i / grid.columns * (grid.cardH + grid.gap);
            if (cardY + grid.cardH >= gridY && cardY <= gridY + gridH) {
                this.renderModuleCard(context, module, cardX, cardY, grid.cardW, grid.cardH, mouseX, mouseY, accent);
            }
        }

        context.method_44380();
    }

    private void renderClientShell(class_332 context, Layout layout, int mouseX, int mouseY, int accent) {
        CosmeticLayout parts = this.cosmeticLayout(layout);
        PremiumRender.shopPanel(context, parts.x, parts.y, parts.width, parts.height, parts.topbarH, parts.footerH);
        this.renderClientTopbar(context, parts, mouseX, mouseY, accent);
        this.renderFooterTabs(context, parts, mouseX, mouseY, accent);
        context.method_25294(parts.gridX - 1, parts.contentY, parts.gridX, parts.y + parts.height - parts.footerH, 1728053247);
        if (parts.preview.width > 0) {
            context.method_25294(parts.preview.x - 1, parts.contentY, parts.preview.x, parts.y + parts.height - parts.footerH, 1728053247);
        }

    }

    private void renderClientTopbar(class_332 context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        int buttonY = parts.y + 11;
        int buttonH = Math.max(18, Math.min(24, parts.topbarH - 18));
        int x = parts.x + 8;
        String primary = this.selectedTab == S9LabClientScreen.ClientTab.MODS ? titleCase(this.selectedCategory.name()).toUpperCase(Locale.ROOT) : "SETTINGS";
        this.renderSquareButton(context, x, buttonY, 116, buttonH, primary, !this.showAllModules, mouseX, mouseY, accent);
        x += 124;
        this.renderSquareButton(context, x, buttonY, 58, buttonH, "ALL", this.showAllModules, mouseX, mouseY, accent);
        x += 66;
        int searchW = Math.max(110, Math.min(220, parts.x + parts.width - x - Math.max(98, parts.width / 8) - 34));
        this.renderShopSearch(context, x, buttonY, searchW, buttonH, mouseX, mouseY, accent);
        int coinsW = Math.max(78, Math.min(120, parts.width / 8));
        int coinsX = parts.x + parts.width - coinsW - 12;
        rect(context, coinsX, buttonY - 3, coinsW, buttonH + 6, 0, -13271112);
        outline(context, coinsX, buttonY - 3, coinsW, buttonH + 6, 0, -10110478);
        context.method_27534(this.field_22793, class_2561.method_43470(formatCoins(BackendState.coins()) + " COINS"), coinsX + coinsW / 2, buttonY + (buttonH - 8) / 2, -1);
    }

    private void renderCoinShop(class_332 context, Layout layout, int mouseX, int mouseY, float deltaTicks, ClientTheme theme) {
        CosmeticLayout parts = this.cosmeticLayout(layout);
        PremiumRender.shopPanel(context, parts.x, parts.y, parts.width, parts.height, parts.topbarH, parts.footerH);
        this.renderCoinShopTopbar(context, parts, mouseX, mouseY, theme.accentColor());
        this.renderFooterTabs(context, parts, mouseX, mouseY, theme.accentColor());
        CoinShopLayout shop = this.coinShopLayout(layout);
        context.method_44379(shop.contentX, shop.contentY, shop.contentX + shop.contentW, shop.contentY + shop.contentH);

        for(int i = 0; i < this.coinPackCards.size(); ++i) {
            int column = i % shop.columns;
            int row = i / shop.columns;
            int cardX = shop.contentX + column * (shop.cardW + shop.gap);
            int cardY = shop.contentY + row * (shop.cardH + shop.gap) - this.coinShopScroll;
            CoinPackCard card = (CoinPackCard)this.coinPackCards.get(i);
            card.setBounds(cardX, cardY, shop.cardW, shop.cardH);
            card.render(context, this.field_22793, mouseX, mouseY, deltaTicks, this.focusedCoinPack == i, theme, this.uiAnimations);
        }

        context.method_44380();
        if (shop.maxScroll > 0) {
            int trackX = shop.contentX + shop.contentW - 3;
            int thumbH = Math.max(20, shop.contentH * shop.contentH / Math.max(shop.contentH, shop.totalHeight));
            int travel = Math.max(1, shop.contentH - thumbH);
            int thumbY = shop.contentY + (int)((long)travel * (long)this.coinShopScroll / (long)Math.max(1, shop.maxScroll));
            context.method_25294(trackX, shop.contentY, trackX + 2, shop.contentY + shop.contentH, 858534724);
            context.method_25294(trackX, thumbY, trackX + 2, thumbY + thumbH, theme.accentColor());
        }

    }

    private void renderCoinShopTopbar(class_332 context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        int buttonY = parts.y + 11;
        int buttonH = Math.max(18, Math.min(24, parts.topbarH - 18));
        int backW = parts.width < 480 ? 54 : 72;
        this.renderSquareButton(context, parts.x + 8, buttonY, backW, buttonH, "< BACK", false, mouseX, mouseY, accent);
        int titleX = parts.x + backW + 20;
        class_327 var10001 = this.field_22793;
        class_5250 var10002 = class_2561.method_43470("S9C COINS");
        Objects.requireNonNull(this.field_22793);
        context.method_27535(var10001, var10002, titleX, buttonY + (buttonH - 9) / 2, -1);
        Rect coins = this.coinButtonBounds(parts);
        rect(context, coins.x, coins.y, coins.width, coins.height, 0, -13271112);
        outline(context, coins.x, coins.y, coins.width, coins.height, 0, -10110478);
        var10001 = this.field_22793;
        var10002 = class_2561.method_43470(formatCoins(BackendState.coins()) + " COINS");
        int var10003 = coins.x + coins.width / 2;
        int var10004 = coins.y;
        int var10005 = coins.height;
        Objects.requireNonNull(this.field_22793);
        context.method_27534(var10001, var10002, var10003, var10004 + (var10005 - 9) / 2, -1);
    }

    private void renderFooterTabs(class_332 context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        if (parts.footerH >= 28) {
            int buttonH = Math.min(24, parts.footerH - 12);
            int y = parts.y + parts.height - parts.footerH + (parts.footerH - buttonH) / 2;
            int gap = 7;
            int[] widths = new int[]{62, 78, 88, 78};
            int socialW = 76;
            int total = widths[0] + widths[1] + widths[2] + widths[3] + socialW + gap * 4;
            int x = parts.x + Math.max(8, (parts.width - total) / 2);
            ClientTab[] tabs = visibleTabs();

            for(int i = 0; i < tabs.length; ++i) {
                ClientTab tab = tabs[i];
                this.renderSquareButton(context, x, y, widths[i], buttonH, tab.label.toUpperCase(Locale.ROOT), this.selectedTab == tab, mouseX, mouseY, accent);
                x += widths[i] + gap;
            }

            String social = BackendState.totalUnreadFriendMessages() > 0 ? "SOCIAL " + Math.min(99, BackendState.totalUnreadFriendMessages()) : "SOCIAL";
            this.renderSquareButton(context, x, y, socialW, buttonH, social, false, mouseX, mouseY, accent);
        }
    }

    private void renderModsCatalog(class_332 context, Layout layout, int mouseX, int mouseY, int accent) {
        CosmeticLayout parts = this.cosmeticLayout(layout);
        if (this.moduleDetailsOpen && this.selectedModule != null) {
            this.renderModuleDetails(context, parts, mouseX, mouseY, accent);
        } else {
            this.renderModuleSidebar(context, parts, mouseX, mouseY, accent);
            if (!this.showAllModules && this.selectedCategory == ModuleCategory.PERFORMANCE) {
                this.renderEmbeddedPerformance(context, parts, mouseX, mouseY, accent);
            } else {
                context.method_27535(this.field_22793, class_2561.method_43470(this.showAllModules ? "ALL MODULES" : titleCase(this.selectedCategory.name()).toUpperCase(Locale.ROOT)), parts.gridX, parts.contentY + 12, -1);
                context.method_25294(parts.gridX + 2, parts.contentY + 36, parts.gridX + parts.gridW - 4, parts.contentY + 37, 1157627903);
                context.method_44379(parts.gridX, parts.gridY, parts.gridX + parts.gridW, parts.gridY + parts.gridH);
                Grid grid = this.grid(parts.gridW, parts.gridH, 136, 116, 4);
                List<Module> modules = this.filteredModules();
                int baseY = parts.gridY - this.scroll;

                for(int i = 0; i < modules.size(); ++i) {
                    Module module = (Module)modules.get(i);
                    int cardX = parts.gridX + i % grid.columns * (grid.cardW + grid.gap);
                    int cardY = baseY + i / grid.columns * (grid.cardH + grid.gap);
                    if (cardY + grid.cardH >= parts.gridY && cardY <= parts.gridY + parts.gridH) {
                        this.renderModuleCard(context, module, cardX, cardY, grid.cardW, grid.cardH, mouseX, mouseY, accent);
                    }
                }

                context.method_44380();
                if (parts.preview.width > 0) {
                    this.renderModulePreviewPanel(context, parts.preview.x, parts.preview.y, parts.preview.width, parts.preview.height, mouseX, mouseY, accent);
                }

            }
        }
    }

    private void renderEmbeddedPerformance(class_332 context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        Rect bounds = this.performanceBounds(parts);
        PerformanceManager manager = S9LabClientClient.getPerformanceManager();
        PerformanceManager.Metrics metrics = manager.metrics();
        List<ModDetectionManager.DetectedMod> mods = manager.modDetectionManager().snapshot();
        long activeMods = mods.stream().filter(ModDetectionManager.DetectedMod::installed).count();
        context.method_44379(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
        int y = bounds.y + 8 - this.scroll;
        int heroH = bounds.width < 430 ? 48 : 56;
        PremiumRender.card(context, bounds.x, y, bounds.width, heroH, 0, -317713380, ClientTheme.withAlpha(accent, 150));
        context.method_25294(bounds.x, y, bounds.x + 3, y + heroH, accent);
        context.method_27535(this.field_22793, class_2561.method_43470("S9LAB PERFORMANCE"), bounds.x + 13, y + 10, -1);
        context.method_27535(this.field_22793, class_2561.method_43470("Integrated optimization stack"), bounds.x + 13, y + 26, -6643278);
        if (heroH > 50) {
            context.method_27535(this.field_22793, class_2561.method_43470("No separate installation required"), bounds.x + 13, y + 40, -9932669);
        }

        String activeLabel = activeMods + "/" + mods.size() + " ACTIVE";
        int statusW = Math.min(104, Math.max(72, this.field_22793.method_1727(activeLabel) + 18));
        int statusX = bounds.x + bounds.width - statusW - 10;
        int statusY = y + (heroH - 24) / 2;
        PremiumRender.card(context, statusX, statusY, statusW, 24, 0, -586081770, activeMods == (long)mods.size() ? -11931025 : -19372);
        context.method_27534(this.field_22793, class_2561.method_43470(activeLabel), statusX + statusW / 2, statusY + 8, activeMods == (long)mods.size() ? -11931025 : -19372);
        y += heroH + 10;
        int metricColumns = bounds.width < 360 ? 1 : 3;
        int gap = 8;
        int metricW = Math.max(1, (bounds.width - gap * (metricColumns - 1)) / metricColumns);
        int metricH = bounds.width < 500 ? 46 : 52;
        MetricCard[] metricCards = new MetricCard[]{new MetricCard("CURRENT FPS", String.valueOf(metrics.fps()), "live"), new MetricCard("GAMEPLAY AVG", metrics.averageFps() <= 0 ? "WARMING UP" : String.valueOf(metrics.averageFps()), "rolling 60 sec"), new MetricCard("MEMORY", metrics.usedRamMb() + " / " + metrics.maxRamMb() + " MB", "Java heap")};

        for(int i = 0; i < metricCards.length; ++i) {
            int x = bounds.x + i % metricColumns * (metricW + gap);
            int cardY = y + i / metricColumns * (metricH + gap);
            this.renderPerformanceMetricCard(context, metricCards[i], x, cardY, metricW, metricH, mouseX, mouseY, accent);
        }

        y += rows(metricCards.length, metricColumns) * (metricH + gap) + 10;
        this.renderPerformanceSectionTitle(context, "PERFORMANCE PROFILE", bounds.x, y, bounds.width);
        y += 18;
        int presetColumns = bounds.width < 430 ? 1 : 3;
        int presetW = Math.max(1, (bounds.width - gap * (presetColumns - 1)) / presetColumns);
        int presetH = 42;

        for(PerformanceManager.PerformancePreset preset : PerformancePreset.values()) {
            int index = preset.ordinal();
            int x = bounds.x + index % presetColumns * (presetW + gap);
            int cardY = y + index / presetColumns * (presetH + gap);
            this.renderPerformancePresetCard(context, manager, preset, x, cardY, presetW, presetH, mouseX, mouseY, accent);
        }

        y += rows(PerformancePreset.values().length, presetColumns) * (presetH + gap) + 12;
        this.renderPerformanceSectionTitle(context, "INTEGRATED OPTIMIZATIONS", bounds.x, y, bounds.width);
        y += 18;
        int modColumns = bounds.width >= 650 ? 4 : (bounds.width >= 430 ? 3 : 2);
        int modW = Math.max(1, (bounds.width - gap * (modColumns - 1)) / modColumns);
        int modH = 42;

        for(int i = 0; i < mods.size(); ++i) {
            ModDetectionManager.DetectedMod mod = (ModDetectionManager.DetectedMod)mods.get(i);
            int x = bounds.x + i % modColumns * (modW + gap);
            int cardY = y + i / modColumns * (modH + gap);
            this.renderPerformanceModCard(context, mod, x, cardY, modW, modH, mouseX, mouseY);
        }

        y += rows(mods.size(), modColumns) * (modH + gap) + 12;
        this.renderPerformanceSectionTitle(context, "CLIENT UI", bounds.x, y, bounds.width);
        y += 18;
        List<Module> performanceModules = S9LabClientClient.getModuleManager().getModules().stream().filter((modulex) -> modulex.getCategory() == ModuleCategory.PERFORMANCE).toList();
        int moduleColumns = bounds.width < 520 ? 1 : 2;
        int moduleW = Math.max(1, (bounds.width - gap * (moduleColumns - 1)) / moduleColumns);
        int moduleH = 58;

        for(int i = 0; i < performanceModules.size(); ++i) {
            Module module = (Module)performanceModules.get(i);
            int x = bounds.x + i % moduleColumns * (moduleW + gap);
            int cardY = y + i / moduleColumns * (moduleH + gap);
            this.renderPerformanceModuleCard(context, module, x, cardY, moduleW, moduleH, mouseX, mouseY, accent);
        }

        y += Math.max(1, rows(performanceModules.size(), moduleColumns)) * (moduleH + gap) + 8;
        context.method_44380();
        int contentHeight = y + this.scroll - bounds.y + 4;
        if (contentHeight > bounds.height) {
            this.renderSlimScrollbar(context, bounds.x + bounds.width - 4, bounds.y + 4, bounds.height - 8, contentHeight, accent);
        }

    }

    private Rect performanceBounds(CosmeticLayout parts) {
        int right = parts.preview.width > 0 ? parts.preview.x + parts.preview.width : parts.gridX + parts.gridW;
        int bottom = parts.y + parts.height - parts.footerH - 8;
        return new Rect(parts.gridX, parts.contentY + 8, Math.max(1, right - parts.gridX), Math.max(1, bottom - parts.contentY - 8));
    }

    private int performanceContentHeight(CosmeticLayout parts) {
        Rect bounds = this.performanceBounds(parts);
        PerformanceManager manager = S9LabClientClient.getPerformanceManager();
        int gap = 8;
        int heroH = bounds.width < 430 ? 48 : 56;
        int metricColumns = bounds.width < 360 ? 1 : 3;
        int metricH = bounds.width < 500 ? 46 : 52;
        int presetColumns = bounds.width < 430 ? 1 : 3;
        int presetH = 42;
        int modColumns = bounds.width >= 650 ? 4 : (bounds.width >= 430 ? 3 : 2);
        int modH = 42;
        int moduleColumns = bounds.width < 520 ? 1 : 2;
        int moduleH = 58;
        int performanceModules = (int)S9LabClientClient.getModuleManager().getModules().stream().filter((module) -> module.getCategory() == ModuleCategory.PERFORMANCE).count();
        int height = heroH + 10;
        height += rows(3, metricColumns) * (metricH + gap) + 10;
        height += 18 + rows(PerformancePreset.values().length, presetColumns) * (presetH + gap) + 12;
        height += 18 + rows(manager.modDetectionManager().snapshot().size(), modColumns) * (modH + gap) + 12;
        height += 18 + Math.max(1, rows(performanceModules, moduleColumns)) * (moduleH + gap) + 8;
        return height + 20;
    }

    private void renderPerformanceSectionTitle(class_332 context, String label, int x, int y, int width) {
        context.method_27535(this.field_22793, class_2561.method_43470(label), x + 2, y, -1);
        context.method_25294(x + 2, y + 13, x + width - 7, y + 14, 1157627903);
    }

    private void renderPerformanceMetricCard(class_332 context, MetricCard card, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        boolean hovered = inside((double)mouseX, (double)mouseY, x, y, width, height);
        PremiumRender.card(context, x, y, width, height, 0, hovered ? -585424078 : -921561312, hovered ? accent : 1442840575);
        context.method_27535(this.field_22793, class_2561.method_43470(card.label()), x + 9, y + 7, -6643278);
        context.method_27535(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, card.value(), width - 18)), x + 9, y + 21, -1);
        if (height > 48) {
            context.method_27535(this.field_22793, class_2561.method_43470(card.hint()), x + 9, y + 36, -9932669);
        }

    }

    private void renderPerformancePresetCard(class_332 context, PerformanceManager manager, PerformanceManager.PerformancePreset preset, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        boolean active = manager.activePreset() == preset;
        boolean hovered = inside((double)mouseX, (double)mouseY, x, y, width, height);
        int fill = active ? ClientTheme.withAlpha(accent, 90) : (hovered ? -518315214 : -804186851);
        PremiumRender.card(context, x, y, width, height, 0, fill, active ? accent : (hovered ? -1426063361 : 1442840575));
        context.method_27535(this.field_22793, class_2561.method_43470(preset.displayName().toUpperCase(Locale.ROOT)), x + 10, y + 8, active ? -1 : -1578254);
        class_327 var10001 = this.field_22793;
        String var10002;
        switch (preset) {
            case QUALITY -> var10002 = "visuals first";
            case BALANCED -> var10002 = "daily default";
            case MAX_FPS -> var10002 = "lowest safe settings";
            default -> throw new MatchException((String)null, (Throwable)null);
        }

        context.method_27535(var10001, class_2561.method_43470(var10002), x + 10, y + 23, -6643278);
    }

    private void renderPerformanceModuleCard(class_332 context, Module module, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        boolean hovered = inside((double)mouseX, (double)mouseY, x, y, width, height);
        PremiumRender.card(context, x, y, width, height, 0, hovered ? -585424078 : -921561312, module.isEnabled() ? ClientTheme.withAlpha(accent, 210) : 1442840575);
        context.method_27535(this.field_22793, class_2561.method_43470(module.getName().toUpperCase(Locale.ROOT)), x + 10, y + 8, -1);
        context.method_27535(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, module.getDescription(), width - 72)), x + 10, y + 23, -6643278);
        int switchX = x + width - 44;
        int switchY = y + height - 24;
        rect(context, switchX, switchY, 34, 18, 0, module.isEnabled() ? ClientTheme.withAlpha(accent, 210) : -13946566);
        context.method_27534(this.field_22793, class_2561.method_43470(module.isEnabled() ? "ON" : "OFF"), switchX + 17, switchY + 5, -1);
        context.method_27535(this.field_22793, class_2561.method_43470("click for settings"), x + 10, y + height - 18, -9932669);
    }

    private void renderPerformanceModCard(class_332 context, ModDetectionManager.DetectedMod mod, int x, int y, int width, int height, int mouseX, int mouseY) {
        boolean hovered = inside((double)mouseX, (double)mouseY, x, y, width, height);
        int border = mod.installed() ? -11931025 : (mod.important() ? -19372 : 1442840575);
        PremiumRender.card(context, x, y, width, height, 0, hovered ? -501537232 : -804186337, border);
        context.method_27535(this.field_22793, class_2561.method_43470(mod.displayName().toUpperCase(Locale.ROOT)), x + 9, y + 7, -1578254);
        String state = mod.installed() ? "ACTIVE " + mod.version() : "NOT LOADED";
        context.method_27535(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, state, width - 18)), x + 9, y + 21, mod.installed() ? -11931025 : (mod.important() ? -19372 : -6643278));
    }

    private void renderSlimScrollbar(class_332 context, int x, int y, int height, int contentHeight, int accent) {
        int max = Math.max(1, contentHeight - height);
        int thumbH = Math.max(16, Math.round((float)height * ((float)height / (float)contentHeight)));
        int thumbY = y + Math.round((float)(height - thumbH) * ((float)this.scroll / (float)max));
        context.method_25294(x, y, x + 2, y + height, 855638016);
        context.method_25294(x, thumbY, x + 2, thumbY + thumbH, accent);
    }

    private void renderSettingsCatalog(class_332 context, Layout layout, int mouseX, int mouseY, int accent) {
        CosmeticLayout parts = this.cosmeticLayout(layout);
        Module module = this.selectedModule;
        if (module == null) {
            List<Module> modules = this.filteredModules();
            if (!modules.isEmpty()) {
                module = (Module)modules.get(0);
                this.selectedModule = module;
            }
        }

        if (module != null) {
            this.renderModuleDetails(context, parts, mouseX, mouseY, accent);
        } else {
            this.renderModuleSidebar(context, parts, mouseX, mouseY, accent);
            context.method_27535(this.field_22793, class_2561.method_43470(module == null ? "SETTINGS" : module.getName().toUpperCase(Locale.ROOT)), parts.gridX, parts.contentY + 12, -1);
            context.method_25294(parts.gridX + 2, parts.contentY + 36, parts.gridX + parts.gridW - 4, parts.contentY + 37, 1157627903);
            int editorY = parts.gridY;
            inside((double)mouseX, (double)mouseY, parts.gridX, editorY, Math.min(180, parts.gridW), 28);
            this.renderSquareButton(context, parts.gridX, editorY, Math.min(180, parts.gridW), 28, "OPEN HUD EDITOR", false, mouseX, mouseY, accent);
            if (module == null) {
                context.method_27534(this.field_22793, class_2561.method_43470("Select a module"), parts.gridX + parts.gridW / 2, editorY + 58, -6643278);
            } else {
                int rowY = editorY + 46;
                int colW = parts.gridW < 360 ? parts.gridW : (parts.gridW - 28) / 2;
                this.renderSettingsLine(context, parts.gridX, rowY, colW, "Enabled", module.isEnabled() ? "ON" : "OFF", module.isEnabled(), mouseX, mouseY, accent);
                rowY += 36;
                int i = 0;

                for(Setting<?> setting : module.getSettings()) {
                    int col = parts.gridW < 360 ? 0 : i % 2;
                    int row = parts.gridW < 360 ? i : i / 2;
                    int sx = parts.gridX + col * (colW + 28);
                    int sy = rowY + row * 36;
                    if (sy > parts.gridY + parts.gridH - 28) {
                        break;
                    }

                    this.renderSettingsLine(context, sx, sy, colW, setting.getName(), settingValue(setting), settingActive(setting), mouseX, mouseY, accent);
                    ++i;
                }

                if (parts.preview.width > 0) {
                    this.renderModulePreviewPanel(context, parts.preview.x, parts.preview.y, parts.preview.width, parts.preview.height, mouseX, mouseY, accent);
                }

            }
        }
    }

    private void renderModuleDetails(class_332 context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        Module module = this.selectedModule;
        if (module != null) {
            if (module instanceof TablistBadgeModule) {
                TablistBadgeModule badgeModule = (TablistBadgeModule)module;
                this.renderNameEffectSettings(context, parts, badgeModule, mouseX, mouseY, accent);
            } else {
                int x = parts.sideX + 10;
                int y = parts.contentY + 12;
                int width = parts.width - 20;
                int bottom = parts.y + parts.height - parts.footerH - 12;
                int height = Math.max(1, bottom - y);
                context.method_25294(x, y, x + width, y + height, 389298756);
                outline(context, x, y, width, height, 0, 1157627903);
                String var10000 = module.getName().toUpperCase(Locale.ROOT);
                String title = "< " + var10000;
                context.method_27535(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, title, width - 270)), x + 14, y + 17, -1);
                int topButtonY = y + 10;
                int resetW = 72;
                int onW = 54;
                int resetX = x + width - resetW - 62;
                int onX = resetX - onW - 10;
                this.renderSquareButton(context, onX, topButtonY, onW, 28, module.isEnabled() ? "ON" : "OFF", module.isEnabled(), mouseX, mouseY, accent);
                this.renderSquareButton(context, resetX, topButtonY, resetW, 28, "RESET", false, mouseX, mouseY, accent);
                this.renderSquareButton(context, resetX + resetW + 10, topButtonY, 34, 28, "R", false, mouseX, mouseY, accent);
                context.method_25294(x, y + 58, x + width, y + 59, 858402357);
                int rowY = y + 78;
                rowY = this.renderModuleDetailSection(context, "KEYBIND", x + 18, rowY, width - 36);
                KeybindSetting keybind = this.keybindSetting(module);
                this.renderModuleDetailRow(context, x + 34, rowY, width - 68, keybind == null ? "Key" : keybind.getName(), keybind == null ? "Not Bound" : keybindValue(keybind), false, mouseX, mouseY, accent);
                rowY += 48;
                rowY = this.renderModuleDetailSection(context, "SETTINGS", x + 18, rowY, width - 36);
                if (module.getSettings().isEmpty()) {
                    context.method_27535(this.field_22793, class_2561.method_43470("No settings for this module."), x + 34, rowY + 10, -6643278);
                } else {
                    for(Setting<?> setting : module.getSettings()) {
                        if (!(setting instanceof KeybindSetting)) {
                            if (rowY > bottom - 34) {
                                break;
                            }

                            this.renderModuleDetailRow(context, x + 34, rowY, width - 68, setting.getName(), settingValue(setting), setting instanceof BooleanSetting, mouseX, mouseY, accent);
                            rowY += 36;
                        }
                    }

                }
            }
        }
    }

    private void renderNameEffectSettings(class_332 context, CosmeticLayout parts, TablistBadgeModule module, int mouseX, int mouseY, int accent) {
        int x = parts.sideX + 10;
        int y = parts.contentY + 12;
        int width = parts.width - 20;
        int bottom = parts.y + parts.height - parts.footerH - 12;
        int height = Math.max(1, bottom - y);
        context.method_25294(x, y, x + width, y + height, -267316197);
        outline(context, x, y, width, height, 0, 1430933856);
        context.method_27535(this.field_22793, class_2561.method_43470("< S9C+ NAMETAG STYLE"), x + 14, y + 13, -1);
        context.method_27535(this.field_22793, class_2561.method_43470("Choose up to 3 effects. They are decoded by one compact built-in shader marker."), x + 14, y + 30, -6643278);
        List<String> selected = module.plusNameEffects();
        int slotY = y + 50;
        int slotGap = 8;
        int slotW = Math.max(90, (width - 28 - slotGap * 2) / 3);

        for(int slot = 0; slot < 3; ++slot) {
            int sx = x + 14 + slot * (slotW + slotGap);
            String value = slot < selected.size() ? S9TextEffects.displayName((String)selected.get(slot)) : "EMPTY SLOT";
            boolean active = slot < selected.size();
            context.method_25294(sx, slotY, sx + slotW, slotY + 28, active ? 1429233080 : 1141970463);
            outline(context, sx, slotY, slotW, 28, 0, active ? accent : 1716146528);
            context.method_27534(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, slot + 1 + ". " + value, slotW - 12)), sx + slotW / 2, slotY + 10, active ? -1 : -6643278);
        }

        String preview = class_310.method_1551().method_1548() == null ? "silas0055" : class_310.method_1551().method_1548().method_1676();
        int previewColor = selected.isEmpty() ? -1 : S9TextEffects.triggerColor(selected) | -16777216;
        context.method_27535(this.field_22793, class_2561.method_43470(preview), x + 14, y + 88, previewColor);
        context.method_27535(this.field_22793, class_2561.method_43470(selected.size() + " / 3 EFFECTS SELECTED"), x + 14, y + 104, -9932669);
        this.renderSquareButton(context, x + width - 92, y + 84, 76, 26, "CLEAR", false, mouseX, mouseY, accent);
        int sectionY = y + 132;
        context.method_27535(this.field_22793, class_2561.method_43470("— AVAILABLE SHADER EFFECTS —"), x + 14, sectionY, -9932669);
        context.method_25294(x + 206, sectionY + 5, x + width - 14, sectionY + 6, 860508512);
        int effectsBottom = this.renderNameEffectGrid(context, module, S9TextEffects.EFFECT_IDS, x + 14, sectionY + 18, width - 28, mouseX, mouseY, accent);
        int toggleY = Math.min(effectsBottom + 12, bottom - 68);
        this.renderNameEffectToggle(context, x + 14, toggleY, width - 28, "RENDER EFFECTS ON MY NAME", module.plusNameEffectsEnabled(), mouseX, mouseY, accent);
        this.renderNameEffectToggle(context, x + 14, toggleY + 32, width - 28, "SHOW OTHER PLAYERS' EFFECTS", module.showOtherPlayersNameEffects(), mouseX, mouseY, accent);
    }

    private int renderNameEffectGrid(class_332 context, TablistBadgeModule module, List<String> ids, int x, int y, int width, int mouseX, int mouseY, int accent) {
        int columns = width >= 760 ? 5 : (width >= 560 ? 4 : 3);
        int gap = 7;
        int buttonW = Math.max(78, (width - gap * (columns - 1)) / columns);
        int buttonH = 27;

        for(int index = 0; index < ids.size(); ++index) {
            String id = (String)ids.get(index);
            int col = index % columns;
            int row = index / columns;
            int bx = x + col * (buttonW + gap);
            int by = y + row * (buttonH + gap);
            boolean selectedEffect = module.isEffectSelected(id);
            boolean hovered = inside((double)mouseX, (double)mouseY, bx, by, buttonW, buttonH);
            int fill = selectedEffect ? 1714445752 : (hovered ? 858615295 : 1712395807);
            context.method_25294(bx, by, bx + buttonW, by + buttonH, fill);
            outline(context, bx, by, buttonW, buttonH, 0, selectedEffect ? accent : (hovered ? -1435791617 : 1716146528));
            int labelColor = selectedEffect ? S9TextEffects.previewColor(id) : (hovered ? -1 : -6643278);
            context.method_27534(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, S9TextEffects.displayName(id), buttonW - 18)), bx + buttonW / 2, by + 9, labelColor);
            if (selectedEffect) {
                context.method_27535(this.field_22793, class_2561.method_43470("✓"), bx + buttonW - 12, by + 3, -1);
            }
        }

        int rows = (ids.size() + columns - 1) / columns;
        return y + rows * (buttonH + gap) - gap;
    }

    private void renderNameEffectToggle(class_332 context, int x, int y, int width, String label, boolean enabled, int mouseX, int mouseY, int accent) {
        boolean hovered = inside((double)mouseX, (double)mouseY, x, y, width, 26);
        context.method_25294(x, y, x + width, y + 26, hovered ? 857614648 : 571545119);
        context.method_27535(this.field_22793, class_2561.method_43470(label), x + 8, y + 8, enabled ? -1 : -6643278);
        int bx = x + width - 22;
        context.method_25294(bx, y + 4, bx + 16, y + 20, enabled ? 1715105791 : 1428499512);
        outline(context, bx, y + 4, 16, 16, 0, enabled ? accent : 1720357798);
        if (enabled) {
            context.method_27534(this.field_22793, class_2561.method_43470("✓"), bx + 8, y + 7, -1);
        }

    }

    private int renderModuleDetailSection(class_332 context, String label, int x, int y, int width) {
        context.method_27535(this.field_22793, class_2561.method_43470("- " + label + " -"), x, y, -9932669);
        context.method_25294(x + 92, y + 5, x + width, y + 6, 859325013);
        return y + 22;
    }

    private void renderModuleDetailRow(class_332 context, int x, int y, int width, String label, String value, boolean checkbox, int mouseX, int mouseY, int accent) {
        context.method_27535(this.field_22793, class_2561.method_43470(label.toUpperCase(Locale.ROOT)), x, y + 10, -1);
        if (checkbox) {
            boolean enabled = "true".equalsIgnoreCase(value);
            int bx = x + width - 28;
            int by = y + 7;
            context.method_25294(bx, by, bx + 16, by + 16, enabled ? ClientTheme.withAlpha(accent, 150) : 1428499512);
            outline(context, bx, by, 16, 16, 0, enabled ? accent : 1720357798);
            if (enabled) {
                context.method_27534(this.field_22793, class_2561.method_43470("✓"), bx + 8, by + 3, -1);
            }
        } else {
            int buttonW = Math.min(132, Math.max(80, this.field_22793.method_1727(value) + 22));
            int bx = x + width - buttonW;
            this.renderSquareButton(context, bx, y + 4, buttonW, 24, value, false, mouseX, mouseY, accent);
        }

        context.method_25294(x, y + 34, x + width, y + 35, 858402357);
    }

    private void renderModuleSidebar(class_332 context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        int itemY = parts.contentY + 6;
        int rowH = Math.max(18, Math.min(27, parts.height / 15));

        for(ModuleCategory category : ModuleCategory.values()) {
            boolean active = !this.showAllModules && category == this.selectedCategory;
            String label = titleCase(category.name()).toUpperCase(Locale.ROOT);
            if (active) {
                context.method_25294(parts.sideX + 2, itemY + 2, parts.sideX + 5, itemY + rowH - 3, accent);
                context.method_25294(parts.sideX + 7, itemY, parts.sideX + parts.sideW - 6, itemY + rowH, 573531592);
            }

            if (inside((double)mouseX, (double)mouseY, parts.sideX + 7, itemY, parts.sideW - 13, rowH)) {
                context.method_25294(parts.sideX + 7, itemY, parts.sideX + parts.sideW - 6, itemY + rowH, 587202559);
            }

            context.method_27535(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, label, parts.sideW - 18)), parts.sideX + 12, itemY + (rowH - 8) / 2, active ? accent : -1);
            itemY += rowH + 5;
        }

    }

    private void renderModulePreviewPanel(class_332 context, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        Module module = this.selectedModule;
        context.method_25294(x, y, x + width, y + height, 520093695);
        outline(context, x, y, width, height, 0, 1157627903);
        String title = module == null ? "EMPTY" : TextLayout.ellipsize(this.field_22793, module.getName().toUpperCase(Locale.ROOT), width - 18);
        context.method_27534(this.field_22793, class_2561.method_43470(title), x + width / 2, y + 24, -1);
        context.method_27534(this.field_22793, class_2561.method_43470(module == null ? "SELECT MODULE" : titleCase(module.getCategory().name())), x + width / 2, y + 10, -6643278);
        if (module == null) {
            context.method_27534(this.field_22793, class_2561.method_43470("Click a module card"), x + width / 2, y + height / 2, -9932669);
        } else {
            this.renderModuleIcon(context, module, x + width / 2, y + Math.max(82, height / 2 - 12), accent);
            context.method_27534(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, module.getDescription(), width - 24)), x + width / 2, y + height - 72, -6643278);
            int buttonY = y + height - 46;
            context.method_27534(this.field_22793, class_2561.method_43470(module.isEnabled() ? "ENABLED" : "DISABLED"), x + width / 2, buttonY + 1, module.isEnabled() ? -1 : -6643278);
            this.renderMiniSwitch(context, x + width / 2 - 13, buttonY + 15, module.isEnabled(), accent);
        }
    }

    private void renderLeftShell(class_332 context, int x, int y, int width, int height, int mouseX, int mouseY, int accent, boolean modules) {
        rect(context, x, y, width, height, 14, 1913063952);
        outline(context, x, y, width, height, 14, 859325013);
        if (modules) {
            this.renderSearch(context, x + 12, y + 16, width - 24, mouseX, mouseY, accent, "Search...");
        }

        if (modules) {
            int itemY = y + 66;

            for(ModuleCategory category : ModuleCategory.values()) {
                boolean active = category == this.selectedCategory;
                this.renderSideItem(context, titleCase(category.name()), this.moduleCount(category), x + 12, itemY, width - 24, 27, active, inside((double)mouseX, (double)mouseY, x + 12, itemY, width - 24, 27), accent);
                itemY += 36;
            }
        } else {
            int listTop = y + 18;
            int listBottom = y + height - 54;
            this.cosmeticSideScroll = clamp(this.cosmeticSideScroll, 0, this.maxCosmeticSideScroll(height));
            context.method_44379(x + 2, listTop, x + width - 2, listBottom);
            int itemY = listTop - this.cosmeticSideScroll;

            for(CosmeticType type : CosmeticType.values()) {
                boolean active = type == this.selectedCosmeticType;
                this.renderSideItem(context, cosmeticMenuLabel(type), -1, x + 12, itemY, width - 24, 28, active, inside((double)mouseX, (double)mouseY, x + 12, itemY, width - 24, 28), accent);
                itemY += 42;
            }

            context.method_44380();
            int max = this.maxCosmeticSideScroll(height);
            if (max > 0) {
                int trackX = x + width - 7;
                int trackY = listTop + 4;
                int trackH = Math.max(1, listBottom - listTop - 8);
                int thumbH = Math.max(18, trackH * trackH / (trackH + max));
                int thumbY = trackY + (trackH - thumbH) * this.cosmeticSideScroll / max;
                context.method_25294(trackX, trackY, trackX + 2, trackY + trackH, 1428302390);
                context.method_25294(trackX, thumbY, trackX + 2, thumbY + thumbH, ClientTheme.withAlpha(accent, 180));
            }
        }

        this.renderBackButton(context, x + 12, y + height - 40, width - 24, 28, inside((double)mouseX, (double)mouseY, x + 12, y + height - 40, width - 24, 28), accent);
    }

    private void renderModuleRow(class_332 context, Module module, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        this.renderModuleCard(context, module, x, y, width, height, mouseX, mouseY, accent);
    }

    private void renderModuleCard(class_332 context, Module module, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        boolean hovered = inside((double)mouseX, (double)mouseY, x, y, width, height);
        boolean selected = module == this.selectedModule;
        int fill = selected ? ClientTheme.withAlpha(accent, 55) : (hovered ? 1077299814 : 620756991);
        rect(context, x, y, width, height, 0, fill);
        outline(context, x, y, width, height, 0, selected ? accent : (hovered ? -855638017 : 1728053247));
        context.method_27535(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, module.getName().toUpperCase(Locale.ROOT), width - 10)), x + 6, y + 7, -1);
        context.method_27535(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, module.getDescription(), width - 42)), x + 6, y + height - 18, -9932669);
        this.renderModuleIcon(context, module, x + width / 2, y + height / 2 - 4, accent);
        this.renderCardDetailButton(context, x + width - 28, y + 5, inside((double)mouseX, (double)mouseY, x + width - 28, y + 5, 22, 22), accent);
        this.renderMiniSwitch(context, x + width - 34, y + height - 18, module.isEnabled(), accent);
    }

    private void renderCardDetailButton(class_332 context, int x, int y, boolean hovered, int accent) {
        context.method_25294(x, y, x + 22, y + 22, hovered ? 1715562630 : 1144076878);
        outline(context, x, y, 22, 22, 0, hovered ? accent : -2008060721);
        context.method_27534(this.field_22793, class_2561.method_43470("..."), x + 11, y + 6, hovered ? -1 : -6643278);
    }

    private void renderModuleDetailEmpty(class_332 context, int x, int y, int width, int height, int accent) {
        rect(context, x, y, width, height, 14, -1978986471);
        outline(context, x, y, width, height, 14, 859325013);
        rect(context, x + width / 2 - 26, y + height / 2 - 38, 52, 52, 14, ClientTheme.withAlpha(accent, 70));
        context.method_27534(this.field_22793, class_2561.method_43470("S9"), x + width / 2, y + height / 2 - 18, -1);
        context.method_27534(this.field_22793, class_2561.method_43470("Select a module"), x + width / 2, y + height / 2 + 22, -6643278);
    }

    private void renderModuleIcon(class_332 context, Module module, int cx, int cy, int accent) {
        String iconName = module.getClass().getSimpleName().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
        class_2960 icon = class_2960.method_60655("s9labclient", "textures/gui/modules/" + iconName + ".png");
        int size = Math.max(30, Math.min(40, Math.min(cx, cy) / 8));
        int x = cx - size / 2;
        int y = cy - size / 2;

        try {
            boolean hasIcon = (Boolean)MODULE_ICON_CACHE.computeIfAbsent(icon, (id) -> class_310.method_1551().method_1478().method_14486(id).isPresent());
            if (!hasIcon) {
                this.renderGeneratedModuleIcon(context, module, cx, cy, accent);
                return;
            }

            context.method_25302(class_10799.field_56883, icon, x, y, 0.0F, 0.0F, size, size, 1024, 1024, 1024, 1024);
        } catch (Exception var12) {
            this.renderGeneratedModuleIcon(context, module, cx, cy, accent);
        }

    }

    private void renderGeneratedModuleIcon(class_332 context, Module module, int cx, int cy, int accent) {
        int x = cx - 25;
        int y = cy - 20;
        rect(context, x, y, 50, 40, 6, 1998661170);
        outline(context, x, y, 50, 40, 6, ClientTheme.withAlpha(accent, 120));
        String name = module.getName().toLowerCase(Locale.ROOT);
        int soft = ClientTheme.withAlpha(accent, 185);
        int bright = ClientTheme.withAlpha(accent, 245);
        int ink = -1380097;
        if (name.contains("fps")) {
            context.method_25294(cx - 13, cy + 7, cx - 8, cy + 12, soft);
            context.method_25294(cx - 4, cy + 2, cx + 1, cy + 12, bright);
            context.method_25294(cx + 5, cy - 5, cx + 10, cy + 12, -1380097);
        } else if (!name.contains("coordinate") && !name.contains("coords")) {
            if (name.contains("ping")) {
                context.method_25294(cx - 13, cy + 8, cx - 9, cy + 12, soft);
                context.method_25294(cx - 6, cy + 4, cx - 2, cy + 12, soft);
                context.method_25294(cx + 1, cy, cx + 5, cy + 12, bright);
                context.method_25294(cx + 8, cy - 5, cx + 12, cy + 12, ink);
            } else if (!name.contains("clock") && !name.contains("date")) {
                if (name.contains("keystroke")) {
                    rect(context, cx - 5, cy - 14, 10, 10, 2, soft);
                    rect(context, cx - 17, cy - 2, 10, 10, 2, soft);
                    rect(context, cx - 5, cy - 2, 10, 10, 2, bright);
                    rect(context, cx + 7, cy - 2, 10, 10, 2, soft);
                } else if (name.contains("zoom")) {
                    outline(context, cx - 11, cy - 11, 18, 18, 9, bright);
                    context.method_25294(cx + 5, cy + 6, cx + 15, cy + 9, ink);
                } else if (name.contains("armor")) {
                    outline(context, cx - 10, cy - 13, 20, 24, 4, bright);
                    context.method_25294(cx - 7, cy - 3, cx + 8, cy, soft);
                } else if (name.contains("cape")) {
                    rect(context, cx - 10, cy - 13, 20, 27, 4, bright);
                    context.method_25294(cx - 6, cy - 9, cx + 6, cy - 6, ink);
                } else if (name.contains("bandana")) {
                    rect(context, cx - 15, cy - 5, 30, 9, 4, bright);
                    context.method_25294(cx + 6, cy + 3, cx + 16, cy + 9, soft);
                } else if (name.contains("wing")) {
                    rect(context, cx - 17, cy - 8, 15, 20, 7, soft);
                    rect(context, cx + 2, cy - 8, 15, 20, 7, bright);
                    context.method_25294(cx - 10, cy - 2, cx + 10, cy + 1, ink);
                } else if (name.contains("hat")) {
                    rect(context, cx - 10, cy - 12, 20, 15, 4, bright);
                    context.method_25294(cx - 16, cy + 3, cx + 17, cy + 7, ink);
                } else if (name.contains("halo")) {
                    outline(context, cx - 15, cy - 12, 30, 10, 5, bright);
                    context.method_25294(cx - 3, cy + 1, cx + 4, cy + 13, ink);
                } else if (name.contains("shield")) {
                    rect(context, cx - 11, cy - 14, 22, 22, 4, soft);
                    outline(context, cx - 11, cy - 14, 22, 22, 4, bright);
                    context.method_25294(cx - 5, cy - 7, cx + 6, cy + 4, ClientTheme.withAlpha(accent, 220));
                    context.method_25294(cx - 4, cy + 8, cx + 5, cy + 13, soft);
                } else if (name.contains("glint")) {
                    outline(context, cx - 10, cy - 10, 20, 20, 4, bright);
                    context.method_25294(cx - 2, cy - 13, cx + 3, cy + 14, soft);
                    context.method_25294(cx - 13, cy - 2, cx + 14, cy + 3, soft);
                } else if (name.contains("music")) {
                    context.method_25294(cx - 8, cy - 12, cx - 4, cy + 9, bright);
                    context.method_25294(cx - 8, cy - 12, cx + 10, cy - 8, bright);
                    rect(context, cx - 15, cy + 6, 9, 7, 4, ink);
                    rect(context, cx + 5, cy + 4, 9, 7, 4, ink);
                } else {
                    String label = module.getName().isBlank() ? "S9" : module.getName().substring(0, Math.min(2, module.getName().length())).toUpperCase(Locale.ROOT);
                    context.method_27534(this.field_22793, class_2561.method_43470(label), cx, cy - 4, bright);
                }
            } else {
                outline(context, cx - 11, cy - 11, 22, 22, 11, bright);
                context.method_25294(cx, cy - 8, cx + 2, cy + 2, ink);
                context.method_25294(cx, cy, cx + 8, cy + 2, ink);
            }
        } else {
            context.method_25294(cx - 13, cy, cx + 14, cy + 2, bright);
            context.method_25294(cx, cy - 13, cx + 2, cy + 14, bright);
            context.method_25294(cx + 8, cy - 9, cx + 12, cy - 5, ink);
        }

    }

    private void renderMiniSwitch(class_332 context, int x, int y, boolean enabled, int accent) {
        context.method_25294(x, y, x + 26, y + 12, enabled ? ClientTheme.withAlpha(accent, 210) : -12236205);
        int knobX = enabled ? x + 15 : x + 2;
        context.method_25294(knobX, y + 2, knobX + 8, y + 10, -1);
    }

    private void renderModuleSettingsPanel(class_332 context, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        Module module = this.selectedModule;
        rect(context, x, y, width, height, 14, 1913063952);
        outline(context, x, y, width, height, 14, 859325013);
        context.method_27535(this.field_22793, class_2561.method_43470(module.getName()), x + 18, y + 18, -1);
        context.method_27535(this.field_22793, class_2561.method_43470("Settings"), x + 18, y + 31, -9932669);
        int rowY = y + 62;
        this.renderSettingRow(context, x + 14, rowY, width - 28, "Enabled", module.isEnabled() ? "ON" : "OFF", module.isEnabled(), mouseX, mouseY, accent);
        rowY += 38;

        for(Setting<?> setting : module.getSettings()) {
            this.renderSettingRow(context, x + 14, rowY, width - 28, setting.getName(), settingValue(setting), settingActive(setting), mouseX, mouseY, accent);
            rowY += 38;
            if (rowY > y + height - 20) {
                break;
            }
        }

    }

    private void renderSettingRow(class_332 context, int x, int y, int width, String label, String value, boolean active, int mouseX, int mouseY, int accent) {
        boolean hovered = inside((double)mouseX, (double)mouseY, x, y, width, 30);
        rect(context, x, y, width, 30, 8, hovered ? -652599248 : 1712658981);
        context.method_27535(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, label, width - 90)), x + 10, y + 10, -1578254);
        if (!"ON".equals(value) && !"OFF".equals(value)) {
            context.method_27535(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, value, 74)), x + width - 84, y + 10, active ? accent : -6643278);
        } else {
            this.renderSwitch(context, x + width - 44, y + 7, active, accent);
        }

    }

    private void renderSettingsPage(class_332 context, Layout layout, int mouseX, int mouseY, int accent) {
        Module module = this.selectedModule;
        if (module == null) {
            List<Module> modules = this.filteredModules();
            if (!modules.isEmpty()) {
                module = (Module)modules.get(0);
            }
        }

        int x = layout.x + 20;
        int y = layout.bodyY() + 10;
        int width = layout.width - 40;
        context.method_27535(this.field_22793, class_2561.method_43470(module == null ? "Settings" : module.getName()), x + 8, y + 5, -1);
        context.method_25294(x, y + 30, x + width, y + 31, -14539220);
        int editorY = y + 44;
        boolean editorHovered = inside((double)mouseX, (double)mouseY, x + 8, editorY, 180, 30);
        rect(context, x + 8, editorY, 180, 30, 8, editorHovered ? ClientTheme.withAlpha(accent, 145) : 1712658981);
        outline(context, x + 8, editorY, 180, 30, 8, editorHovered ? accent : 1714633548);
        context.method_27535(this.field_22793, class_2561.method_43470("Open HUD Editor"), x + 20, editorY + 10, editorHovered ? -1 : -1578254);
        if (module == null) {
            context.method_27534(this.field_22793, class_2561.method_43470("Select a module first."), x + width / 2, y + 98, -6643278);
        } else {
            int rowY = y + 88;
            int colW = (width - 44) / 2;
            this.renderSettingsLine(context, x + 8, rowY, colW, "Enabled", module.isEnabled() ? "ON" : "OFF", module.isEnabled(), mouseX, mouseY, accent);
            rowY += 36;
            int i = 0;

            for(Setting<?> setting : module.getSettings()) {
                int col = i % 2;
                int row = i / 2;
                int sx = x + 8 + col * (colW + 28);
                int sy = rowY + row * 36;
                this.renderSettingsLine(context, sx, sy, colW, setting.getName(), settingValue(setting), settingActive(setting), mouseX, mouseY, accent);
                ++i;
            }

        }
    }

    private void renderSettingsLine(class_332 context, int x, int y, int width, String label, String value, boolean active, int mouseX, int mouseY, int accent) {
        context.method_27535(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, label, width / 2)), x, y + 8, active ? -1578254 : -6643278);
        if (!"ON".equals(value) && !"OFF".equals(value)) {
            context.method_27535(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, value, 86)), x + width - 92, y + 8, active ? accent : -9932669);
        } else {
            this.renderMiniSwitch(context, x + width - 32, y + 7, active, accent);
        }

        context.method_25294(x, y + 28, x + width, y + 29, 858402357);
    }

    private void renderCosmetics(class_332 context, Layout layout, int mouseX, int mouseY, int accent) {
        CosmeticLayout parts = this.cosmeticLayout(layout);
        PremiumRender.shopPanel(context, parts.x, parts.y, parts.width, parts.height, parts.topbarH, parts.footerH);
        this.renderShopTopbar(context, parts, mouseX, mouseY, accent);
        this.renderFooterTabs(context, parts, mouseX, mouseY, accent);
        this.renderShopSidebar(context, parts, mouseX, mouseY, accent);
        context.method_25294(parts.gridX - 1, parts.contentY, parts.gridX, parts.y + parts.height - parts.footerH, 1728053247);
        if (parts.preview.width > 0) {
            context.method_25294(parts.preview.x - 1, parts.contentY, parts.preview.x, parts.y + parts.height - parts.footerH, 1728053247);
        }

        if (this.plusShopOpen) {
            this.renderPlusShop(context, parts, mouseX, mouseY, accent);
        } else if (this.cosmeticDetailsOpen && this.selectedCosmetic != null) {
            this.renderCosmeticDetails(context, parts, mouseX, mouseY, accent);
        } else {
            context.method_27535(this.field_22793, class_2561.method_43470(this.shopTitle()), parts.gridX, parts.contentY + 12, -1);
            context.method_25294(parts.gridX + 2, parts.contentY + 36, parts.gridX + parts.gridW - 4, parts.contentY + 37, 1157627903);
            context.method_44379(parts.gridX, parts.gridY, parts.gridX + parts.gridW, parts.gridY + parts.gridH);
            Grid grid = this.cosmeticGrid(parts.gridW, parts.gridH);
            List<Cosmetic> cosmetics = this.filteredCosmetics();
            int baseY = parts.gridY - this.scroll;

            for(int i = 0; i < cosmetics.size(); ++i) {
                Cosmetic cosmetic = (Cosmetic)cosmetics.get(i);
                int cardX = parts.gridX + i % grid.columns * (grid.cardW + grid.gap);
                int cardY = baseY + i / grid.columns * (grid.cardH + grid.gap);
                if (cardY + grid.cardH >= parts.gridY && cardY <= parts.gridY + parts.gridH) {
                    this.renderCosmeticCard(context, cosmetic, cardX, cardY, grid.cardW, grid.cardH, mouseX, mouseY, accent);
                }
            }

            context.method_44380();
            if (parts.preview.width > 0) {
                this.renderCosmeticPreview(context, parts.preview.x, parts.preview.y, parts.preview.width, parts.preview.height, accent);
            }

        }
    }

    private void renderCosmeticDetails(class_332 context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        Cosmetic cosmetic = this.selectedCosmetic;
        if (this.isBatWingCosmetic(cosmetic)) {
            this.renderBatWingStyleDetails(context, parts, mouseX, mouseY, accent);
        } else {
            int contentBottom = parts.y + parts.height - parts.footerH;
            int variantX = parts.gridX;
            int variantY = parts.contentY + 18;
            int variantW = Math.min(68, Math.max(48, parts.gridW / 5));
            context.method_27535(this.field_22793, class_2561.method_43470("VARIANTS"), variantX, variantY, -1);
            List<Cosmetic> variants = this.variantsForSelectedCosmetic();
            int thumbY = variantY + 24;
            int thumbSize = Math.min(48, Math.max(34, (contentBottom - thumbY - 10) / Math.max(1, Math.min(6, variants.size())) - 5));

            for(int i = 0; i < variants.size() && i < 7; ++i) {
                Cosmetic variant = (Cosmetic)variants.get(i);
                boolean selected = variant == cosmetic;
                int tx = variantX + 4;
                int ty = thumbY + i * (thumbSize + 7);
                context.method_25294(tx, ty, tx + thumbSize, ty + thumbSize, selected ? 1429169608 : 857810482);
                outline(context, tx, ty, thumbSize, thumbSize, 0, selected ? accent : 1720357798);
                this.drawCosmeticTexture(context, variant, tx + 4, ty + 4, thumbSize - 8, thumbSize - 8, accent);
            }

            int previewX = variantX + variantW + 16;
            int previewW = Math.max(120, parts.width - (previewX - parts.x) - 26);
            int previewY = parts.contentY;
            int previewH = parts.contentH;
            context.method_25294(previewX, previewY, previewX + previewW, previewY + previewH, 270607922);
            outline(context, previewX, previewY, previewW, previewH, 0, 1157627903);
            String type = cosmeticMenuLabel(cosmetic.type()).toUpperCase(Locale.ROOT);
            String title = cosmetic.displayName().toUpperCase(Locale.ROOT);
            context.method_27534(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, type, previewW - 40)), previewX + previewW / 2, previewY + 18, -6643278);
            context.method_27534(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, title, previewW - 40)), previewX + previewW / 2, previewY + 32, -1);
            int backX = previewX + 12;
            this.renderSquareButton(context, backX, previewY + 12, 62, 24, "< BACK", false, mouseX, mouseY, accent);
            class_310 client = class_310.method_1551();
            if (client.field_1724 != null) {
                CosmeticPreviewRenderer.draw(context, client.field_1724, cosmetic, this.previewTryOn, this.previewPose, previewX + Math.max(28, previewW / 6), previewY + 54, previewX + previewW - Math.max(28, previewW / 6), previewY + previewH - 88, this.previewZoom, this.previewYaw, this.previewPitch, CosmeticPreviewContext.stableKey("details", cosmetic), BaseVisibility.FULL);
                context.method_27534(this.field_22793, class_2561.method_43470("DRAG / SCROLL"), previewX + previewW / 2, previewY + previewH - 91, -806884622);
            } else {
                this.drawCosmeticTexture(context, cosmetic, previewX + previewW / 2 - 48, previewY + previewH / 2 - 58, 96, 96, accent);
                context.method_27534(this.field_22793, class_2561.method_43470("Join a world for 3D preview"), previewX + previewW / 2, previewY + previewH / 2 + 38, -6643278);
            }

            context.method_27535(this.field_22793, class_2561.method_43470("<"), previewX + 14, previewY + previewH / 2, -1);
            context.method_27535(this.field_22793, class_2561.method_43470(">"), previewX + previewW - 22, previewY + previewH / 2, -1);
            this.renderPreviewControls(context, previewX, previewY + previewH - 78, previewW, mouseX, mouseY, accent);
            int buttonY = previewY + previewH - 36;
            int buttonW = Math.min(260, previewW - 40);
            rect(context, previewX + (previewW - buttonW) / 2, buttonY, buttonW, 26, 0, BackendState.online() ? ClientTheme.withAlpha(accent, 210) : 1427446309);
            context.method_27534(this.field_22793, class_2561.method_43470(this.cosmeticActionLabel(cosmetic)), previewX + previewW / 2, buttonY + 9, -1);
        }
    }

    private void renderBatWingStyleDetails(class_332 context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        Cosmetic cosmetic = this.selectedCosmetic;
        int contentBottom = parts.y + parts.height - parts.footerH;
        int styleX = parts.gridX;
        int styleY = parts.contentY + 18;
        int styleW = Math.min(86, Math.max(58, parts.gridW / 4));
        context.method_27535(this.field_22793, class_2561.method_43470("STYLES"), styleX, styleY, -1);
        this.renderBatWingStyleOption(context, "White", styleX + 4, styleY + 26, styleW, mouseX, mouseY, accent);
        this.renderBatWingStyleOption(context, "Black", styleX + 4, styleY + 26 + styleW + 12, styleW, mouseX, mouseY, accent);
        int previewX = styleX + styleW + 22;
        int previewW = Math.max(150, parts.width - (previewX - parts.x) - 26);
        int previewY = parts.contentY;
        int previewH = parts.contentH;
        context.method_25294(previewX, previewY, previewX + previewW, previewY + previewH, 270607922);
        outline(context, previewX, previewY, previewW, previewH, 0, 1157627903);
        this.renderSquareButton(context, previewX + 12, previewY + 12, 62, 24, "< BACK", false, mouseX, mouseY, accent);
        context.method_27534(this.field_22793, class_2561.method_43470("WINGS"), previewX + previewW / 2, previewY + 18, -6643278);
        context.method_27534(this.field_22793, class_2561.method_43470(("Bat Wings - " + this.batWingStyle()).toUpperCase(Locale.ROOT)), previewX + previewW / 2, previewY + 32, -1);
        class_310 client = class_310.method_1551();
        if (client.field_1724 != null) {
            CosmeticPreviewRenderer.draw(context, client.field_1724, cosmetic, this.previewTryOn, this.previewPose, previewX + Math.max(28, previewW / 6), previewY + 54, previewX + previewW - Math.max(28, previewW / 6), previewY + previewH - 88, this.previewZoom, this.previewYaw, this.previewPitch, CosmeticPreviewContext.stableKey("bat-style", cosmetic), BaseVisibility.FULL);
            context.method_27534(this.field_22793, class_2561.method_43470("DRAG / SCROLL"), previewX + previewW / 2, previewY + previewH - 91, -806884622);
        } else {
            this.drawCosmeticTexture(context, cosmetic, previewX + previewW / 2 - 48, previewY + previewH / 2 - 58, 96, 96, accent);
            context.method_27534(this.field_22793, class_2561.method_43470("Join a world for 3D preview"), previewX + previewW / 2, previewY + previewH / 2 + 38, -6643278);
        }

        this.renderPreviewControls(context, previewX, previewY + previewH - 78, previewW, mouseX, mouseY, accent);
        int buttonY = previewY + previewH - 36;
        int buttonW = Math.min(260, previewW - 40);
        rect(context, previewX + (previewW - buttonW) / 2, buttonY, buttonW, 26, 0, BackendState.online() ? ClientTheme.withAlpha(accent, 210) : 1427446309);
        context.method_27534(this.field_22793, class_2561.method_43470(this.cosmeticActionLabel(cosmetic)), previewX + previewW / 2, buttonY + 9, -1);
    }

    private void renderBatWingStyleOption(class_332 context, String style, int x, int y, int size, int mouseX, int mouseY, int accent) {
        boolean selected = style.equalsIgnoreCase(this.batWingStyle());
        boolean hovered = inside((double)mouseX, (double)mouseY, x, y, size, size);
        context.method_25294(x, y, x + size, y + size, selected ? ClientTheme.withAlpha(accent, 72) : (hovered ? 1144408678 : 857810482));
        outline(context, x, y, size, size, 0, selected ? accent : (hovered ? -1426063361 : 1720357798));
        String var10001 = style.toLowerCase(Locale.ROOT);
        class_2960 texture = class_2960.method_60655("s9labclient", "textures/cosmetics/wings/" + var10001 + "_wings.png");
        context.method_25302(class_10799.field_56883, texture, x + 6, y + 8, 0.0F, 0.0F, size - 12, Math.max(18, size - 26), 64, 64, 64, 64);
        context.method_27534(this.field_22793, class_2561.method_43470(style.toUpperCase(Locale.ROOT)), x + size / 2, y + size - 14, selected ? -1 : -1578254);
        if (selected) {
            context.method_27535(this.field_22793, class_2561.method_43470("✓"), x + size - 13, y + size - 15, -11931025);
        }

    }

    private void renderPlusShop(class_332 context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        context.method_27535(this.field_22793, class_2561.method_43470("S9LAB CLIENT+"), parts.gridX, parts.contentY + 12, -1);
        context.method_25294(parts.gridX + 2, parts.contentY + 36, parts.gridX + parts.gridW - 4, parts.contentY + 37, 1157627903);
        boolean stacked = parts.gridW < 250;
        int cardGap = Math.max(8, parts.gridW / 42);
        int cardW = stacked ? Math.max(96, parts.gridW - 4) : Math.max(112, (parts.gridW - cardGap) / 2);
        int cardH = stacked ? Math.max(92, (parts.gridH - cardGap - 10) / 2) : Math.min(170, Math.max(122, parts.gridH - 16));
        int cardY = parts.gridY + 4;
        this.renderPlusPlanCard(context, parts.gridX, cardY, cardW, cardH, "1 MONTH", 750L, "plus_1m", mouseX, mouseY, accent);
        this.renderPlusPlanCard(context, stacked ? parts.gridX : parts.gridX + cardW + cardGap, stacked ? cardY + cardH + cardGap : cardY, cardW, cardH, "3 MONTHS", 1900L, "plus_3m", mouseX, mouseY, accent);
        if (parts.preview.width > 0) {
            int px = parts.preview.x;
            int py = parts.preview.y;
            int pw = parts.preview.width;
            int ph = parts.preview.height;
            context.method_25294(px, py, px + pw, py + ph, 520093695);
            outline(context, px, py, pw, ph, 0, 1157627903);
            context.method_27534(this.field_22793, class_2561.method_43470("S9LAB CLIENT+"), px + pw / 2, py + 14, -1);
            if (BackendState.plusActive()) {
                boolean settingsHovered = inside((double)mouseX, (double)mouseY, px + pw - 34, py + 8, 24, 22);
                rect(context, px + pw - 34, py + 8, 24, 22, 6, settingsHovered ? ClientTheme.withAlpha(accent, 180) : 1712658981);
                context.method_27534(this.field_22793, class_2561.method_43470("..."), px + pw - 22, py + 14, -1);
            }

            context.method_27534(this.field_22793, class_2561.method_43470(BackendState.plusActive() ? "ACTIVE" : "NOT ACTIVE"), px + pw / 2, py + 30, BackendState.plusActive() ? -11931025 : -6643278);
            int icon = Math.min(72, Math.max(42, pw / 2));
            context.method_25290(class_10799.field_56883, class_2960.method_60655("s9labclient", "textures/font/s9_icon_plus.png"), px + (pw - icon) / 2, py + 58, 0.0F, 0.0F, icon, icon, icon, icon);
            String expires = BackendState.plusActive() ? "UNTIL " + plusExpiryLabel(BackendState.plusExpiresAt()) : "BUY A PLAN";
            context.method_27534(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, expires, pw - 16)), px + pw / 2, py + 142, BackendState.plusActive() ? -11931025 : -19372);
            int textY = py + 166;
            context.method_27535(this.field_22793, class_2561.method_43470("- Plus icon in tablist"), px + 14, textY, -1578254);
            context.method_27535(this.field_22793, class_2561.method_43470("- Plus icon in F5 name"), px + 14, textY + 16, -1578254);
            context.method_27535(this.field_22793, class_2561.method_43470("- Optional rainbow name"), px + 14, textY + 32, -1578254);
            context.method_27535(this.field_22793, class_2561.method_43470("- Exclusive animated Plus aura"), px + 14, textY + 48, -1578254);
        }
    }

    private void renderPlusPlanCard(class_332 context, int x, int y, int width, int height, String label, long price, String planId, int mouseX, int mouseY, int accent) {
        boolean hovered = inside((double)mouseX, (double)mouseY, x, y, width, height);
        context.method_25294(x, y, x + width, y + height, hovered ? 1077299814 : 641427161);
        outline(context, x, y, width, height, 0, hovered ? accent : 1728053247);
        context.method_27535(this.field_22793, class_2561.method_43470(label), x + 8, y + 8, -1);
        context.method_27535(this.field_22793, class_2561.method_43470("S9Lab Client+"), x + 8, y + 24, -6643278);
        int icon = Math.min(54, Math.max(34, width / 4));
        context.method_25290(class_10799.field_56883, class_2960.method_60655("s9labclient", "textures/font/s9_icon_plus.png"), x + (width - icon) / 2, y + 44, 0.0F, 0.0F, icon, icon, icon, icon);
        context.method_27534(this.field_22793, class_2561.method_43470(formatCoins(price) + " COINS"), x + width / 2, y + height - 48, -19372);
        boolean alreadyActive = BackendState.plusActive();
        boolean canBuy = BackendState.online() && !alreadyActive && BackendState.coins() >= price;
        boolean canGift = BackendState.online() && BackendState.coins() >= price;
        int gap = 6;
        int buttonW = Math.max(34, (width - 24 - gap) / 2);
        int buttonY = y + height - 32;
        int buyColor = canBuy ? ClientTheme.withAlpha(accent, 210) : 1427446309;
        rect(context, x + 12, buttonY, buttonW, 24, 0, buyColor);
        outline(context, x + 12, buttonY, buttonW, 24, 0, canBuy ? accent : 859325013);
        String buyLabel = alreadyActive ? "ACTIVE" : (canBuy ? "BUY" : (BackendState.online() ? "NO COINS" : "OFFLINE"));
        context.method_27534(this.field_22793, class_2561.method_43470(buyLabel), x + 12 + buttonW / 2, buttonY + 8, canBuy ? -1 : -6643278);
        int giftX = x + 12 + buttonW + gap;
        rect(context, giftX, buttonY, buttonW, 24, 0, canGift ? -14998220 : 1427446309);
        outline(context, giftX, buttonY, buttonW, 24, 0, canGift ? ClientTheme.withAlpha(accent, 190) : 859325013);
        context.method_27534(this.field_22793, class_2561.method_43470("GIFT"), giftX + buttonW / 2, buttonY + 8, canGift ? -1 : -6643278);
    }

    private void renderCatalog(class_332 context, Layout layout, int mouseX, int mouseY, int accent) {
        this.renderCosmetics(context, layout, mouseX, mouseY, accent);
    }

    private void renderShopTopbar(class_332 context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        int buttonY = parts.y + 11;
        int buttonH = Math.max(18, Math.min(24, parts.topbarH - 18));
        int x = parts.x + 8;
        this.renderSquareButton(context, x, buttonY, 72, buttonH, cosmeticMenuLabel(this.selectedCosmeticType).toUpperCase(Locale.ROOT), !this.showAllCosmetics && !this.plusShopOpen, mouseX, mouseY, accent);
        x += 78;
        this.renderSquareButton(context, x, buttonY, 58, buttonH, "ALL", this.showAllCosmetics && !this.plusShopOpen, mouseX, mouseY, accent);
        x += 66;
        int searchW = Math.max(90, Math.min(190, parts.gridX + parts.gridW - x - 10));
        this.renderShopSearch(context, x, buttonY, searchW, buttonH, mouseX, mouseY, accent);
        int coinsW = Math.max(78, Math.min(120, parts.width / 8));
        int coinsX = parts.x + parts.width - coinsW - 12;
        rect(context, coinsX, buttonY - 3, coinsW, buttonH + 6, 0, -13271112);
        outline(context, coinsX, buttonY - 3, coinsW, buttonH + 6, 0, -10110478);
        context.method_27534(this.field_22793, class_2561.method_43470(formatCoins(BackendState.coins()) + " COINS"), coinsX + coinsW / 2, buttonY + (buttonH - 8) / 2, -1);
    }

    private void renderShopSidebar(class_332 context, CosmeticLayout parts, int mouseX, int mouseY, int accent) {
        int itemY = parts.contentY + 6 - this.cosmeticSideScroll;
        context.method_44379(parts.sideX, parts.contentY, parts.sideX + parts.sideW, parts.y + parts.height - parts.footerH);

        for(CosmeticType type : CosmeticType.values()) {
            boolean active = type == this.selectedCosmeticType && !this.plusShopOpen;
            int rowH = Math.max(18, Math.min(27, parts.height / 15));
            String label = cosmeticMenuLabel(type).toUpperCase(Locale.ROOT);
            int color = active && !this.showAllCosmetics ? accent : -1;
            if (active && !this.showAllCosmetics) {
                context.method_25294(parts.sideX + 2, itemY + 2, parts.sideX + 5, itemY + rowH - 3, accent);
                context.method_25294(parts.sideX + 7, itemY, parts.sideX + parts.sideW - 6, itemY + rowH, 573531592);
            }

            if (inside((double)mouseX, (double)mouseY, parts.sideX + 7, itemY, parts.sideW - 13, rowH)) {
                context.method_25294(parts.sideX + 7, itemY, parts.sideX + parts.sideW - 6, itemY + rowH, 587202559);
            }

            context.method_27535(this.field_22793, class_2561.method_43470(label), parts.sideX + 12, itemY + (rowH - 8) / 2, color);
            itemY += rowH + 5;
        }

        int rowH = Math.max(18, Math.min(27, parts.height / 15));
        boolean plusActive = this.plusShopOpen;
        if (plusActive) {
            context.method_25294(parts.sideX + 2, itemY + 2, parts.sideX + 5, itemY + rowH - 3, accent);
            context.method_25294(parts.sideX + 7, itemY, parts.sideX + parts.sideW - 6, itemY + rowH, 573531592);
        }

        if (inside((double)mouseX, (double)mouseY, parts.sideX + 7, itemY, parts.sideW - 13, rowH)) {
            context.method_25294(parts.sideX + 7, itemY, parts.sideX + parts.sideW - 6, itemY + rowH, 587202559);
        }

        context.method_27535(this.field_22793, class_2561.method_43470("S9LAB+"), parts.sideX + 12, itemY + (rowH - 8) / 2, plusActive ? accent : -1);
        itemY += rowH + 5;
        if (inside((double)mouseX, (double)mouseY, parts.sideX + 7, itemY, parts.sideW - 13, rowH)) {
            context.method_25294(parts.sideX + 7, itemY, parts.sideX + parts.sideW - 6, itemY + rowH, 587202559);
        }

        context.method_27535(this.field_22793, class_2561.method_43470("LOADOUTS"), parts.sideX + 12, itemY + (rowH - 8) / 2, -1);
        context.method_44380();
    }

    private void renderShopSearch(class_332 context, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        boolean hovered = inside((double)mouseX, (double)mouseY, x, y, width, height);
        context.method_25294(x, y, x + width, y + height, -268435456);
        outline(context, x, y, width, height, 0, this.searchFocused ? accent : (hovered ? -1426063361 : -10787725));
        String text = this.search.isBlank() && !this.searchFocused ? "SEARCH..." : this.search + (this.searchFocused && System.currentTimeMillis() / 450L % 2L == 0L ? "_" : "");
        context.method_27535(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, text.toUpperCase(Locale.ROOT), width - 10)), x + 5, y + (height - 8) / 2, this.search.isBlank() && !this.searchFocused ? -6643278 : -1);
    }

    private void renderSquareButton(class_332 context, int x, int y, int width, int height, String label, boolean active, int mouseX, int mouseY, int accent) {
        boolean hovered = inside((double)mouseX, (double)mouseY, x, y, width, height);
        int color = active ? -14670026 : (hovered ? -14342098 : -15263459);
        context.method_25294(x, y, x + width, y + height, color);
        outline(context, x, y, width, height, 0, active ? accent : -12828341);
        context.method_27534(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, label, width - 8)), x + width / 2, y + (height - 8) / 2, active ? -1 : -1578254);
    }

    private void renderCosmeticCard(class_332 context, Cosmetic cosmetic, int x, int y, int width, int height, int mouseX, int mouseY, int accent) {
        boolean hovered = inside((double)mouseX, (double)mouseY, x, y, width, height);
        boolean equipped = cosmetic.id().equals(S9LabClientClient.getConfigManager().getEquippedCosmeticId(cosmetic.type()));
        boolean owned = S9LabClientClient.getConfigManager().isUnlocked(cosmetic.id());
        boolean selected = cosmetic == this.selectedCosmetic;
        BackendState.ShopCosmetic shop = BackendState.catalog(cosmetic.id());
        int border = !selected && !equipped ? (hovered ? -855638017 : 1728053247) : ClientTheme.withAlpha(accent, 240);
        int var10000;
        switch (cosmetic.type()) {
            case WINGS:
            case GLINT:
                var10000 = 859530969;
                break;
            case SHIELD:
                var10000 = 859456864;
                break;
            case EMOTE:
                var10000 = 859336253;
                break;
            default:
                var10000 = 654311423;
        }

        int tint = var10000;
        context.method_25294(x, y, x + width, y + height, hovered ? 1077299814 : tint);
        outline(context, x, y, width, height, 0, border);
        String name = TextLayout.ellipsize(this.field_22793, cosmetic.displayName().toUpperCase(Locale.ROOT), width - 8);
        context.method_27535(this.field_22793, class_2561.method_43470(name), x + 5, y + 5, -1);
        if (this.hasCosmeticDetailOptions(cosmetic)) {
            this.renderCardDetailButton(context, x + width - 28, y + 5, inside((double)mouseX, (double)mouseY, x + width - 28, y + 5, 22, 22), accent);
        }

        int previewX = x + 10;
        int previewY = y + 18;
        int previewW = width - 20;
        int previewH = height - 38;
        class_310 client = class_310.method_1551();
        if (cosmetic.type() == CosmeticType.CAPE) {
            this.renderCapeCardPreview(context, cosmetic, previewX, previewY, previewW, previewH);
        } else if (client.field_1724 != null) {
            float var34;
            switch (cosmetic.type()) {
                case WINGS:
                case SHIELD:
                    var34 = 0.0F;
                    break;
                default:
                    var34 = 180.0F;
            }

            float cardYaw = var34;
            CosmeticPreviewContext.BaseVisibility var35;
            switch (cosmetic.type()) {
                case WINGS:
                    var35 = BaseVisibility.HIDDEN;
                    break;
                case GLINT:
                case SHIELD:
                case EMOTE:
                default:
                    var35 = BaseVisibility.FULL;
                    break;
                case HAT:
                case HALO:
                case BANDANA:
                    var35 = BaseVisibility.HEAD_ONLY;
            }

            CosmeticPreviewContext.BaseVisibility cardVisibility = var35;
            int baseScale = Math.max(28, Math.min(62, Math.min(previewW, previewH) / 2));
            int cardScale = cardVisibility == BaseVisibility.HEAD_ONLY ? Math.min(96, baseScale + 28) : baseScale;
            int previewKey = CosmeticPreviewContext.stableKey("card", cosmetic);
            CosmeticPreviewRenderer.draw(context, client.field_1724, cosmetic, true, PreviewPose.IDLE, previewX, previewY, previewX + previewW, previewY + previewH, cardScale, cardYaw, 5.0F, previewKey, cardVisibility);
        } else {
            this.drawCosmeticTexture(context, cosmetic, previewX, previewY, previewW, previewH, accent);
        }

        int badgeW = owned ? 58 : Math.min(width - 26, Math.max(52, this.field_22793.method_1727(shop.price() + "✦") + 12));
        int badgeX = x + Math.max(6, (width - badgeW) / 2 - 7);
        int badgeY = y + height - 22;
        if (owned) {
            String label = cosmetic.type() == CosmeticType.SHIELD ? "DISABLED" : (equipped ? "EQUIP" : "OWNED");
            int labelColor = cosmetic.type() == CosmeticType.SHIELD ? -6643278 : (equipped ? accent : -11931025);
            context.method_27534(this.field_22793, class_2561.method_43470(label), x + width / 2, y + height - 16, labelColor);
        } else {
            int priceColor = rarityColor(cosmetic);
            context.method_25294(badgeX, badgeY, badgeX + badgeW, badgeY + 17, ClientTheme.withAlpha(priceColor, 75));
            outline(context, badgeX, badgeY, badgeW, 17, 0, priceColor);
            context.method_27534(this.field_22793, class_2561.method_43470(shop.price() + "✦"), badgeX + badgeW / 2, badgeY + 5, priceColor);
        }

        if (owned || equipped) {
            context.method_27535(this.field_22793, class_2561.method_43470("✓"), x + width - 13, y + height - 14, -11931025);
        }

    }

    private void renderCapeCardPreview(class_332 context, Cosmetic cosmetic, int x, int y, int width, int height) {
        if (cosmetic != null && cosmetic.texture() != null && width > 0 && height > 0) {
            int textureWidth = 2048;
            int textureHeight = 1024;
            int sourceX = 32;
            int sourceY = 32;
            int sourceWidth = 320;
            int sourceHeight = 512;
            int availableW = Math.max(1, width - 28);
            int availableH = Math.max(1, height - 18);
            float aspect = 0.625F;
            int drawH = availableH;
            int drawW = Math.max(1, Math.round((float)availableH * aspect));
            if (drawW > availableW) {
                drawW = availableW;
                drawH = Math.max(1, Math.round((float)availableW / aspect));
            }

            int drawX = x + (width - drawW) / 2;
            int drawY = y + (height - drawH) / 2;
            context.method_25294(drawX + 3, drawY + 4, drawX + drawW + 3, drawY + drawH + 4, 1174405120);
            context.method_25302(class_10799.field_56883, cosmetic.texture(), drawX, drawY, 32.0F, 32.0F, drawW, drawH, 320, 512, 2048, 1024);
            outline(context, drawX, drawY, drawW, drawH, 0, 1434158229);
        }
    }

    private void drawCosmeticTexture(class_332 context, Cosmetic cosmetic, int x, int y, int width, int height, int accent) {
        try {
            if (cosmetic.texture() != null) {
                context.method_25290(class_10799.field_56883, cosmetic.texture(), x, y, 0.0F, 0.0F, width, height, width, height);
                return;
            }
        } catch (Throwable var12) {
        }

        int color = cosmetic.type() == CosmeticType.CAPE ? accent : -10919564;
        switch (cosmetic.type()) {
            case WINGS:
                rect(context, x, y + 12, width / 2 - 4, height - 20, 8, ClientTheme.withAlpha(color, 190));
                rect(context, x + width / 2 + 4, y + 12, width / 2 - 4, height - 20, 8, ClientTheme.withAlpha(color, 190));
                break;
            case GLINT:
                outline(context, x + 8, y + 8, width - 16, height - 16, 8, ClientTheme.withAlpha(color, 255));
                rect(context, x + width / 2 - 6, y + height / 2 - 6, 12, 12, 6, ClientTheme.withAlpha(color, 190));
                break;
            case SHIELD:
                int centerX = x + width / 2;
                int top = y + 8;
                int bottom = y + height - 8;
                rect(context, centerX - width / 4, top, width / 2, height - 20, 4, ClientTheme.withAlpha(color, 185));
                outline(context, centerX - width / 4, top, width / 2, height - 20, 4, ClientTheme.withAlpha(accent, 220));
                rect(context, centerX - 7, y + height / 2 - 7, 14, 14, 3, ClientTheme.withAlpha(accent, 180));
                context.method_25294(centerX - 5, bottom - 10, centerX + 5, bottom, ClientTheme.withAlpha(color, 185));
                break;
            case EMOTE:
                rect(context, x + width / 2 - 14, y + 12, 28, 28, 8, ClientTheme.withAlpha(color, 180));
                context.method_27534(this.field_22793, class_2561.method_43470("♪"), x + width / 2, y + 22, -1);
                break;
            case HAT:
                rect(context, x + 8, y + 12, width - 16, height / 3, 6, ClientTheme.withAlpha(color, 200));
                break;
            case HALO:
                outline(context, x + 8, y + 12, width - 16, 14, 7, ClientTheme.withAlpha(color, 255));
                break;
            case BANDANA:
                rect(context, x + 6, y + height / 2 - 5, width - 12, 10, 5, ClientTheme.withAlpha(color, 200));
                break;
            case CAPE:
                rect(context, x + 14, y, width - 28, height, 5, ClientTheme.withAlpha(color, 210));
                break;
            case SHOULDER:
                rect(context, x + 16, y + 18, width - 32, height - 30, 7, ClientTheme.withAlpha(color, 190));
        }

    }

    private void renderCosmeticPreview(class_332 context, int x, int y, int width, int height, int accent) {
        Cosmetic cosmetic = this.selectedCosmetic;
        context.method_25294(x, y, x + width, y + height, 520093695);
        outline(context, x, y, width, height, 0, 1157627903);
        String title = cosmetic == null ? "EMPTY" : TextLayout.ellipsize(this.field_22793, cosmetic.displayName().toUpperCase(Locale.ROOT), width - 18);
        context.method_27534(this.field_22793, class_2561.method_43470(title), x + width / 2, y + 22, cosmetic == null ? -1578254 : -1);
        String state = BackendState.online() ? "CUSTOM " + cosmeticMenuLabel(this.selectedCosmeticType).toUpperCase(Locale.ROOT) : friendlyBackendStatus(BackendState.status());
        context.method_27534(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, state, width - 18)), x + width / 2, y + 10, BackendState.online() ? -570425345 : -19372);
        int boxY = y + 54;
        int boxH = Math.max(64, height - 148);
        class_310 client = class_310.method_1551();
        if (client.field_1724 != null) {
            CosmeticPreviewRenderer.draw(context, client.field_1724, cosmetic, this.previewTryOn, this.previewPose, x + 20, boxY + 4, x + width - 20, boxY + boxH, this.previewZoom, this.previewYaw, this.previewPitch, CosmeticPreviewContext.stableKey("panel", cosmetic), BaseVisibility.FULL);
            if (height > 260) {
                context.method_27534(this.field_22793, class_2561.method_43470("DRAG / SCROLL"), x + width / 2, boxY + boxH - 12, -1075320078);
            }
        } else {
            context.method_27534(this.field_22793, class_2561.method_43470("Join a world for 3D preview"), x + width / 2, boxY + boxH / 2, -6643278);
        }

        this.renderPreviewControls(context, x, y + height - 78, width, -1, -1, accent);
        int buttonY = y + height - 46;
        String status = this.cosmeticActionLabel(cosmetic);
        boolean disabled = cosmetic == null || !BackendState.online();
        boolean owned = cosmetic != null && BackendState.owned(cosmetic.id());
        if (owned) {
            int half = (width - 40) / 2;
            rect(context, x + 16, buttonY, half, 28, 0, disabled ? 1427446309 : ClientTheme.withAlpha(accent, 210));
            context.method_27534(this.field_22793, class_2561.method_43470(status), x + 16 + half / 2, buttonY + 10, -1);
            rect(context, x + 24 + half, buttonY, half, 28, 0, disabled ? 1427446309 : -14998220);
            outline(context, x + 24 + half, buttonY, half, 28, 0, disabled ? 859325013 : ClientTheme.withAlpha(accent, 190));
            context.method_27534(this.field_22793, class_2561.method_43470("Gift"), x + 24 + half + half / 2, buttonY + 10, disabled ? -6643278 : -1);
        } else {
            rect(context, x + 16, buttonY, width - 32, 28, 0, disabled ? 1427446309 : ClientTheme.withAlpha(accent, 210));
            context.method_27534(this.field_22793, class_2561.method_43470(status), x + width / 2, buttonY + 10, -1);
        }

    }

    private void renderPreviewControls(class_332 context, int x, int y, int width, int mouseX, int mouseY, int accent) {
        int gap = 4;
        int buttonW = Math.max(28, (width - 24 - gap * 3) / 4);
        int startX = x + 12;
        String tryOn = this.previewTryOn ? "TRY ON ✓" : "TRY ON";
        this.renderSquareButton(context, startX, y, buttonW, 24, tryOn, this.previewTryOn, mouseX, mouseY, accent);
        this.renderSquareButton(context, startX + buttonW + gap, y, buttonW, 24, "POSE " + this.previewPose.label().toUpperCase(Locale.ROOT), false, mouseX, mouseY, accent);
        this.renderSquareButton(context, startX + (buttonW + gap) * 2, y, buttonW, 24, "RESET", false, mouseX, mouseY, accent);
        this.renderSquareButton(context, startX + (buttonW + gap) * 3, y, buttonW, 24, "STUDIO", false, mouseX, mouseY, accent);
    }

    private boolean handlePreviewControlsClick(Rect bounds, int mouseX, int mouseY) {
        if (bounds.width > 0 && this.selectedCosmetic != null) {
            int gap = 4;
            int buttonW = Math.max(28, (bounds.width - 24 - gap * 3) / 4);
            int startX = bounds.x + 12;
            int y = bounds.y + bounds.height - 78;
            if (inside((double)mouseX, (double)mouseY, startX, y, buttonW, 24)) {
                this.previewTryOn = !this.previewTryOn;
                return true;
            } else if (inside((double)mouseX, (double)mouseY, startX + buttonW + gap, y, buttonW, 24)) {
                this.previewPose = this.previewPose.next();
                return true;
            } else if (inside((double)mouseX, (double)mouseY, startX + (buttonW + gap) * 2, y, buttonW, 24)) {
                this.resetPreviewCamera();
                this.previewPose = PreviewPose.IDLE;
                return true;
            } else if (inside((double)mouseX, (double)mouseY, startX + (buttonW + gap) * 3, y, buttonW, 24)) {
                class_310.method_1551().method_1507(new CosmeticPreviewStudioScreen(this, this.selectedCosmetic));
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void renderGiftDialog(class_332 context, Layout layout, int mouseX, int mouseY, int accent) {
        context.method_25294(0, 0, context.method_51421(), context.method_51443(), 1996488704);
        int w = Math.min(330, Math.max(260, layout.width / 2));
        int h = 154;
        int x = layout.x + (layout.width - w) / 2;
        int y = layout.y + (layout.height - h) / 2;
        PremiumRender.shopPanel(context, x, y, w, h, 42, 0);
        boolean giftingPlus = this.plusGiftPlan != null && !this.plusGiftPlan.isBlank();
        context.method_27535(this.field_22793, class_2561.method_43470(giftingPlus ? "Gift S9Lab Client+" : "Gift Cosmetic"), x + 18, y + 16, -1);
        String name = giftingPlus ? ("plus_3m".equals(this.plusGiftPlan) ? "3 Months" : "1 Month") : (this.selectedCosmetic == null ? "" : this.selectedCosmetic.displayName());
        context.method_27535(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, name, w - 36)), x + 18, y + 31, -6643278);
        int inputY = y + 58;
        PremiumRender.shopInput(context, x + 18, inputY, w - 36, 30, true, ClientTheme.withAlpha(accent, 180));
        String input = this.giftReceiver.isBlank() ? "Player name or UUID" : this.giftReceiver + (System.currentTimeMillis() / 450L % 2L == 0L ? "_" : "");
        context.method_27535(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, input, w - 58)), x + 30, inputY + 10, this.giftReceiver.isBlank() ? -9932669 : -1);
        if (!this.giftStatus.isBlank()) {
            context.method_27535(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, this.giftStatus, w - 36)), x + 18, y + 94, -19372);
        }

        int buttonY = y + h - 38;
        int bw = (w - 46) / 2;
        boolean cancelHovered = inside((double)mouseX, (double)mouseY, x + 18, buttonY, bw, 26);
        boolean sendHovered = inside((double)mouseX, (double)mouseY, x + 28 + bw, buttonY, bw, 26);
        rect(context, x + 18, buttonY, bw, 26, 0, cancelHovered ? -14342098 : -15263459);
        context.method_27534(this.field_22793, class_2561.method_43470("Cancel"), x + 18 + bw / 2, buttonY + 9, cancelHovered ? -1 : -6643278);
        rect(context, x + 28 + bw, buttonY, bw, 26, 0, sendHovered ? ClientTheme.withAlpha(accent, 235) : ClientTheme.withAlpha(accent, 190));
        context.method_27534(this.field_22793, class_2561.method_43470("Send Gift"), x + 28 + bw + bw / 2, buttonY + 9, -1);
    }

    private boolean handleHeaderClick(Layout layout, int mouseX, int mouseY) {
        int y = layout.y + 14;
        int tabX = this.tabStartX(layout);

        for(ClientTab tab : visibleTabs()) {
            int w = this.tabWidth(tab);
            if (inside((double)mouseX, (double)mouseY, tabX, y, w, 28)) {
                this.selectedTab = tab;
                this.searchFocused = false;
                this.search = "";
                this.scroll = 0;
                this.resetPreviewForTab();
                return true;
            }

            tabX += w + 12;
        }

        return false;
    }

    private boolean handleClientShellClick(Layout layout, int mouseX, int mouseY) {
        CosmeticLayout parts = this.cosmeticLayout(layout);
        if (this.coinButtonBounds(parts).contains((double)mouseX, (double)mouseY)) {
            this.openCoinShop();
            return true;
        } else {
            int buttonY = parts.y + 11;
            int buttonH = Math.max(18, Math.min(24, parts.topbarH - 18));
            int x = parts.x + 8;
            if (inside((double)mouseX, (double)mouseY, x, buttonY, 116, buttonH)) {
                this.showAllModules = false;
                this.moduleDetailsOpen = false;
                this.scroll = 0;
                return true;
            } else {
                x += 124;
                if (inside((double)mouseX, (double)mouseY, x, buttonY, 58, buttonH)) {
                    this.showAllModules = true;
                    this.moduleDetailsOpen = false;
                    this.scroll = 0;
                    return true;
                } else {
                    x += 66;
                    int searchW = Math.max(110, Math.min(220, parts.x + parts.width - x - Math.max(98, parts.width / 8) - 34));
                    if (inside((double)mouseX, (double)mouseY, x, buttonY, searchW, buttonH)) {
                        this.searchFocused = true;
                        return true;
                    } else {
                        this.searchFocused = false;
                        return false;
                    }
                }
            }
        }
    }

    private boolean handleFooterTabsClick(Layout layout, int mouseX, int mouseY) {
        CosmeticLayout parts = this.cosmeticLayout(layout);
        if (parts.footerH < 28) {
            return false;
        } else {
            int buttonH = Math.min(24, parts.footerH - 12);
            int y = parts.y + parts.height - parts.footerH + (parts.footerH - buttonH) / 2;
            int gap = 7;
            int[] widths = new int[]{62, 78, 88, 78};
            int socialW = 76;
            int total = widths[0] + widths[1] + widths[2] + widths[3] + socialW + gap * 4;
            int x = parts.x + Math.max(8, (parts.width - total) / 2);
            ClientTab[] tabs = visibleTabs();

            for(int i = 0; i < tabs.length; ++i) {
                if (inside((double)mouseX, (double)mouseY, x, y, widths[i], buttonH)) {
                    this.selectedTab = tabs[i];
                    this.coinShopOpen = false;
                    this.moduleDetailsOpen = false;
                    this.cosmeticDetailsOpen = false;
                    this.searchFocused = false;
                    this.search = "";
                    this.scroll = 0;
                    this.resetPreviewForTab();
                    return true;
                }

                x += widths[i] + gap;
            }

            if (inside((double)mouseX, (double)mouseY, x, y, socialW, buttonH)) {
                class_310.method_1551().method_1507(new FriendsOverlayScreen(this));
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean handleModuleSidebarClick(Layout layout, int mouseX, int mouseY) {
        CosmeticLayout parts = this.cosmeticLayout(layout);
        int itemY = parts.contentY + 6;
        int rowH = Math.max(18, Math.min(27, parts.height / 15));

        for(ModuleCategory category : ModuleCategory.values()) {
            if (inside((double)mouseX, (double)mouseY, parts.sideX + 7, itemY, parts.sideW - 13, rowH)) {
                this.selectedCategory = category;
                this.showAllModules = false;
                this.moduleDetailsOpen = false;
                this.scroll = 0;
                return true;
            }

            itemY += rowH + 5;
        }

        return false;
    }

    private boolean handleModsCatalogClick(Layout layout, int mouseX, int mouseY) {
        if (this.moduleDetailsOpen && this.selectedModule != null) {
            return this.handleModuleDetailsClick(layout, mouseX, mouseY);
        } else if (this.handleModuleSidebarClick(layout, mouseX, mouseY)) {
            return true;
        } else {
            CosmeticLayout parts = this.cosmeticLayout(layout);
            if (!this.showAllModules && this.selectedCategory == ModuleCategory.PERFORMANCE) {
                return this.handleEmbeddedPerformanceClick(parts, mouseX, mouseY);
            } else {
                Grid grid = this.grid(parts.gridW, parts.gridH, 136, 116, 4);
                int baseY = parts.gridY - this.scroll;
                List<Module> modules = this.filteredModules();

                for(int i = 0; i < modules.size(); ++i) {
                    Module module = (Module)modules.get(i);
                    int cardX = parts.gridX + i % grid.columns * (grid.cardW + grid.gap);
                    int cardY = baseY + i / grid.columns * (grid.cardH + grid.gap);
                    if (inside((double)mouseX, (double)mouseY, cardX, cardY, grid.cardW, grid.cardH)) {
                        this.selectedModule = module;
                        if (inside((double)mouseX, (double)mouseY, cardX + grid.cardW - 28, cardY + 5, 22, 22)) {
                            this.moduleDetailsOpen = true;
                            this.scroll = 0;
                            return true;
                        }

                        if (inside((double)mouseX, (double)mouseY, cardX + grid.cardW - 38, cardY + grid.cardH - 22, 34, 20)) {
                            module.setEnabled(!module.isEnabled());
                            S9LabClientClient.getConfigManager().save();
                        }

                        return true;
                    }
                }

                if (parts.preview.width > 0 && this.selectedModule != null) {
                    int buttonY = parts.preview.y + parts.preview.height - 46;
                    int switchX = parts.preview.x + parts.preview.width / 2 - 13;
                    int switchY = buttonY + 15;
                    if (inside((double)mouseX, (double)mouseY, switchX - 4, switchY - 4, 34, 20)) {
                        this.selectedModule.setEnabled(!this.selectedModule.isEnabled());
                        S9LabClientClient.getConfigManager().save();
                        return true;
                    }
                }

                return false;
            }
        }
    }

    private boolean handleEmbeddedPerformanceClick(CosmeticLayout parts, int mouseX, int mouseY) {
        Rect bounds = this.performanceBounds(parts);
        PerformanceManager manager = S9LabClientClient.getPerformanceManager();
        int gap = 8;
        int y = bounds.y + 8 - this.scroll;
        int heroH = bounds.width < 430 ? 48 : 56;
        y += heroH + 10;
        int metricColumns = bounds.width < 360 ? 1 : 3;
        int metricH = bounds.width < 500 ? 46 : 52;
        y += rows(3, metricColumns) * (metricH + gap) + 10;
        y += 18;
        int presetColumns = bounds.width < 430 ? 1 : 3;
        int presetW = Math.max(1, (bounds.width - gap * (presetColumns - 1)) / presetColumns);
        int presetH = 42;

        for(PerformanceManager.PerformancePreset preset : PerformancePreset.values()) {
            int index = preset.ordinal();
            int x = bounds.x + index % presetColumns * (presetW + gap);
            int cardY = y + index / presetColumns * (presetH + gap);
            if (inside((double)mouseX, (double)mouseY, x, cardY, presetW, presetH)) {
                applyPerformancePreset(preset);
                return true;
            }
        }

        y += rows(PerformancePreset.values().length, presetColumns) * (presetH + gap) + 12;
        int modColumns = bounds.width >= 650 ? 4 : (bounds.width >= 430 ? 3 : 2);
        int modH = 42;
        y += 18 + rows(manager.modDetectionManager().snapshot().size(), modColumns) * (modH + gap) + 12;
        y += 18;
        List<Module> performanceModules = S9LabClientClient.getModuleManager().getModules().stream().filter((modulex) -> modulex.getCategory() == ModuleCategory.PERFORMANCE).toList();
        int moduleColumns = bounds.width < 520 ? 1 : 2;
        int moduleW = Math.max(1, (bounds.width - gap * (moduleColumns - 1)) / moduleColumns);
        int moduleH = 58;

        for(int i = 0; i < performanceModules.size(); ++i) {
            Module module = (Module)performanceModules.get(i);
            int x = bounds.x + i % moduleColumns * (moduleW + gap);
            int cardY = y + i / moduleColumns * (moduleH + gap);
            if (inside((double)mouseX, (double)mouseY, x, cardY, moduleW, moduleH)) {
                int switchX = x + moduleW - 44;
                int switchY = cardY + moduleH - 24;
                if (inside((double)mouseX, (double)mouseY, switchX, switchY, 34, 18)) {
                    module.setEnabled(!module.isEnabled());
                    S9LabClientClient.getConfigManager().save();
                    return true;
                }

                this.selectedModule = module;
                this.moduleDetailsOpen = true;
                this.scroll = 0;
                return true;
            }
        }

        return false;
    }

    private boolean handleSettingsCatalogClick(Layout layout, int mouseX, int mouseY) {
        if (this.selectedModule != null) {
            return this.handleModuleDetailsClick(layout, mouseX, mouseY);
        } else if (this.handleModuleSidebarClick(layout, mouseX, mouseY)) {
            return true;
        } else {
            CosmeticLayout parts = this.cosmeticLayout(layout);
            if (inside((double)mouseX, (double)mouseY, parts.gridX, parts.gridY, Math.min(180, parts.gridW), 28)) {
                class_310.method_1551().method_1507(new HudEditorScreen(this));
                return true;
            } else {
                Module module = this.selectedModule;
                if (module == null) {
                    List<Module> modules = this.filteredModules();
                    if (!modules.isEmpty()) {
                        module = (Module)modules.get(0);
                        this.selectedModule = module;
                    }
                }

                if (module == null) {
                    return false;
                } else {
                    int rowY = parts.gridY + 46;
                    int colW = parts.gridW < 360 ? parts.gridW : (parts.gridW - 28) / 2;
                    if (inside((double)mouseX, (double)mouseY, parts.gridX + colW - 36, rowY + 3, 34, 20)) {
                        module.setEnabled(!module.isEnabled());
                        S9LabClientClient.getConfigManager().save();
                        return true;
                    } else {
                        rowY += 36;
                        int i = 0;

                        for(Setting<?> setting : module.getSettings()) {
                            int col = parts.gridW < 360 ? 0 : i % 2;
                            int row = parts.gridW < 360 ? i : i / 2;
                            int sx = parts.gridX + col * (colW + 28);
                            int sy = rowY + row * 36;
                            if (setting instanceof BooleanSetting && inside((double)mouseX, (double)mouseY, sx + colW - 36, sy + 3, 34, 20)) {
                                this.changeSetting(module, setting);
                                S9LabClientClient.getConfigManager().save();
                                return true;
                            }

                            if (!(setting instanceof BooleanSetting) && inside((double)mouseX, (double)mouseY, sx, sy, colW, 30)) {
                                this.changeSetting(module, setting);
                                S9LabClientClient.getConfigManager().save();
                                return true;
                            }

                            ++i;
                        }

                        return false;
                    }
                }
            }
        }
    }

    private boolean handleModuleDetailsClick(Layout layout, int mouseX, int mouseY) {
        CosmeticLayout parts = this.cosmeticLayout(layout);
        Module module = this.selectedModule;
        if (module == null) {
            return false;
        } else if (module instanceof TablistBadgeModule) {
            TablistBadgeModule badgeModule = (TablistBadgeModule)module;
            return this.handleNameEffectSettingsClick(parts, badgeModule, mouseX, mouseY);
        } else {
            int x = parts.sideX + 10;
            int y = parts.contentY + 12;
            int width = parts.width - 20;
            if (inside((double)mouseX, (double)mouseY, x + 8, y + 6, 120, 34)) {
                this.moduleDetailsOpen = false;
                return true;
            } else {
                int topButtonY = y + 10;
                int resetW = 72;
                int onW = 54;
                int resetX = x + width - resetW - 62;
                int onX = resetX - onW - 10;
                if (inside((double)mouseX, (double)mouseY, onX, topButtonY, onW, 28)) {
                    module.setEnabled(!module.isEnabled());
                    S9LabClientClient.getConfigManager().save();
                    return true;
                } else if (!inside((double)mouseX, (double)mouseY, resetX, topButtonY, resetW, 28) && !inside((double)mouseX, (double)mouseY, resetX + resetW + 10, topButtonY, 34, 28)) {
                    int rowY = y + 78;
                    rowY += 22;
                    KeybindSetting keybind = this.keybindSetting(module);
                    int rowX = x + 34;
                    int rowW = width - 68;
                    if (keybind != null && inside((double)mouseX, (double)mouseY, rowX + rowW - 132, rowY + 4, 132, 24)) {
                        this.changeSetting(module, keybind);
                        S9LabClientClient.getConfigManager().save();
                        return true;
                    } else {
                        rowY += 70;

                        for(Setting<?> setting : module.getSettings()) {
                            if (!(setting instanceof KeybindSetting)) {
                                if (setting instanceof BooleanSetting && inside((double)mouseX, (double)mouseY, rowX + rowW - 34, rowY + 3, 34, 24)) {
                                    this.changeSetting(module, setting);
                                    S9LabClientClient.getConfigManager().save();
                                    return true;
                                }

                                if (!(setting instanceof BooleanSetting) && inside((double)mouseX, (double)mouseY, rowX + rowW - 142, rowY + 4, 142, 24)) {
                                    this.changeSetting(module, setting);
                                    S9LabClientClient.getConfigManager().save();
                                    return true;
                                }

                                rowY += 36;
                            }
                        }

                        return false;
                    }
                } else {
                    this.resetModule(module);
                    S9LabClientClient.getConfigManager().save();
                    return true;
                }
            }
        }
    }

    private boolean handleNameEffectSettingsClick(CosmeticLayout parts, TablistBadgeModule module, int mouseX, int mouseY) {
        int x = parts.sideX + 10;
        int y = parts.contentY + 12;
        int width = parts.width - 20;
        int bottom = parts.y + parts.height - parts.footerH - 12;
        if (inside((double)mouseX, (double)mouseY, x + 6, y + 4, 210, 34)) {
            this.moduleDetailsOpen = false;
            return true;
        } else if (inside((double)mouseX, (double)mouseY, x + width - 92, y + 84, 76, 26)) {
            module.clearEffects();
            S9LabClientClient.getConfigManager().save();
            return true;
        } else {
            int gridX = x + 14;
            int gridWidth = width - 28;
            int effectsY = y + 150;
            String clicked = this.clickedNameEffect(S9TextEffects.EFFECT_IDS, gridX, effectsY, gridWidth, mouseX, mouseY);
            if (clicked != null) {
                module.toggleEffect(clicked);
                S9LabClientClient.getConfigManager().save();
                return true;
            } else {
                int effectsBottom = this.nameEffectGridBottom(S9TextEffects.EFFECT_IDS, effectsY, gridWidth);
                int toggleY = Math.min(effectsBottom + 12, bottom - 68);
                if (inside((double)mouseX, (double)mouseY, gridX, toggleY, gridWidth, 26)) {
                    module.setPlusNameEffectsEnabled(!module.plusNameEffectsEnabled());
                    S9LabClientClient.getConfigManager().save();
                    return true;
                } else if (inside((double)mouseX, (double)mouseY, gridX, toggleY + 32, gridWidth, 26)) {
                    module.setShowOtherPlayersNameEffects(!module.showOtherPlayersNameEffects());
                    S9LabClientClient.getConfigManager().save();
                    return true;
                } else {
                    return true;
                }
            }
        }
    }

    private String clickedNameEffect(List<String> ids, int x, int y, int width, int mouseX, int mouseY) {
        int columns = width >= 760 ? 5 : (width >= 560 ? 4 : 3);
        int gap = 7;
        int buttonW = Math.max(78, (width - gap * (columns - 1)) / columns);
        int buttonH = 27;

        for(int index = 0; index < ids.size(); ++index) {
            int col = index % columns;
            int row = index / columns;
            int bx = x + col * (buttonW + gap);
            int by = y + row * (buttonH + gap);
            if (inside((double)mouseX, (double)mouseY, bx, by, buttonW, buttonH)) {
                return (String)ids.get(index);
            }
        }

        return null;
    }

    private int nameEffectGridBottom(List<String> ids, int y, int width) {
        int columns = width >= 760 ? 5 : (width >= 560 ? 4 : 3);
        int rows = (ids.size() + columns - 1) / columns;
        return y + rows * 34 - 7;
    }

    private boolean handleNotificationBannerClick(Layout layout, int mouseX, int mouseY) {
        List<BackendState.Notification> notifications = BackendState.unreadNotificationsSnapshot();
        if (notifications.isEmpty()) {
            return false;
        } else {
            Rect bounds = this.notificationBannerBounds(layout);
            if (!bounds.contains((double)mouseX, (double)mouseY)) {
                return false;
            } else {
                int readX = bounds.x + bounds.width - 64;
                if (inside((double)mouseX, (double)mouseY, readX, bounds.y + 5, 54, bounds.height - 10)) {
                    BackendClient.markNotificationsRead();
                    return true;
                } else {
                    BackendState.Notification latest = (BackendState.Notification)notifications.get(0);
                    S9LabClientClient.getCosmeticRegistry().get(latest.cosmeticId()).ifPresent((cosmetic) -> {
                        this.selectedTab = S9LabClientScreen.ClientTab.COSMETICS;
                        this.selectedCosmeticType = cosmetic.type();
                        this.selectedCosmetic = cosmetic;
                        this.search = "";
                        this.searchFocused = false;
                        this.scroll = 0;
                        this.resetPreviewForCosmeticType(cosmetic.type());
                    });
                    return true;
                }
            }
        }
    }

    private boolean handleModsClick(Layout layout, int mouseX, int mouseY) {
        int x = layout.x + 20;
        int y = layout.bodyY() + 12;
        int width = layout.width - 40;
        int chipX = x;

        for(ModuleCategory category : ModuleCategory.values()) {
            String label = titleCase(category.name());
            int chipW = Math.max(54, this.field_22793.method_1727(label) + 22);
            if (inside((double)mouseX, (double)mouseY, chipX, y, chipW, 20)) {
                this.selectedCategory = category;
                this.selectedModule = null;
                this.scroll = 0;
                return true;
            }

            chipX += chipW + 8;
        }

        int searchW = 130;
        int azX = x + width - searchW - 56;
        if (inside((double)mouseX, (double)mouseY, azX, y, 23, 20)) {
            this.sortAscending = true;
            this.scroll = 0;
            return true;
        } else if (inside((double)mouseX, (double)mouseY, azX + 28, y, 23, 20)) {
            this.sortAscending = false;
            this.scroll = 0;
            return true;
        } else if (inside((double)mouseX, (double)mouseY, x + width - searchW, y, searchW, 20)) {
            this.searchFocused = true;
            return true;
        } else {
            this.searchFocused = false;
            return this.handleModuleListClick(layout, mouseX, mouseY);
        }
    }

    private boolean handleModuleListClick(Layout layout, int mouseX, int mouseY) {
        int x = layout.x + 20;
        int y = layout.bodyY() + 48;
        int width = layout.width - 40;
        int gridH = layout.y + layout.height - y - 20;
        Grid grid = this.grid(width, gridH, 132, 84, 5);
        int baseY = y - this.scroll;
        List<Module> modules = this.filteredModules();

        for(int i = 0; i < modules.size(); ++i) {
            int cardX = x + i % grid.columns * (grid.cardW + grid.gap);
            int cardY = baseY + i / grid.columns * (grid.cardH + grid.gap);
            if (inside((double)mouseX, (double)mouseY, cardX, cardY, grid.cardW, grid.cardH)) {
                Module module = (Module)modules.get(i);
                if (inside((double)mouseX, (double)mouseY, cardX + grid.cardW - 40, cardY + grid.cardH - 24, 38, 22)) {
                    module.toggle();
                    S9LabClientClient.getConfigManager().save();
                } else {
                    this.selectedModule = module;
                    this.selectedTab = S9LabClientScreen.ClientTab.SETTINGS;
                }

                return true;
            }
        }

        return false;
    }

    private boolean handleInlineModuleSettingsClick(int x, int y, int width, int mouseX, int mouseY) {
        int rowY = y + 84;
        if (inside((double)mouseX, (double)mouseY, x + 14, rowY, width - 28, 30)) {
            this.selectedModule.toggle();
            S9LabClientClient.getConfigManager().save();
            return true;
        } else {
            rowY += 38;

            for(Setting<?> setting : this.selectedModule.getSettings()) {
                if (inside((double)mouseX, (double)mouseY, x + 14, rowY, width - 28, 30)) {
                    this.changeSetting(this.selectedModule, setting);
                    S9LabClientClient.getConfigManager().save();
                    return true;
                }

                rowY += 38;
            }

            return false;
        }
    }

    private boolean handleSettingsClick(Layout layout, int mouseX, int mouseY) {
        if (this.selectedModule == null) {
            return false;
        } else {
            int sideX = layout.x + 26;
            int sideY = layout.bodyY() + 22;
            int sideW = 150;
            int settingsW = 250;
            int listX = sideX + sideW + 22;
            int listW = layout.x + layout.width - listX - 30 - settingsW - 22;
            int x = listX + listW + 22 + 14;
            int y = sideY + 58;
            int width = settingsW - 28;
            if (inside((double)mouseX, (double)mouseY, x, y, width, 30)) {
                this.selectedModule.toggle();
                S9LabClientClient.getConfigManager().save();
                return true;
            } else {
                y += 36;

                for(Setting<?> setting : this.selectedModule.getSettings()) {
                    if (inside((double)mouseX, (double)mouseY, x, y, width, 30)) {
                        this.changeSetting(this.selectedModule, setting);
                        S9LabClientClient.getConfigManager().save();
                        return true;
                    }

                    y += 36;
                }

                return false;
            }
        }
    }

    private boolean handleSettingsPageClick(Layout layout, int mouseX, int mouseY) {
        Module module = this.selectedModule;
        if (module == null) {
            List<Module> modules = this.filteredModules();
            if (!modules.isEmpty()) {
                module = (Module)modules.get(0);
            }
        }

        if (module == null) {
            return false;
        } else {
            int x = layout.x + 28;
            int y = layout.bodyY() + 68;
            int width = layout.width - 56;
            if (inside((double)mouseX, (double)mouseY, x, y, 180, 30)) {
                class_310.method_1551().method_1507(new HudEditorScreen(this));
                return true;
            } else {
                y += 40;
                int colW = (width - 44) / 2;
                if (inside((double)mouseX, (double)mouseY, x, y, colW, 30)) {
                    module.toggle();
                    S9LabClientClient.getConfigManager().save();
                    return true;
                } else {
                    y += 36;
                    int i = 0;

                    for(Setting<?> setting : module.getSettings()) {
                        int col = i % 2;
                        int row = i / 2;
                        int sx = x + col * (colW + 28);
                        int sy = y + row * 36;
                        if (inside((double)mouseX, (double)mouseY, sx, sy, colW, 30)) {
                            this.changeSetting(module, setting);
                            S9LabClientClient.getConfigManager().save();
                            return true;
                        }

                        ++i;
                    }

                    return false;
                }
            }
        }
    }

    private boolean handleCosmeticClick(Layout layout, int mouseX, int mouseY) {
        CosmeticLayout parts = this.cosmeticLayout(layout);
        if (this.coinButtonBounds(parts).contains((double)mouseX, (double)mouseY)) {
            this.openCoinShop();
            return true;
        } else {
            int buttonY = parts.y + 11;
            int buttonH = Math.max(18, Math.min(24, parts.topbarH - 18));
            int typeX = parts.x + 8;
            int allX = typeX + 78;
            if (inside((double)mouseX, (double)mouseY, typeX, buttonY, 72, buttonH)) {
                this.plusShopOpen = false;
                this.showAllCosmetics = false;
                this.cosmeticDetailsOpen = false;
                this.scroll = 0;
                return true;
            } else if (inside((double)mouseX, (double)mouseY, allX, buttonY, 58, buttonH)) {
                this.plusShopOpen = false;
                this.showAllCosmetics = true;
                this.cosmeticDetailsOpen = false;
                this.scroll = 0;
                return true;
            } else {
                int searchX = allX + 66;
                int searchW = Math.max(90, Math.min(190, parts.gridX + parts.gridW - searchX - 10));
                if (inside((double)mouseX, (double)mouseY, searchX, buttonY, searchW, buttonH)) {
                    this.searchFocused = true;
                    return true;
                } else {
                    this.searchFocused = false;
                    if (inside((double)mouseX, (double)mouseY, parts.sideX, parts.contentY, parts.sideW, parts.contentH)) {
                        int itemY = parts.contentY + 6 - this.cosmeticSideScroll;
                        int rowH = Math.max(18, Math.min(27, parts.height / 15));

                        for(CosmeticType type : CosmeticType.values()) {
                            if (inside((double)mouseX, (double)mouseY, parts.sideX + 7, itemY, parts.sideW - 13, rowH)) {
                                this.plusShopOpen = false;
                                this.selectedCosmeticType = type;
                                this.showAllCosmetics = false;
                                this.cosmeticDetailsOpen = false;
                                this.selectedCosmetic = (Cosmetic)S9LabClientClient.getCosmeticRegistry().firstByType(type).orElse((Object)null);
                                this.scroll = 0;
                                this.cosmeticSideScroll = clamp(this.cosmeticSideScroll, 0, this.maxCosmeticSideScroll(parts.contentH));
                                this.resetPreviewForCosmeticType(type);
                                return true;
                            }

                            itemY += rowH + 5;
                        }

                        if (inside((double)mouseX, (double)mouseY, parts.sideX + 7, itemY, parts.sideW - 13, rowH)) {
                            this.plusShopOpen = true;
                            this.showAllCosmetics = false;
                            this.cosmeticDetailsOpen = false;
                            this.scroll = 0;
                            return true;
                        }

                        itemY += rowH + 5;
                        if (inside((double)mouseX, (double)mouseY, parts.sideX + 7, itemY, parts.sideW - 13, rowH)) {
                            class_310.method_1551().method_1507(new CosmeticLoadoutScreen(this));
                            return true;
                        }
                    }

                    if (this.plusShopOpen) {
                        return this.handlePlusShopClick(parts, mouseX, mouseY);
                    } else if (this.cosmeticDetailsOpen && this.selectedCosmetic != null) {
                        return this.handleCosmeticDetailsClick(layout, mouseX, mouseY);
                    } else {
                        Grid grid = this.cosmeticGrid(parts.gridW, parts.gridH);
                        int baseY = parts.gridY - this.scroll;
                        List<Cosmetic> cosmetics = this.filteredCosmetics();

                        for(int i = 0; i < cosmetics.size(); ++i) {
                            int cardX = parts.gridX + i % grid.columns * (grid.cardW + grid.gap);
                            int cardY = baseY + i / grid.columns * (grid.cardH + grid.gap);
                            if (inside((double)mouseX, (double)mouseY, cardX, cardY, grid.cardW, grid.cardH)) {
                                Cosmetic cosmetic = (Cosmetic)cosmetics.get(i);
                                this.selectedCosmetic = cosmetic;
                                this.selectedCosmeticType = cosmetic.type();
                                this.resetPreviewForCosmeticType(cosmetic.type());
                                if (this.hasCosmeticDetailOptions(cosmetic) && inside((double)mouseX, (double)mouseY, cardX + grid.cardW - 28, cardY + 5, 22, 22)) {
                                    this.cosmeticDetailsOpen = true;
                                    this.scroll = 0;
                                    return true;
                                }

                                return true;
                            }
                        }

                        Rect preview = this.previewBounds(layout);
                        if (preview.width <= 0) {
                            return false;
                        } else if (this.handlePreviewControlsClick(preview, mouseX, mouseY)) {
                            return true;
                        } else {
                            int actionY = preview.y + preview.height - 46;
                            boolean owned = this.selectedCosmetic != null && BackendState.owned(this.selectedCosmetic.id());
                            if (owned) {
                                int half = (preview.width - 40) / 2;
                                if (inside((double)mouseX, (double)mouseY, preview.x + 16, actionY, half, 28)) {
                                    this.performCosmeticAction();
                                    return true;
                                }

                                if (inside((double)mouseX, (double)mouseY, preview.x + 24 + half, actionY, half, 28)) {
                                    this.openGiftDialog();
                                    return true;
                                }
                            } else if (inside((double)mouseX, (double)mouseY, preview.x + 16, actionY, preview.width - 32, 28)) {
                                this.performCosmeticAction();
                                return true;
                            }

                            if (preview.contains((double)mouseX, (double)mouseY)) {
                                this.previewDragging = true;
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean handleCoinShopClick(Layout layout, int mouseX, int mouseY) {
        CosmeticLayout parts = this.cosmeticLayout(layout);
        int buttonY = parts.y + 11;
        int buttonH = Math.max(18, Math.min(24, parts.topbarH - 18));
        int backW = parts.width < 480 ? 54 : 72;
        if (inside((double)mouseX, (double)mouseY, parts.x + 8, buttonY, backW, buttonH)) {
            this.coinShopOpen = false;
            this.coinShopScroll = 0;
            return true;
        } else {
            for(int i = 0; i < this.coinPackCards.size(); ++i) {
                CoinPackCard card = (CoinPackCard)this.coinPackCards.get(i);
                if (card.contains((double)mouseX, (double)mouseY)) {
                    this.focusedCoinPack = i;
                    if (card.priceContains((double)mouseX, (double)mouseY)) {
                        this.onCoinPackSelected(card.pack());
                    }

                    return true;
                }
            }

            return true;
        }
    }

    private void openCoinShop() {
        this.coinShopOpen = true;
        this.coinShopScroll = 0;
        this.focusedCoinPack = 0;
        this.searchFocused = false;
        this.previewDragging = false;
    }

    private void onCoinPackSelected(CoinPack pack) {
        S9ToastManager.warning("Coin checkout", formatCoins((long)pack.totalCoins()) + " coins selected - checkout is not connected yet");
    }

    private boolean handleCosmeticDetailsClick(Layout layout, int mouseX, int mouseY) {
        CosmeticLayout parts = this.cosmeticLayout(layout);
        Cosmetic cosmetic = this.selectedCosmetic;
        if (cosmetic == null) {
            return false;
        } else if (this.isBatWingCosmetic(cosmetic)) {
            return this.handleBatWingStyleClick(layout, mouseX, mouseY);
        } else {
            Rect bounds = this.cosmeticDetailPreviewBounds(layout);
            if (inside((double)mouseX, (double)mouseY, bounds.x + 12, bounds.y + 12, 62, 24)) {
                this.cosmeticDetailsOpen = false;
                return true;
            } else {
                int contentBottom = parts.y + parts.height - parts.footerH;
                int variantX = parts.gridX;
                int variantY = parts.contentY + 42;
                int variantW = Math.min(68, Math.max(48, parts.gridW / 5));
                List<Cosmetic> variants = this.variantsForSelectedCosmetic();
                int thumbSize = Math.min(48, Math.max(34, (contentBottom - variantY - 10) / Math.max(1, Math.min(6, variants.size())) - 5));

                for(int i = 0; i < variants.size() && i < 7; ++i) {
                    int tx = variantX + 4;
                    int ty = variantY + i * (thumbSize + 7);
                    if (inside((double)mouseX, (double)mouseY, tx, ty, thumbSize, thumbSize)) {
                        this.selectedCosmetic = (Cosmetic)variants.get(i);
                        this.selectedCosmeticType = this.selectedCosmetic.type();
                        this.resetPreviewForCosmeticType(this.selectedCosmetic.type());
                        return true;
                    }
                }

                if (inside((double)mouseX, (double)mouseY, bounds.x + 8, bounds.y + bounds.height / 2 - 18, 34, 42)) {
                    this.selectAdjacentCosmeticVariant(-1);
                    return true;
                } else if (inside((double)mouseX, (double)mouseY, bounds.x + bounds.width - 42, bounds.y + bounds.height / 2 - 18, 34, 42)) {
                    this.selectAdjacentCosmeticVariant(1);
                    return true;
                } else if (this.handlePreviewControlsClick(bounds, mouseX, mouseY)) {
                    return true;
                } else {
                    int buttonW = Math.min(260, bounds.width - 40);
                    int buttonX = bounds.x + (bounds.width - buttonW) / 2;
                    int buttonY = bounds.y + bounds.height - 36;
                    if (inside((double)mouseX, (double)mouseY, buttonX, buttonY, buttonW, 26)) {
                        this.performCosmeticAction();
                        return true;
                    } else if (bounds.contains((double)mouseX, (double)mouseY)) {
                        this.previewDragging = true;
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
    }

    private boolean handleBatWingStyleClick(Layout layout, int mouseX, int mouseY) {
        CosmeticLayout parts = this.cosmeticLayout(layout);
        Rect bounds = this.cosmeticDetailPreviewBounds(layout);
        if (inside((double)mouseX, (double)mouseY, bounds.x + 12, bounds.y + 12, 62, 24)) {
            this.cosmeticDetailsOpen = false;
            return true;
        } else {
            int styleX = parts.gridX + 4;
            int styleY = parts.contentY + 44;
            int styleW = Math.min(86, Math.max(58, parts.gridW / 4));
            if (inside((double)mouseX, (double)mouseY, styleX, styleY, styleW, styleW)) {
                this.setBatWingStyle("White");
                return true;
            } else if (inside((double)mouseX, (double)mouseY, styleX, styleY + styleW + 12, styleW, styleW)) {
                this.setBatWingStyle("Black");
                return true;
            } else if (this.handlePreviewControlsClick(bounds, mouseX, mouseY)) {
                return true;
            } else {
                int buttonW = Math.min(260, bounds.width - 40);
                int buttonX = bounds.x + (bounds.width - buttonW) / 2;
                int buttonY = bounds.y + bounds.height - 36;
                if (inside((double)mouseX, (double)mouseY, buttonX, buttonY, buttonW, 26)) {
                    this.performCosmeticAction();
                    return true;
                } else if (bounds.contains((double)mouseX, (double)mouseY)) {
                    this.previewDragging = true;
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    private boolean handlePlusShopClick(CosmeticLayout parts, int mouseX, int mouseY) {
        if (BackendState.plusActive() && parts.preview.width > 0 && inside((double)mouseX, (double)mouseY, parts.preview.x + parts.preview.width - 34, parts.preview.y + 8, 24, 22)) {
            S9LabClientClient.getModuleManager().getModule("Tablist Badge").ifPresent((module) -> {
                this.selectedModule = module;
                this.selectedCategory = ModuleCategory.UTILITY;
                this.selectedTab = S9LabClientScreen.ClientTab.SETTINGS;
                this.plusShopOpen = false;
            });
            return true;
        } else {
            boolean stacked = parts.gridW < 250;
            int cardGap = Math.max(8, parts.gridW / 42);
            int cardW = stacked ? Math.max(96, parts.gridW - 4) : Math.max(112, (parts.gridW - cardGap) / 2);
            int cardH = stacked ? Math.max(92, (parts.gridH - cardGap - 10) / 2) : Math.min(170, Math.max(122, parts.gridH - 16));
            int cardY = parts.gridY + 4;
            return this.handlePlusPlanButton(parts.gridX, cardY, cardW, cardH, 750L, "plus_1m", mouseX, mouseY) ? true : this.handlePlusPlanButton(stacked ? parts.gridX : parts.gridX + cardW + cardGap, stacked ? cardY + cardH + cardGap : cardY, cardW, cardH, 1900L, "plus_3m", mouseX, mouseY);
        }
    }

    private boolean handlePlusPlanButton(int x, int y, int width, int height, long price, String planId, int mouseX, int mouseY) {
        int gap = 6;
        int buttonW = Math.max(34, (width - 24 - gap) / 2);
        int buttonY = y + height - 32;
        if (inside((double)mouseX, (double)mouseY, x + 12, buttonY, buttonW, 24)) {
            if (BackendState.online() && !BackendState.plusActive() && BackendState.coins() >= price) {
                BackendClient.buyPlus(planId);
            }

            return true;
        } else {
            int giftX = x + 12 + buttonW + gap;
            if (inside((double)mouseX, (double)mouseY, giftX, buttonY, buttonW, 24)) {
                if (BackendState.online() && BackendState.coins() >= price) {
                    this.openPlusGiftDialog(planId);
                }

                return true;
            } else {
                return false;
            }
        }
    }

    private boolean handleGiftDialogClick(Layout layout, int mouseX, int mouseY) {
        int w = Math.min(330, Math.max(260, layout.width / 2));
        int h = 154;
        int x = layout.x + (layout.width - w) / 2;
        int y = layout.y + (layout.height - h) / 2;
        int buttonY = y + h - 38;
        int bw = (w - 46) / 2;
        if (inside((double)mouseX, (double)mouseY, x + 18, buttonY, bw, 26)) {
            this.giftDialogOpen = false;
            this.plusGiftPlan = "";
            return true;
        } else if (inside((double)mouseX, (double)mouseY, x + 28 + bw, buttonY, bw, 26)) {
            this.confirmGift();
            return true;
        } else {
            return true;
        }
    }

    private boolean handleCatalogClick(Layout layout, int mouseX, int mouseY) {
        return this.handleCosmeticClick(layout, mouseX, mouseY);
    }

    private Rect previewBounds(Layout layout) {
        if (this.selectedTab == S9LabClientScreen.ClientTab.MODS && this.selectedModule == null) {
            return S9LabClientScreen.Rect.empty();
        } else if (this.selectedTab != S9LabClientScreen.ClientTab.COSMETICS && this.selectedTab != S9LabClientScreen.ClientTab.SHOP) {
            return S9LabClientScreen.Rect.empty();
        } else {
            return this.cosmeticDetailsOpen && this.selectedCosmetic != null ? this.cosmeticDetailPreviewBounds(layout) : this.cosmeticLayout(layout).preview;
        }
    }

    private Rect cosmeticDetailPreviewBounds(Layout layout) {
        CosmeticLayout parts = this.cosmeticLayout(layout);
        int variantW = Math.min(68, Math.max(48, parts.gridW / 5));
        int previewX = parts.gridX + variantW + 16;
        int previewW = Math.max(120, parts.width - (previewX - parts.x) - 26);
        return new Rect(previewX, parts.contentY, previewW, parts.contentH);
    }

    private Rect notificationBannerBounds(Layout layout) {
        int width = Math.min(270, layout.width - 40);
        return width >= 180 && layout.height >= 230 ? new Rect(layout.x + layout.width - width - 18, layout.y + layout.headerHeight + 5, width, 28) : S9LabClientScreen.Rect.empty();
    }

    private boolean handleHudDragStart(int mouseX, int mouseY) {
        if (!this.moduleDetailsOpen && !this.cosmeticDetailsOpen) {
            if (this.selectedTab == S9LabClientScreen.ClientTab.MODS && this.selectedCategory == ModuleCategory.HUD) {
                class_310 client = class_310.method_1551();
                List<HudModule> modules = S9LabClientClient.getModuleManager().getHudModules();

                for(int i = modules.size() - 1; i >= 0; --i) {
                    HudModule module = (HudModule)modules.get(i);
                    if (module.isEnabled()) {
                        int x = module.getX() - 4;
                        int y = module.getY() - 4;
                        if (inside((double)mouseX, (double)mouseY, x, y, module.getWidth(client) + 8, module.getHeight(client) + 8)) {
                            this.draggingModule = module;
                            this.dragOffsetX = mouseX - module.getX();
                            this.dragOffsetY = mouseY - module.getY();
                            return true;
                        }
                    }
                }

                return false;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void renderSearch(class_332 context, int x, int y, int width, int mouseX, int mouseY, int accent, String placeholder) {
        boolean hovered = inside((double)mouseX, (double)mouseY, x, y, width, 32);
        rect(context, x, y, width, 32, 10, -1979052780);
        outline(context, x, y, width, 32, 10, this.searchFocused ? ClientTheme.withAlpha(accent, 230) : (hovered ? -1438956197 : 859325013));
        String text = this.search.isBlank() && !this.searchFocused ? placeholder : this.search + (this.searchFocused && System.currentTimeMillis() / 450L % 2L == 0L ? "_" : "");
        context.method_27535(this.field_22793, class_2561.method_43470("⌕"), x + 13, y + 11, -9932669);
        context.method_27535(this.field_22793, class_2561.method_43470(TextLayout.ellipsize(this.field_22793, text, width - 42)), x + 32, y + 11, this.search.isBlank() && !this.searchFocused ? -9932669 : -1);
    }

    private void renderSideItem(class_332 context, String label, int count, int x, int y, int width, int height, boolean active, boolean hovered, int accent) {
        if (active) {
            rect(context, x, y, width, height, 9, ClientTheme.withAlpha(accent, 145));
            rect(context, x, y + 5, 3, height - 10, 2, -1);
        } else if (hovered) {
            rect(context, x, y, width, height, 9, 1025846332);
        }

        context.method_27535(this.field_22793, class_2561.method_43470(label), x + 14, y + (height - 8) / 2, active ? -1 : (hovered ? -1578254 : -6643278));
        if (count >= 0) {
            context.method_27535(this.field_22793, class_2561.method_43470(String.valueOf(count)), x + width - 24, y + (height - 8) / 2, active ? -1 : -9932669);
        }

    }

    private void renderBackButton(class_332 context, int x, int y, int width, int height, boolean hovered, int accent) {
        rect(context, x, y, width, height, 8, hovered ? 1427644462 : 856362517);
        outline(context, x, y, width, height, 8, hovered ? accent : 859325013);
        context.method_27535(this.field_22793, class_2561.method_43470("‹  Back"), x + 14, y + 9, hovered ? -1 : -6643278);
    }

    private void renderSwitch(class_332 context, int x, int y, boolean enabled, int accent) {
        rect(context, x, y, 40, 20, 10, enabled ? ClientTheme.withAlpha(accent, 220) : -13419957);
        int knobX = enabled ? x + 22 : x + 3;
        rect(context, knobX, y + 3, 14, 14, 7, -1);
    }

    private void resetPreviewCamera() {
        this.previewYaw = 180.0F;
        this.previewPitch = 8.0F;
        this.previewZoom = 78;
    }

    private void resetPreviewForTab() {
        if (this.selectedTab == S9LabClientScreen.ClientTab.COSMETICS || this.selectedTab == S9LabClientScreen.ClientTab.SHOP) {
            this.selectedCosmetic = (Cosmetic)S9LabClientClient.getCosmeticRegistry().firstByType(this.selectedCosmeticType).orElse(this.selectedCosmetic);
            this.resetPreviewForCosmeticType(this.selectedCosmeticType);
        }

    }

    private void resetPreviewForCosmeticType(CosmeticType type) {
        this.previewTryOn = true;
        this.previewPose = PreviewPose.IDLE;
        byte var10001;
        switch (type) {
            case WINGS:
            case SHIELD:
            case CAPE:
                var10001 = 82;
                break;
            case GLINT:
            case EMOTE:
            case HAT:
            case HALO:
                var10001 = 96;
                break;
            case BANDANA:
            default:
                var10001 = 78;
        }

        this.previewZoom = var10001;
        float var2;
        switch (type) {
            case WINGS:
            case SHIELD:
            case CAPE:
                var2 = 0.0F;
                break;
            default:
                var2 = 180.0F;
        }

        this.previewYaw = var2;
        switch (type) {
            case HAT:
            case HALO:
                var2 = -8.0F;
                break;
            default:
                var2 = 8.0F;
        }

        this.previewPitch = var2;
    }

    private int moduleCount(ModuleCategory category) {
        return (int)S9LabClientClient.getModuleManager().getModules().stream().filter((module) -> module.getCategory() == category).count();
    }

    private List<Module> filteredModules() {
        String query = this.search.trim().toLowerCase(Locale.ROOT);
        Comparator<Module> comparator = Comparator.comparing((module) -> module.getName().toLowerCase(Locale.ROOT));
        if (!this.sortAscending) {
            comparator = comparator.reversed();
        }

        return S9LabClientClient.getModuleManager().getModules().stream().filter((module) -> this.showAllModules || module.getCategory() == this.selectedCategory).filter((module) -> query.isEmpty() || module.getName().toLowerCase(Locale.ROOT).contains(query) || module.getDescription().toLowerCase(Locale.ROOT).contains(query)).sorted(comparator).toList();
    }

    private List<Cosmetic> filteredCosmetics() {
        String query = this.search.trim().toLowerCase(Locale.ROOT);
        Comparator<Cosmetic> comparator = Comparator.comparing((cosmetic) -> cosmetic.displayName().toLowerCase(Locale.ROOT));
        if (!this.sortAscending) {
            comparator = comparator.reversed();
        }

        return S9LabClientClient.getCosmeticRegistry().all().stream().filter((cosmetic) -> this.showAllCosmetics || cosmetic.type() == this.selectedCosmeticType).filter((cosmetic) -> query.isEmpty() || cosmetic.displayName().toLowerCase(Locale.ROOT).contains(query) || cosmetic.id().toLowerCase(Locale.ROOT).contains(query)).sorted(comparator).toList();
    }

    private List<Cosmetic> variantsForSelectedCosmetic() {
        Cosmetic cosmetic = this.selectedCosmetic;
        CosmeticType type = cosmetic == null ? this.selectedCosmeticType : cosmetic.type();
        return S9LabClientClient.getCosmeticRegistry().all().stream().filter((variant) -> variant.type() == type).sorted(Comparator.comparing((variant) -> variant.displayName().toLowerCase(Locale.ROOT))).toList();
    }

    private boolean hasCosmeticDetailOptions(Cosmetic cosmetic) {
        return cosmetic != null && (this.isBatWingCosmetic(cosmetic) || cosmetic.type() == CosmeticType.HALO);
    }

    private boolean isBatWingCosmetic(Cosmetic cosmetic) {
        return cosmetic != null && "s9lab_bat_wings".equals(CosmeticIdAliases.normalize(cosmetic.id()));
    }

    private String batWingStyle() {
        return (String)this.batWingStyleSetting().map(Setting::getValue).orElse("White");
    }

    private void setBatWingStyle(String style) {
        this.batWingStyleSetting().ifPresent((setting) -> {
            if (!style.equals(setting.getValue())) {
                setting.setValue(style);
                S9LabClientClient.getConfigManager().save();
                BackendClient.pushSettingsAsync();
                S9ToastManager.success("Bat Wings", style + " style selected");
            }
        });
    }

    private Optional<ModeSetting> batWingStyleSetting() {
        return S9LabClientClient.getModuleManager().getModule("Wings").flatMap((module) -> module.getSettings().stream().filter((setting) -> setting instanceof ModeSetting && setting.getName().equalsIgnoreCase("Bat Wing Style")).map((setting) -> (ModeSetting)setting).findFirst());
    }

    private static String friendlyBackendStatus(String status) {
        if (status != null && !status.isBlank()) {
            String var10000;
            switch (status) {
                case "cosmetic_not_available":
                    var10000 = "Cosmetic unavailable";
                    break;
                case "cosmetic_not_owned":
                    var10000 = "Cosmetic not owned";
                    break;
                case "plus_required":
                case "receiver_plus_required":
                    var10000 = "S9Lab+ required";
                    break;
                case "not_enough_coins":
                case "insufficient_coins":
                    var10000 = "Not enough coins";
                    break;
                case "backend_disabled":
                    var10000 = "Backend disabled";
                    break;
                default:
                    var10000 = status.startsWith("backend_http_") ? "Backend error " + status.substring("backend_http_".length()) : status.replace('_', ' ');
            }

            return var10000;
        } else {
            return "Backend offline";
        }
    }

    private void selectAdjacentCosmeticVariant(int direction) {
        List<Cosmetic> variants = this.variantsForSelectedCosmetic();
        if (!variants.isEmpty()) {
            int index = Math.max(0, variants.indexOf(this.selectedCosmetic));
            this.selectedCosmetic = (Cosmetic)variants.get(Math.floorMod(index + direction, variants.size()));
            this.selectedCosmeticType = this.selectedCosmetic.type();
            this.resetPreviewForCosmeticType(this.selectedCosmetic.type());
        }
    }

    private KeybindSetting keybindSetting(Module module) {
        for(Setting<?> setting : module.getSettings()) {
            if (setting instanceof KeybindSetting keybindSetting) {
                return keybindSetting;
            }
        }

        return null;
    }

    private static String keybindValue(KeybindSetting keybindSetting) {
        return (Integer)keybindSetting.getValue() == 0 ? "Not Bound" : String.valueOf(keybindSetting.getValue());
    }

    private void resetModule(Module module) {
        boolean themeModule = "UI Theme".equalsIgnoreCase(module.getName());
        module.setEnabled(themeModule);

        for(Setting<?> setting : module.getSettings()) {
            label52: {
                if (themeModule && setting instanceof ColorSetting colorSetting) {
                    if ("Main Color".equals(setting.getName())) {
                        colorSetting.setValue(-11503903);
                        break label52;
                    }
                }

                if (themeModule && setting instanceof BooleanSetting booleanSetting) {
                    if ("Blur Background".equals(setting.getName())) {
                        booleanSetting.setValue(true);
                        break label52;
                    }
                }

                if (setting instanceof BooleanSetting booleanSetting) {
                    booleanSetting.setValue(false);
                } else if (setting instanceof KeybindSetting keybindSetting) {
                    keybindSetting.setValue(0);
                } else {
                    label34: {
                        if (setting instanceof ModeSetting modeSetting) {
                            if (!modeSetting.getModes().isEmpty()) {
                                modeSetting.setValue((String)modeSetting.getModes().get(0));
                                break label34;
                            }
                        }

                        if (setting instanceof NumberSetting numberSetting) {
                            numberSetting.setValue(numberSetting.getMin());
                        } else if (setting instanceof ColorSetting colorSetting) {
                            colorSetting.setValue(-645315096);
                        }
                    }
                }
            }

            syncSettingToCosmetic(module, setting);
            syncSettingToPerformance(module, setting);
            syncSettingToTheme(module, setting, true);
        }

    }

    private void changeSetting(Module module, Setting<?> setting) {
        if (setting instanceof ColorSetting colorSetting) {
            this.colorPickerSetting = colorSetting;
            this.colorPickerModule = module;
            this.colorPickerDrag = 0;
        } else {
            if (setting instanceof BooleanSetting booleanSetting) {
                booleanSetting.setValue(!(Boolean)booleanSetting.getValue());
            } else if (setting instanceof ModeSetting modeSetting) {
                List<String> modes = modeSetting.getModes();
                modeSetting.setValue((String)modes.get(Math.floorMod(modes.indexOf(modeSetting.getValue()) + 1, modes.size())));
            } else if (setting instanceof NumberSetting numberSetting) {
                double next = (Double)numberSetting.getValue() + numberSetting.getStep();
                numberSetting.setValue(next > numberSetting.getMax() ? numberSetting.getMin() : next);
            } else if (setting instanceof KeybindSetting keybindSetting) {
                keybindSetting.setValue(0);
            }

            syncSettingToCosmetic(module, setting);
            syncSettingToPerformance(module, setting);
            syncSettingToTheme(module, setting, true);
        }
    }

    private static void syncSettingToTheme(Module module, Setting<?> setting, boolean save) {
        if ("UI Theme".equalsIgnoreCase(module.getName())) {
            if (setting instanceof ColorSetting) {
                ColorSetting colorSetting = (ColorSetting)setting;
                if ("Main Color".equals(setting.getName())) {
                    if (save) {
                        ThemeManager.setAccentColor((Integer)colorSetting.getValue());
                    } else {
                        ThemeManager.applyAccentColor((Integer)colorSetting.getValue());
                    }

                    return;
                }
            }

            if (setting instanceof BooleanSetting) {
                BooleanSetting booleanSetting = (BooleanSetting)setting;
                if ("Blur Background".equals(setting.getName())) {
                    if (save) {
                        ThemeManager.setBlurBackground((Boolean)booleanSetting.getValue());
                    } else {
                        ThemeManager.applyBlurBackground((Boolean)booleanSetting.getValue());
                    }
                }
            }

        }
    }

    private static void syncSettingToPerformance(Module module, Setting<?> setting) {
        if ("Performance Optimizer".equalsIgnoreCase(module.getName()) && setting instanceof ModeSetting modeSetting) {
            PerformanceManager.PerformancePreset preset = performancePreset((String)modeSetting.getValue());
            applyPerformancePreset(preset);
        }
    }

    private static void applyPerformancePreset(PerformanceManager.PerformancePreset preset) {
        S9LabClientClient.getPerformanceManager().applyPreset(preset, class_310.method_1551());
        S9LabClientClient.getModuleManager().getModule("Performance Optimizer").ifPresent((module) -> {
            for(Setting<?> setting : module.getSettings()) {
                if (setting instanceof ModeSetting modeSetting) {
                    if (modeSetting.getName().equalsIgnoreCase("Preset")) {
                        modeSetting.setValue(preset.displayName());
                    }
                }
            }

        });
        S9LabClientClient.getConfigManager().save();
    }

    private static PerformanceManager.PerformancePreset performancePreset(String value) {
        if (value == null) {
            return PerformancePreset.BALANCED;
        } else {
            PerformanceManager.PerformancePreset var10000;
            switch (value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "")) {
                case "quality" -> var10000 = PerformancePreset.QUALITY;
                case "maxfps" -> var10000 = PerformancePreset.MAX_FPS;
                default -> var10000 = PerformancePreset.BALANCED;
            }

            return var10000;
        }
    }

    private static void syncModuleSelection(Cosmetic cosmetic) {
        String var10000;
        switch (cosmetic.type()) {
            case WINGS -> var10000 = "Wings";
            case GLINT -> var10000 = "Glint";
            case SHIELD -> var10000 = "Shield";
            case EMOTE -> var10000 = "S9 Emote";
            case HAT -> var10000 = "Hat";
            case HALO -> var10000 = "Halo";
            case BANDANA -> var10000 = "Bandana";
            case CAPE -> var10000 = "Cape";
            case SHOULDER -> var10000 = "Shoulder Buddy";
            default -> throw new MatchException((String)null, (Throwable)null);
        }

        String moduleName = var10000;
        S9LabClientClient.getModuleManager().getModule(moduleName).ifPresent((module) -> {
            module.setEnabled(true);

            for(Setting<?> setting : module.getSettings()) {
                if (setting instanceof ModeSetting modeSetting) {
                    modeSetting.setValue(cosmetic.type() == CosmeticType.EMOTE ? cosmetic.id().replace("s9lab_emote_", "") : cosmetic.id());
                }
            }

        });
    }

    private String cosmeticActionLabel(Cosmetic cosmetic) {
        if (cosmetic == null) {
            return "Select cosmetic";
        } else if (!BackendState.online()) {
            return "Backend offline";
        } else {
            boolean equipped = cosmetic.id().equals(S9LabClientClient.getConfigManager().getEquippedCosmeticId(cosmetic.type()));
            if (equipped) {
                return "Unequip";
            } else if (cosmetic.type() == CosmeticType.SHIELD) {
                return "Disabled";
            } else if (S9LabClientClient.getConfigManager().isUnlocked(cosmetic.id())) {
                return "Equip";
            } else {
                BackendState.ShopCosmetic shop = BackendState.catalog(cosmetic.id());
                if (!shop.enabled()) {
                    return "Unavailable";
                } else if (shop.plusExclusive() && !BackendState.plusActive()) {
                    return "S9Lab+ required";
                } else {
                    return shop.price() <= 0L ? "Unlock free" : "Buy - " + shop.price() + " coins";
                }
            }
        }
    }

    private void performCosmeticAction() {
        Cosmetic cosmetic = this.selectedCosmetic;
        if (cosmetic != null) {
            if (!BackendState.online()) {
                S9ToastManager.warning("Backend offline", friendlyBackendStatus(BackendState.status()));
            } else {
                boolean equipped = cosmetic.id().equals(S9LabClientClient.getConfigManager().getEquippedCosmeticId(cosmetic.type()));
                if (equipped) {
                    BackendClient.unequipCosmetic(cosmetic.type());
                } else if (cosmetic.type() == CosmeticType.SHIELD) {
                    S9ToastManager.warning("Shield equip disabled", "Custom shields are temporarily disabled");
                } else if (S9LabClientClient.getConfigManager().isUnlocked(cosmetic.id())) {
                    syncModuleSelection(cosmetic);
                    BackendClient.equipCosmetic(cosmetic.type(), cosmetic.id());
                } else {
                    BackendState.ShopCosmetic shop = BackendState.catalog(cosmetic.id());
                    if (!shop.enabled()) {
                        S9ToastManager.warning("Cosmetic unavailable", cosmetic.displayName() + " is not available right now.");
                    } else if (shop.plusExclusive() && !BackendState.plusActive()) {
                        S9ToastManager.warning("S9Lab+ required", cosmetic.displayName() + " needs S9Lab Client+.");
                    } else if (shop.price() > BackendState.coins()) {
                        String var10001 = cosmetic.displayName();
                        S9ToastManager.warning("Not enough coins", var10001 + " costs " + shop.price() + " coins.");
                    } else {
                        BackendClient.buyCosmetic(cosmetic.id());
                    }
                }
            }
        }
    }

    private void openGiftDialog() {
        if (this.selectedCosmetic != null && BackendState.online() && BackendState.owned(this.selectedCosmetic.id())) {
            this.plusGiftPlan = "";
            this.giftReceiver = "";
            this.giftStatus = "";
            this.giftDialogOpen = true;
            this.searchFocused = false;
        }
    }

    private void openPlusGiftDialog(String planId) {
        if (BackendState.online() && planId != null && !planId.isBlank()) {
            this.plusGiftPlan = planId;
            this.giftReceiver = "";
            this.giftStatus = "";
            this.giftDialogOpen = true;
            this.searchFocused = false;
        }
    }

    private void confirmGift() {
        boolean giftingPlus = this.plusGiftPlan != null && !this.plusGiftPlan.isBlank();
        if (!giftingPlus && this.selectedCosmetic == null) {
            this.giftDialogOpen = false;
        } else {
            String receiver = this.giftReceiver.trim();
            if (receiver.isBlank()) {
                this.giftStatus = "Enter a player name or UUID.";
            } else {
                if (giftingPlus) {
                    BackendClient.giftPlus(receiver, this.plusGiftPlan);
                } else {
                    BackendClient.giftCosmetic(receiver, this.selectedCosmetic.id());
                }

                this.giftStatus = "Gift request sent.";
                this.giftDialogOpen = false;
                this.plusGiftPlan = "";
            }
        }
    }

    private static void syncSettingToCosmetic(Module module, Setting<?> setting) {
        if (setting instanceof ModeSetting) {
            ModeSetting modeSetting = (ModeSetting)setting;
            CosmeticType var10000;
            switch (module.getName().toLowerCase(Locale.ROOT)) {
                case "cape" -> var10000 = CosmeticType.CAPE;
                case "bandana" -> var10000 = CosmeticType.BANDANA;
                case "wings" -> var10000 = CosmeticType.WINGS;
                case "hat" -> var10000 = CosmeticType.HAT;
                case "halo" -> var10000 = CosmeticType.HALO;
                case "shield" -> var10000 = CosmeticType.SHIELD;
                case "shoulder buddy" -> var10000 = CosmeticType.SHOULDER;
                case "glint" -> var10000 = CosmeticType.GLINT;
                case "s9 emote" -> var10000 = CosmeticType.EMOTE;
                default -> var10000 = null;
            }

            CosmeticType type = var10000;
            if (type != null) {
                if (type != CosmeticType.SHIELD) {
                    cosmeticId = type == CosmeticType.EMOTE ? "s9lab_emote_" + (String)modeSetting.getValue() : (String)modeSetting.getValue();
                    S9LabClientClient.getCosmeticRegistry().get(cosmeticId).filter((cosmetic) -> cosmetic.type() == type).filter((cosmetic) -> S9LabClientClient.getConfigManager().isUnlocked(cosmetic.id())).filter((cosmetic) -> BackendState.online()).ifPresent((cosmetic) -> BackendClient.equipCosmetic(type, cosmetic.id()));
                }
            }
        }
    }

    private int maxCosmeticSideScroll(int panelH) {
        int total = (CosmeticType.values().length + 2) * 42 + 28;
        int visible = Math.max(1, panelH - 72);
        return Math.max(0, total - visible);
    }

    private int maxScroll(Layout layout) {
        if (this.selectedTab == S9LabClientScreen.ClientTab.MODS) {
            if (this.moduleDetailsOpen) {
                return 0;
            } else {
                CosmeticLayout parts = this.cosmeticLayout(layout);
                if (!this.showAllModules && this.selectedCategory == ModuleCategory.PERFORMANCE) {
                    Rect bounds = this.performanceBounds(parts);
                    return Math.max(0, this.performanceContentHeight(parts) - bounds.height);
                } else {
                    Grid grid = this.grid(parts.gridW, parts.gridH, 136, 116, 4);
                    return Math.max(0, rows(this.filteredModules().size(), grid.columns) * (grid.cardH + grid.gap) - parts.gridH);
                }
            }
        } else if (this.selectedTab == S9LabClientScreen.ClientTab.SETTINGS) {
            return 0;
        } else if (this.cosmeticDetailsOpen) {
            return 0;
        } else if (this.plusShopOpen) {
            return 0;
        } else {
            CosmeticLayout parts = this.cosmeticLayout(layout);
            Grid grid = this.cosmeticGrid(parts.gridW, parts.gridH);
            int rows = rows(this.filteredCosmetics().size(), grid.columns);
            return Math.max(0, rows * (grid.cardH + grid.gap) - parts.gridH);
        }
    }

    private void clampScroll() {
        this.scroll = clamp(this.scroll, 0, this.maxScroll(this.layout()));
    }

    private Layout layout() {
        ScreenLayout screen = this.centeredLayout(960, 520, 360, 250);
        int header = screen.height() < 300 ? 42 : 48;
        return new Layout(screen.x(), screen.y(), screen.width(), screen.height(), screen.padding(), header);
    }

    private CosmeticLayout cosmeticLayout(Layout layout) {
        int margin = Math.max(6, layout.pad / 2);
        int x = layout.x + margin;
        int y = layout.y + margin;
        int width = Math.max(1, layout.width - margin * 2);
        int height = Math.max(1, layout.y + layout.height - y - margin);
        int topbarH = height < 260 ? 38 : 48;
        int footerH = height < 270 ? 30 : 44;
        int contentY = y + topbarH;
        int contentH = Math.max(1, height - topbarH - footerH);
        int gap = Math.max(5, ResponsiveLayout.adaptiveGap(width, height));
        int sideW = clamp(width / 8, 70, 126);
        int previewW = clamp(width / 4, 116, 238);
        if (width < 470) {
            previewW = clamp(width / 4, 96, 128);
        }

        int previewX = x + width - previewW;
        int gridX = x + sideW + gap;
        int gridW = previewX - gridX - gap;
        if (gridW < 96) {
            previewW = Math.max(90, previewW - (96 - gridW));
            previewX = x + width - previewW;
            gridW = Math.max(96, previewX - gridX - gap);
        }

        int gridY = contentY + (height < 290 ? 42 : 58);
        int gridH = Math.max(1, y + height - footerH - gridY);
        Rect preview = new Rect(previewX, contentY, previewW, contentH);
        return new CosmeticLayout(x, y, width, height, topbarH, footerH, contentY, contentH, x, sideW, gridX, gridY, gridW, gridH, preview);
    }

    private Rect coinButtonBounds(CosmeticLayout parts) {
        int buttonY = parts.y + 11;
        int buttonH = Math.max(18, Math.min(24, parts.topbarH - 18));
        int coinsW = Math.max(78, Math.min(120, parts.width / 8));
        int coinsX = parts.x + parts.width - coinsW - 12;
        return new Rect(coinsX, buttonY - 3, coinsW, buttonH + 6);
    }

    private CoinShopLayout coinShopLayout(Layout layout) {
        CosmeticLayout parts = this.cosmeticLayout(layout);
        int contentX = parts.x + 14;
        int contentY = parts.contentY + 14;
        int contentW = Math.max(1, parts.width - 28);
        int contentH = Math.max(1, parts.contentH - 28);
        int columns = contentW >= 680 ? 4 : 2;
        int gap = Math.max(7, ResponsiveLayout.adaptiveGap(contentW, contentH));
        int cardW = Math.max(72, (contentW - gap * (columns - 1)) / columns);
        int rowCount = rows(this.coinPackCards.size(), columns);
        int availablePerRow = Math.max(112, (contentH - gap * (rowCount - 1)) / rowCount);
        int cardH = columns == 4 ? clamp(availablePerRow, 148, 270) : clamp(availablePerRow, 126, 210);
        int totalHeight = rowCount * cardH + Math.max(0, rowCount - 1) * gap;
        return new CoinShopLayout(contentX, contentY, contentW, contentH, columns, gap, cardW, cardH, totalHeight, Math.max(0, totalHeight - contentH));
    }

    private Grid cosmeticGrid(int width, int height) {
        int gap = Math.max(6, ResponsiveLayout.adaptiveGap(width, height));
        int minCardW = width < 270 ? 86 : 106;
        int columns = Math.max(1, Math.min(4, (width + gap) / Math.max(1, minCardW + gap)));
        int cardW = Math.max(76, (width - gap * (columns - 1)) / columns);
        int cardH = clamp(Math.round((float)cardW * 1.08F), 96, height < 210 ? 112 : 148);
        return new Grid(columns, gap, cardW, cardH);
    }

    private Grid grid(int width, int height, int minCardW, int preferredCardH, int maxColumns) {
        int gap = ResponsiveLayout.adaptiveGap(width, height);
        int columns = Math.max(1, Math.min(maxColumns, (width + gap) / Math.max(1, minCardW + gap)));
        int cardW = Math.max(64, (width - gap * (columns - 1)) / columns);
        int cardH = Math.max(64, preferredCardH);
        return new Grid(columns, gap, cardW, cardH);
    }

    private static boolean settingActive(Setting<?> setting) {
        Object value = setting.getValue();
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        } else if (value instanceof Number numberValue) {
            return numberValue.doubleValue() > (double)0.0F;
        } else {
            return value != null && !String.valueOf(value).isBlank();
        }
    }

    private static String settingValue(Setting<?> setting) {
        Object value = setting.getValue();
        if (setting instanceof ColorSetting colorSetting) {
            int argb = (Integer)colorSetting.getValue();
            return String.format(Locale.ROOT, "#%06X  %d%%", argb & 16777215, Math.round((float)(argb >>> 24 & 255) * 100.0F / 255.0F));
        } else if (value instanceof Double doubleValue) {
            return String.valueOf(Math.round(doubleValue));
        } else {
            if (value instanceof Integer intValue) {
                if (intValue == 0) {
                    return "None";
                }
            }

            return String.valueOf(value);
        }
    }

    private void renderColorPicker(class_332 context, int mouseX, int mouseY, int accent) {
        int w = Math.min(300, this.field_22789 - 24);
        int h = Math.min(224, this.field_22790 - 24);
        int x = (this.field_22789 - w) / 2;
        int y = (this.field_22790 - h) / 2;
        context.method_25294(0, 0, this.field_22789, this.field_22790, -1728053248);
        PremiumRender.card(context, x, y, w, h, 0, -99543779, ClientTheme.withAlpha(accent, 210));
        String title = this.colorPickerSetting == null ? "COLOR" : this.colorPickerSetting.getName().toUpperCase(Locale.ROOT);
        context.method_27535(this.field_22793, class_2561.method_43470(title), x + 16, y + 14, -1);
        context.method_27535(this.field_22793, class_2561.method_43470("X"), x + w - 24, y + 14, inside((double)mouseX, (double)mouseY, x + w - 34, y + 7, 27, 25) ? -1 : -6643278);
        int argb = (Integer)this.colorPickerSetting.getValue();
        int rgb = argb & 16777215;
        float[] hsb = Color.RGBtoHSB(rgb >>> 16 & 255, rgb >>> 8 & 255, rgb & 255, (float[])null);
        int wheelSize = Math.min(108, h - 70);
        int wheelX = x + 18;
        int wheelY = y + 46;
        ColorWheelTexture.ensureRegistered();
        context.method_25302(class_10799.field_56883, ColorWheelTexture.ID, wheelX, wheelY, 0.0F, 0.0F, wheelSize, wheelSize, 128, 128, 128, 128);
        float angle = hsb[0] * ((float)Math.PI * 2F);
        float radius = hsb[1] * ((float)wheelSize / 2.0F - 2.0F);
        int markerX = Math.round((float)wheelX + (float)wheelSize / 2.0F + class_3532.method_15362((double)angle) * radius);
        int markerY = Math.round((float)wheelY + (float)wheelSize / 2.0F - class_3532.method_15374((double)angle) * radius);
        outline(context, markerX - 3, markerY - 3, 7, 7, 3, -1);
        int sliderX = wheelX + wheelSize + 18;
        int sliderW = Math.max(60, x + w - 18 - sliderX);
        this.renderColorSlider(context, sliderX, y + 58, sliderW, 14, hsb[0], hsb[1], false, hsb[2]);
        context.method_27535(this.field_22793, class_2561.method_43470("BRIGHTNESS"), sliderX, y + 45, -6643278);
        int brightX = sliderX + Math.round(hsb[2] * (float)sliderW);
        context.method_25294(brightX - 1, y + 56, brightX + 1, y + 74, -1);
        context.method_27535(this.field_22793, class_2561.method_43470("TRANSPARENCY"), sliderX, y + 91, -6643278);
        this.renderAlphaSlider(context, sliderX, y + 104, sliderW, 14, rgb);
        float alpha = (float)(argb >>> 24 & 255) / 255.0F;
        int alphaX = sliderX + Math.round(alpha * (float)sliderW);
        context.method_25294(alphaX - 1, y + 102, alphaX + 1, y + 120, -1);
        rect(context, sliderX, y + 139, sliderW, 30, 0, argb);
        outline(context, sliderX, y + 139, sliderW, 30, 0, -1711276033);
        context.method_27534(this.field_22793, class_2561.method_43470(String.format(Locale.ROOT, "#%08X", argb)), sliderX + sliderW / 2, y + 150, hsb[2] > 0.55F && alpha > 0.45F ? -15724008 : -1);
        context.method_27535(this.field_22793, class_2561.method_43470("Drag the wheel and sliders"), x + 18, y + h - 24, -9932669);
    }

    private void renderColorSlider(class_332 context, int x, int y, int w, int h, float hue, float saturation, boolean unused, float value) {
        for(int i = 0; i < w; ++i) {
            float brightness = (float)i / (float)Math.max(1, w - 1);
            int color = -16777216 | Color.HSBtoRGB(hue, saturation, brightness) & 16777215;
            context.method_25294(x + i, y, x + i + 1, y + h, color);
        }

        outline(context, x, y, w, h, 0, -1996488705);
    }

    private void renderAlphaSlider(class_332 context, int x, int y, int w, int h, int rgb) {
        for(int i = 0; i < w; ++i) {
            int alpha = Math.round((float)i * 255.0F / (float)Math.max(1, w - 1));
            int checker = (i / 6 & 1) == 0 ? -13354685 : -11183257;
            context.method_25294(x + i, y, x + i + 1, y + h, checker);
            context.method_25294(x + i, y, x + i + 1, y + h, alpha << 24 | rgb);
        }

        outline(context, x, y, w, h, 0, -1996488705);
    }

    private boolean handleColorPickerClick(int mouseX, int mouseY) {
        int w = Math.min(300, this.field_22789 - 24);
        int h = Math.min(224, this.field_22790 - 24);
        int x = (this.field_22789 - w) / 2;
        int y = (this.field_22790 - h) / 2;
        if (!inside((double)mouseX, (double)mouseY, x + w - 34, y + 7, 27, 25) && inside((double)mouseX, (double)mouseY, x, y, w, h)) {
            int wheelSize = Math.min(108, h - 70);
            int wheelX = x + 18;
            int wheelY = y + 46;
            int sliderX = wheelX + wheelSize + 18;
            int sliderW = Math.max(60, x + w - 18 - sliderX);
            if (inside((double)mouseX, (double)mouseY, wheelX, wheelY, wheelSize, wheelSize)) {
                this.colorPickerDrag = 1;
            } else if (inside((double)mouseX, (double)mouseY, sliderX, y + 54, sliderW, 22)) {
                this.colorPickerDrag = 2;
            } else if (inside((double)mouseX, (double)mouseY, sliderX, y + 100, sliderW, 22)) {
                this.colorPickerDrag = 3;
            }

            if (this.colorPickerDrag != 0) {
                this.updateColorPicker(mouseX, mouseY, this.colorPickerDrag);
            }

            return true;
        } else {
            this.colorPickerSetting = null;
            this.colorPickerModule = null;
            this.colorPickerDrag = 0;
            S9LabClientClient.getConfigManager().save();
            return true;
        }
    }

    private void updateColorPicker(int mouseX, int mouseY, int mode) {
        if (this.colorPickerSetting != null) {
            int w = Math.min(300, this.field_22789 - 24);
            int h = Math.min(224, this.field_22790 - 24);
            int x = (this.field_22789 - w) / 2;
            int y = (this.field_22790 - h) / 2;
            int argb = (Integer)this.colorPickerSetting.getValue();
            int rgb = argb & 16777215;
            int alpha = argb >>> 24 & 255;
            float[] hsb = Color.RGBtoHSB(rgb >>> 16 & 255, rgb >>> 8 & 255, rgb & 255, (float[])null);
            int wheelSize = Math.min(108, h - 70);
            int wheelX = x + 18;
            int wheelY = y + 46;
            int sliderX = wheelX + wheelSize + 18;
            int sliderW = Math.max(60, x + w - 18 - sliderX);
            if (mode == 1) {
                float dx = (float)mouseX - ((float)wheelX + (float)wheelSize / 2.0F);
                float dy = (float)wheelY + (float)wheelSize / 2.0F - (float)mouseY;
                float radius = (float)wheelSize / 2.0F - 2.0F;
                hsb[1] = Math.min(1.0F, (float)Math.sqrt((double)(dx * dx + dy * dy)) / radius);
                hsb[0] = (float)(Math.atan2((double)dy, (double)dx) / (Math.PI * 2D));
                if (hsb[0] < 0.0F) {
                    int var10002 = hsb[0]++;
                }
            } else if (mode == 2) {
                hsb[2] = class_3532.method_15363((float)(mouseX - sliderX) / (float)sliderW, 0.0F, 1.0F);
            } else if (mode == 3) {
                alpha = Math.round(class_3532.method_15363((float)(mouseX - sliderX) / (float)sliderW, 0.0F, 1.0F) * 255.0F);
            }

            int newRgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]) & 16777215;
            this.colorPickerSetting.setValue(alpha << 24 | newRgb);
            if (this.colorPickerModule != null) {
                syncSettingToTheme(this.colorPickerModule, this.colorPickerSetting, false);
            }

        }
    }

    private static int rows(int count, int columns) {
        return (count + Math.max(1, columns) - 1) / Math.max(1, columns);
    }

    private static String titleCase(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        String var10000 = lower.substring(0, 1).toUpperCase(Locale.ROOT);
        return var10000 + lower.substring(1);
    }

    private static String plusExpiryLabel(long expiresAt) {
        long secondsLeft = expiresAt - System.currentTimeMillis() / 1000L;
        if (secondsLeft <= 0L) {
            return "EXPIRED";
        } else {
            long days = Math.max(1L, secondsLeft / 86400L);
            return days + " DAY" + (days == 1L ? "" : "S");
        }
    }

    private static String cosmeticMenuLabel(CosmeticType type) {
        String var10000;
        switch (type) {
            case WINGS -> var10000 = "Wings";
            case GLINT -> var10000 = "Glint";
            case SHIELD -> var10000 = "Shield";
            case EMOTE -> var10000 = "Emotes";
            case HAT -> var10000 = "Hat";
            case HALO -> var10000 = "Halo";
            case BANDANA -> var10000 = "Bandana";
            case CAPE -> var10000 = "Cape";
            case SHOULDER -> var10000 = "Shoulder";
            default -> throw new MatchException((String)null, (Throwable)null);
        }

        return var10000;
    }

    private static ClientTab[] visibleTabs() {
        return new ClientTab[]{S9LabClientScreen.ClientTab.MODS, S9LabClientScreen.ClientTab.SETTINGS, S9LabClientScreen.ClientTab.COSMETICS, S9LabClientScreen.ClientTab.SHOP};
    }

    private int tabStartX(Layout layout) {
        int total = 0;

        for(ClientTab tab : visibleTabs()) {
            total += this.tabWidth(tab);
        }

        total += (visibleTabs().length - 1) * 12;
        int minX = layout.x + 72;
        int maxX = layout.x + layout.width - total - 130;
        return clamp(layout.x + layout.width / 2 - total / 2, minX, Math.max(minX, maxX));
    }

    private int tabWidth(ClientTab tab) {
        int base = this.field_22793 == null ? tab.label.length() * 7 : this.field_22793.method_1727(tab.label);
        return Math.max(48, Math.min(78, base + 20));
    }

    private static String cosmeticRarity(Cosmetic cosmetic) {
        int hash = Math.abs(cosmetic.id().hashCode());
        String var10000;
        switch (hash % 4) {
            case 0 -> var10000 = "Common";
            case 1 -> var10000 = "Rare";
            case 2 -> var10000 = "Epic";
            default -> var10000 = "Legendary";
        }

        return var10000;
    }

    private static int rarityColor(Cosmetic cosmetic) {
        int var10000;
        switch (cosmeticRarity(cosmetic)) {
            case "Rare" -> var10000 = -12998657;
            case "Epic" -> var10000 = -4832513;
            case "Legendary" -> var10000 = -19372;
            default -> var10000 = -9932669;
        }

        return var10000;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return width > 0 && height > 0 && mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height);
    }

    private static void shadow(class_332 context, int x, int y, int width, int height, int radius, int color) {
        rect(context, x + 3, y + 4, width, height, radius, color);
    }

    private static void glowLine(class_332 context, int x, int y, int width, int color) {
        context.method_25294(x, y, x + width, y + 1, ClientTheme.withAlpha(color, 70));
    }

    private static void rect(class_332 context, int x, int y, int width, int height, int radius, int color) {
        PremiumRender.roundedRect(context, x, y, width, height, radius, color);
    }

    private static void outline(class_332 context, int x, int y, int width, int height, int radius, int color) {
        PremiumRender.outline(context, x, y, width, height, radius, color);
    }

    private static String formatCoins(long coins) {
        return String.format(Locale.GERMANY, "%,d", coins);
    }

    private static String displayMetric(int value) {
        return value < 0 ? "..." : String.valueOf(value);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Environment(EnvType.CLIENT)
    private static record Layout(int x, int y, int width, int height, int pad, int headerHeight) {
        private int bodyX() {
            return this.x + this.pad;
        }

        private int bodyY() {
            return this.y + this.headerHeight;
        }

        private int bodyWidth() {
            return Math.max(1, this.width - this.pad * 2);
        }

        private int bodyHeight() {
            return Math.max(1, this.height - this.headerHeight - this.pad);
        }
    }

    @Environment(EnvType.CLIENT)
    private static record Grid(int columns, int gap, int cardW, int cardH) {
        private int contentHeight(int itemCount) {
            return S9LabClientScreen.rows(itemCount, this.columns) * (this.cardH + this.gap);
        }
    }

    @Environment(EnvType.CLIENT)
    private static record MetricCard(String label, String value, String hint) {
    }

    @Environment(EnvType.CLIENT)
    private static record CosmeticLayout(int x, int y, int width, int height, int topbarH, int footerH, int contentY, int contentH, int sideX, int sideW, int gridX, int gridY, int gridW, int gridH, Rect preview) {
    }

    @Environment(EnvType.CLIENT)
    private static record Rect(int x, int y, int width, int height) {
        private static Rect empty() {
            return new Rect(0, 0, 0, 0);
        }

        private boolean contains(double mouseX, double mouseY) {
            return S9LabClientScreen.inside(mouseX, mouseY, this.x, this.y, this.width, this.height);
        }
    }

    @Environment(EnvType.CLIENT)
    private static record CoinShopLayout(int contentX, int contentY, int contentW, int contentH, int columns, int gap, int cardW, int cardH, int totalHeight, int maxScroll) {
    }

    @Environment(EnvType.CLIENT)
    public static enum ClientTab {
        MODS("Mods"),
        SETTINGS("Settings"),
        COSMETICS("Cosmetics"),
        SHOP("Catalog");

        private final String label;

        private ClientTab(String label) {
            this.label = label;
        }
    }
}
