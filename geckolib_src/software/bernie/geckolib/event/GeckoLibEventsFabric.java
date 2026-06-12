package software.bernie.geckolib.event;

import net.minecraft.class_10017;
import net.minecraft.class_10034;
import net.minecraft.class_11659;
import net.minecraft.class_11954;
import net.minecraft.class_1297;
import net.minecraft.class_1792;
import net.minecraft.class_2586;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.event.armor.CompileArmorRenderLayersEvent;
import software.bernie.geckolib.event.armor.CompileArmorRenderStateEvent;
import software.bernie.geckolib.event.armor.GeoArmorPreRenderEvent;
import software.bernie.geckolib.event.block.CompileBlockRenderLayersEvent;
import software.bernie.geckolib.event.block.CompileBlockRenderStateEvent;
import software.bernie.geckolib.event.block.GeoBlockPreRenderEvent;
import software.bernie.geckolib.event.entity.CompileEntityRenderLayersEvent;
import software.bernie.geckolib.event.entity.CompileEntityRenderStateEvent;
import software.bernie.geckolib.event.entity.GeoEntityPreRenderEvent;
import software.bernie.geckolib.event.item.CompileItemRenderLayersEvent;
import software.bernie.geckolib.event.item.CompileItemRenderStateEvent;
import software.bernie.geckolib.event.item.GeoItemPreRenderEvent;
import software.bernie.geckolib.event.object.CompileObjectRenderLayersEvent;
import software.bernie.geckolib.event.object.CompileObjectRenderStateEvent;
import software.bernie.geckolib.event.object.GeoObjectPreRenderEvent;
import software.bernie.geckolib.event.replacedentity.CompileReplacedEntityRenderLayersEvent;
import software.bernie.geckolib.event.replacedentity.CompileReplacedEntityRenderStateEvent;
import software.bernie.geckolib.event.replacedentity.GeoReplacedEntityPreRenderEvent;
import software.bernie.geckolib.renderer.*;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.service.GeckoLibEvents;

/**
 * Fabric service implementation for GeckoLib's various events
 */
@SuppressWarnings("unchecked")
public class GeckoLibEventsFabric implements GeckoLibEvents {
    /**
     * Fire the {@link GeoRenderEvent.Block.CompileRenderLayers} event
     */
    @Override
    public <T extends class_2586 & GeoAnimatable, R extends class_11954 & GeoRenderState>
    void fireCompileBlockRenderLayers(GeoBlockRenderer<T, R> renderer) {
        CompileBlockRenderLayersEvent.EVENT.invoker().handle(new CompileBlockRenderLayersEvent<>(renderer));
    }

    /**
     * Fire the {@link GeoRenderEvent.Block.CompileRenderState} event
     */
    @Override
    public <T extends class_2586 & GeoAnimatable, R extends class_11954 & GeoRenderState>
    void fireCompileBlockRenderState(GeoBlockRenderer<T, R> renderer, R renderState, T animatable) {
        CompileBlockRenderStateEvent.EVENT.invoker().handle(new CompileBlockRenderStateEvent<>(renderer, renderState, animatable));
    }

    @Override
    public <T extends class_2586 & GeoAnimatable, R extends class_11954 & GeoRenderState>
    boolean fireBlockPreRender(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks) {
        return GeoBlockPreRenderEvent.EVENT.invoker().handle(new GeoBlockPreRenderEvent<>(renderPassInfo, renderTasks));
    }

    /**
     * Fire the {@link GeoRenderEvent.Armor.CompileRenderLayers} event
     */
    @Override
    public <T extends class_1792 & GeoItem, R extends class_10034 & GeoRenderState>
    void fireCompileArmorRenderLayers(GeoArmorRenderer<T, R> renderer) {
        CompileArmorRenderLayersEvent.EVENT.invoker().handle(new CompileArmorRenderLayersEvent<>(renderer));
    }

    /**
     * Fire the {@link GeoRenderEvent.Armor.Pre} event, returning true if the event was not cancelled
     */
    @Override
    public <T extends class_1792 & GeoItem, O extends GeoArmorRenderer.RenderData, R extends class_10034 & GeoRenderState>
    void fireCompileArmorRenderState(GeoArmorRenderer<T, R> renderer, R renderState, T animatable, O renderData) {
        CompileArmorRenderStateEvent.EVENT.invoker().handle(new CompileArmorRenderStateEvent<>(renderer, renderState, animatable, renderData));
    }

    /**
     * Fire the {@link GeoRenderEvent.Armor.Pre} event, returning true if the event was not cancelled
     */
    @Override
    public <T extends class_1792 & GeoItem, R extends class_10034 & GeoRenderState>
    boolean fireArmorPreRender(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks) {
        return GeoArmorPreRenderEvent.EVENT.invoker().handle(new GeoArmorPreRenderEvent<>(renderPassInfo, renderTasks));
    }

    /**
     * Fire the {@link GeoRenderEvent.Entity.CompileRenderLayers} event
     */
    @Override
    public <T extends class_1297 & GeoAnimatable, R extends class_10017 & GeoRenderState>
    void fireCompileEntityRenderLayers(GeoEntityRenderer<T, R> renderer) {
        CompileEntityRenderLayersEvent.EVENT.invoker().handle(new CompileEntityRenderLayersEvent<>(renderer));
    }

