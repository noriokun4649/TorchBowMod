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
import net.minecraft.world.InteractionResultHolder;
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

    private static class Offsets {
        private final float X;
        private final float Y;

        Offsets(float x,float y){
            this.X = x;
            this.Y = y;
        }
    }

    static {
        TORCH_BOW_ONLY = TORCH.or(MULTI_TORCH).or(TORCH_ARROW);
    }

    public TorchBow(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i1) {
        if (livingEntity instanceof Player player) {
            ItemStack itemstack = player.getProjectile(itemStack);
            if (!itemstack.isEmpty()) {
                int i = this.getUseDuration(itemStack, livingEntity) - i1;
                i = ForgeEventFactory.onArrowLoose(itemStack, level, player, i, true);
                if (i < 0) {
                    return;
                }

                float f = getPowerForTime(i);
                if (!((double)f < 0.1)) {
                    List<ItemStack> list = draw(itemStack, itemstack, player);
                    if (level instanceof ServerLevel) {
                        ServerLevel serverlevel = (ServerLevel)level;
                        if (!list.isEmpty()) {
                            ItemStack pickup = list.getFirst();
                            if (pickup.is(multiTorch.get())){
                                ItemStack item = pickup.copy();
                                list.addAll(Collections.nCopies(8, item));
                            }
                            this.shoot(serverlevel, player, player.getUsedItemHand(), itemStack, list, f * 3.0F, 1.0F, f == 1.0F, (LivingEntity)null);
                        }
                    }

                    level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                    player.awardStat(Stats.ITEM_USED.get(this));
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
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        boolean flag = !player.getProjectile(itemstack).isEmpty();
        InteractionResultHolder<ItemStack> ret = ForgeEventFactory.onArrowNock(itemstack, level, player, interactionHand, flag);
        if (ret != null) {
            return ret;
        } else if (!player.hasInfiniteMaterials() && !flag) {
            return InteractionResultHolder.fail(itemstack);
        } else {
            player.startUsingItem(interactionHand);
            return InteractionResultHolder.consume(itemstack);
        }
    }

    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return TORCH_BOW_ONLY;
    }

    public int getDefaultProjectileRange() {
        return 15;
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack2.is(Items.FLINT_AND_STEEL) || super.isValidRepairItem(itemStack, itemStack2);
    }

    @Override
    protected Projectile createProjectile(Level worldIn, LivingEntity livingEntity, ItemStack weaponStack, ItemStack pickupItem, boolean b) {
        if (pickupItem.is(multiTorch.get())) pickupItem = Items.TORCH.getDefaultInstance();
        EntityTorch abstractedly = new EntityTorch(worldIn, livingEntity, pickupItem.copyWithCount(1), weaponStack);
        return abstractedly;
    }
}
