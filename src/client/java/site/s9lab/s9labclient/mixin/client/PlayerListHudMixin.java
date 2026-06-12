package site.s9lab.s9labclient.mixin.client;

import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.backend.BackendState;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.util.S9BadgeText;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void s9labclient$addOwnBadge(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        if (S9LabClientClient.getModuleManager() == null || !isBadgeEnabled()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getSession() == null) {
            return;
        }

        UUID entryUuid = entry.getProfile().id();
        UUID ownUuid = client.getSession().getUuidOrNull();
        if ((ownUuid == null || !ownUuid.equals(entryUuid)) && !BackendState.isS9Player(entryUuid)) {
            return;
        }

        Text originalName = cir.getReturnValue();
        cir.setReturnValue(S9BadgeText.withBadge(originalName));
    }

    private static boolean isBadgeEnabled() {
        Module module = S9LabClientClient.getModuleManager().getModule("Tablist Badge").orElse(null);
        return module != null && module.isEnabled();
    }
}
