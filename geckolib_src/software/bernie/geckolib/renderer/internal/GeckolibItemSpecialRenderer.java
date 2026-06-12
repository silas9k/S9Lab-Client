package software.bernie.geckolib.renderer.internal;

import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.function.Consumer;
import net.minecraft.class_10444;
import net.minecraft.class_10515;
import net.minecraft.class_11566;
import net.minecraft.class_11659;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_638;
import net.minecraft.class_811;

/**
 * SpecialModelRenderer instance to facilitate rendering GeckoLib items using the vanilla special renderer system
 *
 * @param <T> Item animatable class type
 */
@ApiStatus.Internal
public class GeckolibItemSpecialRenderer<T extends class_1792 & GeoAnimatable> implements class_10515<GeckolibItemSpecialRenderer.RenderData<T>> {
    @Override
    public void submit(GeckolibItemSpecialRenderer.@Nullable RenderData<T> renderData, class_811 itemDisplayContext, class_4587 poseStack, class_11659 renderTasks,
                       int packedLight, int packedOverlay, boolean hasGlint, int outlineColor) {
        if (renderData == null)
            return;

        renderData.renderState.addGeckolibData(DataTickets.HAS_GLINT, hasGlint);
        renderData.renderState.addGeckolibData(DataTickets.PACKED_OVERLAY, packedOverlay);
        renderData.renderState.addGeckolibData(DataTickets.PACKED_LIGHT, packedLight);

        renderData.renderer.submit(renderData.renderState, poseStack, renderTasks, outlineColor);
    }

    @Override
    public void method_72175(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(0, 0, 0));
    }

    /**
     * Wrap the {@link #method_65695(class_1799)} call to provide all the context available, rather than just what Mojang provides
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public GeckolibItemSpecialRenderer.@Nullable RenderData<T> extractArgument(class_1799 itemStack, class_10444 renderState, class_811 context,
                                                                     @Nullable class_638 level, @Nullable class_11566 itemOwner) {
        T item = makeCovariantItem(itemStack.method_7909());

        if (item == null)
            return null;

        GeoItemRenderer<T> renderer = (GeoItemRenderer)GeoRenderProvider.of(item).getGeoItemRenderer();

        if (renderer == null)
            return null;

        return new RenderData<>(item, buildRenderState(item, itemStack, renderer, renderState, context, level, itemOwner), renderer);
    }

    /**
     * Should not be used
     */
    @Deprecated
    @Override
    public GeckolibItemSpecialRenderer.@Nullable RenderData<T> method_65695(class_1799 itemStack) {
        return extractArgument(itemStack, new class_10444(), class_811.field_4319, null, null);
    }

    @SuppressWarnings("unchecked")
    private @Nullable T makeCovariantItem(class_1792 item) {
        return item instanceof GeoAnimatable ? (T)item : null;
    }

    private GeoRenderState buildRenderState(T animatable, class_1799 itemStack, GeoItemRenderer<T> renderer, class_10444 renderState, class_811 context,
                                            @Nullable class_638 level, @Nullable class_11566 itemOwner) {
        final GeoItemRenderer.RenderData renderData = new GeoItemRenderer.RenderData(itemStack, renderState, context, level, itemOwner);

        return renderer.fillRenderState(animatable, renderData, renderer.createRenderState(animatable, renderData),
                                        class_310.method_1551().method_61966().method_60637(true));
    }

    public static class Unbaked implements class_10515.class_10516 {
        public static final MapCodec<GeckolibItemSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public @Nullable class_10515<?> method_65698(class_11695 context) {
            return new GeckolibItemSpecialRenderer<>();
        }

        @Override
        public MapCodec<? extends class_10515.class_10516> method_65696() {
            return MAP_CODEC;
        }
    }

    public record RenderData<T extends class_1792 & GeoAnimatable>(T item, GeoRenderState renderState, GeoItemRenderer<T> renderer) {}
}