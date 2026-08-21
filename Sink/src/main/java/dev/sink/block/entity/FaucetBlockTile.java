package dev.sink.block.entity;

import dev.sink.ModBlockEntities;
import dev.sink.block.FaucetBlock;
import dev.sink.particle.FaucetDripParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

/**
 * 水龙头方块实体。
 * <p>
 * 基于 NeoForge 流体能力（{@link Capabilities.FluidHandler#BLOCK}）工作，无需任何 mod/lib 前置。
 * 背面可以是：任意液体容器（Create 液罐、其他 mod 储罐、本模组无限水槽/无限岩浆槽）、
 * 流体方块、或炼药锅（含隔一个方块）；背面抽不出时按模组"无限"主题视为无限供给。
 * 下方可以是：任意液体容器、炼药锅、或空位（直接放置流体方块）。
 * 开启后每 0.5 秒向下方输出一次水体（默认 1000mb）。
 * <p>
 * 视觉：在喷嘴处周期生成水滴粒子，客户端按该流体自身的贴图/颜色渲染（自动适配任意 mod 流体）。
 */
public class FaucetBlockTile extends BlockEntity {
	public static final int COOLDOWN = 10;          // 0.5 秒（20 tick = 1s）
	/** 每次向下方容器转移的流体量（mB）。 */
	public static final int TRANSFER_AMOUNT = 1000;
	/** 动态水滴粒子的生成间隔（tick）。 */
	public static final int DRIP_INTERVAL = 5;
	private int transferCooldown = 0;
	private int dripCounter = 0;

