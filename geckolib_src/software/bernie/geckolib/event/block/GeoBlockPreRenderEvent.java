package software.bernie.geckolib.event.block;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.class_11659;
import net.minecraft.class_11954;
import net.minecraft.class_12075;
import net.minecraft.class_2586;
import net.minecraft.class_4587;
import org.jetbrains.annotations.ApiStatus;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.model.BakedGeoModel;
import software.bernie.geckolib.event.GeoRenderEvent;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

/**
 * Pre-render event for block entities being rendered by {@link GeoBlockRenderer}
 * <p>
 * This event is called before rendering, but after {@link GeoRenderer#preRenderPass}
 * <p>
 * This event is cancellable.<br>
 * If the event is cancelled, the block entity will not be rendered.
 * <p>
 * <b><u>NOTE:</u></b> Some methods on this event are not overridden in this class. Check {@link GeoRenderEvent}
 *
 * @param <T> BlockEntity animatable class type
 * @param <R> RenderState class type
 * @see GeoRenderEvent
 * @see Pre
 */
public class GeoBlockPreRenderEvent<T extends class_2586 & GeoAnimatable, R extends class_11954 & GeoRenderState> implements GeoRenderEvent.Block.Pre<T, R> {
    public static final Event<Listener> EVENT = EventFactory.createArrayBacked(Listener.class, event -> true, listeners -> event -> {
        for (Listener<?, ?> listener : listeners) {
            if (!listener.handle(event))
                return false;
        }

        return true;
    });
    private final RenderPassInfo<R> renderPassInfo;
    private final class_11659 renderTasks;

    public GeoBlockPreRenderEvent(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks) {
        this.renderPassInfo = renderPassInfo;
        this.renderTasks = renderTasks;
    }

    @Override
    public RenderPassInfo<R> getRenderPassInfo() {
        return this.renderPassInfo;
    }

    @Override
    public GeoBlockRenderer<T, R> getRenderer() {
        return (GeoBlockRenderer)this.renderPassInfo.renderer();
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
     * Event listener interface for the {@link Block.Pre} GeoRenderEvent
     * <p>
     * Return false to cancel the render pass
     *
     * @param <T> BlockEntity animatable class type
     * @param <R> RenderState class type
     */
    @FunctionalInterface
    public interface Listener<T extends class_2586 & GeoAnimatable, R extends class_11954 & GeoRenderState> {
        boolean handle(GeoBlockPreRenderEvent<T, R> event);
    }
}
