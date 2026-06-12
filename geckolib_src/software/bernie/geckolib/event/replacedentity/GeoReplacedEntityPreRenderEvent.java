package software.bernie.geckolib.event.replacedentity;

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
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

/**
 * Pre-render event for replaced entities being rendered by {@link GeoReplacedEntityRenderer}
 * <p>
 * This event is called before rendering, but after {@link GeoRenderer#preRenderPass}
 * <p>
 * This event is cancellable.<br>
 * If the event is cancelled, the entity will not be rendered.
 * <p>
 * <b><u>NOTE:</u></b> Some methods on this event are not overridden in this class. Check {@link GeoRenderEvent}
 *
 * @param <T> Entity animatable class type. This is the animatable being rendered
 * @param <E> Entity class type. This is the entity being replaced
 * @param <R> RenderState class type. Typically, this would match the RenderState class the replaced entity uses in their renderer
 * @see GeoRenderEvent
 * @see Pre
 */
public class GeoReplacedEntityPreRenderEvent<T extends GeoAnimatable, E extends class_1297, R extends class_10017 & GeoRenderState> implements GeoRenderEvent.ReplacedEntity.Pre<T, E, R> {
    public static final Event<Listener> EVENT = EventFactory.createArrayBacked(Listener.class, event -> true, listeners -> event -> {
        for (Listener<?, ?, ?> listener : listeners) {
            if (!listener.handle(event))
                return false;
        }

        return true;
    });
    private final RenderPassInfo<R> renderPassInfo;
    private final class_11659 renderTasks;

    public GeoReplacedEntityPreRenderEvent(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks) {
        this.renderPassInfo = renderPassInfo;
        this.renderTasks = renderTasks;
    }

    @Override
    public RenderPassInfo<R> getRenderPassInfo() {
        return this.renderPassInfo;
    }

    @Override
    public GeoReplacedEntityRenderer<T, E, R> getRenderer() {
        return (GeoReplacedEntityRenderer)this.renderPassInfo.renderer();
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
     * Event listener interface for the {@link ReplacedEntity.Pre} GeoRenderEvent
     * <p>
     * Return false to cancel the render pass
     *
     * @param <T> Entity animatable class type. This is the animatable being rendered
     * @param <E> Entity class type. This is the entity being replaced
     * @param <R> RenderState class type. Typically, this would match the RenderState class the replaced entity uses in their renderer
     */
    @FunctionalInterface
    public interface Listener<T extends GeoAnimatable, E extends net.minecraft.class_1297, R extends class_10017 & GeoRenderState> {
        boolean handle(GeoReplacedEntityPreRenderEvent<T, E, R> event);
    }
}
