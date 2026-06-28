package site.s9lab.s9labclient.mixin.client;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import site.s9lab.s9labclient.client.backend.BackendClient;
import site.s9lab.s9labclient.client.server.ServerFavoritesManager;
import site.s9lab.s9labclient.client.notification.S9ToastManager;
import site.s9lab.s9labclient.client.ui.FeaturedServerTextureCache;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public abstract class MultiplayerServerEntryMixin {
    @Shadow @Final private ServerInfo server;
    @Shadow @Final private MultiplayerScreen screen;

    @Inject(method = "render", at = @At("HEAD"))
    private void s9labclient$renderServerBanner(DrawContext context, int index, int y, boolean hovered,
                                                 float tickDelta, CallbackInfo ci) {
        String url = BackendClient.serverBannerAssetUrl(server.address);
        Identifier texture = FeaturedServerTextureCache.texture(url);
        if (texture == null) return;

        MultiplayerServerListWidget.ServerEntry entry = (MultiplayerServerListWidget.ServerEntry) (Object) this;
        int x = entry.getContentX();
        int width = entry.getContentWidth();
        int height = entry.getContentHeight();
        int rowY = entry.getY() + 2;
        height = Math.max(1, height - 4);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, rowY, 0.0F, 0.0F,
                width, height, width, height, width, height);
        context.fill(x, rowY, x + width, rowY + height, hovered ? 0x55000000 : 0x77000000);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void s9labclient$renderFavorite(DrawContext context, int mouseX, int mouseY, boolean hovered,
                                             float tickDelta, CallbackInfo ci) {
        MultiplayerServerListWidget.ServerEntry entry = (MultiplayerServerListWidget.ServerEntry) (Object) this;
        boolean favorite = ServerFavoritesManager.isFavorite(server.address);
        int color = favorite ? 0xFFFFCC33 : hovered ? 0xFFB8C1D8 : 0xFF6F7788;
        context.drawTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,
                Text.literal(favorite ? "★" : "☆"), entry.getContentRightEnd() - 10,
                entry.getY() + entry.getContentHeight() - 11, color);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void s9labclient$handleServerActions(Click click, boolean doubled,
                                                  CallbackInfoReturnable<Boolean> cir) {
        MultiplayerServerListWidget.ServerEntry entry = (MultiplayerServerListWidget.ServerEntry) (Object) this;
        int starX = entry.getContentRightEnd() - 14;
        int starY = entry.getY() + entry.getContentHeight() - 14;
        if (click.button() == 0 && click.x() >= starX && click.x() < starX + 14
                && click.y() >= starY && click.y() < starY + 14) {
            boolean favorite = ServerFavoritesManager.toggle(server.address);
            ServerFavoritesManager.sort(screen.getServerList());
            screen.getServerList().saveFile();
            ((MultiplayerScreenAccessor) screen).s9labclient$getServerListWidget().setServers(screen.getServerList());
            S9ToastManager.success(favorite ? "Server favorited" : "Favorite removed", server.name);
            cir.setReturnValue(true);
            return;
        }
        if (click.button() == 1) {
            net.minecraft.client.MinecraftClient.getInstance().keyboard.setClipboard(server.address);
            S9ToastManager.success("Server address copied", server.address);
            cir.setReturnValue(true);
        }
    }
}
