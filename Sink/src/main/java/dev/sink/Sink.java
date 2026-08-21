package dev.sink;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
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
		// Register fluid handler capabilities for the block entities
		modEventBus.addListener(ModBlockEntities::registerCapabilities);
	}
}