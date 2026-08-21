package dev.sink.particle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.sink.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * 通用流体水滴粒子选项：携带被抽取流体的注册 id（仅代码表 id），
 * 客户端据此通过 {@code IClientFluidTypeExtensions} 动态获取该流体的静止贴图与颜色。
 * <p>
 * 此判定完全基于原版/NeoForge API，因此可以适配任意 mod 的流体且本 mod 无需任何前置。
 * 编解码（{@link #CODEC} / {@link #STREAM_CODEC}）由对应的 {@link FaucetDripParticleType} 对外提供。
 */
public class FaucetDripParticleOptions implements ParticleOptions {

	public static final MapCodec<FaucetDripParticleOptions> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("fluid").forGetter(FaucetDripParticleOptions::fluidId)
		).apply(instance, FaucetDripParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FaucetDripParticleOptions> STREAM_CODEC =
		StreamCodec.composite(
			ResourceLocation.STREAM_CODEC, FaucetDripParticleOptions::fluidId,
			FaucetDripParticleOptions::new);

	private final ResourceLocation fluidId;

	public FaucetDripParticleOptions(ResourceLocation fluidId) {
		this.fluidId = fluidId;
	}

	public ResourceLocation fluidId() {
		return this.fluidId;
	}

	@Override
	public ParticleType<FaucetDripParticleOptions> getType() {
		return ModParticles.FAUCET_DRIP.get();
	}
}