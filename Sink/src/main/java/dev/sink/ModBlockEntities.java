package dev.sink;

import dev.sink.block.entity.InfiniteLavaSinkBlockEntity;
import dev.sink.block.entity.InfiniteSinkBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
		DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Sink.MODID);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfiniteSinkBlockEntity>> INFINITE_SINK =
		BLOCK_ENTITIES.register("infinite_sink",
			() -> BlockEntityType.Builder.of(InfiniteSinkBlockEntity::new, ModBlocks.INFINITE_SINK.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfiniteLavaSinkBlockEntity>> INFINITE_LAVA_SINK =
		BLOCK_ENTITIES.register("infinite_lava_sink",
			() -> BlockEntityType.Builder.of(InfiniteLavaSinkBlockEntity::new, ModBlocks.INFINITE_LAVA_SINK.get()).build(null));

	public static void register(IEventBus bus) {
		BLOCK_ENTITIES.register(bus);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, INFINITE_SINK.get(), (be, side) -> be.getWaterHandler());
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, INFINITE_LAVA_SINK.get(), (be, side) -> be.getLavaHandler());
	}
}