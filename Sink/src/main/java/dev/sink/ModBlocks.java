package dev.sink;

import dev.sink.block.FaucetBlock;
import dev.sink.block.InfiniteLavaSinkBlock;
import dev.sink.block.InfiniteSinkBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Sink.MODID);

	public static final DeferredHolder<Block, InfiniteSinkBlock> INFINITE_SINK =
		BLOCKS.register("infinite_sink", () -> new InfiniteSinkBlock(sinkProperties()));

	public static final DeferredHolder<Block, InfiniteLavaSinkBlock> INFINITE_LAVA_SINK =
		BLOCKS.register("infinite_lava_sink", () -> new InfiniteLavaSinkBlock(sinkProperties()));

	public static final DeferredHolder<Block, FaucetBlock> FAUCET =
		BLOCKS.register("faucet", () -> new FaucetBlock(faucetProperties()));

	public static void register(IEventBus bus) {
		BLOCKS.register(bus);
		registerBlockItems();
	}

	public static void registerBlockItems() {
		BLOCKS.getEntries().forEach(holder ->
			ModItems.ITEMS.register(holder.getId().getPath(),
				() -> new BlockItem(holder.get(), new Item.Properties())));
	}

	private static BlockBehaviour.Properties sinkProperties() {
		return BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.strength(3.5F, 6.0F)
			.sound(SoundType.STONE)
			.noOcclusion()
			.pushReaction(PushReaction.NORMAL);
	}

	private static BlockBehaviour.Properties faucetProperties() {
		return BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.strength(3.0F, 4.8F)
			.sound(SoundType.METAL)
			.noOcclusion()
			.pushReaction(PushReaction.NORMAL);
	}
}