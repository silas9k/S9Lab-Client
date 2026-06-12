package software.bernie.geckolib.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_1044;
import net.minecraft.class_1049;
import net.minecraft.class_10537;
import net.minecraft.class_10539;
import net.minecraft.class_1060;
import net.minecraft.class_2960;
import net.minecraft.client.renderer.texture.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.renderer.texture.GeckoLibAnimatedTexture;

/**
 * Injection into TextureManager's access point for runtime-derived textures to allow GeckoLib to swap them out with {@code GeckoLibAnimatedTexture}
 * for animated texture purposes
 * <p>
 * Because GeckoLibAnimatedTexture extends {@link net.minecraft.class_1049 SimpleTexture}, the replacement should be seamless
 */
@Mixin(value = class_1060.class, priority = 2000)
public abstract class TextureManagerMixin {
	@Shadow protected abstract class_10539 loadContentsSafe(class_2960 textureId, class_10537 texture);

	@Shadow public abstract void register(class_2960 path, class_1044 texture);

    /**
     * Swap out the vanilla SimpleTexture for a GeckoLibAnimatedTexture if the texture is animated
     */
	@WrapOperation(method = "getTexture(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/texture/AbstractTexture;",
			at = @At(value = "NEW", target = "(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/texture/SimpleTexture;"),
			require = 0)
	private class_1049 geckolib$replaceAnimatableTexture(class_2960 location, Operation<class_1049> original) {
		GeckoLibAnimatedTexture animatableTexture = new GeckoLibAnimatedTexture(location);

		class_10539 contents = loadContentsSafe(location, animatableTexture);

		if (animatableTexture.isAnimated()) {
			animatableTexture.method_65857(contents);
			register(location, animatableTexture);

			return animatableTexture;
		}

		animatableTexture.close();

		return original.call(location);
	}

    /**
     * Force-cancel texture registration if texture is GeckolibAnimatedTexture, since we already did it in {@code geckolib$replaceAnimatableTexture}
     */
	@WrapWithCondition(method = "getTexture(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/texture/AbstractTexture;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;registerAndLoad(Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/renderer/texture/ReloadableTexture;)V"),
			require = 0)
	private boolean geckolib$skipAnimatableTextureRegistration(class_1060 textureManager, class_2960 id, class_10537 texture) {
		return !(texture instanceof GeckoLibAnimatedTexture);
	}
}
