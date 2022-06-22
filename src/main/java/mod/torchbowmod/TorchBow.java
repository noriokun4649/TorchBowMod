package mod.torchbowmod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.function.Predicate;

import static mod.torchbowmod.TorchBowMod.multiTorch;
import static mod.torchbowmod.TorchBowMod.torchArrow;
import static net.minecraft.world.item.BowItem.getPowerForTime;

public class TorchBow extends ProjectileWeaponItem implements Vanishable {
    private ItemStack torchbinder;
    private ItemStack sitemstack;
    private boolean sitem;
    private boolean storageid;

    private boolean binder;
    public static final Predicate<ItemStack> TORCH = (itemStack) ->
            itemStack.getItem() == torchArrow.get() ||
            itemStack.getItem() == Blocks.TORCH.asItem() ||
            itemStack.getItem() == multiTorch.get() ||
            (itemStack.getItem() == TorchBowMod.torchbinder && itemStack.getOrCreateTagElement("TorchBandolier").getInt("Count") > 0) ||
            (itemStack.getItem() == TorchBowMod.StorageBox &&
                    (ItemStack.of(itemStack.getTag().getCompound("StorageItemData")).getItem() == Blocks.TORCH.asItem() ||
                            ItemStack.of(itemStack.getTag().getCompound("StorageItemData")).getItem() == multiTorch.get()));

    public TorchBow(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return TORCH;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 0;
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack2.getItem() == Items.FLINT_AND_STEEL || super.isValidRepairItem(itemStack, itemStack2);
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player playerentity) {
            boolean flag = playerentity.getAbilities().invulnerable || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
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
                        boolean isMultitorch = itemstack.getItem() == multiTorch.get() || (itemstack.getItem() == TorchBowMod.StorageBox && ItemStack.of(itemstack.getTag().getCompound("StorageItemData")).getItem() == multiTorch.get());
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
                        } else if (sitem) {//StorageBoxだった場合の処理
                            if (!worldIn.isClientSide) {
                                if (storageid) {
                                    int Size = sitemstack.getTag().getInt("StorageSize");//今のアイテムの数取得
                                    int retrun_size = --Size;
                                    if (retrun_size != 0) {
                                        sitemstack.getTag().putInt("StorageSize", retrun_size);//ストレージBoxの中のアイテムの数減少させる。
                                    } else {
                                        sitemstack.getTag().remove("StorageItemData");
                                    }
                                }
                            }
                        } else if (binder) {//TorchBandolierだった場合の処理
                            if (!worldIn.isClientSide) {
                                int Size = torchbinder.getOrCreateTagElement("TorchBandolier").getInt("Count");//今のアイテムの数取得
                                int retrun_size = --Size;
                                torchbinder.getOrCreateTagElement("TorchBandolier").putInt("Count", retrun_size);//TorchBandolierのアイテムの数減少させる。
                            }
                        }
                    }

                    playerentity.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    private void shootTorch( Player entitle, LivingEntity livingEntity, Level worldIn, ItemStack itemstack, ItemStack stack, boolean flag1, float f) {
        shootTorch(entitle,livingEntity,worldIn,itemstack,stack,flag1,f,0f,0f);
    }

        private void shootTorch( Player entitle, LivingEntity livingEntity, Level worldIn, ItemStack itemstack, ItemStack stack, boolean flag1, float f, float x,float y) {

        EntityTorch.EntityTorchMode entityTorchMode = itemstack.getItem() == torchArrow.get() ? EntityTorch.EntityTorchMode.ARROW_STATE : EntityTorch.EntityTorchMode.TORCH_STATE;
        EntityTorch abstractedly = new EntityTorch(worldIn, livingEntity, entityTorchMode);
        abstractedly.shootFromRotation(entitle, entitle.getXRot() + x, entitle.getYRot() + y, 0.0F, f * 3.0F, 1.0F);
        if (f == 1.0F) {
            abstractedly.setCritArrow(true);
        }

        int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (j > 0) {
            abstractedly.setBaseDamage(abstractedly.getBaseDamage() + (double) j * 0.5D + 0.5D);
        }

        int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (k > 0) {
            abstractedly.setKnockback(k);
        }

        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
            abstractedly.setSecondsOnFire(100);
        }

        stack.hurtAndBreak(1, entitle, (player) -> {
            player.broadcastBreakEvent(entitle.getUsedItemHand());
        });
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
        binder = getSilentsMod(playerIn);
        storageid = getStorageMod(playerIn);
        boolean flag = !playerIn.getProjectile(itemstack).isEmpty();

        InteractionResultHolder<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, worldIn, playerIn, handIn, flag);
        if (ret != null) return ret;

        if (!playerIn.getAbilities().instabuild && !flag && !binder && !storageid) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, itemstack);
        } else {
            playerIn.startUsingItem(handIn);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
        }
    }

    /***
     * アイテムからアイテムスタック取得。
     * @param player
     * @param item
     * @return　ItemStack
     */
    private ItemStack getStack(Player player, Item item) {
        for (int i = 0; i < player.getInventory().items.size(); ++i) {
            player.getInventory();
            if (player.getInventory().items.get(i).getItem() == item/*TorchBowMod.StorageBox*/) {
                ItemStack itemstack = player.getInventory().items.get(i);
                //アイテムスタックがからじゃなかったら
                if (itemstack.getTag() == null) {//NBTがNullだったら
                    itemstack.setTag(new CompoundTag());//新しい空のNBTを書き込む
                }
                return itemstack;
            }
        }
        return new ItemStack(Items.BONE);
    }

    /***
     *  Modのアイテムが有効かどうか、松明が切れてないかどうか
     *  StorageBoxMod用処理
     * @param player
     * @return Modお問い合わせ
     */
    private boolean getStorageMod(Player player) {
        sitemstack = getStack(player, TorchBowMod.StorageBox);//ItemStack取得
        boolean as = sitemstack.getItem() == TorchBowMod.StorageBox;//正しいかどうかチェック
        boolean storageid = false;//ストレージBoxに入ってるItemのIDチェック用の変数初期化　初期値：無効
        int ssize = 0;
        if (as) {//ただしかったら
            CompoundTag a = sitemstack.getTag().getCompound("StorageItemData");//StrageBoxに入ってるItemStackを取得
            Item itemname = ItemStack.of(a).getItem();//スロトレージBoxのなかのID取得
            Item itemid = new ItemStack(Blocks.TORCH).getItem();//対象のID取得
            Item itemid2 = new ItemStack(multiTorch.get()).getItem();
            sitem = itemname == itemid || itemname == itemid2;
            if (sitem) {//同じ場合
                ssize = sitemstack.getTag().getInt("StorageSize");//有効に
                storageid = ssize != 0;//無効に
            }
        }
        return storageid;
    }

    /**
     * Modのアイテムが有効かどうか、松明が切れてないかどうか
     * TorchBandolier用処理
     *
     * @param player 　プレイヤー
     * @return Modお問い合わせ
     */
    private boolean getSilentsMod(Player player) {
        torchbinder = getStack(player, TorchBowMod.torchbinder);//ItemStack取得
        boolean mitem = torchbinder.getItem() == TorchBowMod.torchbinder;//正しいかどうかチェック
        boolean myes = false;//ストレージBoxに入ってるItemのIDチェック用の変数初期化　初期値：無効
        if (mitem) {
            int ssize = 0;
            ssize = torchbinder.getOrCreateTagElement("TorchBandolier").getInt("Count");//有効に
            myes = ssize != 0;//無効に
        }
        return myes;
    }
}
