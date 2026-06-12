package software.bernie.geckolib.event.item;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_1792;
import net.minecraft.class_4587;
import org.jetbrains.annotations.ApiStatus;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.model.BakedGeoModel;
import software.bernie.geckolib.event.GeoRenderEvent;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

/**
 * Pre-render event for items being rendered by {@link GeoItemRenderer}
 * <p>
 * This event is called before rendering, but after {@link GeoRenderer#preRenderPass}
 * <p>
 * This event is cancellable.<br>
 * If the event is cancelled, the entity will not be rendered.
 * <p>
 * <b><u>NOTE:</u></b> Some methods on this event are not overridden in this class. Check {@link GeoRenderEvent}
 *
 * @param <T> Item animatable class type
 * @see GeoRenderEvent
 * @see Pre
 */
public class GeoItemPreRenderEvent<T extends class_1792 & GeoAnimatable> implements GeoRenderEvent.Item.Pre<T> {
    public static final Event<Listener> EVENT = EventFactory.createArrayBacked(Listener.class, event -> true, listeners -> event -> {
        for (Listener<?> listener : listeners) {
            if (!listener.handle(event))
                return false;
        }

        return true;
    });
    private final RenderPassInfo<GeoRenderState> renderPassInfo;
    private final class_11659 renderTasks;

    public GeoItemPreRenderEvent(RenderPassInfo<GeoRenderState> renderPassInfo, class_11659 renderTasks) {
        this.renderPassInfo = renderPassInfo;
        this.renderTasks = renderTasks;
    }

    @Override
    public RenderPassInfo<GeoRenderState> getRenderPassInfo() {
        return this.renderPassInfo;
    }

    @Override
    public GeoItemRenderer<T> getRenderer() {
        return (GeoItemRenderer)this.renderPassInfo.renderer();
    }

    @ApiStatus.Internal
    @Override
    public GeoRenderState getRenderState() {
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
     * Event listener interface for the {@link class_1792.Pre} GeoRenderEvent
     * <p>
     * Return false to cancel the render pass
     *
     * @param <T> Item animatable class type
     */
    @FunctionalInterface
    public interface Listener<T extends net.minecraft.class_1792 & GeoAnimatable> {
        boolean handle(GeoItemPreRenderEvent<T> event);
    }
}
