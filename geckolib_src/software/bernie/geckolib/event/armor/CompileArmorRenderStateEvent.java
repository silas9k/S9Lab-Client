package software.bernie.geckolib.event.armor;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.class_10034;
import net.minecraft.class_1792;
import org.jetbrains.annotations.ApiStatus;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.event.GeoRenderEvent;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * Pre-render event for armor pieces being rendered by {@link GeoArmorRenderer}
 * <p>
 * This event is called in preparation for rendering, when the renderer is gathering data to pass through
 * <p>
 * Use this event to add data that you may need in a later {@link Armor} event, or to override/replace data used in rendering
 * <p>
 * <b><u>NOTE:</u></b> Some methods on this event are not overridden in this class. Check {@link GeoRenderEvent}
 *
 * @param <T> Item animatable class type
 * @param <R> RenderState class type - GeckoLib armor is based on Humanoid rendering and requires {@link class_10034} as a minimum
 * @see GeoRenderEvent
 * @see CompileRenderState
 */
public class CompileArmorRenderStateEvent<T extends class_1792 & GeoItem, R extends class_10034 & GeoRenderState> implements GeoRenderEvent.Armor.CompileRenderState<T, R> {
    public static final Event<Listener> EVENT = EventFactory.createArrayBacked(Listener.class, post -> {}, listeners -> event -> {
        for (Listener<?, ?> listener : listeners) {
            listener.handle(event);
        }
    });
    private final GeoArmorRenderer<T, R> renderer;
    private final R renderState;
    private final T animatable;
    private final GeoArmorRenderer.RenderData renderData;

    public CompileArmorRenderStateEvent(GeoArmorRenderer<T, R> renderer, R renderState, T animatable, GeoArmorRenderer.RenderData renderData) {
        this.renderer = renderer;
        this.renderState = renderState;
        this.animatable = animatable;
        this.renderData = renderData;
    }

    @Override
    public GeoArmorRenderer<T, R> getRenderer() {
        return this.renderer;
    }

    @Override
    public T getAnimatable() {
        return this.animatable;
    }

    @ApiStatus.Internal
    @Override
    public R getRenderState() {
        return this.renderState;
    }

    @Override
    public GeoArmorRenderer.RenderData getRenderData() {
        return this.renderData;
    }

    /**
     * Event listener interface for the {@link Armor.CompileRenderState} GeoRenderEvent
     *
     * @param <T> Item animatable class type
     * @param <R> RenderState class type - GeckoLib armor is based on Humanoid rendering and requires {@link class_10034} as a minimum
     */
    @FunctionalInterface
    public interface Listener<T extends net.minecraft.class_1792 & GeoItem, R extends class_10034 & GeoRenderState> {
        void handle(CompileArmorRenderStateEvent<T, R> event);
    }
}
