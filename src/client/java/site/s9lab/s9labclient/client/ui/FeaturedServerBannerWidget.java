package site.s9lab.s9labclient.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import site.s9lab.s9labclient.client.backend.BackendState;
import site.s9lab.s9labclient.client.ui.premium.PremiumRender;

public final class FeaturedServerBannerWidget extends ClickableWidget {
    private final MultiplayerScreen screen;

    public FeaturedServerBannerWidget(MultiplayerScreen screen, int x, int y, int width, int height) {
        super(x, y, width, height, Text.literal("Featured Server"));
        this.screen = screen;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        BackendState.FeaturedServer server = BackendState.featuredServer();
        if (server == null) {
            visible = false;
            return;
        }
        visible = true;
        int accent = accent(server.accentColor());
        int background = hovered ? 0xF21D222C : 0xF0151820;
        PremiumRender.card(context, getX(), getY(), width, height, 0, background, hovered ? accent : 0x887A8496);
        context.fill(getX(), getY(), getX() + 4, getY() + height, accent);

        int imageW = Math.min(width / 3, 150);
        Identifier texture = FeaturedServerTextureCache.texture(server.imageUrl());
        if (texture != null && imageW >= 48) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, getX() + 4, getY() + 1,
                    0, 0, imageW, height - 2, imageW, height - 2, imageW, height - 2);
        } else {
            context.fill(getX() + 4, getY() + 1, getX() + 4 + imageW, getY() + height - 1, 0xFF202A3B);
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal("S9"),
                    getX() + 4 + imageW / 2, getY() + height / 2 - 4, accent);
        }

        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int textX = getX() + imageW + 16;
        int joinW = 76;
        int textW = Math.max(40, width - imageW - joinW - 32);
        context.drawTextWithShadow(textRenderer, Text.literal("FEATURED SERVER"), textX, getY() + 8, accent);
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, server.title(), textW)), textX, getY() + 23, 0xFFFFFFFF);
        String detail = server.subtitle() == null || server.subtitle().isBlank() ? server.address() : server.subtitle() + "  •  " + server.address();
        context.drawTextWithShadow(textRenderer, Text.literal(TextLayout.ellipsize(textRenderer, detail, textW)), textX, getY() + 38, 0xFF9AA1B2);

        int joinX = getX() + width - joinW - 10;
        int joinY = getY() + (height - 26) / 2;
        context.fill(joinX, joinY, joinX + joinW, joinY + 26, hovered ? accent : 0xFF26344F);
        PremiumRender.outline(context, joinX, joinY, joinW, 26, 0, accent);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("JOIN"), joinX + joinW / 2, joinY + 9, 0xFFFFFFFF);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        BackendState.FeaturedServer server = BackendState.featuredServer();
        if (server == null) return;
        screen.connect(new ServerInfo(server.title(), server.address(), ServerInfo.ServerType.OTHER));
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }

    private static int accent(String value) {
        try {
            return 0xFF000000 | Integer.parseInt(value == null ? "4F7DFF" : value.replace("#", ""), 16);
        } catch (RuntimeException ignored) {
            return 0xFF4F7DFF;
        }
    }
}
