package software.bernie.geckolib.event.armor;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.class_10034;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_1792;
import net.minecraft.class_4587;
import org.jetbrains.annotations.ApiStatus;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.model.BakedGeoModel;
import software.bernie.geckolib.event.GeoRenderEvent;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

/**
 * Pre-render event for armor pieces being rendered by {@link GeoArmorRenderer}
 * <p>
 * This event is called before rendering, but after {@link GeoRenderer#preRenderPass}
 * <p>
 * This event is cancellable.<br>
 * If the event is cancelled, the armor piece will not be rendered.
 * <p>
 * <b><u>NOTE:</u></b> Some methods on this event are not overridden in this class. Check {@link GeoRenderEvent}
 *
 * @param <T> Item animatable class type
 * @param <R> RenderState class type - GeckoLib armor is based on Humanoid rendering and requires {@link class_10034} as a minimum
 * @see GeoRenderEvent
 * @see Pre
 */
public class GeoArmorPreRenderEvent<T extends class_1792 & GeoItem, R extends class_10034 & GeoRenderState> implements GeoRenderEvent.Armor.Pre<T, R> {
    public static final Event<Listener> EVENT = EventFactory.createArrayBacked(Listener.class, event -> true, listeners -> event -> {
        for (Listener<?, ?> listener : listeners) {
            if (!listener.handle(event))
                return false;
        }

        return true;
    });
    private final RenderPassInfo<R> renderPassInfo;
    private final class_11659 renderTasks;

    public GeoArmorPreRenderEvent(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks) {
        this.renderPassInfo = renderPassInfo;
        this.renderTasks = renderTasks;
    }

    @Override
    public RenderPassInfo<R> getRenderPassInfo() {
        return this.renderPassInfo;
    }

    @Override
    public GeoArmorRenderer<T, R> getRenderer() {
        return (GeoArmorRenderer)this.renderPassInfo.renderer();
    }

    @ApiStatus.Internal
    @Override
    public R getRenderState() {
        return this.renderPassInfo.renderState();
    }

    @Override
    public class_4587 getPoseStack() {
        return this.renderPassInfo.poseStack();
    }

    @Override
    public BakedGeoModel getModel() {
        return this.renderPassInfo.model();
    }

    @Override
    public class_11659 getRenderTasks() {
        return this.renderTasks;
    }

    @Override
    public class_12075 getCameraState() {
        return this.renderPassInfo.cameraState();
    }

    /**
     * Event listener interface for the {@link Armor.Pre} GeoRenderEvent
     * <p>
     * Return false to cancel the render pass
     *
     * @param <T> Item animatable class type
     * @param <R> RenderState class type - GeckoLib armor is based on Humanoid rendering and requires {@link class_10034} as a minimum
     */
    @FunctionalInterface
    public interface Listener<T extends net.minecraft.class_1792 & GeoItem, R extends class_10034 & GeoRenderState> {
        boolean handle(GeoArmorPreRenderEvent<T, R> event);
    }
}
