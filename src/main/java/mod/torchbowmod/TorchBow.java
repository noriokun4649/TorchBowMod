package mod.torchbowmod;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.EnumActionResult;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import static mod.torchbowmod.TorchBowMod.*;
public class TorchBow extends Item {
    private ItemStack torchbinder;
    private ItemStack sitemstack;
    private boolean binder;
    private boolean sitem;
    private boolean storageid;

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

    private ItemStack findAmmo(EntityPlayer player) {
        if (this.isArrow(player.getHeldItem(EnumHand.OFF_HAND))) {
            return player.getHeldItem(EnumHand.OFF_HAND);
        } else if (this.isArrow(player.getHeldItem(EnumHand.MAIN_HAND))) {
            return player.getHeldItem(EnumHand.MAIN_HAND);
        } else {
            for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                ItemStack itemstack = player.inventory.getStackInSlot(i);
                if (this.isArrow(itemstack)) {
                    return itemstack;
                } else if (storageid) {
                    return sitemstack;
                } else if (binder) {
                    return torchbinder;
                }
            }

            return ItemStack.EMPTY;
        }
    }

    protected boolean isArrow(ItemStack stack) {
        return stack.getItem() == Blocks.TORCH.asItem() || stack.getItem() == multiTorch;
    }

    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) entityLiving;
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
                    boolean flag1 = entityplayer.abilities.isCreativeMode || itemstack.getItem() instanceof ItemArrow && ((ItemArrow) itemstack.getItem()).isInfinite(itemstack, stack, entityplayer);
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

                    worldIn.playSound((EntityPlayer) null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                    if (!entityplayer.abilities.isCreativeMode){
                        if (itemstack.getItem() == Blocks.TORCH.asItem() || itemstack.getItem() == multiTorch ) {
                            itemstack.shrink(1);
                            if (itemstack.isEmpty()) {
                                entityplayer.inventory.deleteStack(itemstack);
                            }
                        }else if (sitem) {//StorageBoxだった場合の処理
                            if (!worldIn.isRemote) {
                                if (storageid) {
                                    int Size = sitemstack.getTag().getInt("StorageSize");//今のアイテムの数取得
                                    int retrun_size = --Size;
                                    if (retrun_size != 0) {
                                        sitemstack.getTag().setInt("StorageSize", retrun_size);//ストレージBoxの中のアイテムの数減少させる。
                                    } else {
                                        sitemstack.getTag().removeTag("StorageItemData");
                                    }
                                }
                            }
                        }  else if (binder) {//TorchBandolierだった場合の処理
                            if (!worldIn.isRemote) {
                                int Size = torchbinder.getOrCreateChildTag("TorchBandolier").getInt("Count");//今のアイテムの数取得
                                int retrun_size = --Size;
                                torchbinder.getOrCreateChildTag("TorchBandolier").setInt("Count", retrun_size);//TorchBandolierのアイテムの数減少させる。
                            }
                        }
                    }

                    entityplayer.addStat(StatList.ITEM_USED.get(this));
                }
            }
        }

    }

    private void shootTorch(float offsetPitch, float offsetYaw, EntityPlayer entityplayer, World worldIn, ItemStack itemstack, ItemStack stack, boolean flag1, float f) {
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

        stack.damageItem(1, entityplayer);
        if (flag1 || entityplayer.abilities.isCreativeMode && (itemstack.getItem() == Blocks.TORCH.asItem())) {
            entityarrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
        }

        worldIn.spawnEntity(entityarrow);
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

    public EnumAction getUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        binder = getSilentsMod(playerIn);
        storageid = getStorageMod(playerIn);
        boolean flag = !this.findAmmo(playerIn).isEmpty();
        ActionResult<ItemStack> ret = ForgeEventFactory.onArrowNock(itemstack, worldIn, playerIn, handIn, flag);
        if (ret != null) {
            return ret;
        } else if (!playerIn.abilities.isCreativeMode && !flag && !storageid && !binder) {
            return flag ? new ActionResult(EnumActionResult.PASS, itemstack) : new ActionResult(EnumActionResult.FAIL, itemstack);
        } else {
            playerIn.setActiveHand(handIn);
            return new ActionResult(EnumActionResult.SUCCESS, itemstack);
        }
    }
    /***
     *  Modのアイテムが有効かどうか、松明が切れてないかどうか
     *  StorageBoxMod用処理
     * @param player
     * @return Modお問い合わせ
     */
    private boolean getStorageMod(EntityPlayer player) {
        sitemstack = getStack(player, TorchBowMod.StorageBox);//ItemStack取得
        boolean as = sitemstack.getItem() == TorchBowMod.StorageBox;//正しいかどうかチェック
        boolean storageid = false;//ストレージBoxに入ってるItemのIDチェック用の変数初期化　初期値：無効
        int ssize = 0;
        if (as) {//ただしかったら
            NBTTagCompound a = sitemstack.getTag().getCompound("StorageItemData");//StrageBoxに入ってるItemStackを取得
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
    /***
     * アイテムからアイテムスタック取得。
     * @param player
     * @param item
     * @return　ItemStack
     */
    private ItemStack getStack(EntityPlayer player, Item item) {
        for (int i = 0; i < player.inventory.mainInventory.size(); ++i) {
            if (player.inventory.mainInventory.get(i) != null && player.inventory.mainInventory.get(i).getItem() == item/*TorchBowMod.StorageBox*/) {
                ItemStack itemstack = player.inventory.mainInventory.get(i);
                if (itemstack != null) {//アイテムスタックがからじゃなかったら
                    if (itemstack.getTag() == null) {//NBTがNullだったら
                        itemstack.setTag(new NBTTagCompound());//新しい空のNBTを書き込む
                    }
                }
                return itemstack;
            }
        }
        ItemStack stack = new ItemStack(Items.BONE);//取得できなかったら適当に骨入れる
        return stack;
    }
    /**
     * Modのアイテムが有効かどうか、松明が切れてないかどうか
     * TorchBandolier用処理
     *
     * @param player 　プレイヤー
     * @return Modお問い合わせ
     */
    private boolean getSilentsMod(EntityPlayer player) {
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
