package site.s9lab.s9labclient.client.ui;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import site.s9lab.s9labclient.client.backend.BackendClient;
import site.s9lab.s9labclient.client.backend.BackendState;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;
import site.s9lab.s9labclient.client.ui.premium.theme.ClientTheme;
import site.s9lab.s9labclient.client.ui.premium.theme.ThemeManager;

/** Compact Fortnite-inspired social panel using the S9Lab RShift visual language. */
public final class FriendsOverlayScreen extends ResponsiveScreen {
    private static final int PANEL_W = 382;
    private static final int TOP = 18;
    private static final int BOTTOM = 18;
    private static final int HEADER_H = 48;
    private static final int TABS_H = 34;
    private static final int ROW_H = 48;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    private final Screen parent;
    private Tab tab = Tab.FRIENDS;
    private Focus focus = Focus.NONE;
    private String addText = "";
    private String searchText = "";
    private String messageText = "";
    private UUID selectedFriend;
    private int scroll;

    public FriendsOverlayScreen(Screen parent) {
        super(Text.literal("S9Lab Social"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        BackendClient.fetchFriendsAsync();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ensureResponsiveLayout();
        ClientTheme theme = ThemeManager.theme();
        int x = panelX();
        int h = height - TOP - BOTTOM;

        PremiumRender.shopBackdrop(context);
        context.fill(0, 0, x, height, 0x8A000000);
        PremiumRender.shopPanel(context, x, TOP, PANEL_W, h, HEADER_H, 0);
        context.fill(x, TOP, x + 3, TOP + h, theme.accentColor());

        renderHeader(context, x, mouseX, mouseY, theme);
        renderTabs(context, x, mouseX, mouseY, theme);

        int bodyTop = TOP + HEADER_H + TABS_H;
        if (selectedFriend != null && tab == Tab.FRIENDS) {
            renderChat(context, x, bodyTop, mouseX, mouseY, theme);
        } else {
            renderListPanel(context, x, bodyTop, mouseX, mouseY, theme);
        }
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    private void renderHeader(DrawContext context, int x, int mouseX, int mouseY, ClientTheme theme) {
        context.drawTextWithShadow(textRenderer, Text.literal("S9LAB SOCIAL"), x + 18, TOP + 12, 0xFFFFFFFF);
        long online = BackendState.friendsSnapshot().stream().filter(BackendState.Friend::online).count();
        context.drawTextWithShadow(textRenderer, Text.literal(online + " online"), x + 18, TOP + 28, online > 0 ? 0xFF53E77B : 0xFF7C8493);
        PremiumRender.shopButton(context, Text.literal("×"), x + PANEL_W - 36, TOP + 11, 24, 24, false,
                inside(mouseX, mouseY, x + PANEL_W - 36, TOP + 11, 24, 24), 0xFFFF6B77);
    }

    private void renderTabs(DrawContext context, int x, int mouseX, int mouseY, ClientTheme theme) {
        int y = TOP + HEADER_H;
        int innerX = x + 10;
        int gap = 4;
        int tabW = (PANEL_W - 20 - gap * 2) / 3;
        drawTab(context, innerX, y + 4, tabW, 26, "FRIENDS " + BackendState.friendsSnapshot().size(), tab == Tab.FRIENDS, mouseX, mouseY, theme);
        drawTab(context, innerX + tabW + gap, y + 4, tabW, 26, "REQUESTS " + BackendState.incomingFriendRequestsSnapshot().size(), tab == Tab.REQUESTS, mouseX, mouseY, theme);
        drawTab(context, innerX + (tabW + gap) * 2, y + 4, tabW, 26, "PENDING " + BackendState.outgoingFriendRequestsSnapshot().size(), tab == Tab.PENDING, mouseX, mouseY, theme);
    }

    private void drawTab(DrawContext context, int x, int y, int w, int h, String label, boolean active, int mouseX, int mouseY, ClientTheme theme) {
        PremiumRender.shopButton(context, Text.literal(label), x, y, w, h, active, inside(mouseX, mouseY, x, y, w, h), theme.accentColor());
    }

    private void renderListPanel(DrawContext context, int x, int top, int mouseX, int mouseY, ClientTheme theme) {
        int innerX = x + 12;
        int innerW = PANEL_W - 24;
        int contentTop = top + 8;

        if (tab == Tab.FRIENDS) {
            drawInput(context, innerX, contentTop, innerW - 38, 25, addText, "Add friend by name / UUID", focus == Focus.ADD);
            PremiumRender.shopButton(context, Text.literal("+"), innerX + innerW - 32, contentTop, 32, 25, false,
                    inside(mouseX, mouseY, innerX + innerW - 32, contentTop, 32, 25), theme.accentColor());
            drawInput(context, innerX, contentTop + 33, innerW, 23, searchText, "Search friends...", focus == Focus.SEARCH);
            contentTop += 64;
        } else {
            context.drawTextWithShadow(textRenderer, Text.literal(tab == Tab.REQUESTS ? "INCOMING REQUESTS" : "SENT REQUESTS"), innerX, contentTop + 4, 0xFFB8C0CF);
            contentTop += 24;
        }

        int bottom = height - BOTTOM - 10;
        context.enableScissor(innerX, contentTop, innerX + innerW, bottom);
        int rowY = contentTop - scroll;

        if (tab == Tab.FRIENDS) {
            List<BackendState.Friend> friends = filteredFriends();
            for (BackendState.Friend friend : friends) {
                if (rowY + ROW_H >= contentTop && rowY <= bottom) renderFriendRow(context, friend, innerX, rowY, innerW, mouseX, mouseY, theme);
                rowY += ROW_H + 5;
            }
            if (friends.isEmpty()) drawEmpty(context, innerX, contentTop, innerW, "No friends found");
        } else {
            List<BackendState.FriendRequest> requests = tab == Tab.REQUESTS
                    ? BackendState.incomingFriendRequestsSnapshot()
                    : BackendState.outgoingFriendRequestsSnapshot();
            for (BackendState.FriendRequest request : requests) {
                if (rowY + 58 >= contentTop && rowY <= bottom) renderRequestRow(context, request, innerX, rowY, innerW, mouseX, mouseY, theme, tab == Tab.REQUESTS);
                rowY += 63;
            }
            if (requests.isEmpty()) drawEmpty(context, innerX, contentTop, innerW, tab == Tab.REQUESTS ? "No incoming requests" : "No pending requests");
        }
        context.disableScissor();
    }

    private void renderFriendRow(DrawContext context, BackendState.Friend friend, int x, int y, int w, int mouseX, int mouseY, ClientTheme theme) {
        boolean hover = inside(mouseX, mouseY, x, y, w, ROW_H);
        PremiumRender.card(context, x, y, w, ROW_H, 0, hover ? PremiumRender.SHOP_CARD_HOVER : 0xC7141820, hover ? theme.accentColor() : 0x553D4655);
        renderAvatar(context, friend.name(), x + 7, y + 7, 34, friend.online() ? 0xFF2F9D5B : 0xFF3A4351);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, friend.name(), w - 92)), x + 50, y + 8, 0xFFFFFFFF);
        String status = friend.online() ? (friend.status().isBlank() ? "Online" : friend.status()) : lastSeen(friend.lastSeen());
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, status, w - 92)), x + 50, y + 25, friend.online() ? 0xFF62E487 : 0xFF818A9A);
        if (friend.favorite()) context.drawTextWithShadow(textRenderer, Text.literal("★"), x + w - 43, y + 9, 0xFFFFC857);
        context.drawTextWithShadow(textRenderer, Text.literal("›"), x + w - 19, y + 17, hover ? theme.accentColor() : 0xFF828B9A);
        if (friend.unreadMessages() > 0) {
            String count = friend.unreadMessages() > 99 ? "99+" : Integer.toString(friend.unreadMessages());
            int bw = Math.max(16, textRenderer.getWidth(count) + 8);
            context.fill(x + w - bw - 35, y + 27, x + w - 35, y + 42, theme.accentColor());
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(count), x + w - bw / 2 - 35, y + 30, 0xFFFFFFFF);
        }
    }

    private void renderRequestRow(DrawContext context, BackendState.FriendRequest request, int x, int y, int w, int mouseX, int mouseY, ClientTheme theme, boolean incoming) {
        PremiumRender.card(context, x, y, w, 58, 0, 0xD0141820, 0x663D4655);
        renderAvatar(context, request.name(), x + 8, y + 8, 34, 0xFF33425A);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, request.name(), w - 150)), x + 50, y + 9, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal(incoming ? "Wants to be your friend" : "Request sent"), x + 50, y + 27, 0xFF818A9A);
        if (incoming) {
            int acceptX = x + w - 92;
            int declineX = x + w - 46;
            PremiumRender.shopButton(context, Text.literal("✓"), acceptX, y + 15, 38, 28, false, inside(mouseX, mouseY, acceptX, y + 15, 38, 28), 0xFF4DE47A);
            PremiumRender.shopButton(context, Text.literal("×"), declineX, y + 15, 38, 28, false, inside(mouseX, mouseY, declineX, y + 15, 38, 28), 0xFFFF6B77);
        }
    }

    private void renderChat(DrawContext context, int x, int top, int mouseX, int mouseY, ClientTheme theme) {
        BackendState.Friend friend = BackendState.friend(selectedFriend);
        if (friend == null) { selectedFriend = null; return; }
        int innerX = x + 12;
        int innerW = PANEL_W - 24;
        PremiumRender.card(context, innerX, top + 8, innerW, 46, 0, 0xD0141820, 0x663D4655);
        renderAvatar(context, friend.name(), innerX + 7, top + 14, 32, friend.online() ? 0xFF2F9D5B : 0xFF3A4351);
        context.drawTextWithShadow(textRenderer, Text.literal(friend.name()), innerX + 48, top + 15, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal(friend.online() ? friend.status() : lastSeen(friend.lastSeen())), innerX + 48, top + 31, 0xFF818A9A);
        PremiumRender.shopButton(context, Text.literal("‹"), innerX + innerW - 34, top + 18, 26, 24, false, inside(mouseX, mouseY, innerX + innerW - 34, top + 18, 26, 24), theme.accentColor());

        int messagesTop = top + 62;
        int inputY = height - BOTTOM - 38;
        context.enableScissor(innerX, messagesTop, innerX + innerW, inputY - 5);
        List<BackendState.DirectMessage> messages = BackendState.conversationSnapshot(selectedFriend);
        int rowY = Math.max(messagesTop + 4, inputY - 8 - messages.size() * 36);
        UUID own = ownUuid();
        for (BackendState.DirectMessage message : messages) {
            boolean mine = own != null && own.toString().equals(message.senderUuid());
            renderMessage(context, message, innerX, rowY, innerW, mine, theme);
            rowY += 36;
        }
        context.disableScissor();
        drawInput(context, innerX, inputY, innerW - 38, 26, messageText, "Message...", focus == Focus.MESSAGE);
        PremiumRender.shopButton(context, Text.literal("➤"), innerX + innerW - 32, inputY, 32, 26, false,
                inside(mouseX, mouseY, innerX + innerW - 32, inputY, 32, 26), theme.accentColor());
    }

    private void renderMessage(DrawContext context, BackendState.DirectMessage message, int x, int y, int w, boolean mine, ClientTheme theme) {
        String value = TextLayout.ellipsize(textRenderer, message.message(), w - 70);
        int bubbleW = Math.min(w - 34, Math.max(90, textRenderer.getWidth(value) + 18));
        int bx = mine ? x + w - bubbleW : x;
        PremiumRender.card(context, bx, y, bubbleW, 31, 0, mine ? 0xD02A1645 : 0xD0141820, mine ? theme.accentColor() : 0x663D4655);
        context.drawTextWithShadow(textRenderer, Text.literal(value), bx + 8, y + 6, 0xFFFFFFFF);
        String time = TIME.format(Instant.ofEpochSecond(message.sentAt()));
        context.drawTextWithShadow(textRenderer, Text.literal(time), bx + bubbleW - textRenderer.getWidth(time) - 6, y + 19, 0xFF778192);
    }

    private void renderAvatar(DrawContext context, String name, int x, int y, int size, int color) {
        context.fill(x, y, x + size, y + size, color);
        PremiumRender.outline(context, x, y, size, size, 0, 0x778A96A8);
        String letter = name == null || name.isBlank() ? "?" : name.substring(0, 1).toUpperCase(Locale.ROOT);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(letter), x + size / 2, y + (size - textRenderer.fontHeight) / 2, 0xFFFFFFFF);
    }

    private void drawEmpty(DrawContext context, int x, int y, int w, String text) {
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(text), x + w / 2, y + 30, 0xFF747D8D);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mx = (int) click.x();
        int my = (int) click.y();
        int x = panelX();
        if (mx < x) { close(); return true; }
        if (inside(mx, my, x + PANEL_W - 36, TOP + 11, 24, 24)) { close(); return true; }

        int tabsY = TOP + HEADER_H + 4;
        int tabX = x + 10;
        int gap = 4;
        int tabW = (PANEL_W - 20 - gap * 2) / 3;
        for (int i = 0; i < 3; i++) {
            if (inside(mx, my, tabX + i * (tabW + gap), tabsY, tabW, 26)) {
                tab = Tab.values()[i]; selectedFriend = null; scroll = 0; focus = Focus.NONE; BackendClient.fetchFriendsAsync(); return true;
            }
        }

        int top = TOP + HEADER_H + TABS_H;
        int innerX = x + 12;
        int innerW = PANEL_W - 24;
        int contentTop = top + 8;

        if (selectedFriend != null && tab == Tab.FRIENDS) {
            if (inside(mx, my, innerX + innerW - 34, top + 18, 26, 24)) { selectedFriend = null; focus = Focus.NONE; return true; }
            int inputY = height - BOTTOM - 38;
            if (inside(mx, my, innerX, inputY, innerW - 38, 26)) { focus = Focus.MESSAGE; return true; }
            if (inside(mx, my, innerX + innerW - 32, inputY, 32, 26)) { submitMessage(); return true; }
            return true;
        }

        if (tab == Tab.FRIENDS) {
            if (inside(mx, my, innerX, contentTop, innerW - 38, 25)) { focus = Focus.ADD; return true; }
            if (inside(mx, my, innerX + innerW - 32, contentTop, 32, 25)) { submitFriendRequest(); return true; }
            if (inside(mx, my, innerX, contentTop + 33, innerW, 23)) { focus = Focus.SEARCH; return true; }
            contentTop += 64;
            int rowY = contentTop - scroll;
            for (BackendState.Friend friend : filteredFriends()) {
                if (inside(mx, my, innerX, rowY, innerW, ROW_H)) {
                    try { selectedFriend = UUID.fromString(friend.uuid()); BackendClient.fetchFriendMessages(friend.uuid()); focus = Focus.MESSAGE; }
                    catch (IllegalArgumentException ignored) { }
                    return true;
                }
                rowY += ROW_H + 5;
            }
        } else {
            contentTop += 24;
            List<BackendState.FriendRequest> requests = tab == Tab.REQUESTS
                    ? BackendState.incomingFriendRequestsSnapshot()
                    : BackendState.outgoingFriendRequestsSnapshot();
            int rowY = contentTop - scroll;
            for (BackendState.FriendRequest request : requests) {
                if (tab == Tab.REQUESTS) {
                    int acceptX = innerX + innerW - 92;
                    int declineX = innerX + innerW - 46;
                    if (inside(mx, my, acceptX, rowY + 15, 38, 28)) {
                        BackendClient.respondFriendRequest(request.uuid(), true);
                        BackendClient.fetchFriendsAsync();
                        return true;
                    }
                    if (inside(mx, my, declineX, rowY + 15, 38, 28)) {
                        BackendClient.respondFriendRequest(request.uuid(), false);
                        BackendClient.fetchFriendsAsync();
                        return true;
                    }
                }
                rowY += 63;
            }
        }
        focus = Focus.NONE;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int count = switch (tab) {
            case FRIENDS -> filteredFriends().size();
            case REQUESTS -> BackendState.incomingFriendRequestsSnapshot().size();
            case PENDING -> BackendState.outgoingFriendRequestsSnapshot().size();
        };
        int row = tab == Tab.FRIENDS ? ROW_H + 5 : 63;
        int max = Math.max(0, count * row - Math.max(100, height - 190));
        scroll = Math.max(0, Math.min(max, scroll - (int) Math.round(verticalAmount * 28.0)));
        return true;
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!input.isValidChar()) return super.charTyped(input);
        String v = input.asString();
        switch (focus) {
            case ADD -> { if (addText.length() < 48) addText += v; }
            case SEARCH -> { if (searchText.length() < 40) searchText += v; }
            case MESSAGE -> { if (messageText.length() < 240) messageText += v; }
            default -> { return super.charTyped(input); }
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.isEscape()) { if (selectedFriend != null) selectedFriend = null; else if (focus != Focus.NONE) focus = Focus.NONE; else close(); return true; }
        if (input.getKeycode() == GLFW.GLFW_KEY_ENTER) { if (focus == Focus.ADD) submitFriendRequest(); else if (focus == Focus.MESSAGE) submitMessage(); return true; }
        if (input.getKeycode() == GLFW.GLFW_KEY_BACKSPACE) {
            switch (focus) {
                case ADD -> addText = removeLast(addText);
                case SEARCH -> searchText = removeLast(searchText);
                case MESSAGE -> messageText = removeLast(messageText);
                default -> { return super.keyPressed(input); }
            }
            return true;
        }
        return super.keyPressed(input);
    }

    private void submitFriendRequest() { String v = addText.trim(); if (!v.isBlank()) { BackendClient.addFriend(v); addText = ""; BackendClient.fetchFriendsAsync(); } }
    private void submitMessage() { if (selectedFriend != null && !messageText.isBlank()) { BackendClient.sendFriendMessage(selectedFriend.toString(), messageText.trim()); messageText = ""; } }

    private List<BackendState.Friend> filteredFriends() {
        String q = searchText.trim().toLowerCase(Locale.ROOT);
        if (q.isBlank()) return BackendState.friendsSnapshot();
        return BackendState.friendsSnapshot().stream().filter(f -> f.name().toLowerCase(Locale.ROOT).contains(q) || f.uuid().contains(q)).toList();
    }

    private void drawInput(DrawContext context, int x, int y, int w, int h, String value, String hint, boolean focused) {
        PremiumRender.shopInput(context, x, y, w, h, focused, ThemeManager.theme().accentColor());
        String shown = value.isBlank() ? hint : value;
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, shown, w - 12)), x + 6, y + (h - textRenderer.fontHeight) / 2, value.isBlank() ? 0xFF70798A : 0xFFFFFFFF);
    }

    @Override public void close() { MinecraftClient.getInstance().setScreen(parent); }
    private int panelX() { return Math.max(0, width - PANEL_W - 14); }
    private static boolean inside(double mx, double my, int x, int y, int w, int h) { return mx >= x && mx <= x + w && my >= y && my <= y + h; }
    private static String removeLast(String s) { if (s == null || s.isEmpty()) return ""; return s.substring(0, s.offsetByCodePoints(s.length(), -1)); }
    private static String lastSeen(long epoch) { if (epoch <= 0) return "Offline"; long sec = Math.max(0, Instant.now().getEpochSecond() - epoch); if (sec < 60) return "Just offline"; if (sec < 3600) return "Offline for " + sec / 60 + " min"; if (sec < 86400) return "Offline for " + sec / 3600 + " hours"; return "Offline for " + sec / 86400 + " days"; }
    private static UUID ownUuid() { try { return MinecraftClient.getInstance().getSession().getUuidOrNull(); } catch (RuntimeException e) { return null; } }

    private enum Tab { FRIENDS, REQUESTS, PENDING }
    private enum Focus { NONE, ADD, SEARCH, MESSAGE }
}
