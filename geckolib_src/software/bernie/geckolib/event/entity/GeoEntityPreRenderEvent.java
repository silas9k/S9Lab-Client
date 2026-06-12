package software.bernie.geckolib.event.entity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.class_10017;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_1297;
import net.minecraft.class_4587;
import org.jetbrains.annotations.ApiStatus;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.model.BakedGeoModel;
import software.bernie.geckolib.event.GeoRenderEvent;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

/**
 * Pre-render event for entities being rendered by {@link GeoEntityRenderer}
 * <p>
 * This event is called before rendering, but after {@link GeoRenderer#preRenderPass}
 * <p>
 * This event is cancellable.<br>
 * If the event is cancelled, the entity will not be rendered.
 * <p>
 * <b><u>NOTE:</u></b> Some methods on this event are not overridden in this class. Check {@link GeoRenderEvent}
 *
 * @param <T> Entity animatable class type
 * @param <R> RenderState class type
 * @see GeoRenderEvent
 * @see Pre
 */
public class GeoEntityPreRenderEvent<T extends class_1297 & GeoAnimatable, R extends class_10017 & GeoRenderState> implements GeoRenderEvent.Entity.Pre<T, R> {
    public static final Event<Listener> EVENT = EventFactory.createArrayBacked(Listener.class, event -> true, listeners -> event -> {
        for (Listener<?, ?> listener : listeners) {
            if (!listener.handle(event))
                return false;
        }

        return true;
    });
    private final RenderPassInfo<R> renderPassInfo;
    private final class_11659 renderTasks;

    public GeoEntityPreRenderEvent(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks) {
        this.renderPassInfo = renderPassInfo;
        this.renderTasks = renderTasks;
    }

    @Override
    public RenderPassInfo<R> getRenderPassInfo() {
        return this.renderPassInfo;
    }

    @Override
    public GeoEntityRenderer<T, R> getRenderer() {
        return (GeoEntityRenderer)this.renderPassInfo.renderer();
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
     * Event listener interface for the {@link class_1297.Pre} GeoRenderEvent
     * <p>
     * Return false to cancel the render pass
     *
     * @param <T> Entity animatable class type
     * @param <R> RenderState class type
     */
    @FunctionalInterface
    public interface Listener<T extends net.minecraft.class_1297 & GeoAnimatable, R extends class_10017 & GeoRenderState> {
        boolean handle(GeoEntityPreRenderEvent<T, R> event);
    }
}
