package mod.torchbowmod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import static mod.torchbowmod.TorchBowMod.CeilingTorch;
import static mod.torchbowmod.TorchBowMod.TORCH_ENTITY;
import static net.minecraft.block.HorizontalBlock.HORIZONTAL_FACING;
import static net.minecraft.util.Direction.DOWN;
import static net.minecraft.util.Direction.UP;

public class EntityTorch extends AbstractArrowEntity {

    public EntityTorch(FMLPlayMessages.SpawnEntity packet, World worldIn﻿) {
        super(TORCH_ENTITY, worldIn﻿);
    }

    public EntityTorch(EntityType<? extends EntityTorch> p_i50172_1_, World p_i50172_2_) {
        super(p_i50172_1_, p_i50172_2_);
    }

    public EntityTorch(World worldIn, double x, double y, double z) {
        super(TORCH_ENTITY, x, y, z, worldIn);
    }

    public EntityTorch(World worldIn, LivingEntity shooter) {
        super(TORCH_ENTITY, shooter, worldIn);
    }

    @Override
    protected void onEntityHit(EntityRayTraceResult entityRayTraceResult) {
        super.onEntityHit(entityRayTraceResult);
        Entity entity = entityRayTraceResult.getEntity();
        entity.setFire(100);
    }

    @Override
    protected void func_230299_a_(BlockRayTraceResult raytraceResultIn) {
        super.func_230299_a_(raytraceResultIn);
        RayTraceResult.Type raytraceresult$type = raytraceResultIn.getType();
        if (raytraceresult$type == RayTraceResult.Type.BLOCK) {
            BlockRayTraceResult blockraytraceresult = raytraceResultIn;
            BlockState blockstate = this.world.getBlockState(blockraytraceresult.getPos());
            setTorch(blockraytraceresult, blockstate, raytraceResultIn);
        }
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void setTorch(BlockRayTraceResult blockraytraceresult, BlockState blockstate, RayTraceResult raytraceResultIn) {
        BlockPos blockpos = blockraytraceresult.getPos();
        if (!blockstate.isAir(this.world, blockpos)) {
            if (!world.isRemote) {
                Direction face = ((BlockRayTraceResult) raytraceResultIn).getFace();
                BlockState torch_state = Blocks.WALL_TORCH.getDefaultState();
                BlockPos setBlockPos = getPosOfFace(blockpos, face);
                if (isBlockAIR(setBlockPos)) {
                    if (face == UP) {
                        torch_state = Blocks.TORCH.getDefaultState();
                        world.setBlockState(setBlockPos, torch_state);
                        this.setDead();
                    } else if (face == DOWN && CeilingTorch != null) {
                        BlockState ceiling_torch = CeilingTorch.getDefaultState();
                        world.setBlockState(setBlockPos, ceiling_torch);
                        this.setDead();
                    } else if (face != DOWN) {
                        world.setBlockState(setBlockPos, torch_state.with(HORIZONTAL_FACING, face));
                        this.setDead();
                    }
                }
            }
        }
    }

    private BlockPos getPosOfFace(BlockPos blockPos, Direction face) {
        switch (face) {
            case UP:
                return blockPos.up();
            case EAST:
                return blockPos.east();
            case WEST:
                return blockPos.west();
            case SOUTH:
                return blockPos.south();
            case NORTH:
                return blockPos.north();
            case DOWN:
                return blockPos.down();
        }
        return blockPos;
    }

    private void setDead() {
        this.remove();
    }

    @Override
    protected ItemStack getArrowStack() {
        return new ItemStack(Blocks.TORCH);
    }

    private boolean isBlockAIR(BlockPos pos) {
        Block getBlock = this.world.getBlockState(pos).getBlock();
        if (getBlock instanceof BushBlock) return true;
        Block[] a = {Blocks.CAVE_AIR, Blocks.AIR, Blocks.SNOW, Blocks.VINE};//空気だとみなすブロックリスト
        for (Block traget : a) {
            if (getBlock == traget) return true;
        }
        return false;
    }

}