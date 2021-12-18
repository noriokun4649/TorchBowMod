package mod.torchbowmod;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;

import static mod.torchbowmod.TorchBowMod.CeilingTorch;
import static mod.torchbowmod.TorchBowMod.TORCH_ENTITY;
import static net.minecraft.core.Direction.DOWN;
import static net.minecraft.core.Direction.UP;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class EntityTorch extends AbstractArrow {

    public EntityTorch(PlayMessages.SpawnEntity packet, Level worldIn﻿) {
        super(TORCH_ENTITY, worldIn﻿);
    }

    public EntityTorch(EntityType<? extends EntityTorch> p_i50172_1_, Level p_i50172_2_) {
        super(p_i50172_1_, p_i50172_2_);
    }

    public EntityTorch(Level worldIn, double x, double y, double z) {
        super(TORCH_ENTITY, x, y, z, worldIn);
    }

    public EntityTorch(Level worldIn, LivingEntity shooter) {
        super(TORCH_ENTITY, shooter, worldIn);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityRayTraceResult) {
        super.onHitEntity(entityRayTraceResult);
        Entity entity = entityRayTraceResult.getEntity();
        entity.setRemainingFireTicks(100);
    }

    @Override
    protected void onHitBlock(BlockHitResult raytraceResultIn) {
        super.onHitBlock(raytraceResultIn);
        HitResult.Type raytraceresult$type = raytraceResultIn.getType();
        if (raytraceresult$type == HitResult.Type.BLOCK) {
            BlockHitResult blockraytraceresult = raytraceResultIn;
            setTorch(blockraytraceresult, raytraceResultIn);
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void setTorch(BlockHitResult blockraytraceresult, HitResult raytraceResultIn) {
        BlockPos blockpos = blockraytraceresult.getBlockPos();
        if (!this.level.getBlockState(blockpos).isAir()) {
            if (!level.isClientSide) {
                Direction face = ((BlockHitResult) raytraceResultIn).getDirection();
                BlockState torch_state = Blocks.WALL_TORCH.defaultBlockState();
                BlockPos setBlockPos = getPosOfFace(blockpos, face);
                if (isBlockAIR(setBlockPos)) {
                    if (face == UP) {
                        torch_state = Blocks.TORCH.defaultBlockState();
                        level.setBlock(setBlockPos,torch_state,3);
                        this.remove(RemovalReason.KILLED);
                    } else if (face == DOWN && CeilingTorch != null) {
                        BlockState ceiling_torch = CeilingTorch.defaultBlockState();
                        level.setBlock(setBlockPos, ceiling_torch,3);
                        this.remove(RemovalReason.KILLED);
                    } else if (face != DOWN) {
                        level.setBlock(setBlockPos, torch_state.setValue(HORIZONTAL_FACING, face), 3);
                        this.remove(RemovalReason.KILLED);
                    }
                }
            }
        }
    }

    private BlockPos getPosOfFace(BlockPos blockPos, Direction face) {
        switch (face) {
            case UP:
                return blockPos.above();
            case EAST:
                return blockPos.east();
            case WEST:
                return blockPos.west();
            case SOUTH:
                return blockPos.south();
            case NORTH:
                return blockPos.north();
            case DOWN:
                return blockPos.below();
        }
        return blockPos;
    }

    private boolean isBlockAIR(BlockPos pos) {
        Block getBlock = this.level.getBlockState(pos).getBlock();
        if (getBlock instanceof BushBlock) return true;
        Block[] a = {Blocks.CAVE_AIR, Blocks.AIR, Blocks.SNOW, Blocks.VINE};//空気だとみなすブロックリスト
        for (Block traget : a) {
            if (getBlock == traget) return true;
        }
        return false;
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Blocks.TORCH);
    }
}