package mod.torchbowmod;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potions;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static mod.torchbowmod.TorchBowMod.EMERALD_ARROW;
import static net.minecraft.block.HorizontalBlock.HORIZONTAL_FACING;
import static net.minecraft.util.Direction.*;

public class EntityTorch extends AbstractArrowEntity {
    private int xTile;
    private int yTile;
    private int zTile;
    @Nullable
    private BlockState inBlockState;
    private double damage;

    public EntityTorch(EntityType<? extends AbstractArrowEntity> type, World worldIn) {
        super(type, worldIn);
        this.xTile = -1;
        this.yTile = -1;
        this.zTile = -1;
        this.pickupStatus = EntityTorch.PickupStatus.DISALLOWED;
        this.damage = 2.0D;
        //this.setSize(0.5F, 0.5F);
    }

    public EntityTorch(EntityType<? extends AbstractArrowEntity> type, double x, double y, double z, World worldIn) {
        this(type, worldIn);
        this.setPosition(x, y, z);
    }

    public EntityTorch(EntityType<? extends AbstractArrowEntity> type, LivingEntity shooter, World worldIn) {
        this(type, shooter.posX, shooter.posY + (double) shooter.getEyeHeight() - 0.10000000149011612D, shooter.posZ, worldIn);
        this.setShooter(shooter);

        if (shooter instanceof PlayerEntity) {
            this.pickupStatus = EntityTorch.PickupStatus.ALLOWED;
        }
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

    /**
     * Called when the arrow hits a block or an entity
     */
    @Override
    protected void onHit(RayTraceResult raytraceResultIn) {
        super.onHit(raytraceResultIn);
        RayTraceResult.Type raytraceresult$type = raytraceResultIn.getType();
        if (raytraceresult$type == RayTraceResult.Type.ENTITY) {
            this.func_213868_a((EntityRayTraceResult)raytraceResultIn);
        } else if (raytraceresult$type == RayTraceResult.Type.BLOCK) {
            BlockRayTraceResult blockraytraceresult = (BlockRayTraceResult)raytraceResultIn;
            BlockState blockstate = this.world.getBlockState(blockraytraceresult.getPos());
            this.inBlockState = blockstate;
            Vec3d vec3d = blockraytraceresult.getHitVec().subtract(this.posX, this.posY, this.posZ);
            this.setMotion(vec3d);
            Vec3d vec3d1 = vec3d.normalize().scale((double)0.05F);
            this.posX -= vec3d1.x;
            this.posY -= vec3d1.y;
            this.posZ -= vec3d1.z;
            this.playSound(this.getHitGroundSound(), 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
            this.inGround = true;
            this.arrowShake = 7;
            this.setIsCritical(false);
            this.func_213872_b((byte)0);
            this.func_213869_a(SoundEvents.ENTITY_ARROW_HIT);
            this.func_213865_o(false);
            //this.func_213870_w();
            blockstate.onProjectileCollision(this.world, blockstate, blockraytraceresult, this);
            BlockPos blockpos = blockraytraceresult.getPos();
            if (!blockstate.isAir(this.world, blockpos)) {
                this.inBlockState.onEntityCollision(this.world, blockpos, this);
                if (!world.isRemote) {
                    int x = this.xTile;
                    int y = this.yTile;
                    int z = this.zTile;
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
        Block[] a = {Blocks.AIR, Blocks.SNOW};//空気だとみなすブロックリスト
        for (Block traget : a) {
            if (getBlock == traget) return true;
        }
        return false;
    }

}