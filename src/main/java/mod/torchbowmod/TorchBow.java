package mod.torchbowmod;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.function.Predicate;

import static mod.torchbowmod.TorchBowMod.multiTorch;
import static net.minecraft.item.BowItem.getArrowVelocity;

public class TorchBow extends ShootableItem {
    private ItemStack torchbinder;
    private ItemStack sitemstack;
    private boolean sitem;
    private boolean storageid;

    private boolean binder;
    public static final Predicate<ItemStack> TORCH = (itemStack) -> {
        return itemStack.getItem() == Blocks.TORCH.asItem() ||
                itemStack.getItem() == multiTorch ||
                (itemStack.getItem() == TorchBowMod.torchbinder && itemStack.getOrCreateChildTag("TorchBandolier").getInt("Count") > 0 ) ||
                (itemStack.getItem() == TorchBowMod.StorageBox &&
                        (ItemStack.read(itemStack.getTag().getCompound("StorageItemData")).getItem() == Blocks.TORCH.asItem() ||
                        ItemStack.read(itemStack.getTag().getCompound("StorageItemData")).getItem() == multiTorch ));
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
                        shootTorch(playerentity.rotationPitch, playerentity.rotationYaw, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                        if (itemstack.getItem() == multiTorch || ItemStack.read(itemstack.getTag().getCompound("StorageItemData")).getItem() == multiTorch ) {
                            shootTorch(playerentity.rotationPitch - size, playerentity.rotationYaw + size, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                            shootTorch(playerentity.rotationPitch - size, playerentity.rotationYaw, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                            shootTorch(playerentity.rotationPitch - size, playerentity.rotationYaw - size, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                            shootTorch(playerentity.rotationPitch + size, playerentity.rotationYaw + size, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                            shootTorch(playerentity.rotationPitch + size, playerentity.rotationYaw, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                            shootTorch(playerentity.rotationPitch + size, playerentity.rotationYaw - size, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                            shootTorch(playerentity.rotationPitch, playerentity.rotationYaw + size, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                            shootTorch(playerentity.rotationPitch, playerentity.rotationYaw - size, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                        }
                    }

                    worldIn.playSound((PlayerEntity) entityLiving, playerentity.posX, playerentity.posY, playerentity.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                    if (!playerentity.abilities.isCreativeMode){
                        if (itemstack.getItem() == Blocks.TORCH.asItem() || itemstack.getItem() == multiTorch ) {
                            itemstack.shrink(1);
                            if (itemstack.isEmpty()) {
                                playerentity.inventory.deleteStack(itemstack);
                            }
                        }else if (sitem) {//StorageBoxだった場合の処理
                            if (!worldIn.isRemote) {
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
                        }  else if (binder) {//TorchBandolierだった場合の処理
                            if (!worldIn.isRemote) {
                                int Size = torchbinder.getOrCreateChildTag("TorchBandolier").getInt("Count");//今のアイテムの数取得
                                int retrun_size = --Size;
                                torchbinder.getOrCreateChildTag("TorchBandolier").putInt("Count", retrun_size);//TorchBandolierのアイテムの数減少させる。
                            }
                        }
                    }

                    playerentity.addStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    private void shootTorch(float offsetPitch, float offsetYaw, PlayerEntity entitle, LivingEntity livingEntity, World worldIn, ItemStack itemstack, ItemStack stack, boolean flag1, float f) {
        EntityTorch abstractedly = new EntityTorch(worldIn, livingEntity);
        abstractedly.shoot(entitle, offsetPitch, offsetYaw, 0.0F, f * 3.0F, 1.0F);
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

        stack.damageItem(1, entitle, (p_220009_1_) -> {
            p_220009_1_.sendBreakAnimation(entitle.getActiveHand());
        });
        if (flag1 || entitle.abilities.isCreativeMode && (itemstack.getItem() == Blocks.TORCH.asItem())) {
            abstractedly.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
        }
        worldIn.addEntity(abstractedly);
    }

    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        binder = getSilentsMod(playerIn);
        storageid = getStorageMod(playerIn);
        boolean flag = !playerIn.func_213356_f(itemstack).isEmpty();

        ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, worldIn, playerIn, handIn, flag);
        if (ret != null) return ret;

        if (!playerIn.abilities.isCreativeMode && !flag && !binder && !storageid) {
            return flag ? new ActionResult<>(ActionResultType.PASS, itemstack) : new ActionResult<>(ActionResultType.FAIL, itemstack);
        } else {
            playerIn.setActiveHand(handIn);
            return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
        }
    }
    /***
     * アイテムからアイテムスタック取得。
     * @param player
     * @param item
     * @return　ItemStack
     */
    private ItemStack getStack(PlayerEntity player, Item item) {
        for (int i = 0; i < player.inventory.mainInventory.size(); ++i) {
            if (player.inventory.mainInventory.get(i) != null && player.inventory.mainInventory.get(i).getItem() == item/*TorchBowMod.StorageBox*/) {
                ItemStack itemstack = player.inventory.mainInventory.get(i);
                if (itemstack != null) {//アイテムスタックがからじゃなかったら
                    if (itemstack.getTag() == null) {//NBTがNullだったら
                        itemstack.setTag(new CompoundNBT());//新しい空のNBTを書き込む
                    }
                }
                return itemstack;
            }
        }
        ItemStack stack = new ItemStack(Items.BONE);//取得できなかったら適当に骨入れる
        return stack;
    }
    /***
     *  Modのアイテムが有効かどうか、松明が切れてないかどうか
     *  StorageBoxMod用処理
     * @param player
     * @return Modお問い合わせ
     */
    private boolean getStorageMod(PlayerEntity player) {
        sitemstack = getStack(player, TorchBowMod.StorageBox);//ItemStack取得
        boolean as = sitemstack.getItem() == TorchBowMod.StorageBox;//正しいかどうかチェック
        boolean storageid = false;//ストレージBoxに入ってるItemのIDチェック用の変数初期化　初期値：無効
        int ssize = 0;
        if (as) {//ただしかったら
            CompoundNBT a = sitemstack.getTag().getCompound("StorageItemData");//StrageBoxに入ってるItemStackを取得
            if (a != null) {
                Item itemname = ItemStack.read(a).getItem();//スロトレージBoxのなかのID取得
                Item itemid = new ItemStack(Blocks.TORCH).getItem();//対象のID取得
                Item itemid2 = new ItemStack(multiTorch).getItem();
                sitem = itemname == itemid || itemname == itemid2;
                if (sitem) {//同じ場合
                    ssize = sitemstack.getTag().getInt("StorageSize");
                    storageid = true;//有効に
                    if (ssize == 0) {
                        storageid = false;//無効に
                    }
                }
            } else {
                sitem = false;
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
    private boolean getSilentsMod(PlayerEntity player) {
        torchbinder = getStack(player, TorchBowMod.torchbinder);//ItemStack取得
        boolean mitem = torchbinder.getItem() == TorchBowMod.torchbinder;//正しいかどうかチェック
        boolean myes = false;//ストレージBoxに入ってるItemのIDチェック用の変数初期化　初期値：無効
        if (mitem) {
            int ssize = 0;
            ssize = torchbinder.getOrCreateChildTag("TorchBandolier").getInt("Count");
            myes = true;//有効に
            if (ssize == 0) {
                myes = false;//無効に
            }
        }
        return myes;
    }
}
