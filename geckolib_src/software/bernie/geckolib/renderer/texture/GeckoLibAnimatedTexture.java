package software.bernie.geckolib.renderer.texture;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.class_1011;
import net.minecraft.class_1049;
import net.minecraft.class_10539;
import net.minecraft.class_1060;
import net.minecraft.class_1061;
import net.minecraft.class_1079;
import net.minecraft.class_1080;
import net.minecraft.class_1084;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_7764;
import net.minecraft.class_7771;
import net.minecraft.class_9848;
import net.minecraft.client.renderer.texture.*;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Animated texture handler for GeckoLib animated textures.
 * <p>
 * Uses the vanilla {@link class_1079 animated texture schema}, but extrapolates it for non-atlas textures
 * <p>
 * <b><u>NOTE:</u></b> Initially, GeckoLib wraps all texture retrievals in this, to check for animation meta. If it {@link #isAnimated() exists}, this instance is kept,
 * otherwise, a new instance of {@link class_1049} is returned to preserve expected runtime operation for things like Iris.
 */
public class GeckoLibAnimatedTexture extends class_1049 implements class_1061 {
    protected @Nullable AnimationInfo animatedTexture = null;
    protected int frameWidth;
    protected int frameHeight;
    protected @Nullable class_1011 baseImage;

    public GeckoLibAnimatedTexture(class_2960 location) {
        super(location);
    }

    /**
     * If GeckoLib found and constructed a valid animated texture schema.
     * <p>
     * Returning false from here makes this no different from a standard {@link class_1049}, and an instance of that should be used instead
     */
    public boolean isAnimated() {
        return this.animatedTexture != null;
    }

    @Override
    public class_10539 method_65809(class_3300 resourceManager) throws IOException {
        class_3298 resource = resourceManager.getResourceOrThrow(method_65859());

        try (InputStream stream = resource.method_14482()) {
            this.baseImage = class_1011.method_4309(stream);
        }

        this.animatedTexture = resource.method_14481().method_43041(class_1079.field_55537).map(this::buildAnimatedTexture).orElse(null);

        return new class_10539(this.baseImage, resource.method_14481().method_43041(class_1084.field_55542).orElse(null));
    }

    @Override
    public void method_65857(class_10539 textureContents) {
        if (this.baseImage != null) {
            AddressMode address = textureContents.method_65873() ? AddressMode.CLAMP_TO_EDGE : AddressMode.REPEAT;
            FilterMode filter = textureContents.method_65872() ? FilterMode.LINEAR : FilterMode.NEAREST;
            this.field_63613 = RenderSystem.getSamplerCache().method_75293(address, address, filter, filter, false);

            method_65856(this.baseImage);
        }
    }

    @Override
    public void method_65856(class_1011 image) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        class_2960 textureId = method_65859();

        Objects.requireNonNull(textureId);

        this.field_56974 = gpuDevice.createTexture(textureId::toString, 5, TextureFormat.RGBA8, this.frameWidth, this.frameHeight, 1, 1);
        this.field_60597 = gpuDevice.createTextureView(this.field_56974);

        uploadFrame(gpuDevice, image, 0, 0, this.field_56974);
    }

    /**
     * Compile the AnimatedTexture information for this texture instance
     * <p>
     * Mostly used for interpolation handling and tick-frame advancement
     */
    protected GeckoLibAnimatedTexture.@Nullable AnimationInfo buildAnimatedTexture(class_1079 animMeta) {
        if (this.baseImage == null)
            return null;

        final class_7771 frameSize = animMeta.method_24143(this.baseImage.method_4307(), this.baseImage.method_4323());
        this.frameWidth = frameSize.comp_1049();
        this.frameHeight = frameSize.comp_1050();
        final int frameColumns = this.baseImage.method_4307() / this.frameWidth;
        final int frameRows = this.baseImage.method_4323() / this.frameHeight;
        final int frames = frameColumns * frameRows;
        final int defaultFrameTime = animMeta.comp_3456();
        final int frameCount = animMeta.comp_3453().map(List::size).orElse(frames);

        if (frameCount <= 1)
            return null;

        final List<FrameInfo> frameList = new ObjectArrayList<>(frameCount);

        if (animMeta.comp_3453().isEmpty()) {
            for (int i = 0; i < frames; i++) {
                frameList.add(new FrameInfo(i, defaultFrameTime));
            }
        }
        else {
            for (class_1080 frame : animMeta.comp_3453().get()) {
                frameList.add(new FrameInfo(frame.comp_3451(), frame.method_4691(defaultFrameTime)));
            }

            int frameIndex = 0;
            IntSet validFrames = new IntOpenHashSet();

            for (Iterator<FrameInfo> iterator = frameList.iterator(); iterator.hasNext(); frameIndex++) {
                FrameInfo frameInfo = iterator.next();
                boolean validFrame = true;

                if (frameInfo.time <= 0) {
                    GeckoLibConstants.LOGGER.warn("Invalid frame duration on sprite {} frame {}: {}", method_65859(), frameIndex, frameInfo.time);
                    validFrame = false;
                }

                if (frameInfo.index < 0 || frameInfo.index >= frames) {
                    GeckoLibConstants.LOGGER.warn("Invalid frame index on sprite {} frame {}: {}", method_65859(), frameIndex, frameInfo.index);
                    validFrame = false;
                }

                if (validFrame) {
                    validFrames.add(frameInfo.index);
                }
                else {
                    iterator.remove();
                }
            }

            int[] unusedFrames = IntStream.range(0, frames).filter(frame -> !validFrames.contains(frame)).toArray();

            if (unusedFrames.length > 0)
                GeckoLibConstants.LOGGER.warn("Unused frames in sprite {}: {}", method_65859(), Arrays.toString(unusedFrames));
        }

        return new AnimationInfo(List.copyOf(frameList), frameColumns, animMeta.comp_3457());
    }

    /**
     * Upload the given {@link class_1011} to the in-memory texture buffer, with an optional offset for non-interpolated frames
     */
    protected void uploadFrame(GpuDevice gpuDevice, class_1011 image, int x, int y, GpuTexture gpuTexture) {
        gpuDevice.createCommandEncoder().writeToTexture(gpuTexture, image, 0, 0, x, y, this.frameWidth, this.frameHeight, 0, 0);
    }

    /**
     * Called by {@link class_1060} every tick to allow the texture to update itself as necessary.
     * <p>
     * This effectively caps "real" frames at 20fps, but interpolation allows us to fudge this a little.
     */
    @Override
    public void method_4622() {
        if (this.animatedTexture != null)
            this.animatedTexture.tick();
    }

    @Override
    public void close() {
        if (this.baseImage != null)
            this.baseImage.close();

        if (this.animatedTexture != null)
            this.animatedTexture.close();

        super.close();
    }

    /**
     * Container class for the animation information for this texture instance
     * <p>
     * Functionally somewhat of a clone of {@link class_7764.class_12298}, but extrapolated to be extensible and manageable
     */
    protected class AnimationInfo implements AutoCloseable {
        protected final List<FrameInfo> frames;
        protected final int frameRowSize;
        protected final boolean interpolateFrames;

        protected final @Nullable InterpolationData interpolationData;
        protected final class_1011 currentFrameBuffer;
        int currentFrame;
        int subFrame;

        public AnimationInfo(List<FrameInfo> frames, int frameRowSize, boolean interpolateFrames) {
            this.frames = frames;
            this.frameRowSize = frameRowSize;
            this.interpolateFrames = interpolateFrames;
            this.interpolationData = this.interpolateFrames ? new InterpolationData(GeckoLibAnimatedTexture.this.frameWidth, GeckoLibAnimatedTexture.this.frameHeight) : null;
            this.currentFrameBuffer = new class_1011(GeckoLibAnimatedTexture.this.frameWidth, GeckoLibAnimatedTexture.this.frameHeight, false);
        }

        int getFrameColumn(int frameIndex) {
            return frameIndex % this.frameRowSize;
        }

        int getFrameRow(int frameIndex) {
            return frameIndex / this.frameRowSize;
        }

        public void tick() {
            if (GeckoLibAnimatedTexture.this.baseImage == null)
                return;

            this.subFrame++;
            FrameInfo prevFrameInfo = this.frames.get(this.currentFrame);

            if (this.subFrame >= prevFrameInfo.time) {
                this.currentFrame = (this.currentFrame + 1) % this.frames.size();
                this.subFrame = 0;
                int frameIndex = this.frames.get(this.currentFrame).index;

                if (prevFrameInfo.index != frameIndex) {
                    GeckoLibAnimatedTexture instance = GeckoLibAnimatedTexture.this;
                    int frameX = getFrameColumn(frameIndex) * instance.frameWidth;
                    int frameY = getFrameRow(frameIndex) * instance.frameHeight;

                    instance.baseImage.method_47594(this.currentFrameBuffer, frameX, frameY, 0, 0, instance.frameWidth, instance.frameHeight, false, false);

                    uploadFrame(RenderSystem.getDevice(), this.currentFrameBuffer, 0, 0, method_68004());
                }
            }
            else if (this.interpolationData != null) {
                this.interpolationData.tickAndUpload(GeckoLibAnimatedTexture.this.baseImage, method_68004());
            }
        }

        @Override
        public void close() {
            if (this.interpolationData != null)
                this.interpolationData.close();
        }

        /**
         * Handler class for interpolated frame generation and injection
         * <p>
         * This class is only instantiated if the {@link class_1079} enables {@link class_1079#comp_3457() interpolation}
         */
        protected class InterpolationData implements AutoCloseable {
            protected final class_1011 buffer;

            public InterpolationData(int frameWidth, int frameHeight) {
                this.buffer = new class_1011(frameWidth, frameHeight, false);
            }

            /**
             * Check and upload a newly created, interpolated frame, as necessary
             */
            protected void tickAndUpload(class_1011 image, GpuTexture gpuTexture) {
                AnimationInfo instance = AnimationInfo.this;
                List<FrameInfo> frames = instance.frames;
                FrameInfo currentFrameInfo = frames.get(instance.currentFrame);
                int nextFrameIndex = frames.get((instance.currentFrame + 1) % frames.size()).index;

                if (currentFrameInfo.index != nextFrameIndex) {
                    float partialFrame = instance.subFrame / (float)currentFrameInfo.time;
                    int frameHeight = GeckoLibAnimatedTexture.this.frameHeight;
                    int frameWidth = GeckoLibAnimatedTexture.this.frameWidth;

                    for (int pixelY = 0; pixelY < frameHeight; pixelY++) {
                        for (int pixelX = 0; pixelX < frameWidth; pixelX++) {
                            int framePixel = getPixel(image, instance, currentFrameInfo.index, pixelX, pixelY, frameWidth, frameHeight);
                            int nextFramePixel = getPixel(image, instance, nextFrameIndex, pixelX, pixelY, frameWidth, frameHeight);

                            this.buffer.method_61941(pixelX, pixelY, class_9848.method_75605(partialFrame, framePixel, nextFramePixel));
                        }
                    }

                    GeckoLibAnimatedTexture.this.uploadFrame(RenderSystem.getDevice(), this.buffer, 0, 0, gpuTexture);
                }
            }

            /**
             * Get the frame-relative pixel for the given input coordinates and frame index from the root texture
             */
            protected int getPixel(class_1011 image, AnimationInfo animationInfo, int frameIndex, int x, int y, int frameWidth, int frameHeight) {
                return image.method_61940(x + animationInfo.getFrameColumn(frameIndex) * frameWidth, y + animationInfo.getFrameRow(frameIndex) * frameHeight);
            }

            @Override
            public void close() {
                this.buffer.close();
            }
        }
    }

    /**
     * Container class for holding a single animation frame's data
     */
    protected record FrameInfo(int index, int time) {}
}
