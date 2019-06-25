package mod.torchbowmod;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.*;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.function.Predicate;

import static mod.torchbowmod.TorchBowMod.EMERALD_ARROW;
import static mod.torchbowmod.TorchBowMod.multiTorch;
import static net.minecraft.item.BowItem.getArrowVelocity;

public class TorchBow extends ShootableItem {
    public static final Predicate<ItemStack> TORCH = (p_220002_0_) -> {
        return p_220002_0_.getItem() == Blocks.TORCH.asItem() || p_220002_0_.getItem() == multiTorch;
    };

    public TorchBow(Properties properties) {
        super(properties);
        this.addPropertyOverride(new ResourceLocation("pull"), (stack, world, entity) -> {
            if (entity == null) {
                return 0.0F;
            } else {
                return entity.getActiveItemStack().getItem() != Items.BOW ? 0.0F : (float) (stack.getUseDuration() - entity.getItemInUseCount()) / 20.0F;
            }
        });
        this.addPropertyOverride(new ResourceLocation("pulling"), (stack, worldIn, entityIn) -> {
            return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
        });
    }

    @Override
    public Predicate<ItemStack> getInventoryAmmoPredicate() {
        return TORCH;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof PlayerEntity) {
            PlayerEntity playerentity = (PlayerEntity) entityLiving;
            boolean flag = playerentity.abilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
            ItemStack itemstack = playerentity.func_213356_f(stack);

            int i = this.getUseDuration(stack) - timeLeft;
            i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, worldIn, playerentity, i, !itemstack.isEmpty() || flag);
            if (i < 0) return;

            if (!itemstack.isEmpty() || flag) {
                if (itemstack.isEmpty()) {
                    itemstack = new ItemStack(Blocks.TORCH);
                }

                float f = getArrowVelocity(i);
                if (!((double) f < 0.1D)) {
                    boolean flag1 = playerentity.abilities.isCreativeMode || (itemstack.getItem() instanceof ArrowItem && ((ArrowItem) itemstack.getItem()).isInfinite(itemstack, stack, playerentity));
                    if (!worldIn.isRemote) {
                        float size = 10;
                        EntityTorch abstractedly = new EntityTorch(EMERALD_ARROW, entityLiving, worldIn);
                        abstractedly.shoot(playerentity, playerentity.rotationPitch, playerentity.rotationYaw, 0.0F, f * 3.0F, 1.0F);
                        if (itemstack.getItem() == multiTorch) {
                            abstractedly.shoot(playerentity, playerentity.rotationPitch - size, playerentity.rotationYaw + size, 0.0F, f * 3.0F, 1.0F);
                            abstractedly.shoot(playerentity, playerentity.rotationPitch - size, playerentity.rotationYaw, 0.0F, f * 3.0F, 1.0F);
                            abstractedly.shoot(playerentity, playerentity.rotationPitch - size, playerentity.rotationYaw - size, 0.0F, f * 3.0F, 1.0F);
                            abstractedly.shoot(playerentity, playerentity.rotationPitch + size, playerentity.rotationYaw + size, 0.0F, f * 3.0F, 1.0F);
                            abstractedly.shoot(playerentity, playerentity.rotationPitch + size, playerentity.rotationYaw, 0.0F, f * 3.0F, 1.0F);
                            abstractedly.shoot(playerentity, playerentity.rotationPitch + size, playerentity.rotationYaw - size, 0.0F, f * 3.0F, 1.0F);
                            abstractedly.shoot(playerentity, playerentity.rotationPitch, playerentity.rotationYaw + size, 0.0F, f * 3.0F, 1.0F);
                            abstractedly.shoot(playerentity, playerentity.rotationPitch, playerentity.rotationYaw - size, 0.0F, f * 3.0F, 1.0F);
                        }
                        if (f == 1.0F) {
                            abstractedly.setIsCritical(true);
                        }

                        int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
                        if (j > 0) {
                            abstractedly.setDamage(abstractedly.getDamage() + (double) j * 0.5D + 0.5D);
                        }

                        int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);
                        if (k > 0) {
                            abstractedly.setKnockbackStrength(k);
                        }

                        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0) {
                            abstractedly.setFire(100);
                        }

                        stack.damageItem(1, playerentity, (p_220009_1_) -> {
                            p_220009_1_.sendBreakAnimation(playerentity.getActiveHand());
                        });
                        if (flag1 || playerentity.abilities.isCreativeMode && (itemstack.getItem() == Blocks.TORCH.asItem())) {
                            abstractedly.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                        }
                        worldIn.addEntity(abstractedly);
                    }

                    worldIn.playSound((PlayerEntity) entityLiving, playerentity.posX, playerentity.posY, playerentity.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                    if (!flag1 && !playerentity.abilities.isCreativeMode) {
                        itemstack.shrink(1);
                        if (itemstack.isEmpty()) {
                            playerentity.inventory.deleteStack(itemstack);
                        }
                    }

                    playerentity.addStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        boolean flag = !playerIn.func_213356_f(itemstack).isEmpty();

        ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, worldIn, playerIn, handIn, flag);
        if (ret != null) return ret;

        if (!playerIn.abilities.isCreativeMode && !flag) {
            return flag ? new ActionResult<>(ActionResultType.PASS, itemstack) : new ActionResult<>(ActionResultType.FAIL, itemstack);
        } else {
            playerIn.setActiveHand(handIn);
            return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
        }
    }
}
