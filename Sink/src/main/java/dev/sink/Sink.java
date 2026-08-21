package dev.sink;

import dev.sink.client.ParticleFactoryRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Sink.MODID)
public class Sink {
	// Define mod id in a common place for everything to reference
	public static final String MODID = "sink";
	// Directly reference a slf4j logger
	private static final Logger LOGGER = LoggerFactory.getLogger(Sink.class);

	// The constructor for the mod class is the first code that is run when your mod is loaded.
	public Sink(IEventBus modEventBus) {
		// Register the Deferred Registers to the mod event bus
		ModItems.register(modEventBus);
		ModBlocks.register(modEventBus);
		ModBlockEntities.register(modEventBus);
		ModParticles.register(modEventBus);
		// Register fluid handler capabilities for the block entities
		modEventBus.addListener(ModBlockEntities::registerCapabilities);
		// Client-only registrations (粒子工厂等) — 仅构建/启动客户端逻辑，dedicated server 不加载
		if (FMLEnvironment.dist.isClient()) {
			modEventBus.addListener(ParticleFactoryRegistry::onRegisterParticleFactory);
		}
	}
}