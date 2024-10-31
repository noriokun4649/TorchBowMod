package mod.torchbowmod;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.ForgeEventFactory;

import static mod.torchbowmod.TorchBowMod.multiTorch;
import static mod.torchbowmod.TorchBowMod.torchArrow;

public class TorchBow extends ProjectileWeaponItem {
    public static final int MAX_DRAW_DURATION = 20;
    public static final int DEFAULT_RANGE = 15;

    public static final Predicate<ItemStack> TORCH = itemStack -> itemStack.is(Blocks.TORCH.asItem());
    public static final Predicate<ItemStack> MULTI_TORCH = itemStack -> itemStack.is(multiTorch.get());
    public static final Predicate<ItemStack> TORCH_ARROW = itemStack -> itemStack.is(torchArrow.get());
    public static final Predicate<ItemStack> TORCH_BOW_ONLY;

    private class Offsets {
        private float X;
        private float Y;

        Offsets(float x,float y){
            this.X = x;
            this.Y = y;
        }

        public float getX() {
            return X;
        }

        public float getY() {
            return Y;
        }
    }

    static {
        TORCH_BOW_ONLY = TORCH.or(MULTI_TORCH).or(TORCH_ARROW);
    }

    public TorchBow(Item.Properties p_40660_) {
        super(p_40660_);
    }

    @Override
    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i1) {
        if (!(livingEntity instanceof Player player)) {
            return false;
        } else {
            ItemStack itemstack = player.getProjectile(itemStack);
            if (itemstack.isEmpty()) {
                return false;
            } else {
                int i = this.getUseDuration(itemStack, livingEntity) - i1;
                i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(itemStack, level, player, i, true);
                if (i < 0) return false;

                float f = getPowerForTime(i);
                if ((double)f < 0.1) {
                    return false;
                } else {
                    List<ItemStack> list = draw(itemStack, itemstack, player);
                    if (level instanceof ServerLevel serverlevel && !list.isEmpty()) {
                        if (list.getFirst().is(multiTorch.get())){
                            ItemStack item = list.getFirst().copy();
                            list.addAll(Collections.nCopies(8, item));
                        }
                        this.shoot(serverlevel, player, player.getUsedItemHand(), itemStack, list, f * 3.0F, 1.0F, f == 1.0F, null);
                    }

                    level.playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            SoundEvents.ARROW_SHOOT,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F
                    );
                    player.awardStat(Stats.ITEM_USED.get(this));
                    return true;
                }
            }
        }
    }

    @Override
    protected void shootProjectile(LivingEntity livingEntity, Projectile projectile, int i, float v, float v1, float v2, @Nullable LivingEntity livingEntity1) {
        float offsetX = 0F;
        float offsetY = 0F;
        if (i < 9){
            float range = 10F;
            Offsets[] offsets = {
                new Offsets(0F,0F),
                new Offsets(-range, -range),
                new Offsets(-range, 0.0F),
                new Offsets(-range, range),
                new Offsets(0.0F, -range),
                new Offsets(0.0F, range),
                new Offsets(range, -range),
                new Offsets(range, 0.0F),
                new Offsets(range, range)
            };
            offsetX = offsets[i].X;
            offsetY = offsets[i].Y;
        }
        projectile.shootFromRotation(livingEntity, livingEntity.getXRot() + offsetX, livingEntity.getYRot() + offsetY + v2, 0.0F, v, v1);
    }

    public static float getPowerForTime(int i) {
        float f = (float)i / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        boolean flag = !player.getProjectile(itemstack).isEmpty();
        var ret = ForgeEventFactory.onArrowNock(itemstack, level, player, interactionHand, flag);
        if (ret != null) {
            return ret;
        } else if (!player.hasInfiniteMaterials() && !flag) {
            return InteractionResult.FAIL;
        } else {
            player.startUsingItem(interactionHand);
            return InteractionResult.CONSUME;
        }
    }

    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return TORCH_BOW_ONLY;
    }

    public int getDefaultProjectileRange() {
        return 15;
    }

    @Override
    protected Projectile createProjectile(Level worldIn, LivingEntity livingEntity, ItemStack weaponStack, ItemStack pickupItem, boolean p_336242_) {
        if (pickupItem.is(multiTorch.get())) pickupItem = Items.TORCH.getDefaultInstance();
        EntityTorch abstractedly = new EntityTorch(worldIn, livingEntity, pickupItem.copyWithCount(1), weaponStack);
        return abstractedly;
    }
}
