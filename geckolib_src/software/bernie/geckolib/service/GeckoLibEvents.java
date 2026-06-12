package software.bernie.geckolib.service;

import net.minecraft.class_10017;
import net.minecraft.class_10034;
import net.minecraft.class_11659;
import net.minecraft.class_11954;
import net.minecraft.class_1297;
import net.minecraft.class_1792;
import net.minecraft.class_2586;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.event.GeoRenderEvent;
import software.bernie.geckolib.renderer.*;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

/**
 * Loader-agnostic service interface for GeckoLib's various events
 */
@ApiStatus.Internal
public interface GeckoLibEvents {
    /**
     * Fire the {@link GeoRenderEvent.Block.CompileRenderLayers} event
     */
    <T extends class_2586 & GeoAnimatable, R extends class_11954 & GeoRenderState>
    void fireCompileBlockRenderLayers(GeoBlockRenderer<T, R> renderer);
    /**
     * Fire the {@link GeoRenderEvent.Block.CompileRenderState} event
     */
    <T extends class_2586 & GeoAnimatable, R extends class_11954 & GeoRenderState>
    void fireCompileBlockRenderState(GeoBlockRenderer<T, R> renderer, R renderState, T animatable);
    /**
     * Fire the {@link GeoRenderEvent.Block.Pre} event, returning true if the event was not cancelled
     */
    <T extends class_2586 & GeoAnimatable, R extends class_11954 & GeoRenderState>
    boolean fireBlockPreRender(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks);

    /**
     * Fire the {@link GeoRenderEvent.Armor.CompileRenderLayers} event
     */
    <T extends class_1792 & GeoItem, R extends class_10034 & GeoRenderState>
    void fireCompileArmorRenderLayers(GeoArmorRenderer<T, R> renderer);
    /**
     * Fire the {@link GeoRenderEvent.Armor.CompileRenderState} event
     */
    <T extends class_1792 & GeoItem, O extends GeoArmorRenderer.RenderData, R extends class_10034 & GeoRenderState>
    void fireCompileArmorRenderState(GeoArmorRenderer<T, R> renderer, R renderState, T animatable, O renderData);
    /**
     * Fire the {@link GeoRenderEvent.Armor.Pre} event, returning true if the event was not cancelled
     */
    <T extends class_1792 & GeoItem, R extends class_10034 & GeoRenderState>
    boolean fireArmorPreRender(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks);

    /**
     * Fire the {@link GeoRenderEvent.Entity.CompileRenderLayers} event
     */
    <T extends class_1297 & GeoAnimatable, R extends class_10017 & GeoRenderState>
    void fireCompileEntityRenderLayers(GeoEntityRenderer<T, R> renderer);
    /**
     * Fire the {@link GeoRenderEvent.Entity.CompileRenderState} event
     */
    <T extends class_1297 & GeoAnimatable, R extends class_10017 & GeoRenderState>
    void fireCompileEntityRenderState(GeoEntityRenderer<T, R> renderer, R renderState, T animatable);
    /**
     * Fire the {@link GeoRenderEvent.Entity.Pre} event, returning true if the event was not cancelled
     */
    <T extends class_1297 & GeoAnimatable, R extends class_10017 & GeoRenderState>
    boolean fireEntityPreRender(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks);

    /**
     * Fire the {@link GeoRenderEvent.ReplacedEntity.CompileRenderLayers} event
     */
    <T extends GeoAnimatable, E extends class_1297, R extends class_10017 & GeoRenderState>
    void fireCompileReplacedEntityRenderLayers(GeoReplacedEntityRenderer<T, E, R> renderer);
    /**
     * Fire the {@link GeoRenderEvent.ReplacedEntity.CompileRenderState} event
     */
    <T extends GeoAnimatable, E extends class_1297, R extends class_10017 & GeoRenderState>
    void fireCompileReplacedEntityRenderState(GeoReplacedEntityRenderer<T, E, R> renderer, R renderState, T animatable, E entity);
    /**
     * Fire the {@link GeoRenderEvent.ReplacedEntity.Pre} event, returning true if the event was not cancelled
     */
    <T extends GeoAnimatable, E extends class_1297, R extends class_10017 & GeoRenderState>
    boolean fireReplacedEntityPreRender(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks);

    /**
     * Fire the {@link GeoRenderEvent.Item.CompileRenderLayers} event
     */
    <T extends class_1792 & GeoAnimatable> void fireCompileItemRenderLayers(GeoItemRenderer<T> renderer);
    /**
     * Fire the {@link GeoRenderEvent.Item.CompileRenderState} event
     */
    <T extends class_1792 & GeoAnimatable, O extends GeoItemRenderer.RenderData, R extends GeoRenderState>
    void fireCompileItemRenderState(GeoItemRenderer<T> renderer, R renderState, T animatable, O renderData);
    /**
     * Fire the {@link GeoRenderEvent.Item.Pre} event, returning true if the event was not cancelled
     */
    <T extends class_1792 & GeoAnimatable>
    boolean fireItemPreRender(RenderPassInfo<GeoRenderState> renderPassInfo, class_11659 renderTasks);

    /**
     * Fire the {@link GeoRenderEvent.Object.CompileRenderLayers} event
     */
    <T extends GeoAnimatable, E, R extends GeoRenderState>
    void fireCompileObjectRenderLayers(GeoObjectRenderer<T, E, R> renderer);
    /**
     * Fire the {@link GeoRenderEvent.Object.CompileRenderState} event
     */
    <T extends GeoAnimatable, E, R extends GeoRenderState>
    void fireCompileObjectRenderState(GeoObjectRenderer<T, E, R> renderer, R renderState, T animatable, @Nullable E relatedObject);
    /**
     * Fire the {@link GeoRenderEvent.Object.Pre} event, returning true if the event was not cancelled
     */
    <T extends GeoAnimatable, E, R extends GeoRenderState>
    boolean fireObjectPreRender(RenderPassInfo<R> renderPassInfo, class_11659 renderTasks);
}