    /**
     * Fire the {@link GeoRenderEvent.Entity.CompileRenderState} event
     */
    @Override
    public <T extends class_1297 & GeoAnimatable, R extends class_10017 & GeoRenderState>
    void fireCompileEntityRenderState(GeoEntityRenderer<T, R> renderer, R renderState, T animatable) {
        CompileEntityRenderStateEvent.EVENT.invoker().handle(new CompileEntityRenderStateEvent<>(renderer, renderState, animatable));
    }

    /**
     * Fire the {@link GeoRenderEvent.Entity.Pre} event, returning true if the event was not cancelled
     */
    @Override
    public <T extends class_1297 & GeoAnimatable, R extends class_10017 & GeoRenderState>
    boolean fireEntityPreRender(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks) {
        return GeoEntityPreRenderEvent.EVENT.invoker().handle(new GeoEntityPreRenderEvent<>(renderPassInfo, renderTasks));
    }

    /**
     * Fire the {@link GeoRenderEvent.ReplacedEntity.CompileRenderLayers} event
     */
    @Override
    public <T extends GeoAnimatable, E extends class_1297, R extends class_10017 & GeoRenderState>
    void fireCompileReplacedEntityRenderLayers(GeoReplacedEntityRenderer<T, E, R> renderer) {
        CompileReplacedEntityRenderLayersEvent.EVENT.invoker().handle(new CompileReplacedEntityRenderLayersEvent<>(renderer));
    }

    /**
     * Fire the {@link GeoRenderEvent.ReplacedEntity.CompileRenderState} event
     */
    @Override
    public <T extends GeoAnimatable, E extends class_1297, R extends class_10017 & GeoRenderState>
    void fireCompileReplacedEntityRenderState(GeoReplacedEntityRenderer<T, E, R> renderer, R renderState, T animatable, E entity) {
        CompileReplacedEntityRenderStateEvent.EVENT.invoker().handle(new CompileReplacedEntityRenderStateEvent<>(renderer, renderState, animatable, entity));
    }

    /**
     * Fire the {@link GeoRenderEvent.ReplacedEntity.Pre} event, returning true if the event was not cancelled
     */
    @Override
    public <T extends GeoAnimatable, E extends class_1297, R extends class_10017 & GeoRenderState>
    boolean fireReplacedEntityPreRender(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks) {
        return GeoReplacedEntityPreRenderEvent.EVENT.invoker().handle(new GeoReplacedEntityPreRenderEvent<>(renderPassInfo, renderTasks));
    }

    /**
     * Fire the {@link GeoRenderEvent.Item.CompileRenderLayers} event
     */
    @Override
    public <T extends class_1792 & GeoAnimatable>
    void fireCompileItemRenderLayers(GeoItemRenderer<T> renderer) {
        CompileItemRenderLayersEvent.EVENT.invoker().handle(new CompileItemRenderLayersEvent<>(renderer));
    }

    /**
     * Fire the {@link GeoRenderEvent.Item.CompileRenderState} event
     */
    @Override
    public <T extends class_1792 & GeoAnimatable, O extends GeoItemRenderer.RenderData, R extends GeoRenderState>
    void fireCompileItemRenderState(GeoItemRenderer<T> renderer, R renderState, T animatable, O renderData) {
        CompileItemRenderStateEvent.EVENT.invoker().handle(new CompileItemRenderStateEvent<>(renderer, renderState, animatable, renderData));
    }

    /**
     * Fire the {@link GeoRenderEvent.Item.Pre} event, returning true if the event was not cancelled
     */
    @Override
    public <T extends class_1792 & GeoAnimatable>
    boolean fireItemPreRender(RenderPassInfo<GeoRenderState> renderPassInfo, class_11659 renderTasks) {
        return GeoItemPreRenderEvent.EVENT.invoker().handle(new GeoItemPreRenderEvent<>(renderPassInfo, renderTasks));
    }

    /**
     * Fire the {@link GeoRenderEvent.Object.CompileRenderLayers} event
     */
    @Override
    public <T extends GeoAnimatable, E, R extends GeoRenderState>
    void fireCompileObjectRenderLayers(GeoObjectRenderer<T, E, R> renderer) {
        CompileObjectRenderLayersEvent.EVENT.invoker().handle(new CompileObjectRenderLayersEvent<>(renderer));
    }

    /**
     * Fire the {@link GeoRenderEvent.Object.CompileRenderState} event
     */
    @Override
    public <T extends GeoAnimatable, E, R extends GeoRenderState>
    void fireCompileObjectRenderState(GeoObjectRenderer<T, E, R> renderer, R renderState, T animatable, @Nullable E relatedObject) {
        CompileObjectRenderStateEvent.EVENT.invoker().handle(new CompileObjectRenderStateEvent<>(renderer, renderState, animatable, relatedObject));
    }

    /**
     * Fire the {@link GeoRenderEvent.Object.Pre} event, returning true if the event was not cancelled
     */
    @Override
    public <T extends GeoAnimatable, E, R extends GeoRenderState>
    boolean fireObjectPreRender(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks) {
        return GeoObjectPreRenderEvent.EVENT.invoker().handle(new GeoObjectPreRenderEvent<>(renderPassInfo, renderTasks));
    }
}
