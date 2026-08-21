package dev.sink.client.particle;

import dev.sink.particle.FaucetDripParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.Nullable;

/**
 * 水龙头通用流体水滴粒子（参考森罗物语酒馆的 TapDripParticle）。
 * <p>
 * 不再绑定固定贴图：通过 {@link IClientFluidTypeExtensions} 读取抽取流体自身的静止贴图与着色，
 * 因此可适配任意 mod 的流体，且渲染完全基于 NeoForge/MC API。
 * 贴图取自方块图集（流体贴图均在其中），故渲染使用 {@link ParticleRenderType#TERRAIN_SHEET}。
 */
@OnlyIn(Dist.CLIENT)
public class FaucetDripParticle extends TextureSheetParticle {
	private static final ResourceLocation FALLBACK = ResourceLocation.withDefaultNamespace("block/water_still");

	public FaucetDripParticle(ClientLevel level, double x, double y, double z, FaucetDripParticleOptions options) {
		super(level, x, y, z);
		this.setSize(0.01F, 0.01F);

		Fluid fluid = BuiltInRegistries.FLUID.get(options.fluidId());
		IClientFluidTypeExtensions ext = fluid != null && fluid != Fluids.EMPTY
			? IClientFluidTypeExtensions.of(fluid) : IClientFluidTypeExtensions.DEFAULT;

		ResourceLocation tex = ext.getStillTexture();
		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
			.apply(tex != null ? tex : FALLBACK);
		this.setSprite(sprite);

		int argb = ext.getTintColor();
		this.setColor(((argb >> 16) & 0xFF) / 255F, ((argb >> 8) & 0xFF) / 255F, (argb & 0xFF) / 255F);

		// 缓慢下坠的动态水流
		this.gravity = 0.03F;
		this.lifetime = 40 + this.random.nextInt(20);
		this.yd = -0.02F;
	}

	@Nullable
	public static Particle createProvider(FaucetDripParticleOptions options, ClientLevel level,
			double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		// 未知流体（mod 被卸载等）直接不生成粒子
		if (BuiltInRegistries.FLUID.get(options.fluidId()) == Fluids.EMPTY) {
			return null;
		}
		return new FaucetDripParticle(level, x, y, z, options);
	}

	@Override
	public ParticleRenderType getRenderType() {
		// 流体贴图位于方块图集，使用地形图集渲染
		return ParticleRenderType.TERRAIN_SHEET;
	}
}