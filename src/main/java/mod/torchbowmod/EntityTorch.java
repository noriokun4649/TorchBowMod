package mod.torchbowmod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static mod.torchbowmod.TorchBowMod.EMERALD_ARROW;
import static net.minecraft.block.BlockTorchWall.HORIZONTAL_FACING;
import static net.minecraft.util.EnumFacing.*;

public class EntityTorch extends EntityArrow {
    private int xTile;
    private int yTile;
    private int zTile;
    @Nullable
    private IBlockState inBlockState;
    private double damage;

    public EntityTorch(EntityType<?> type, World worldIn) {
        super(type, worldIn);
        this.xTile = -1;
        this.yTile = -1;
        this.zTile = -1;
        this.pickupStatus = EntityTorch.PickupStatus.DISALLOWED;
        this.damage = 2.0D;
        this.setSize(0.5F, 0.5F);
    }

    public EntityTorch(World world) {
        super(EMERALD_ARROW, world);
    }

    public EntityTorch(EntityType<?> type, double x, double y, double z, World worldIn) {
        this(type, worldIn);
        this.setPosition(x, y, z);
    }

    public EntityTorch(EntityType<?> type, EntityLivingBase shooter, World worldIn) {
        this(type, shooter.posX, shooter.posY + (double) shooter.getEyeHeight() - 0.10000000149011612D, shooter.posZ, worldIn);
        func_212361_a(shooter);

        if (shooter instanceof EntityPlayer) {
            this.pickupStatus = EntityTorch.PickupStatus.ALLOWED;
        }
    }

    @Override
    protected void onHitEntity(RayTraceResult p_203046_1_) {
        super.onHitEntity(p_203046_1_);
        Entity entity = p_203046_1_.entity;
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
        if (raytraceResultIn.entity == null) {
            BlockPos blockpos = raytraceResultIn.getBlockPos();
            this.xTile = blockpos.getX();
            this.yTile = blockpos.getY();
            this.zTile = blockpos.getZ();
            IBlockState iblockstate = this.world.getBlockState(blockpos);
            this.inBlockState = iblockstate;
            this.motionX = (double) ((float) (raytraceResultIn.hitVec.x - this.posX));
            this.motionY = (double) ((float) (raytraceResultIn.hitVec.y - this.posY));
            this.motionZ = (double) ((float) (raytraceResultIn.hitVec.z - this.posZ));
            float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ) * 20.0F;
            this.posX -= this.motionX / (double) f;
            this.posY -= this.motionY / (double) f;
            this.posZ -= this.motionZ / (double) f;
            this.playSound(this.getHitGroundSound(), 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
            this.inGround = true;
            this.arrowShake = 7;
            this.setIsCritical(false);

            if (iblockstate.getMaterial() != Material.AIR) {
                this.inBlockState.onEntityCollision(this.world, blockpos, this);
                if (!world.isRemote) {
                    int x = this.xTile;
                    int y = this.yTile;
                    int z = this.zTile;
                    World world = this.world;
                    IBlockState torch_state = Blocks.TORCH.getDefaultState();
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
        if (getBlock instanceof BlockBush) return true;
        Block[] a = {Blocks.CAVE_AIR, Blocks.AIR, Blocks.SNOW,Blocks.VINE};//空気だとみなすブロックリスト
        for (Block traget : a) {
            if (getBlock == traget) return true;
        }
        return false;
    }

}