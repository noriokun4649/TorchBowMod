package mod.torchbowmod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.Sys;

import javax.annotation.Nullable;

import static mod.torchbowmod.TorchBowMod.MODID;
import static mod.torchbowmod.TorchBowMod.glowstonetorch;
import static mod.torchbowmod.TorchBowMod.loggers;
import static net.minecraftforge.common.ForgeVersion.MOD_ID;

public class TorchBow extends Item {
    //private ItemStack mitemstack;
    private ItemStack sitemstack;
    private ItemStack torchbinder;
    private boolean sitem;
    private int Size;
    private boolean storageid;
    private boolean binder;

    public TorchBow()
    {
        this.maxStackSize = 1;
        this.setMaxDamage(384);
        this.setCreativeTab(TorchBowMod.TorchBowModTab);
        this.setUnlocalizedName("torchbow");
        this.setRegistryName(new ResourceLocation(MODID,"torchbow"));
        this.addPropertyOverride(new ResourceLocation("pull"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
            {
                if (entityIn == null)
                {
                    return 0.0F;
                }
                else
                {
                    return entityIn.getActiveItemStack().getItem() != Items.BOW ? 0.0F : (float)(stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / 20.0F;
                }
            }
        });
        this.addPropertyOverride(new ResourceLocation("pulling"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
            {
                return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
            }
        });
    }

    private ItemStack findAmmo(EntityPlayer player)
    {
        if (this.isArrow(player.getHeldItem(EnumHand.OFF_HAND)))
        {
            return player.getHeldItem(EnumHand.OFF_HAND);
        }
        else if (this.isArrow(player.getHeldItem(EnumHand.MAIN_HAND)))
        {
            return player.getHeldItem(EnumHand.MAIN_HAND);
        }
        else
        {
            for (int i = 0; i < player.inventory.getSizeInventory(); ++i)
            {
                ItemStack itemstack = player.inventory.getStackInSlot(i);

                Item glo = new ItemStack(glowstonetorch).getItem();
                if (this.isArrow(itemstack))
                {
                    return itemstack;
                }else if ( storageid){
                    return sitemstack;
                }else if (binder){
                    return torchbinder;
                }else if (glo != null){
                    if (itemstack.getItem() == glo) {
                        return itemstack;
                    }
                }
            }

            return ItemStack.EMPTY;
        }
    }

    protected boolean isArrow(ItemStack stack)
    {
        if (stack.getItem() ==  new ItemStack(Blocks.TORCH).getItem()) {
            return true;
        }
        return false;
    }

    /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft)
    {
        if (entityLiving instanceof EntityPlayer)
        {
            EntityPlayer entityplayer = (EntityPlayer)entityLiving;
            //storageid = getStorageMod(entityplayer);
            boolean flag = entityplayer.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
            ItemStack itemstack = this.findAmmo(entityplayer);

            int i = this.getMaxItemUseDuration(stack) - timeLeft;
            i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, worldIn, entityplayer, i, !itemstack.isEmpty() || flag);
            if (i < 0) return;

            if (!itemstack.isEmpty() || flag)
            {
                if (itemstack.isEmpty())
                {
                    itemstack = new ItemStack(Blocks.TORCH);
                }

                float f = getArrowVelocity(i);

                if ((double)f >= 0.1D)
                {
                    boolean flag1 = entityplayer.capabilities.isCreativeMode ;
                    //boolean moremodyes = getMoreMod(entityLiving);
                    EntityTorch entityarrow = new EntityTorch(worldIn,entityplayer);
                    entityarrow.setSetingBlock(Blocks.TORCH);
                    if (!flag1)
                    {
                        if (itemstack.getItem() == new ItemStack(Blocks.TORCH).getItem()){
                            itemstack.shrink(1);
                            if (itemstack.isEmpty())
                            {
                                entityplayer.inventory.deleteStack(itemstack);
                            }
                        }

                        /*
                        else if(moremodyes){//MoreInventoryModだった場合の処理
                            if (mitemstack.getItemDamage() < mitemstack.getMaxDamage() - 2)//条件
                            {
                                mitemstack.damageItem(1, p_77615_3_);//Torchholderから松明を消費
                            }
                        }
                        */

                        else if (itemstack.getItem() == new ItemStack(glowstonetorch).getItem()) {
                            entityarrow.setSetingBlock(glowstonetorch);
                            itemstack.shrink(1);
                            if (itemstack.isEmpty())
                            {
                                entityplayer.inventory.deleteStack(itemstack);
                            }
                        }else if (sitem){//StorageBoxだった場合の処理
                            boolean isTorch = true;
                            if (!worldIn.isRemote) {
                                if (storageid) {
                                    isTorch = new ItemStack(sitemstack.getTagCompound().getCompoundTag("StorageItemData")).getItem() == new ItemStack(Blocks.TORCH).getItem();
                                    Size = sitemstack.getTagCompound().getInteger("StorageSize");//今のアイテムの数取得
                                    int retrun_size = --Size;
                                    if (retrun_size != 0) {
                                        sitemstack.getTagCompound().setInteger("StorageSize", retrun_size);//ストレージBoxの中のアイテムの数減少させる。
                                    }else {
                                        sitemstack.getTagCompound().removeTag("StorageItemData");
                                    }
                                }
                            }
                            if(!isTorch){
                                entityarrow.setSetingBlock(glowstonetorch);
                            }
                        }else if(binder){//Silent's Gemsだった場合の処理
                            if (!worldIn.isRemote) {
                                Size = torchbinder.getTagCompound().getInteger("BlockCount");//今のアイテムの数取得
                                int retrun_size = --Size;
                                torchbinder.getTagCompound().setInteger("BlockCount", retrun_size);//ストレージBoxの中のアイテムの数減少させる。
                            }
                        }
                    }

                    if (!worldIn.isRemote)
                    {
                        //ItemArrow itemarrow = (ItemArrow)(itemstack.getItem() instanceof ItemArrow ? itemstack.getItem() : Items.ARROW);
                        entityarrow.shoot(entityplayer, entityplayer.rotationPitch, entityplayer.rotationYaw, 0.0F, f * 3.0F, 1.0F);

                        if (f == 1.0F)
                        {
                            entityarrow.setIsCritical(true);
                        }

                        int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);

                        if (j > 0)
                        {
                            entityarrow.setDamage(entityarrow.getDamage() + (double)j * 0.5D + 0.5D);
                        }

                        int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);

                        if (k > 0)
                        {
                            entityarrow.setKnockbackStrength(k);
                        }

                        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0)
                        {
                            entityarrow.setFire(100);
                        }

                        stack.damageItem(1, entityplayer);

                        ItemBlock glo =new ItemBlock(glowstonetorch);
                        if (flag1 || entityplayer.capabilities.isCreativeMode && itemstack.getItem() == new ItemBlock(Blocks.TORCH) )
                        {
                            entityarrow.pickupStatus = EntityTorch.PickupStatus.CREATIVE_ONLY;
                        }else if (glo != null ){
                            if (flag1 || entityplayer.capabilities.isCreativeMode && itemstack.getItem() == glo){
                                entityarrow.pickupStatus = EntityTorch.PickupStatus.CREATIVE_ONLY;
                            }
                        }

                        worldIn.spawnEntity(entityarrow);
                    }

