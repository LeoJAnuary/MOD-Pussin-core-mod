package dev.sink;

import dev.sink.particle.FaucetDripParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 粒子类型注册。
 * 水龙头开启时按背后流体实时生成动态水滴粒子。
 * 该粒子通过携带的流体 id 在客户端动态取贴图/颜色，可适配任意 mod 的流体，且本 mod 无需任何前置。
 */
public class ModParticles {
	public static final DeferredRegister<ParticleType<?>> PARTICLES =
		DeferredRegister.create(Registries.PARTICLE_TYPE, Sink.MODID);

	/** 水龙头开启时、背后有流体时，从喷嘴滴落的通用流体水滴粒子（流体 id 由选项携带）。 */
	public static final DeferredHolder<ParticleType<?>, FaucetDripParticleType> FAUCET_DRIP =
		PARTICLES.register("faucet_drip", () -> new FaucetDripParticleType(true));

	public static void register(IEventBus bus) {
		PARTICLES.register(bus);
	}
}