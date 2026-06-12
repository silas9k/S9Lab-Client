package software.bernie.geckolib.event.object;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_4587;
import org.jetbrains.annotations.ApiStatus;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.model.BakedGeoModel;
import software.bernie.geckolib.event.GeoRenderEvent;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

/**
 * Pre-render event for miscellaneous animatables being rendered by {@link GeoObjectRenderer}
 * <p>
 * This event is called before rendering, but after {@link GeoRenderer#preRenderPass}
 * <p>
 * This event is cancellable.<br>
 * If the event is cancelled, the entity will not be rendered.
 * <p>
 * <b><u>NOTE:</u></b> Some methods on this event are not overridden in this class. Check {@link GeoRenderEvent}
 *
 * @param <T> Object animatable class type
 * @param <E> Associated object class type, or {@link Void} if none
 * @param <R> RenderState class type
 * @see GeoRenderEvent
 * @see Pre
 */
public class GeoObjectPreRenderEvent<T extends GeoAnimatable, E, R extends GeoRenderState> implements GeoRenderEvent.Object.Pre<T, E, R> {
    public static final Event<Listener> EVENT = EventFactory.createArrayBacked(Listener.class, event -> true, listeners -> event -> {
        for (Listener<?, ?, ?> listener : listeners) {
            if (!listener.handle(event))
                return false;
        }

        return true;
    });
    private final RenderPassInfo<R> renderPassInfo;
    private final class_11659 renderTasks;

    public GeoObjectPreRenderEvent(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks) {
        this.renderPassInfo = renderPassInfo;
        this.renderTasks = renderTasks;
    }

    @Override
    public RenderPassInfo<R> getRenderPassInfo() {
        return this.renderPassInfo;
    }

    @Override
    public GeoObjectRenderer<T, E, R> getRenderer() {
        return (GeoObjectRenderer)this.renderPassInfo.renderer();
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
     * Event listener interface for the {@link java.lang.Object.Pre} GeoRenderEvent
     * <p>
     * Return false to cancel the render pass
     *
     * @param <T> Object animatable class type
     * @param <E> Associated object class type, or {@link Void} if none
     * @param <R> RenderState class type
     */
    @FunctionalInterface
    public interface Listener<T extends GeoAnimatable, E, R extends GeoRenderState> {
        boolean handle(GeoObjectPreRenderEvent<T, E, R> event);
    }
}
