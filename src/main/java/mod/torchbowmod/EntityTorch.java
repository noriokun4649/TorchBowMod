package mod.torchbowmod;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BlockItem;
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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

import static mod.torchbowmod.TorchBowMod.*;
import static net.minecraft.core.Direction.DOWN;
import static net.minecraft.core.Direction.UP;
import static net.minecraft.world.entity.EntityType.LIGHTNING_BOLT;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class EntityTorch extends AbstractArrow {
    private static final EntityDataAccessor<ItemStack> TORCH_ITEM =
            SynchedEntityData.defineId(EntityTorch.class, EntityDataSerializers.ITEM_STACK);

    public EntityTorch(SpawnEntity spawnEntity, Level level) {
        this(entityTorch.get(), level);
    }

    public EntityTorch(Level worldIn, LivingEntity shooter, ItemStack pickup, @Nullable ItemStack weaponStack) {
        super(entityTorch.get(), shooter, worldIn,pickup, weaponStack);
        this.entityData.set(TORCH_ITEM, pickup);
    }

    public EntityTorch(EntityType<EntityTorch> entityTorchEntityType, Level level) {
        super(entityTorchEntityType,level);
    }

    @Override
    protected void setPickupItemStack(@NotNull ItemStack pickupItemStack) {
        super.setPickupItemStack(pickupItemStack);
        this.entityData.set(TORCH_ITEM, pickupItemStack);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TORCH_ITEM, this.getDefaultPickupItem());
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult entityRayTraceResult) {
        super.onHitEntity(entityRayTraceResult);
        Entity entity = entityRayTraceResult.getEntity();
        if (entity instanceof Creeper creeper){
            creeperIgnite(creeper);
        }
        if (entity instanceof LivingEntity livingentity) {
            if (!this.level().isClientSide && this.getPierceLevel() <= 0) {
                livingentity.setArrowCount(livingentity.getArrowCount() - 1);
            }
        }
        entity.setRemainingFireTicks(100);
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult raytraceResultIn) {
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
    protected @NotNull ItemStack getDefaultPickupItem() {
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

    private void setTorch(@NotNull BlockHitResult blockraytraceresult, HitResult raytraceResultIn) {
        BlockPos blockpos = blockraytraceresult.getBlockPos();
        if (!this.level().getBlockState(blockpos).isAir()) {
            if (!level().isClientSide) {
                Direction face = ((BlockHitResult) raytraceResultIn).getDirection();
                BlockState wallBlockState = getWallBlockState();
                BlockPos setBlockPos = getPosOfFace(blockpos, face);
                if (isBlockAIR(setBlockPos)) {
                    if (face == UP) {
                        level().setBlock(setBlockPos,getBlockState(),3);
                        this.remove(RemovalReason.KILLED);
                    } else if (face == DOWN && isVanillaTorch(wallBlockState)) {
                        BlockState ceiling_torch = getCeilingBlockState(wallBlockState);
                        level().setBlock(setBlockPos, ceiling_torch,3);
                        this.remove(RemovalReason.KILLED);
                    } else if (face != DOWN) {
                        level().setBlock(setBlockPos, wallBlockState.setValue(HORIZONTAL_FACING, face), 3);
                        this.remove(RemovalReason.KILLED);
                    }
                }
            }
        }
    }

    private BlockState getWallBlockState(){
        if (this.getPickupItem().getItem() instanceof BlockItem blockItem){
            return ITEM_TO_WALL_BLOCK.get(blockItem).defaultBlockState();
        }
        return Blocks.WALL_TORCH.defaultBlockState();
    }
    private BlockState getBlockState(){
        if (this.getPickupItem().getItem() instanceof BlockItem blockItem){
            return blockItem.getBlock().defaultBlockState();
        }
        return Blocks.TORCH.defaultBlockState();
    }
    private BlockState getCeilingBlockState(BlockState state){
        if (CeilingTorch == null) return Blocks.WALL_TORCH.defaultBlockState();
        var CEILING_MAP = Map.of(
                Blocks.WALL_TORCH, CeilingTorch.get(),
                Blocks.SOUL_WALL_TORCH, CeilingSoulTorch.get()
        );
        return CEILING_MAP.getOrDefault(state.getBlock(), Blocks.WALL_TORCH).defaultBlockState();
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
        for (Block target : a) {
            if (getBlock == target) return true;
        }
        return false;
    }

    private boolean isVanillaTorch(BlockState state){
        if (CeilingTorch == null) return false;
        var vanillaTorch = Set.of(Blocks.WALL_TORCH, Blocks.SOUL_WALL_TORCH);
        return vanillaTorch.contains(state.getBlock());
    }

    public ItemStack getTorchItem(){
        return this.entityData.get(TORCH_ITEM);
    }
}