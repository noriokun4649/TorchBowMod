package mod.torchbowmod;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

import static mod.torchbowmod.TorchBowMod.multiTorch;
import static mod.torchbowmod.TorchBowMod.torchArrow;
import static net.minecraft.world.item.BowItem.getPowerForTime;

public class TorchBow extends ProjectileWeaponItem  {
    public static final Predicate<ItemStack> TORCH = itemStack -> itemStack.is(Blocks.TORCH.asItem());
    public static final Predicate<ItemStack> MULTI_TORCH = itemStack -> itemStack.is(multiTorch.get());
    public static final Predicate<ItemStack> TORCH_ARROW = itemStack -> itemStack.is(torchArrow.get());
    public static final Predicate<ItemStack> TORCH_BOW_ONLY;

    static {
        TORCH_BOW_ONLY = TORCH.or(MULTI_TORCH).or(TORCH_ARROW);
    }

    public TorchBow(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return TORCH_BOW_ONLY;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 0;
    }

    @Override
    protected void shootProjectile(LivingEntity p_330864_, Projectile p_328720_, int p_328740_, float p_335337_, float p_332934_, float p_329948_, @Nullable LivingEntity p_329516_) {

    }

    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack2.is(Items.FLINT_AND_STEEL) || super.isValidRepairItem(itemStack, itemStack2);
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player playerentity) {
            boolean flag = playerentity.getAbilities().invulnerable || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
            ItemStack itemstack = playerentity.getProjectile(stack);

            int i = this.getUseDuration(stack) - timeLeft;
            i = ForgeEventFactory.onArrowLoose(stack, worldIn, playerentity, i, !itemstack.isEmpty() || flag);
            if (i < 0) return;

            if (!itemstack.isEmpty() || flag) {
                if (itemstack.isEmpty()) {
                    itemstack = new ItemStack(Blocks.TORCH);
                }

                float f = getPowerForTime(i);
                if ((double) f >= 0.1D) {
                    boolean flag1 = playerentity.getAbilities().invulnerable || (itemstack.getItem() instanceof ArrowItem && ((ArrowItem) itemstack.getItem()).isInfinite(itemstack, stack, playerentity));
                    if (!worldIn.isClientSide) {
                        boolean isMultitorch = itemstack.getItem() == multiTorch.get();
                        shootTorch(playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                        if (isMultitorch){
                            float size = 10f;
                            shootTorch(playerentity, entityLiving, worldIn, itemstack, stack, flag1, f, -size,size );
                            shootTorch(playerentity, entityLiving, worldIn, itemstack, stack, flag1, f, -size,0f );
                            shootTorch(playerentity, entityLiving, worldIn, itemstack, stack, flag1, f, -size,-size );
                            shootTorch(playerentity, entityLiving, worldIn, itemstack, stack, flag1, f, size,size );
                            shootTorch(playerentity, entityLiving, worldIn, itemstack, stack, flag1, f, size,0f );
                            shootTorch(playerentity, entityLiving, worldIn, itemstack, stack, flag1, f, size,-size );
                            shootTorch(playerentity, entityLiving, worldIn, itemstack, stack, flag1, f, 0f,size );
                            shootTorch(playerentity, entityLiving, worldIn, itemstack, stack, flag1, f, 0f,-size );
                        }
                    }

                    worldIn.playSound((Player) entityLiving, playerentity.getX(), playerentity.getY(), playerentity.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (worldIn.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                    if (!playerentity.getAbilities().invulnerable) {
                        if (itemstack.getItem() == Blocks.TORCH.asItem() || itemstack.getItem() == multiTorch.get()
                                || itemstack.getItem() == torchArrow.get()) {
                            itemstack.shrink(1);
                            if (itemstack.isEmpty()) {
                                playerentity.getInventory().removeItem(itemstack);
                            }
                        }
                    }

                    playerentity.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    private void shootTorch(Player entitle, LivingEntity livingEntity, Level worldIn, ItemStack itemstack, ItemStack stack, boolean flag1, float f) {
        shootTorch(entitle, livingEntity, worldIn, itemstack, stack, flag1, f, 0f, 0f);
    }

    private void shootTorch(Player entitle, LivingEntity livingEntity, Level worldIn, ItemStack itemstack, ItemStack stack, boolean flag1, float f, float x, float y) {

        EntityTorch abstractedly = new EntityTorch(worldIn, livingEntity, itemstack.copyWithCount(1));
        abstractedly.shootFromRotation(entitle, entitle.getXRot() + x, entitle.getYRot() + y, 0.0F, f * 3.0F, 1.0F);
        if (f == 1.0F) {
            abstractedly.setCritArrow(true);
        }

        int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER, stack);
        if (j > 0) {
            abstractedly.setBaseDamage(abstractedly.getBaseDamage() + (double) j * 0.5D + 0.5D);
        }

        int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH, stack);
        if (k > 0) {
            abstractedly.setKnockback(k);
        }

        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAME, stack) > 0) {
            abstractedly.igniteForSeconds(100);
        }

        stack.hurtAndBreak(1, entitle, LivingEntity.getSlotForHand(entitle.getUsedItemHand()));
        if (flag1 || entitle.getAbilities().instabuild && (itemstack.getItem() == Blocks.TORCH.asItem())) {
            abstractedly.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        }
        worldIn.addFreshEntity(abstractedly);
    }

    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.BOW;
    }


    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        boolean flag = !playerIn.getProjectile(itemstack).isEmpty();

        InteractionResultHolder<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, worldIn, playerIn, handIn, flag);
        if (ret != null) return ret;

        if (!playerIn.getAbilities().instabuild && !flag) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, itemstack);
        } else {
            playerIn.startUsingItem(handIn);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
        }
    }

}
