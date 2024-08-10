package mod.torchbowmod;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.packets.SpawnEntity;

import javax.annotation.Nullable;

import static mod.torchbowmod.TorchBowMod.*;
import static net.minecraft.core.Direction.DOWN;
import static net.minecraft.core.Direction.UP;
import static net.minecraft.world.entity.EntityType.LIGHTNING_BOLT;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class EntityTorch extends AbstractArrow {

    public EntityTorch(SpawnEntity spawnEntity, Level level) {
        this(entityTorch.get(), level);
    }

    public EntityTorch(Level worldIn, LivingEntity shooter, ItemStack pickup, @Nullable ItemStack weaponStack) {
        super(entityTorch.get(), shooter, worldIn,pickup, weaponStack);
    }

    public EntityTorch(EntityType<EntityTorch> entityTorchEntityType, Level level) {
        super(entityTorchEntityType,level);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityRayTraceResult) {
        super.onHitEntity(entityRayTraceResult);
        Entity entity = entityRayTraceResult.getEntity();
        if (entity instanceof Creeper creeper){
            creeperIgnite(creeper);
        }
        entity.setRemainingFireTicks(100);
    }

    @Override
    protected void onHitBlock(BlockHitResult raytraceResultIn) {
        super.onHitBlock(raytraceResultIn);
        HitResult.Type raytraceresult$type = raytraceResultIn.getType();
        if (raytraceresult$type == HitResult.Type.BLOCK) {
            var statePos = raytraceResultIn.getBlockPos();
            if (level().getBlockState(statePos).getBlock() == Blocks.TNT){
                tntIgnite(raytraceResultIn);
            }else {
                setTorch(raytraceResultIn, raytraceResultIn);
            }
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Blocks.TORCH);
    }

    private void creeperIgnite(Creeper creeper){
        if (Math.random() < 0.05) {
            creeper.ignite();
            var bolt = new LightningBolt(LIGHTNING_BOLT, level());
            bolt.setPos(creeper.getOnPos().getCenter());
            level().addFreshEntity(bolt);
        } else if (Math.random() < 0.3) {
            creeper.ignite();
        }
    }

    private void tntIgnite(BlockHitResult blockHitResult){
        var world = level();
        var blockPos = blockHitResult.getBlockPos();
        var blockState = world.getBlockState(blockPos);
        var block = blockState.getBlock();
        block.onCaughtFire(blockState,world,blockPos,null,null);
        world.removeBlock(blockPos, false);
        this.remove(RemovalReason.KILLED);
    }

    private void setTorch(BlockHitResult blockraytraceresult, HitResult raytraceResultIn) {
        BlockPos blockpos = blockraytraceresult.getBlockPos();
        if (!this.level().getBlockState(blockpos).isAir()) {
            if (!level().isClientSide) {
                Direction face = ((BlockHitResult) raytraceResultIn).getDirection();
                BlockState torch_state = Blocks.WALL_TORCH.defaultBlockState();
                BlockPos setBlockPos = getPosOfFace(blockpos, face);
                if (isBlockAIR(setBlockPos)) {
                    if (face == UP) {
                        torch_state = Blocks.TORCH.defaultBlockState();
                        level().setBlock(setBlockPos,torch_state,3);
                        this.remove(RemovalReason.KILLED);
                    } else if (face == DOWN && CeilingTorch != null) {
                        BlockState ceiling_torch = CeilingTorch.defaultBlockState();
                        level().setBlock(setBlockPos, ceiling_torch,3);
                        this.remove(RemovalReason.KILLED);
                    } else if (face != DOWN) {
                        level().setBlock(setBlockPos, torch_state.setValue(HORIZONTAL_FACING, face), 3);
                        this.remove(RemovalReason.KILLED);
                    }
                }
            }
        }
    }

    private BlockPos getPosOfFace(BlockPos blockPos, Direction face) {
        return switch (face) {
            case UP -> blockPos.above();
            case EAST -> blockPos.east();
            case WEST -> blockPos.west();
            case SOUTH -> blockPos.south();
            case NORTH -> blockPos.north();
            case DOWN -> blockPos.below();
        };
    }

    private boolean isBlockAIR(BlockPos pos) {
        Block getBlock = this.level().getBlockState(pos).getBlock();
        if (getBlock instanceof BushBlock) return true;
        Block[] a = {Blocks.CAVE_AIR, Blocks.AIR, Blocks.SNOW, Blocks.VINE};//空気だとみなすブロックリスト
        for (Block traget : a) {
            if (getBlock == traget) return true;
        }
        return false;
    }

}