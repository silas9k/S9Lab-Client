package software.bernie.geckolib.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.class_10034;
import net.minecraft.class_11659;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_572;
import net.minecraft.class_970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

@Mixin(class_970.class)
public abstract class HumanoidArmorLayerMixin<S extends class_10034, M extends class_572<S>, A extends class_572<S>> extends class_3887<S, M> {
    public HumanoidArmorLayerMixin(class_3883<S, M> renderer) {
        super(renderer);
    }

    /**
     * Injection into the render point for armor on HumanoidModels (Players, Zombies, etc.) to defer to GeckoLib item-armor rendering as applicable
     * <p>
     * Does nothing if GeckoLib has nothing to handle for the given arguments
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @WrapWithCondition(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V"))
    public boolean geckolib$wrapArmorPieceRender(class_970<S, M, A> layer, class_4587 poseStack, class_11659 renderTasks, class_1799 stack, class_1304 slot, int packedLight, S entityRenderState) {
        return entityRenderState instanceof class_10034 && !GeoArmorRenderer.tryRenderGeoArmorPiece(
                (renderState, equipmentSlot) -> (class_572)layer.method_4172((S)renderState, equipmentSlot),
                poseStack, renderTasks, stack, slot, packedLight, (class_10034 & GeoRenderState)entityRenderState);
    }
}