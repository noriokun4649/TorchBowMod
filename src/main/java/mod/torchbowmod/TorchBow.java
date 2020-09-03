package mod.torchbowmod;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Vanishable;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.function.Predicate;

import static mod.torchbowmod.TorchBowMod.MULCH_TORCH_ITEM;


public class TorchBow extends BowItem implements Vanishable {

    public static final Predicate<ItemStack> TORCH = (itemStack) -> itemStack.getItem() == Blocks.TORCH.asItem() ||
            itemStack.getItem() == MULCH_TORCH_ITEM;

    public TorchBow(Settings settings) {
        super(settings);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity) user;
            boolean bl = playerEntity.abilities.creativeMode || EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0;
            ItemStack itemStack = playerEntity.getArrowType(stack);
            if (!itemStack.isEmpty() || bl) {
                if (itemStack.isEmpty()) {
                    itemStack = new ItemStack(Blocks.TORCH);
                }

                int i = this.getMaxUseTime(stack) - remainingUseTicks;
                float f = getPullProgress(i);
                if ((double) f >= 0.1D) {
                    boolean bl2 = bl && itemStack.getItem() == Items.ARROW;
                    if (!world.isClient) {
                        int size = 10;
                        shootTorch(playerEntity, user, world, itemStack, stack, bl2, f);
                        if (itemStack.getItem() == MULCH_TORCH_ITEM) {
                            shootTorch(-size, size, playerEntity, user, world, itemStack, stack, bl2, f);
                            shootTorch(-size, 0, playerEntity, user, world, itemStack, stack, bl2, f);
                            shootTorch(-size, -size, playerEntity, user, world, itemStack, stack, bl2, f);
                            shootTorch(size, size, playerEntity, user, world, itemStack, stack, bl2, f);
                            shootTorch(size, 0, playerEntity, user, world, itemStack, stack, bl2, f);
                            shootTorch(size, -size, playerEntity, user, world, itemStack, stack, bl2, f);
                            shootTorch(0, size, playerEntity, user, world, itemStack, stack, bl2, f);
                            shootTorch(0, -size, playerEntity, user, world, itemStack, stack, bl2, f);
                        }
                    }

                    world.playSound((PlayerEntity) null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (RANDOM.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                    if (!bl2 && !playerEntity.abilities.creativeMode) {
                        itemStack.decrement(1);
                        if (itemStack.isEmpty()) {
                            playerEntity.inventory.removeOne(itemStack);
                        }
                    }

                    playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
                }
            }
        }
    }

    private void shootTorch(PlayerEntity entitle, LivingEntity livingEntity, World worldIn, ItemStack itemstack, ItemStack stack, boolean flag1, float f) {
        shootTorch(0, 0, entitle, livingEntity, worldIn, itemstack, stack, flag1, f);
    }

    private void shootTorch(int offsetX, int offsetY, PlayerEntity entitle, LivingEntity livingEntity, World worldIn, ItemStack itemstack, ItemStack stack, boolean flag1, float f) {
        TorchEntity abstractedly = new TorchEntity(worldIn, livingEntity);
        abstractedly.setProperties(entitle, entitle.pitch + offsetX, entitle.yaw + offsetY, 0F, f * 3.0F, 1.0F);
        if (f == 1.0F) {
            abstractedly.setCritical(true);
        }

        int j = EnchantmentHelper.getLevel(Enchantments.POWER, stack);
        if (j > 0) {
            abstractedly.setDamage(abstractedly.getDamage() + (double) j * 0.5D + 0.5D);
        }

        int k = EnchantmentHelper.getLevel(Enchantments.PUNCH, stack);
        if (k > 0) {
            abstractedly.setPunch(k);
        }

        if (EnchantmentHelper.getLevel(Enchantments.FLAME, stack) > 0) {
            abstractedly.setOnFireFor(100);
        }

        stack.damage(1, entitle, (p_220009_1_) -> {
            p_220009_1_.sendToolBreakStatus(entitle.getActiveHand());
        });
        if (flag1 || entitle.abilities.creativeMode && (itemstack.getItem() == Blocks.TORCH.asItem())) {
            abstractedly.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
        }
        worldIn.spawnEntity(abstractedly);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return ingredient.getItem() == Items.FLINT_AND_STEEL || super.canRepair(stack, ingredient);
    }

    @Override
    public Predicate<ItemStack> getProjectiles() {
        return TORCH;
    }

    @Override
    public int getRange() {
        return 15;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

}
