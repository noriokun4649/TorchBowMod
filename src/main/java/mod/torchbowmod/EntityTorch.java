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
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

import static mod.torchbowmod.TorchBowMod.TORCH_ENTITY;
import static net.minecraft.block.HorizontalBlock.HORIZONTAL_FACING;
import static net.minecraft.util.Direction.*;

public class EntityTorch extends AbstractArrowEntity {
    @Nullable
    private BlockState inBlockState;
    private double damage;

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
    protected void func_213868_a(EntityRayTraceResult p_213868_1_) {
        super.func_213868_a(p_213868_1_);
        Entity entity = p_213868_1_.getEntity();
        //String type = EntityList.getEntityString(entity);//文字に変換
        //if (type != null && !type.equals("lmmx.LittleMaidX")) {//リトルメイド以外だったら
        entity.setFire(100);//火を付ける
        //}
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    /**
     * Called when the arrow hits a block or an entity
     */
    @Override
    protected void onHit(RayTraceResult raytraceResultIn) {
        super.onHit(raytraceResultIn);
        RayTraceResult.Type raytraceresult$type = raytraceResultIn.getType();
        if (raytraceresult$type == RayTraceResult.Type.ENTITY) {
            this.func_213868_a((EntityRayTraceResult) raytraceResultIn);
        } else if (raytraceresult$type == RayTraceResult.Type.BLOCK) {
            BlockRayTraceResult blockraytraceresult = (BlockRayTraceResult) raytraceResultIn;
            BlockState blockstate = this.world.getBlockState(blockraytraceresult.getPos());
            this.inBlockState = blockstate;
            Vec3d vec3d = blockraytraceresult.getHitVec().subtract(this.posX, this.posY, this.posZ);
            this.setMotion(vec3d);
            Vec3d vec3d1 = vec3d.normalize().scale((double) 0.05F);
            this.posX -= vec3d1.x;
            this.posY -= vec3d1.y;
            this.posZ -= vec3d1.z;
            this.playSound(this.getHitGroundSound(), 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
            this.inGround = true;
            this.arrowShake = 7;
            this.setIsCritical(false);
            this.func_213872_b((byte) 0);
            this.setHitSound(SoundEvents.ENTITY_ARROW_HIT);
            this.func_213865_o(false);
            //this.func_213870_w();
            blockstate.onProjectileCollision(this.world, blockstate, blockraytraceresult, this);
            BlockPos blockpos = blockraytraceresult.getPos();
            if (!blockstate.isAir(this.world, blockpos)) {
                this.inBlockState.onEntityCollision(this.world, blockpos, this);
                if (!world.isRemote) {
                    int x = blockpos.getX();
                    int y = blockpos.getY();
                    int z = blockpos.getZ();
                    World world = this.world;
                    BlockState torch_state = Blocks.TORCH.getDefaultState();
                    BlockPos up_pos = new BlockPos(x, y + 1, z);
                    if (isBlockAIR(up_pos)) {
                        world.setBlockState(up_pos, torch_state);
                        this.setDead();
                    } else {
                        torch_state = Blocks.WALL_TORCH.getDefaultState();
                        switch (this.getHorizontalFacing()) {
                            case EAST:
                                BlockPos east_pos = new BlockPos(x + 1, y, z);
                                if (isBlockAIR(east_pos)) {
                                    world.setBlockState(east_pos, torch_state.with(HORIZONTAL_FACING, EAST));
                                    this.setDead();
                                    break;
                                }
                            case WEST:
                                BlockPos west_pos = new BlockPos(x - 1, y, z);
                                if (isBlockAIR(west_pos)) {
                                    world.setBlockState(west_pos, torch_state.with(HORIZONTAL_FACING, WEST));
                                    this.setDead();
                                    break;
                                }
                            case NORTH:
                                BlockPos north_pos = new BlockPos(x, y, z + 1);
                                if (isBlockAIR(north_pos)) {
                                    world.setBlockState(north_pos, torch_state.with(HORIZONTAL_FACING, SOUTH));
                                    this.setDead();
                                    break;
                                }
                            case SOUTH:
                                BlockPos south_pos = new BlockPos(x, y, z - 1);
                                if (isBlockAIR(south_pos)) {
                                    world.setBlockState(south_pos, torch_state.with(HORIZONTAL_FACING, NORTH));
                                    this.setDead();
                                    break;
                                }
                        }
                    }
                }
            }
        }

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
        Block[] a = {Blocks.CAVE_AIR, Blocks.AIR, Blocks.SNOW,Blocks.VINE};//空気だとみなすブロックリスト
        for (Block traget : a) {
            if (getBlock == traget) return true;
        }
        return false;
    }

}