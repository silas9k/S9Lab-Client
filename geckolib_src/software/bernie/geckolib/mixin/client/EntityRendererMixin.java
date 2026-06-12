package software.bernie.geckolib.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.class_10017;
import net.minecraft.class_10034;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_897;
import net.minecraft.class_922;
import net.minecraft.class_970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

@Mixin(value = class_897.class, priority = 5000)
public class EntityRendererMixin<T extends class_1297, S extends class_10017> {
    /**
     * Override the maximum distance a GeckoLib entity's nameplate can render at, since vanilla caps it at 64 blocks
     */
    @ModifyConstant(method = "extractRenderState", constant = @Constant(doubleValue = 4096.0F), require = 0)
    public double modifyMaxNameplateDistance(double constant) {
        return (Object)this instanceof GeoEntityRenderer<?, ?> ? 256 * 256 : constant;
    }

    /**
     * Injection mixin to allow for capture of data for {@link GeoRenderState}s for {@link GeoArmorRenderer}s,
     * given that they never normally receive the entity context
     */
    @SuppressWarnings({"ConstantValue", "rawtypes", "unchecked"})
    @WrapMethod(method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;")
    public S geckolib$captureDataForArmorLayer(T entity, float partialTick, Operation<S> original) {
        S renderState = original.call(entity, partialTick);

        if (renderState instanceof class_10034 && (Object)this instanceof class_922 livingRenderer) {
            for (Object layer : livingRenderer.field_4738) {
                if (layer instanceof class_970 armorLayer) {
                    GeoArmorRenderer.captureRenderStates(geckolib$castRenderState(renderState), (class_1309)entity, partialTick,
                                                         (renderState2, slot) -> armorLayer.method_4172(renderState2, slot),
                                                         slot -> geckolib$castRenderState(slot == class_1304.field_6169 ? renderState : original.call(entity, partialTick)));

                    break;
                }
            }
        }

        return renderState;
    }

    /**
     * Sugar method for blind-casting RenderStates to GeckoLib-supported generic types
     */
    @SuppressWarnings("unchecked")
    @Unique
    private static <R extends class_10034 & GeoRenderState> R geckolib$castRenderState(class_10017 renderState) {
        return (R)renderState;
    }
}
