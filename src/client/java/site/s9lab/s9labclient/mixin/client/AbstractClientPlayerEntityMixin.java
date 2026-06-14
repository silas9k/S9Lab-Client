package site.s9lab.s9labclient.mixin.client;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import site.s9lab.s9labclient.client.S9LabClientClient;
import site.s9lab.s9labclient.client.cosmetics.Cosmetic;
import site.s9lab.s9labclient.client.cosmetics.CosmeticResolver;
import site.s9lab.s9labclient.client.cosmetics.CosmeticPreviewContext;
import site.s9lab.s9labclient.client.cosmetics.CosmeticType;
import site.s9lab.s9labclient.client.module.Module;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void s9labclient$applyCape(CallbackInfoReturnable<SkinTextures> cir) {
        boolean preview = CosmeticPreviewContext.active();
        if (!preview && S9LabClientClient.getModuleManager() == null) {
            return;
        }

        Module capeModule = S9LabClientClient.getModuleManager() == null
                ? null
                : S9LabClientClient.getModuleManager().getModule("Cape").orElse(null);
        if (!preview && (capeModule == null || !capeModule.isEnabled())) {
            return;
        }

        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        Identifier selectedCape = CosmeticResolver.equippedForPlayer(player.getUuid(), CosmeticType.CAPE)
                .map(Cosmetic::texture)
                .orElse(null);
        if (selectedCape == null) {
            return;
        }

        AssetInfo.TextureAsset capeAsset = new AssetInfo.TextureAsset() {
            @Override
            public Identifier id() {
                return selectedCape;
            }

            @Override
            public Identifier texturePath() {
                return selectedCape;
            }
        };

        SkinTextures original = cir.getReturnValue();
        cir.setReturnValue(new SkinTextures(original.body(), capeAsset, original.elytra(), original.model(), false));
    }
}
