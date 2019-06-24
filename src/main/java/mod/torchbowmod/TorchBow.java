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
import net.minecraftforge.event.ForgeEventFactory;

import static mod.torchbowmod.TorchBowMod.EMERALD_ARROW;
import static mod.torchbowmod.TorchBowMod.multiTorch;

public class TorchBow extends Item {

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

    private ItemStack findAmmo(PlayerEntity player) {
        if (this.isArrow(player.getHeldItem(Hand.OFF_HAND))) {
            return player.getHeldItem(Hand.OFF_HAND);
        } else if (this.isArrow(player.getHeldItem(Hand.MAIN_HAND))) {
            return player.getHeldItem(Hand.MAIN_HAND);
        } else {
            for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                ItemStack itemstack = player.inventory.getStackInSlot(i);
                if (this.isArrow(itemstack)) {
                    return itemstack;
                }
            }

            return ItemStack.EMPTY;
        }
    }

    protected boolean isArrow(ItemStack stack) {
        return stack.getItem() == Blocks.TORCH.asItem() || stack.getItem() == multiTorch;
    }

    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof PlayerEntity) {
            PlayerEntity entityplayer = (PlayerEntity) entityLiving;
            boolean flag = entityplayer.abilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
            ItemStack itemstack = findAmmo(entityplayer);
            int i = this.getUseDuration(stack) - timeLeft;
            i = ForgeEventFactory.onArrowLoose(stack, worldIn, entityplayer, i, !itemstack.isEmpty() || flag);
            if (i < 0) {
                return;
            }

            if (!itemstack.isEmpty() || flag) {
                if (itemstack.isEmpty()) {
                    itemstack = new ItemStack(Blocks.TORCH);
                }

                float f = getArrowVelocity(i);
                if ((double) f >= 0.1D) {
                    boolean flag1 = entityplayer.abilities.isCreativeMode || itemstack.getItem() instanceof ArrowItem && ((ArrowItem) itemstack.getItem()).isInfinite(itemstack, stack, entityplayer);
                    if (!worldIn.isRemote) {
                        float size = 10;
                        shootTorch(entityplayer.rotationPitch, entityplayer.rotationYaw, entityplayer, worldIn, itemstack, stack, flag1, f);
                        if (itemstack.getItem() == multiTorch) {
                            shootTorch(entityplayer.rotationPitch - size, entityplayer.rotationYaw + size, entityplayer, worldIn, itemstack, stack, flag1, f);
                            shootTorch(entityplayer.rotationPitch - size, entityplayer.rotationYaw, entityplayer, worldIn, itemstack, stack, flag1, f);
                            shootTorch(entityplayer.rotationPitch - size, entityplayer.rotationYaw - size, entityplayer, worldIn, itemstack, stack, flag1, f);
                            shootTorch(entityplayer.rotationPitch + size, entityplayer.rotationYaw + size, entityplayer, worldIn, itemstack, stack, flag1, f);
                            shootTorch(entityplayer.rotationPitch + size, entityplayer.rotationYaw, entityplayer, worldIn, itemstack, stack, flag1, f);
                            shootTorch(entityplayer.rotationPitch + size, entityplayer.rotationYaw - size, entityplayer, worldIn, itemstack, stack, flag1, f);
                            shootTorch(entityplayer.rotationPitch, entityplayer.rotationYaw + size, entityplayer, worldIn, itemstack, stack, flag1, f);
                            shootTorch(entityplayer.rotationPitch, entityplayer.rotationYaw - size, entityplayer, worldIn, itemstack, stack, flag1, f);
                        }
                    }

                    worldIn.playSound((PlayerEntity) null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                    if (!flag1 && !entityplayer.abilities.isCreativeMode) {
                        itemstack.shrink(1);
                        if (itemstack.isEmpty()) {
                            entityplayer.inventory.deleteStack(itemstack);
                        }
                    }

                    entityplayer.addStat(Stats.ITEM_USED.get(this));
                }
            }
        }

    }

    private void shootTorch(float offsetPitch, float offsetYaw, PlayerEntity entityplayer, World worldIn, ItemStack itemstack, ItemStack stack, boolean flag1, float f) {
        EntityTorch entityarrow = new EntityTorch(EMERALD_ARROW, entityplayer, worldIn);
        entityarrow.shoot(entityplayer, offsetPitch, offsetYaw, 0.0F, f * 3.0F, 1.0F);

        if (f == 1.0F) {
            entityarrow.setIsCritical(true);
        }

        int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
        if (j > 0) {
            entityarrow.setDamage(entityarrow.getDamage() + (double) j * 0.5D + 0.5D);
        }

        int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);
        if (k > 0) {
            entityarrow.setKnockbackStrength(k);
        }

        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0) {
            entityarrow.setFire(100);
        }

        stack.damageItem(1, entityplayer, (p_220009_1_) -> {
            p_220009_1_.sendBreakAnimation(entityplayer.getActiveHand());
        });
        if (flag1 || entityplayer.abilities.isCreativeMode && (itemstack.getItem() == Blocks.TORCH.asItem())) {
            entityarrow.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
        }

        worldIn.addEntity(entityarrow);
    }

    public static float getArrowVelocity(int charge) {
        float f = (float) charge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
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
