package mafuyu33.fallingworld.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

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

		// 可以在这里添加更多的需要排除的方块
	}
	@Unique
	@Final
	int fallingRadius = 7;

	@Inject(at = @At("TAIL"), method = "tick")
	private void init(CallbackInfo info) {
		if (!this.getWorld().isClient) {
			for (int yOffset = -fallingRadius; yOffset <= fallingRadius; yOffset++) {
				for (int xOffset = -fallingRadius; xOffset <= fallingRadius; xOffset++) {
					for (int zOffset = -fallingRadius; zOffset <= fallingRadius; zOffset++) {
						BlockPos targetPos = this.getBlockPos().add(xOffset, yOffset, zOffset);
						BlockState blockState = this.getWorld().getBlockState(targetPos);
						Block block = blockState.getBlock();
						if(FallingBlock.canFallThrough(this.getWorld().getBlockState(targetPos.down()))) {//如果下方方块可以掉下去
							if (!EXCLUDED_BLOCKS.contains(block)) {
								// 如果方块在范围内且不是特定方块，且如果方块是流体，小于等于8就忽略。
								// 掉落！
								fallingworld$generateFallingBlock(targetPos, blockState, this.getWorld());
							}
						}
					}
				}
			}
		}
	}

	@Unique
	private boolean fallingworld$isFluidExceedingThreshold(BlockState blockState) {
		if (blockState.getBlock() == Blocks.WATER || blockState.getBlock() == Blocks.LAVA) {
			int level = blockState.get(Properties.LEVEL_1_8);
			return level < 8 && level >= 0; // 忽略流动中的水和岩浆,还未实现
		}
		return false;
	}
	@Unique
	private void fallingworld$generateFallingBlock(BlockPos targetPos ,BlockState blockState, World world) {

		world.setBlockState(targetPos, Blocks.AIR.getDefaultState(), 3);

		FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(EntityType.FALLING_BLOCK , world);

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

		world.spawnEntity(fallingBlockEntity);
		// 如果方块有附加的 BlockEntity 数据，可以设置 blockEntityData 字段
		BlockEntity blockEntity = world.getBlockEntity(targetPos);
		if (blockEntity != null) {
			NbtCompound blockEntityData = new NbtCompound();
			blockEntity.writeNbt(blockEntityData);
			fallingBlockEntity.blockEntityData = blockEntityData;
		}
	}
}

