package dev.sink.block.entity;

import dev.sink.ModBlockEntities;
import dev.sink.block.FaucetBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * 精简自包含版水龙头方块实体。
 * 逻辑：当水龙头打开时，从其背面方向的水源抽取水，灌注到正下方的容器里。
 * 若不理会 Supplementaries 对 Moonlight / SoftFluid / 动态模型 的深度依赖，
 * 这里只需要一个简单的周期逻辑即可工作。
 */
public class FaucetBlockTile extends BlockEntity {
	public static final int COOLDOWN = 20;
	private int transferCooldown = 0;

	public FaucetBlockTile(BlockPos pos, BlockState state) {
		super(ModBlockEntities.FAUCET.get(), pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, FaucetBlockTile tile) {
		if (!tile.isOpen()) {
			tile.transferCooldown = 0;
			return;
		}
		if (tile.transferCooldown > 0) {
			tile.transferCooldown--;
			return;
		}
		if (tile.pourIfSource()) {
			tile.transferCooldown = COOLDOWN;
		}
	}

	// 打开条件与原版一致：红石信号取反开关
	public boolean isOpen() {
		BlockState state = this.getBlockState();
		return state.getValue(FaucetBlock.POWERED) ^ state.getValue(FaucetBlock.ENABLED);
	}

	/**
	 * 从背面方向的水源抽水并灌注下方，若成功返回 true。
	 * 本模组主题是"无限"，因此这里假定背面的水是无限供给，不消耗水源。
	 */
	private boolean pourIfSource() {
		if (this.level == null) return false;
		Direction facing = getBlockState().getValue(FaucetBlock.FACING);
		BlockPos sourcePos = this.worldPosition.relative(facing.getOpposite());
		FluidState fluidState = this.level.getFluidState(sourcePos);
		if (!fluidState.is(Fluids.WATER)) return false;
		return pourBelow();
	}

	private boolean pourBelow() {
		BlockPos below = this.worldPosition.below();
		BlockState belowState = this.level.getBlockState(below);

		if (belowState.is(Blocks.CAULDRON)) {
			this.level.setBlockAndUpdate(below, Blocks.WATER_CAULDRON.defaultBlockState()
				.setValue(LayeredCauldronBlock.LEVEL, 1));
			playDripSound();
			return true;
		}
		if (belowState.is(Blocks.WATER_CAULDRON)) {
			int level = belowState.getValue(LayeredCauldronBlock.LEVEL);
			if (level < 3) {
				this.level.setBlockAndUpdate(below, belowState.setValue(LayeredCauldronBlock.LEVEL, level + 1));
				playDripSound();
				return true;
			}
			return false; // 已满
		}
		// 空位直接倒出水
		if (belowState.isAir() || belowState.getFluidState().is(Fluids.WATER)) {
			this.level.setBlockAndUpdate(below, Blocks.WATER.defaultBlockState());
			playDripSound();
			return true;
		}
		return false;
	}

	private void playDripSound() {
		if (this.level == null) return;
		this.level.playSound(null, worldPosition, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER,
			SoundSource.BLOCKS, 0.5F, 1.0F);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		this.transferCooldown = tag.getInt("TransferCooldown");
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putInt("TransferCooldown", this.transferCooldown);
	}
}