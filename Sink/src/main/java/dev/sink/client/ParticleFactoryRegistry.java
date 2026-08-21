package dev.sink.client;

import dev.sink.ModParticles;
import dev.sink.client.particle.FaucetDripParticle;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * 客户端粒子工厂注册（仅客户端，通过 {@code Sink} 构造函数按客户端侧注册）。
 * 将 faucet_drip 粒子类型绑定到 FaucetDripParticle 的生成逻辑
 * （无 json 贴图列表，贴图/颜色由抽取到的流体自身动态决定）。
 */
public class ParticleFactoryRegistry {
	public static void onRegisterParticleFactory(RegisterParticleProvidersEvent event) {
		event.registerSpecial(ModParticles.FAUCET_DRIP.get(), FaucetDripParticle::createProvider);
	}
}