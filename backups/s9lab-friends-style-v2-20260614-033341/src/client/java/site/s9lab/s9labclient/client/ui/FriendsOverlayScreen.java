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

/** Fortnite-style right side social panel with friend requests and private messages. */
public final class FriendsOverlayScreen extends ResponsiveScreen {
    private static final int HEADER_H = 54;
    private static final int LIST_W = 238;
    private static final int ROW_H = 43;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    private final Screen parent;
    private Focus focus = Focus.NONE;
    private String addText = "";
    private String searchText = "";
    private String messageText = "";
    private UUID selectedFriend;
    private int listScroll;
    private int messageScroll;

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
        int panelW = width - x;

        context.fill(0, 0, x, height, 0x77000000);
        PremiumRender.roundedRect(context, x, 0, panelW, height, 0, 0xFA080B11);
        context.fill(x, 0, x + 2, height, theme.accentColor());
        renderHeader(context, mouseX, mouseY, theme);
        renderSidebar(context, mouseX, mouseY, theme);
        renderChat(context, mouseX, mouseY, theme);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    private void renderHeader(DrawContext context, int mouseX, int mouseY, ClientTheme theme) {
        int x = panelX();
        context.fill(x + 2, 0, width, HEADER_H, 0xFF0E131D);
        context.drawTextWithShadow(textRenderer, Text.literal("S9LAB SOCIAL"), x + 16, 13, 0xFFFFFFFF);
        long online = BackendState.friendsSnapshot().stream().filter(BackendState.Friend::online).count();
        String sub = online + " online  •  " + BackendState.friendsSnapshot().size() + " friends";
        context.drawTextWithShadow(textRenderer, Text.literal(sub), x + 16, 31, 0xFF8E98AA);
        button(context, width - 36, 12, 24, 24, "×", inside(mouseX, mouseY, width - 36, 12, 24, 24), 0xFFFF6B77);
    }

    private void renderSidebar(DrawContext context, int mouseX, int mouseY, ClientTheme theme) {
        int x = panelX() + 2;
        int y = HEADER_H;
        int w = Math.min(LIST_W, Math.max(188, width - panelX() - 210));
        context.fill(x, y, x + w, height, 0xFF0B0F16);
        context.fill(x + w - 1, y, x + w, height, 0x66364050);

        drawInput(context, x + 10, y + 10, w - 52, 24, addText, "Name oder UUID", focus == Focus.ADD);
        button(context, x + w - 36, y + 10, 26, 24, "+", inside(mouseX, mouseY, x + w - 36, y + 10, 26, 24), theme.accentColor());
        drawInput(context, x + 10, y + 42, w - 20, 22, searchText, "Freunde suchen...", focus == Focus.SEARCH);

        int contentY = y + 72;
        List<BackendState.FriendRequest> requests = BackendState.incomingFriendRequestsSnapshot();
        if (!requests.isEmpty()) {
            context.drawTextWithShadow(textRenderer, Text.literal("ANFRAGEN  " + requests.size()), x + 12, contentY, 0xFFFFC857);
            contentY += 16;
            for (BackendState.FriendRequest request : requests) {
                if (contentY > height - 72) break;
                renderRequest(context, request, x + 8, contentY, w - 16, mouseX, mouseY);
                contentY += 36;
            }
            contentY += 5;
        }

        context.drawTextWithShadow(textRenderer, Text.literal("FREUNDE"), x + 12, contentY, 0xFF8E98AA);
        int listTop = contentY + 16;
        int listBottom = height - 10;
        List<BackendState.Friend> friends = filteredFriends();
        context.enableScissor(x, listTop, x + w, listBottom);
        int rowY = listTop - listScroll;
        for (BackendState.Friend friend : friends) {
            if (rowY + ROW_H >= listTop && rowY <= listBottom) {
                renderFriendRow(context, friend, x + 6, rowY, w - 12, mouseX, mouseY, theme);
            }
            rowY += ROW_H + 3;
        }
        context.disableScissor();
        if (friends.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Keine Freunde gefunden"), x + w / 2, listTop + 24, 0xFF687083);
        }
    }