	public FaucetBlockTile(BlockPos pos, BlockState state) {
		super(ModBlockEntities.FAUCET.get(), pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, FaucetBlockTile tile) {
		if (!tile.isOpen()) {
			tile.transferCooldown = 0;
			tile.dripCounter = 0;
			return;
		}
		FluidStack drawn = tile.drawnStack();
		if (!drawn.isEmpty()) {
			tile.dripCounter--;
			if (tile.dripCounter <= 0) {
				tile.spawnDripParticle(drawn.getFluid());
				tile.dripCounter = DRIP_INTERVAL;
			}
		}
		if (tile.transferCooldown > 0) {
			tile.transferCooldown--;
			return;
		}
		if (tile.pour(drawn)) {
			tile.transferCooldown = COOLDOWN;
		}
	}

	// 在喷嘴（方块下沿中心）处向客户端发送携带流体 id 的通用水滴粒子，
	// 客户端据此按该流体的贴图/颜色渲染（自动适配任意 mod 流体）。
	private void spawnDripParticle(Fluid drawn) {
		if (!(this.level instanceof ServerLevel serverLevel) || drawn == null) return;
		ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(drawn);
		double x = this.worldPosition.getX() + 0.5;
		double y = this.worldPosition.getY() + 0.3;
		double z = this.worldPosition.getZ() + 0.5;
		serverLevel.sendParticles(new FaucetDripParticleOptions(fluidId), x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
	}

	/** 背面方向、距离 dist 的位置（dist=1 直接相邻，dist=2 隔一个方块）。 */
	private BlockPos backPos(int dist) {
		return this.worldPosition.relative(getBlockState().getValue(FaucetBlock.FACING).getOpposite(), dist);
	}

	/**
	 * 背面方向的"液体容器"流体能力处理器。
	 * 优先直接相邻；若相邻为空位/可贯穿，则再探测隔一个方块处。
	 * 中间若为不透气实心块则不再跨越。
	 */
	private @org.jetbrains.annotations.Nullable IFluidHandler getSourceHandler() {
		if (this.level == null) return null;
		Direction facing = getBlockState().getValue(FaucetBlock.FACING);
		for (int dist = 1; dist <= 2; dist++) {
			if (dist == 2) {
				BlockState prev = this.level.getBlockState(backPos(1));
				if (!prev.isAir() && !prev.canBeReplaced()) break; // 中间是实心块，不跨越
			}
			IFluidHandler handler = getHandlerAt(backPos(dist), facing);
			if (handler != null) return handler;
		}
		return null;
	}

	/** 正下方方块的流体能力处理器。 */
	private @org.jetbrains.annotations.Nullable IFluidHandler getBelowHandler() {
		return getHandlerAt(this.worldPosition.below(), Direction.UP);
	}

	private @org.jetbrains.annotations.Nullable IFluidHandler getHandlerAt(BlockPos pos, Direction context) {
		if (this.level == null) return null;
		return Capabilities.FluidHandler.BLOCK.getCapability(
			this.level, pos, this.level.getBlockState(pos), this.level.getBlockEntity(pos), context);
	}

	/**
	 * 背面"液体容器"中第一个非空的流体类型；支持直接相邻或隔一个方块。
	 * 若背面是纯流体方块或炼药锅（非容器），退回按方块状态识别。
	 */
	private FluidStack drawnStack() {
		IFluidHandler back = getSourceHandler();
		if (back != null) {
			for (int i = 0; i < back.getTanks(); i++) {
				FluidStack stack = back.getFluidInTank(i);
				if (!stack.isEmpty()) return stack;
			}
			return FluidStack.EMPTY;
		}
		if (this.level == null) return FluidStack.EMPTY;
		for (int dist = 1; dist <= 2; dist++) {
			if (dist == 2) {
				BlockState prev = this.level.getBlockState(backPos(1));
				if (!prev.isAir() && !prev.canBeReplaced()) break; // 中间是实心块
			}
			BlockPos pos = backPos(dist);
			FluidState fluidState = this.level.getFluidState(pos);
			if (!fluidState.isEmpty()) return new FluidStack(fluidState.getType(), TRANSFER_AMOUNT);
			Fluid cauldronFluid = fluidOfCauldron(this.level.getBlockState(pos));
			if (cauldronFluid != null) return new FluidStack(cauldronFluid, TRANSFER_AMOUNT);
			BlockState bs = this.level.getBlockState(pos);
			if (!bs.isAir() && !bs.canBeReplaced()) break; // 该位置是实心非流体块，停止
		}
		return FluidStack.EMPTY;
	}

	/** 从方块状态识别炼药锅中的流体（水/岩浆），否则返回 null。 */
	private @org.jetbrains.annotations.Nullable Fluid fluidOfCauldron(BlockState state) {
		if (state.is(Blocks.WATER_CAULDRON)) return Fluids.WATER;
		if (state.is(Blocks.LAVA_CAULDRON)) return Fluids.LAVA;
		return null;
	}

	// 打开条件与原版一致：红石信号取反开关
	public boolean isOpen() {
		BlockState state = this.getBlockState();
		return state.getValue(FaucetBlock.POWERED) ^ state.getValue(FaucetBlock.ENABLED);
	}

	/**
	 * 把背面流体灌入正下方，若成功返回 true。
	 * <ul>
	 *   <li>下方是容器且能接受 → 灌入（优先从背面真实抽取；抽不出则视为无限供给）。</li>
	 *   <li>下方是炼药锅/空位 或 容器拒绝 → 走方块兜底（灌炼药锅/放置流体方块）。</li>
	 * </ul>
	 */
	private boolean pour(FluidStack drawn) {
		if (this.level == null || drawn.isEmpty()) return false;
		IFluidHandler back = getSourceHandler();
		IFluidHandler below = getBelowHandler();
		Fluid fluid = drawn.getFluid();

		if (below != null) {
			FluidStack probe = new FluidStack(fluid, TRANSFER_AMOUNT);
			int accepted = below.fill(probe, FluidAction.SIMULATE);
			if (accepted > 0) {
				// 优先从背面真实抽取；若背面抽不出（炼药锅/纯流体/无限不作消耗），按无限供给
				FluidStack drained = FluidStack.EMPTY;
				if (back != null) {
					drained = back.drain(new FluidStack(fluid, accepted), FluidAction.EXECUTE);
				}
				if (drained.isEmpty() || drained.getAmount() <= 0) {
					drained = new FluidStack(fluid, accepted);
				}
				int put = below.fill(drained, FluidAction.EXECUTE);
				if (put > 0) {
					playDripSound();
					return true;
				}
			}
			// 下方容器存在但拒绝（炼药锅能力等）或灌不进去 → 走方块兜底
			return pourBelowAsBlock(drawn);
		}
		return pourBelowAsBlock(drawn);
	}

	/** 兜底：往正下方的炼药锅灌入、或向空位放置流体源方块（兼容任意 mod 流体）。 */
	private boolean pourBelowAsBlock(FluidStack drawn) {
		if (this.level == null || drawn.isEmpty()) return false;
		Fluid fluid = drawn.getFluid();
		boolean water = fluid == Fluids.WATER;
		BlockPos below = this.worldPosition.below();
		BlockState belowState = this.level.getBlockState(below);

		if (belowState.is(Blocks.CAULDRON)) {
			if (water) {
				this.level.setBlockAndUpdate(below,
					Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1));
				playDripSound();
				return true;
			}
			return false; // 岩浆及其他流体不倒进空炼药锅
		}
		if (belowState.is(Blocks.WATER_CAULDRON)) {
			if (!water) return false;
			int level = belowState.getValue(LayeredCauldronBlock.LEVEL);
			if (level < 3) {
				this.level.setBlockAndUpdate(below, belowState.setValue(LayeredCauldronBlock.LEVEL, level + 1));
				playDripSound();
				return true;
			}
			return false; // 已满
		}
		if (belowState.is(Blocks.LAVA_CAULDRON)) {
			return false; // 不可再灌入岩浆锅
		}
		if (belowState.isAir() || belowState.getFluidState().is(Fluids.WATER) || belowState.canBeReplaced()) {
			BlockState fluidBlock = fluid.defaultFluidState().createLegacyBlock();
			if (!fluidBlock.isAir()) {
				this.level.setBlockAndUpdate(below, fluidBlock);
				playDripSound();
				return true;
			}
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