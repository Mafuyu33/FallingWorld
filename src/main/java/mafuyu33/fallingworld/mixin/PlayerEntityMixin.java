package mafuyu33.fallingworld.mixin;

import mafuyu33.fallingworld.FallingWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.sql.rowset.Predicate;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static mafuyu33.fallingworld.FallingWorld.*;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Unique
	private static final Set<Block> EXCLUDED_BLOCKS = new HashSet<>();

	static {
		EXCLUDED_BLOCKS.add(Blocks.OBSIDIAN);
		EXCLUDED_BLOCKS.add(Blocks.AIR);
		EXCLUDED_BLOCKS.add(Blocks.LAVA);
		EXCLUDED_BLOCKS.add(Blocks.WATER);
		EXCLUDED_BLOCKS.add(Blocks.BEDROCK);
		EXCLUDED_BLOCKS.add(Blocks.TALL_SEAGRASS);
		EXCLUDED_BLOCKS.add(Blocks.TALL_GRASS);
		EXCLUDED_BLOCKS.add(Blocks.LARGE_FERN);
//		EXCLUDED_BLOCKS.add(Blocks.SCAFFOLDING);

		// 可以在这里添加更多的需要排除的方块
	}

	@Inject(at = @At("TAIL"), method = "tick")
	private void init(CallbackInfo info) {
		if (!this.getWorld().isClient) {
			int fallingRadius_x = this.getWorld().getGameRules().getInt(FALLING_RANGE_HORIZONTAL);
			int fallingRadius_y = this.getWorld().getGameRules().getInt(FALLING_RANGE_VERTICAL);
			int fallingRadius_z = this.getWorld().getGameRules().getInt(FALLING_RANGE_HORIZONTAL);

			if(fallingRadius_x!=0 && fallingRadius_y!=0 && fallingRadius_z!=0) {//都不为0
				for (int yOffset = -fallingRadius_y; yOffset <= fallingRadius_y; yOffset++) {
					for (int xOffset = -fallingRadius_x; xOffset <= fallingRadius_x; xOffset++) {
						for (int zOffset = -fallingRadius_z; zOffset <= fallingRadius_z; zOffset++) {
							BlockPos targetPos = this.getBlockPos().add(xOffset, yOffset, zOffset);
							BlockState blockState = this.getWorld().getBlockState(targetPos);
							Block block = blockState.getBlock();
							if (FallingBlock.canFallThrough(this.getWorld().getBlockState(targetPos.down()))) {//如果下方方块可以掉下去
								if (!EXCLUDED_BLOCKS.contains(block)) {
									// 如果方块在范围内且不是特定方块。掉落！
									fallingworld$generateFallingBlock(targetPos, blockState, this.getWorld());
								} else if (fallingworld$isFluidExceedingThreshold(targetPos)) {//如果方块是流体源头，处理他的掉落
									fallingworld$generateFallingBlock(targetPos, blockState, this.getWorld());
								}
							}
						}
					}
				}
			}
		}
	}

	@Unique
	private boolean fallingworld$isFluidExceedingThreshold(BlockPos blockPos) {
		BlockState blockState = this.getWorld().getBlockState(blockPos);
		if (blockState.getBlock() == Blocks.WATER || blockState.getBlock() == Blocks.LAVA) {
			int level = blockState.get(Properties.LEVEL_15);
			if (level==0){//如果是源头的话
				//底下是不是可以掉落的部分?如果是液体，就掉落
                return fallingworld$canFluidFallThrough(this.getWorld().getBlockState(blockPos.down()));
			}
		}
		return false;
	}
	@Unique
	private static boolean fallingworld$canFluidFallThrough(BlockState state) {
		return state.isAir() || state.isIn(BlockTags.FIRE);
	}
	@Unique
	private void fallingworld$generateFallingBlock(BlockPos targetPos ,BlockState blockState, World world) {
			BlockEntity blockEntity = world.getBlockEntity(targetPos);

			world.setBlockState(targetPos, Blocks.AIR.getDefaultState(), 3);

			FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(EntityType.FALLING_BLOCK, world);

			fallingBlockEntity.block = blockState;
			fallingBlockEntity.timeFalling = 1;
			fallingBlockEntity.setNoGravity(false);
			fallingBlockEntity.intersectionChecked = true;
			fallingBlockEntity.setPosition(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
			fallingBlockEntity.setVelocity(Vec3d.ZERO);
			fallingBlockEntity.prevX = targetPos.getX() + 0.5;
			fallingBlockEntity.prevY = targetPos.getY();
			fallingBlockEntity.prevZ = targetPos.getZ() + 0.5;
			fallingBlockEntity.setFallingBlockPos(targetPos);

			// 如果方块有附加的 BlockEntity 数据，可以设置 blockEntityData 字段
			if (blockEntity != null) {
				fallingBlockEntity.blockEntityData = blockEntity.createNbtWithIdentifyingData();
			}

			world.spawnEntity(fallingBlockEntity);
	}
}

