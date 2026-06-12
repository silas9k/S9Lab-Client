package software.bernie.geckolib.renderer.specialty;

import net.minecraft.class_10017;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1676;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_5617;
import net.minecraft.class_7833;
import net.minecraft.class_7923;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

/**
 * Specialty class for rendering directionally oriented projectiles.
 * <p>
 * Automatically handles transforms for the entity based on its current rotation.
 * <p>
 * <b><u>NOTE:</u></b> This renderer assumes your model is laying flat, pointing directly north
 */
public class DirectionalProjectileRenderer<T extends class_1676 & GeoAnimatable, R extends class_10017 & GeoRenderState> extends GeoEntityRenderer<T, R> {
    /**
     * Creates a new defaulted renderer instance, using the entity's registered id as the file name for its assets
     */
    public DirectionalProjectileRenderer(class_5617.class_5618 context, class_1299<? extends T> entityType) {
        this(context, new DefaultedEntityGeoModel<>(class_7923.field_41177.method_10221(entityType)));
    }

    public DirectionalProjectileRenderer(class_5617.class_5618 context, GeoModel<T> model) {
        super(context, model);
    }

    /**
     * Applies rotation transformations to the renderer prior to render time to account for various entity states
     */
    @Override
    protected void applyRotations(RenderPassInfo<R> renderPassInfo, class_4587 poseStack, float nativeScale) {
        poseStack.method_22907(class_7833.field_40716.rotationDegrees(renderPassInfo.getOrDefaultGeckolibData(DataTickets.ENTITY_YAW, 0f)));
        poseStack.method_22907(class_7833.field_40714.rotationDegrees(renderPassInfo.getOrDefaultGeckolibData(DataTickets.ENTITY_PITCH, 0f)));
    }

    /**
     * Calculate the yaw of the given animatable.
     * <p>
     * Normally only called for non-{@link class_1309 LivingEntities}, and shouldn't be considered a safe place to modify rotation<br>
     * Do that in {@link software.bernie.geckolib.renderer.base.GeoRendererInternals#addRenderData(GeoAnimatable, Object, GeoRenderState, float)} instead
     */
    @Override
    protected float calculateYRot(T animatable, float yHeadRot, float partialTick) {
        return class_3532.method_16439(partialTick, animatable.field_5982, animatable.method_36454()) + 180f;
    }
}
