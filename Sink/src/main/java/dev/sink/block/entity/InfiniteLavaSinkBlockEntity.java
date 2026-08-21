package dev.sink.block.entity;

import dev.sink.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class InfiniteLavaSinkBlockEntity extends BlockEntity {
	/** 无限岩浆槽对外呈现的内容量（mB），永久保持不变，用于自动适配水龙头等读取方。 */
	public static final int CONTENT = 10000;
	private final InfiniteLavaHandler lavaHandler = new InfiniteLavaHandler();

	public InfiniteLavaSinkBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.INFINITE_LAVA_SINK.get(), pos, state);
	}

	public IFluidHandler getLavaHandler() {
		return lavaHandler;
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return super.getUpdateTag(registries);
	}

	public class InfiniteLavaHandler implements IFluidHandler {
		@Override
		public int getTanks() {
			return 1;
		}

		@Override
		public FluidStack getFluidInTank(int tank) {
			return new FluidStack(Fluids.LAVA, CONTENT);
		}

		@Override
		public int getTankCapacity(int tank) {
			return CONTENT;
		}

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			return stack.getFluid() == Fluids.LAVA;
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			return 0;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			if (resource.getFluid() == Fluids.LAVA) {
				return new FluidStack(Fluids.LAVA, resource.getAmount());
			}
			return FluidStack.EMPTY;
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			return new FluidStack(Fluids.LAVA, maxDrain);
		}
	}
}