    private void renderRequest(DrawContext context, BackendState.FriendRequest request, int x, int y, int w, int mouseX, int mouseY) {
        PremiumRender.card(context, x, y, w, 32, 0, 0xFF151A24, 0x665B6578);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, request.name(), w - 74)), x + 8, y + 7, 0xFFFFFFFF);
        boolean accept = inside(mouseX, mouseY, x + w - 54, y + 5, 22, 22);
        boolean decline = inside(mouseX, mouseY, x + w - 28, y + 5, 22, 22);
        button(context, x + w - 54, y + 5, 22, 22, "✓", accept, 0xFF48E06E);
        button(context, x + w - 28, y + 5, 22, 22, "×", decline, 0xFFFF6B77);
    }

    private void renderFriendRow(DrawContext context, BackendState.Friend friend, int x, int y, int w, int mouseX, int mouseY, ClientTheme theme) {
        boolean selected = selectedFriend != null && selectedFriend.toString().equals(friend.uuid());
        boolean hovered = inside(mouseX, mouseY, x, y, w, ROW_H);
        int bg = selected ? 0xFF1B2740 : hovered ? 0xFF161C27 : 0xFF10151E;
        int border = selected ? theme.accentColor() : 0x55364050;
        PremiumRender.card(context, x, y, w, ROW_H, 0, bg, border);
        int dot = friend.online() ? 0xFF3DE767 : 0xFF687083;
        context.fill(x + 9, y + 11, x + 16, y + 18, dot);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, friend.name(), w - 68)), x + 23, y + 7, 0xFFFFFFFF);
        String status = friend.online() ? (friend.status().isBlank() ? "Online" : friend.status()) : lastSeen(friend.lastSeen());
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, status, w - 56)), x + 23, y + 23, friend.online() ? 0xFF8EEAA3 : 0xFF7E8796);
        if (friend.favorite()) {
            context.drawTextWithShadow(textRenderer, Text.literal("★"), x + w - 18, y + 7, 0xFFFFC857);
        }
        if (friend.unreadMessages() > 0) {
            String unread = friend.unreadMessages() > 99 ? "99+" : Integer.toString(friend.unreadMessages());
            int badgeW = Math.max(16, textRenderer.getWidth(unread) + 8);
            PremiumRender.roundedRect(context, x + w - badgeW - 7, y + 23, badgeW, 14, 0, theme.accentColor());
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(unread), x + w - badgeW / 2 - 7, y + 26, 0xFFFFFFFF);
        }
    }

    private void renderChat(DrawContext context, int mouseX, int mouseY, ClientTheme theme) {
        int sideW = Math.min(LIST_W, Math.max(188, width - panelX() - 210));
        int x = panelX() + 2 + sideW;
        int y = HEADER_H;
        int w = width - x;
        if (selectedFriend == null) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Wähle einen Freund aus"), x + w / 2, height / 2 - 10, 0xFFB8C1D1);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Private Nachrichten erscheinen hier"), x + w / 2, height / 2 + 8, 0xFF687083);
            return;
        }
        BackendState.Friend friend = BackendState.friend(selectedFriend);
        if (friend == null) {
            selectedFriend = null;
            return;
        }

        context.fill(x, y, width, y + 46, 0xFF101621);
        context.fill(x, y + 45, width, y + 46, 0x55364050);
        context.fill(x + 14, y + 17, x + 22, y + 25, friend.online() ? 0xFF3DE767 : 0xFF687083);
        context.drawTextWithShadow(textRenderer, Text.literal(friend.name()), x + 30, y + 10, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal(friend.online() ? friend.status() : lastSeen(friend.lastSeen())), x + 30, y + 26, 0xFF8E98AA);
        button(context, width - 73, y + 10, 24, 24, friend.favorite() ? "★" : "☆", inside(mouseX, mouseY, width - 73, y + 10, 24, 24), 0xFFFFC857);
        button(context, width - 41, y + 10, 26, 24, "−", inside(mouseX, mouseY, width - 41, y + 10, 26, 24), 0xFFFF6B77);

        int messagesTop = y + 52;
        int inputY = height - 42;
        context.enableScissor(x, messagesTop, width, inputY - 4);
        List<BackendState.DirectMessage> messages = BackendState.conversationSnapshot(selectedFriend);
        int contentHeight = messages.size() * 38;
        int startY = Math.max(messagesTop + 6, inputY - 10 - contentHeight) - messageScroll;
        int rowY = startY;
        UUID own = ownUuid();
        for (BackendState.DirectMessage message : messages) {
            boolean mine = own != null && own.toString().equals(message.senderUuid());
            renderMessage(context, message, x + 10, rowY, w - 20, mine, theme);
            rowY += 38;
        }
        context.disableScissor();

        context.fill(x, inputY - 5, width, height, 0xFF0D121B);
        drawInput(context, x + 10, inputY, w - 56, 26, messageText, "Nachricht an " + friend.name(), focus == Focus.MESSAGE);
        button(context, width - 40, inputY, 28, 26, "➤", inside(mouseX, mouseY, width - 40, inputY, 28, 26), theme.accentColor());
    }

    private void renderMessage(DrawContext context, BackendState.DirectMessage message, int x, int y, int w, boolean mine, ClientTheme theme) {
        String value = TextLayout.ellipsize(textRenderer, message.message(), Math.max(80, w - 58));
        int bubbleW = Math.min(w - 28, Math.max(78, textRenderer.getWidth(value) + 18));
        int bubbleX = mine ? x + w - bubbleW : x;
        int bg = mine ? 0xFF233B66 : 0xFF171E2A;
        int border = mine ? theme.accentColor() : 0x66505B6D;
        PremiumRender.card(context, bubbleX, y, bubbleW, 31, 0, bg, border);
        context.drawTextWithShadow(textRenderer, Text.literal(value), bubbleX + 8, y + 6, 0xFFFFFFFF);
        String time = TIME.format(Instant.ofEpochSecond(message.sentAt()));
        context.drawTextWithShadow(textRenderer, Text.literal(time), bubbleX + bubbleW - textRenderer.getWidth(time) - 6, y + 19, 0xFF798395);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mx = (int) click.x();
        int my = (int) click.y();
        if (mx < panelX()) {
            close();
            return true;
        }
        if (inside(mx, my, width - 36, 12, 24, 24)) {
            close();
            return true;
        }
        int sideX = panelX() + 2;
        int sideW = Math.min(LIST_W, Math.max(188, width - panelX() - 210));
        int y = HEADER_H;
        if (inside(mx, my, sideX + 10, y + 10, sideW - 52, 24)) {
            focus = Focus.ADD;
            return true;
        }
        if (inside(mx, my, sideX + sideW - 36, y + 10, 26, 24)) {
            submitFriendRequest();
            return true;
        }
        if (inside(mx, my, sideX + 10, y + 42, sideW - 20, 22)) {
            focus = Focus.SEARCH;
            return true;
        }

        int contentY = y + 72;
        for (BackendState.FriendRequest request : BackendState.incomingFriendRequestsSnapshot()) {
            if (inside(mx, my, sideX + 8 + sideW - 16 - 54, contentY + 5, 22, 22)) {
                BackendClient.respondFriendRequest(request.uuid(), true);
                return true;
            }
            if (inside(mx, my, sideX + 8 + sideW - 16 - 28, contentY + 5, 22, 22)) {
                BackendClient.respondFriendRequest(request.uuid(), false);
                return true;
            }
            contentY += 36;
        }
        if (!BackendState.incomingFriendRequestsSnapshot().isEmpty()) contentY += 5;
        int listTop = contentY + 16;
        int rowY = listTop - listScroll;
        for (BackendState.Friend friend : filteredFriends()) {
            if (inside(mx, my, sideX + 6, rowY, sideW - 12, ROW_H)) {
                selectedFriend = UUID.fromString(friend.uuid());
                BackendClient.fetchFriendMessages(friend.uuid());
                messageScroll = 0;
                focus = Focus.MESSAGE;
                return true;
            }
            rowY += ROW_H + 3;
        }

        if (selectedFriend != null) {
            BackendState.Friend friend = BackendState.friend(selectedFriend);
            if (friend != null && inside(mx, my, width - 73, HEADER_H + 10, 24, 24)) {
                BackendClient.setFriendFavorite(friend.uuid(), !friend.favorite());
                return true;
            }
            if (inside(mx, my, width - 41, HEADER_H + 10, 26, 24)) {
                BackendClient.removeFriend(selectedFriend.toString());
                selectedFriend = null;
                return true;
            }
            int chatX = sideX + sideW;
            int inputY = height - 42;
            if (inside(mx, my, chatX + 10, inputY, width - chatX - 56, 26)) {
                focus = Focus.MESSAGE;
                return true;
            }
            if (inside(mx, my, width - 40, inputY, 28, 26)) {
                submitMessage();
                return true;
            }
        }
        focus = Focus.NONE;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int sideW = Math.min(LIST_W, Math.max(188, width - panelX() - 210));
        if (mouseX < panelX() + 2 + sideW) {
            int max = Math.max(0, filteredFriends().size() * (ROW_H + 3) - Math.max(80, height - 160));
            listScroll = Math.max(0, Math.min(max, listScroll - (int) Math.round(verticalAmount * 26.0)));
            return true;
        }
        if (selectedFriend != null) {
            int max = Math.max(0, BackendState.conversationSnapshot(selectedFriend).size() * 38 - Math.max(60, height - 160));
            messageScroll = Math.max(0, Math.min(max, messageScroll - (int) Math.round(verticalAmount * 26.0)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!input.isValidChar()) return super.charTyped(input);
        String value = input.asString();
        switch (focus) {
            case ADD -> { if (addText.length() < 48) addText += value; }
            case SEARCH -> { if (searchText.length() < 32) { searchText += value; listScroll = 0; } }
            case MESSAGE -> { if (messageText.length() < 240) messageText += value; }
            default -> { return super.charTyped(input); }
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.isEscape()) {
            if (focus != Focus.NONE) {
                focus = Focus.NONE;
                return true;
            }
            close();
            return true;
        }
        if (input.getKeycode() == GLFW.GLFW_KEY_ENTER) {
            if (focus == Focus.ADD) submitFriendRequest();
            else if (focus == Focus.MESSAGE) submitMessage();
            return true;
        }
        if (input.getKeycode() == GLFW.GLFW_KEY_BACKSPACE) {
            switch (focus) {
                case ADD -> addText = removeLast(addText);
                case SEARCH -> { searchText = removeLast(searchText); listScroll = 0; }
                case MESSAGE -> messageText = removeLast(messageText);
                default -> { return super.keyPressed(input); }
            }
            return true;
        }
        return super.keyPressed(input);
    }

    private void submitFriendRequest() {
        String value = addText.trim();
        if (value.isBlank()) return;
        BackendClient.addFriend(value);
        addText = "";
        focus = Focus.NONE;
    }

    private void submitMessage() {
        if (selectedFriend == null || messageText.isBlank()) return;
        BackendClient.sendFriendMessage(selectedFriend.toString(), messageText.trim());
        messageText = "";
    }

    private List<BackendState.Friend> filteredFriends() {
        String query = searchText.trim().toLowerCase(Locale.ROOT);
        if (query.isBlank()) return BackendState.friendsSnapshot();
        return BackendState.friendsSnapshot().stream()
                .filter(friend -> friend.name().toLowerCase(Locale.ROOT).contains(query) || friend.uuid().contains(query))
                .toList();
    }

    private void drawInput(DrawContext context, int x, int y, int w, int h, String value, String hint, boolean focused) {
        PremiumRender.card(context, x, y, w, h, 0, focused ? 0xFF171F2D : 0xFF101620, focused ? ThemeManager.theme().accentColor() : 0x55404A5A);
        String shown = value.isBlank() ? hint : value;
        int color = value.isBlank() ? 0xFF687083 : 0xFFFFFFFF;
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, shown, w - 12)), x + 6, y + (h - 8) / 2, color);
        if (focused && (System.currentTimeMillis() / 500L) % 2L == 0L) {
            int cursor = x + 7 + textRenderer.getWidth(TextLayout.ellipsize(textRenderer, value, w - 16));
            context.fill(cursor, y + 5, cursor + 1, y + h - 5, 0xFFFFFFFF);
        }
    }

    private void button(DrawContext context, int x, int y, int w, int h, String label, boolean hovered, int accent) {
        PremiumRender.card(context, x, y, w, h, 0, hovered ? 0xFF243149 : 0xFF151C29, hovered ? accent : 0x66404A5A);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + w / 2, y + (h - 8) / 2, hovered ? 0xFFFFFFFF : 0xFFBFC8D8);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    private int panelX() {
        int panelW = Math.min(650, Math.max(390, width * 56 / 100));
        return width - panelW;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static String removeLast(String value) {
        if (value == null || value.isEmpty()) return "";
        int end = value.offsetByCodePoints(value.length(), -1);
        return value.substring(0, end);
    }

    private static String lastSeen(long epochSeconds) {
        if (epochSeconds <= 0) return "Offline";
        long seconds = Math.max(0L, Instant.now().getEpochSecond() - epochSeconds);
        if (seconds < 60) return "Gerade offline";
        if (seconds < 3600) return "Vor " + (seconds / 60) + " Min.";
        if (seconds < 86400) return "Vor " + (seconds / 3600) + " Std.";
        return "Vor " + (seconds / 86400) + " Tagen";
    }

    private static UUID ownUuid() {
        try {
            return MinecraftClient.getInstance().getSession().getUuidOrNull();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private enum Focus { NONE, ADD, SEARCH, MESSAGE }
}
