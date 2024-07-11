//package mafuyu33.fallingworld.event;
//
//import com.mojang.brigadier.exceptions.CommandSyntaxException;
//import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
//import net.fabricmc.fabric.api.event.player.UseBlockCallback;
//import net.minecraft.block.*;
//import net.minecraft.block.entity.BlockEntity;
//import net.minecraft.entity.*;
//import net.minecraft.entity.mob.MobEntity;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.nbt.NbtCompound;
//import net.minecraft.nbt.NbtHelper;
//import net.minecraft.registry.entry.RegistryEntry;
//import net.minecraft.registry.tag.BlockTags;
//import net.minecraft.server.command.ServerCommandSource;
//import net.minecraft.server.world.ServerWorld;
//import net.minecraft.state.property.Properties;
//import net.minecraft.text.Text;
//import net.minecraft.util.ActionResult;
//import net.minecraft.util.Hand;
//import net.minecraft.util.hit.BlockHitResult;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.Vec3d;
//import net.minecraft.world.World;
//
//public class UseBlockHandler implements UseBlockCallback {
//
//    @Override
//    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
//        if (!world.isClient) {
//            BlockPos blockPos = hitResult.getBlockPos();
//            BlockState blockState = world.getBlockState(blockPos);
//
//            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3);
//
//            FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(EntityType.FALLING_BLOCK , world);
//
//            fallingBlockEntity.block = blockState;
//            fallingBlockEntity.timeFalling = 1;
//            fallingBlockEntity.setNoGravity(false);
//            fallingBlockEntity.intersectionChecked = true;
//            fallingBlockEntity.setPosition(blockPos.getX() + 0.5,blockPos.getY(),blockPos.getZ() + 0.5);
//            fallingBlockEntity.setVelocity(Vec3d.ZERO);
//            fallingBlockEntity.prevX = blockPos.getX() + 0.5;
//            fallingBlockEntity.prevY = blockPos.getY();
//            fallingBlockEntity.prevZ = blockPos.getZ() + 0.5;
//            fallingBlockEntity.setFallingBlockPos(blockPos);
//
//            world.spawnEntity(fallingBlockEntity);
//            // 如果方块有附加的 BlockEntity 数据，可以设置 blockEntityData 字段
//            BlockEntity blockEntity = world.getBlockEntity(blockPos);
//            if (blockEntity != null) {
//                NbtCompound blockEntityData = new NbtCompound();
//                blockEntity.writeNbt(blockEntityData);
//                fallingBlockEntity.blockEntityData = blockEntityData;
//            }
//
//        }
//        return ActionResult.PASS;
//    }
//}
