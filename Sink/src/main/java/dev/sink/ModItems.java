package dev.sink;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Sink.MODID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
		DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Sink.MODID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> INFINITE_SINK_TAB =
		CREATIVE_MODE_TABS.register("infinite_sink_tab", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.sink"))
			.icon(() -> new ItemStack(ModBlocks.INFINITE_SINK.get()))
			.displayItems((parameters, output) -> {
				output.accept(ModBlocks.INFINITE_SINK.get());
				output.accept(ModBlocks.INFINITE_LAVA_SINK.get());
				output.accept(ModBlocks.FAUCET.get());
			})
			.build());

	public static void register(IEventBus bus) {
		ITEMS.register(bus);
		CREATIVE_MODE_TABS.register(bus);
	}
}