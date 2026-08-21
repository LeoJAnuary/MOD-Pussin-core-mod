package dev.sink.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 通用流体水滴粒子的类型标记（无默认状态、不携带数据）。
 * 数据（抽取到的流体 id）由 {@link FaucetDripParticleOptions} 携带。
 * <p>
 * 使用该类型注册的唯一实例作为 {@link net.minecraft.core.particles.ParticleOptions#getType()}，
 * 因此粒子在服务端与客户端都通过同一个注册表实例被识别。
 */
public class FaucetDripParticleType extends ParticleType<FaucetDripParticleOptions> {

	public FaucetDripParticleType(boolean overrideLimiter) {
		super(overrideLimiter);
	}

	@Override
	public MapCodec<FaucetDripParticleOptions> codec() {
		return FaucetDripParticleOptions.CODEC;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, FaucetDripParticleOptions> streamCodec() {
		return FaucetDripParticleOptions.STREAM_CODEC;
	}
}