package site.s9lab.s9labclient.mixin.client;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.option.ServerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import site.s9lab.s9labclient.client.server.ServerFavoritesManager;

@Mixin(MultiplayerServerListWidget.class)
public abstract class MultiplayerServerListWidgetMixin {
    @Inject(method = "setServers", at = @At("HEAD"))
    private void s9labclient$sortFavorites(ServerList servers, CallbackInfo ci) {
        ServerFavoritesManager.sort(servers);
    }
}