                    worldIn.playSound((EntityPlayer)null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);


                    entityplayer.addStat(StatList.getObjectUseStats(this));
                }
            }
        }
    }

    /**
     * Gets the velocity of the arrow entity from the bow's charge
     */
    private static float getArrowVelocity(int charge)
    {
        float f = (float)charge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;

        if (f > 1.0F)
        {
            f = 1.0F;
        }

        return f;
    }

    /**
     * How long it takes to use or consume an item
     */
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 72000;
    }

    /**
     * returns the action that specifies what animation to play when the item is being used
     */
    public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.BOW;
    }

    /**
     * Called when the equipped item is right clicked.
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        storageid = getStorageMod(playerIn);
        binder = getSilentsMod(playerIn);
        boolean flag = !this.findAmmo(playerIn).isEmpty();

        ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, worldIn, playerIn, handIn, flag);
        if (ret != null) return ret;

        if (!playerIn.capabilities.isCreativeMode && !flag && !storageid && !binder)
        {
            return flag ? new ActionResult(EnumActionResult.PASS, itemstack) : new ActionResult(EnumActionResult.FAIL, itemstack);
        }
        else
        {
            playerIn.setActiveHand(handIn);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
        }
    }
    /***
     *  Modのアイテムが有効かどうか、松明が切れてないかどうか
     *  StorageBoxMod用処理
     * @param player
     * @return Modお問い合わせ
     */
    private boolean getStorageMod(EntityPlayer player){
        sitemstack = getStack(player, TorchBowMod.StorageBox);//ItemStack取得
        boolean as = sitemstack.getItem() == TorchBowMod.StorageBox;//正しいかどうかチェック
        boolean storageid = false;//ストレージBoxに入ってるItemのIDチェック用の変数初期化　初期値：無効
        int ssize = 0;
        if (as){//ただしかったら
            NBTTagCompound a = sitemstack.getTagCompound().getCompoundTag("StorageItemData");//StrageBoxに入ってるItemStackを取得
            if (a != null) {
                Item itemname = new ItemStack(a).getItem();//スロトレージBoxのなかのID取得
                Item itemid = new ItemStack(Blocks.TORCH).getItem();//対象のID取得
                Item itemid2 = new ItemStack(glowstonetorch).getItem();
                sitem = itemname == itemid || itemname  == itemid2;
                if (sitem) {//同じ場合
                    ssize = sitemstack.getTagCompound().getInteger("StorageSize");
                    storageid = true;//有効に
                    if (ssize == 0) {
                        storageid = false;//無効に
                    }
                }
            }else {
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
    private ItemStack getStack(EntityPlayer player,Item item){
        for (int i = 0; i < player.inventory.mainInventory.size(); ++i)
        {
            if (player.inventory.mainInventory.get(i) != null && player.inventory.mainInventory.get(i).getItem() == item/*TorchBowMod.StorageBox*/)
            {
                ItemStack itemstack = player.inventory.mainInventory.get(i);
                if (itemstack != null){//アイテムスタックがからじゃなかったら
                    if (itemstack.getTagCompound() == null){//NBTがNullだったら
                        itemstack.setTagCompound(new NBTTagCompound());//新しい空のNBTを書き込む
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
     * Silent's Gems用処理
     * @param player　プレイヤー
     * @return Modお問い合わせ
     */
    private boolean getSilentsMod(EntityPlayer player){
        torchbinder = getStack(player,TorchBowMod.torchbinder);//ItemStack取得
        boolean mitem = torchbinder.getItem() == TorchBowMod.torchbinder;//正しいかどうかチェック
        boolean myes = false;//ストレージBoxに入ってるItemのIDチェック用の変数初期化　初期値：無効
        if (mitem){
            int ssize = 0;
            ssize = torchbinder.getTagCompound().getInteger("BlockCount");
            myes = true;//有効に
            if (ssize == 0) {
                myes = false;//無効に
            }
        }
        return myes;
    }


    /**
     * Modのアイテムが有効かどうか、松明が切れてないかどうか
     * MoreInventoryMod用処理
     * @param player　プレイヤー
     * @return Modお問い合わせ
     */
    /*
    private boolean getMoreMod(EntityPlayer player){
        mitemstack = getStack(player,TorchBowMod.torchholder);//ItemStack取得
        boolean mitem = mitemstack.getItem() == TorchBowMod.torchholder;//正しいかどうかチェック
        boolean myes = false;//ホルダーに松明が入ってるかの変数初期化　初期値：無効
        if (mitem){//ただしかったら
            int mnowsize = mitemstack.getItemDamage();//現在のダメージを取得
            int msize = mitemstack.getMaxDamage();//最高ダメージを取得
            if (mnowsize < msize - 2){//松明が切れたかどうか
                myes = true;//切れてなかったら有効
            }
        }
        return myes;
    }

    */
    /**
     * Return the enchantability factor of the item, most of the time is based on material.
     */
    public int getItemEnchantability()
    {
        return 1;
    }
}
