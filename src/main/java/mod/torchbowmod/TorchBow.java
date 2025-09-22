package mod.torchbowmod;

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
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static mod.torchbowmod.TorchBowMod.*;

public class TorchBow extends ProjectileWeaponItem {
    public static final Set<BlockItem> TORCH_ITEMS = new HashSet<>();

    public static final Predicate<ItemStack> TORCH = itemStack -> itemStack.getItem() instanceof BlockItem bi && ITEM_TO_WALL_BLOCK.containsKey(bi);
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

    public TorchBow(Item.Properties p_40660_) {
        super(p_40660_);
    }

    @Override
    public boolean releaseUsing(@NotNull ItemStack itemStack,@NotNull  Level level,@NotNull  LivingEntity livingEntity, int i1) {
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
    protected void shootProjectile(@NotNull LivingEntity livingEntity,@NotNull  Projectile projectile, int i, float v, float v1, float v2, @Nullable LivingEntity livingEntity1) {
        float offsetX = 0F;
        float offsetY = 0F;
        if (i < 9){
            float range = 10F;
            Offsets[] offsets = {
                    new Offsets(0F, 0F),
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
    public int getUseDuration(@NotNull ItemStack itemStack, @NotNull LivingEntity livingEntity) {
        return 72000;
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(@NotNull ItemStack itemStack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand interactionHand) {
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

    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return TORCH_BOW_ONLY;
    }

    public int getDefaultProjectileRange() {
        return 15;
    }

    @Override
    protected @NotNull Projectile createProjectile(@NotNull Level worldIn, @NotNull LivingEntity livingEntity, @NotNull ItemStack weaponStack, ItemStack pickupItem, boolean p_336242_) {
        if (pickupItem.is(multiTorch.get())) pickupItem = Items.TORCH.getDefaultInstance();
        return new EntityTorch(worldIn, livingEntity, pickupItem.copyWithCount(1), weaponStack);
    }
}
