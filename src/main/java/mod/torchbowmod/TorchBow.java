package mod.torchbowmod;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;

public class TorchBow extends Item
{
	public static final String[] bowPullIconNameArray = new String[] {"pulling_0", "pulling_1", "pulling_2"};
	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;
	private final String __OBFID = "CL_00001777";
	private ItemStack mitemstack;
	private ItemStack sitemstack;
	private boolean sitem;
	private int Size;
	public TorchBow()
	{
		this.maxStackSize = 1;
		this.setMaxDamage(384);
		this.setCreativeTab(TorchBowMod.TorchBowModTab);
	}

	/**
	 * called when the player releases the use item button. Args: itemstack, world, entityplayer, itemInUseCount
	 */
	public void onPlayerStoppedUsing(ItemStack p_77615_1_, World p_77615_2_, EntityPlayer p_77615_3_, int p_77615_4_)
	{
		int j = this.getMaxItemUseDuration(p_77615_1_) - p_77615_4_;

		ArrowLooseEvent event = new ArrowLooseEvent(p_77615_3_, p_77615_1_, j);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled())
		{
			return;
		}
		j = event.charge;
		boolean flag = p_77615_3_.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, p_77615_1_) > 0;
		boolean storageid = getStorageMod(p_77615_3_);
		boolean moremodyes = getMoreMod(p_77615_3_);
		boolean torch = p_77615_3_.inventory.hasItem(Item.getItemFromBlock(Blocks.torch));
		if (flag || torch || storageid || moremodyes)
		{
			float f = (float)j / 20.0F;
			f = (f * f + f * 2.0F) / 3.0F;

			if ((double)f < 0.1D)
			{
				return;
			}

			if (f > 1.0F)
			{
				f = 1.0F;
			}

			EntityTorch entityarrow = new EntityTorch(p_77615_2_, p_77615_3_, f * 2.0F);

			if (f == 1.0F)
			{
				entityarrow.setIsCritical(true);
			}

			int k = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, p_77615_1_);

			if (k > 0)
			{
				entityarrow.setDamage(entityarrow.getDamage() + (double)k * 0.5D + 0.5D);
			}

			int l = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, p_77615_1_);

			if (l > 0)
			{
				entityarrow.setKnockbackStrength(l);
			}

			if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, p_77615_1_) > 0)
			{
				entityarrow.setFire(100);
			}

			p_77615_1_.damageItem(1, p_77615_3_);
			p_77615_2_.playSoundAtEntity(p_77615_3_, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);

			if (flag)
			{
				entityarrow.canBePickedUp = 2;
			}
			else
			{
				if (torch){
					p_77615_3_.inventory.consumeInventoryItem(Item.getItemFromBlock(Blocks.torch));
				}else if(moremodyes){//MoreInventoryModだった場合の処理
					if (mitemstack.getItemDamage() < mitemstack.getMaxDamage() - 2)//条件
					{
						mitemstack.damageItem(1, p_77615_3_);//Torchholderから松明を消費
					}
				}else if (sitem){//StorageBoxだった場合の処理
					if (storageid)
					{
						Size = sitemstack.getTagCompound().getInteger("StorageSize");//今のアイテムの数取得
						sitemstack.getTagCompound().setInteger("StorageSize", Size-1);//ストレージBoxの中のアイテムの数減少させる。
					}
				}
			}

			if (!p_77615_2_.isRemote)
			{
				p_77615_2_.spawnEntityInWorld(entityarrow);
			}
		}
	}
	/***
	 * アイテムからアイテムスタック取得。
	 * @param プレイヤ
	 * @param アイテム
	 * @return　ItemStack
	 */
	private ItemStack getStack(EntityPlayer p_77615_3_,Item item){
		for (int i = 0; i < p_77615_3_.inventory.mainInventory.length; ++i)
		{
			if (p_77615_3_.inventory.mainInventory[i] != null && p_77615_3_.inventory.mainInventory[i].getItem() == item/*TorchBowMod.StorageBox*/)
			{
				ItemStack itemstack = p_77615_3_.inventory.mainInventory[i];
				if (itemstack != null){//アイテムスタックがからじゃなかったら
					if (itemstack.getTagCompound() == null){//NBTがNullだったら
						itemstack.setTagCompound(new NBTTagCompound());//新しい空のNBTを書き込む
					}
				}
				return itemstack;
			}
		}
		ItemStack stack = new ItemStack(Items.bone);//取得できなかったら適当に骨入れる
		return stack;
	}
	public ItemStack onEaten(ItemStack p_77654_1_, World p_77654_2_, EntityPlayer p_77654_3_)
	{
		return p_77654_1_;
	}

	/**
	 * How long it takes to use or consume an item
	 */
	public int getMaxItemUseDuration(ItemStack p_77626_1_)
	{
		return 72000;
	}

	/**
	 * returns the action that specifies what animation to play when the items is being used
	 */
	public EnumAction getItemUseAction(ItemStack p_77661_1_)
	{
		return EnumAction.bow;
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
	 */
	public ItemStack onItemRightClick(ItemStack p_77659_1_, World p_77659_2_, EntityPlayer p_77659_3_)
	{
		ArrowNockEvent event = new ArrowNockEvent(p_77659_3_, p_77659_1_);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled())
		{
			return event.result;
		}
		boolean storageid = getStorageMod(p_77659_3_);
		boolean moremodyes = getMoreMod(p_77659_3_);
		if (p_77659_3_.capabilities.isCreativeMode || p_77659_3_.inventory.hasItem(Item.getItemFromBlock(Blocks.torch))|| storageid || moremodyes)
		{
			p_77659_3_.setItemInUse(p_77659_1_, this.getMaxItemUseDuration(p_77659_1_));
		}

		return p_77659_1_;
	}

	/**
	 * Return the enchantability factor of the item, most of the time is based on material.
	 */
	public int getItemEnchantability()
	{
		return 1;
	}
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister p_94581_1_)
	{
		this.itemIcon = p_94581_1_.registerIcon(this.getIconString() + "_standby");
		this.iconArray = new IIcon[bowPullIconNameArray.length];

		for (int i = 0; i < this.iconArray.length; ++i)
		{
			this.iconArray[i] = p_94581_1_.registerIcon(this.getIconString() + "_" + bowPullIconNameArray[i]);
		}
	}
	@Override
	public IIcon getIcon( ItemStack items, int renderPass, EntityPlayer eplayer, ItemStack usingItem, int remainTime ) {
		if ( usingItem != null && usingItem == items ) {
			int r = usingItem.getMaxItemUseDuration() - remainTime;
			if ( r >= 18.0F / 1.0F ) return iconArray[2];
			if ( r >  13.0F / 1.0F ) return iconArray[1];
			if ( r >   0.0F               ) return iconArray[0];
		}

		return super.getIcon( items, renderPass, eplayer, usingItem, remainTime );
	}
	
	/***
	 *  Modのアイテムが有効かどうか、松明が切れてないかどうか
	 *  StorageBoxMod用処理
	 * @param player
	 * @return Modお問い合わせ
	 */
	private boolean getStorageMod(EntityPlayer player){
		sitemstack = getStack(player, TorchBowMod.StorageBox);//ItemStack取得
		sitem = sitemstack.getItem() == TorchBowMod.StorageBox;//正しいかどうかチェック
		boolean storageid = false;//ストレージBoxに入ってるItemのIDチェック用の変数初期化　初期値：無効
		int ssize = 0;
		if (sitem){//ただしかったら
			int itemname = sitemstack.getTagCompound().getInteger("StorageItem");//スロトレージBoxのなかのID取得
			int itemid = Item.getIdFromItem(new ItemStack(Blocks.torch).getItem());//対象のID取得
			if(itemname == itemid){//同じ場合
				ssize = sitemstack.getTagCompound().getInteger("StorageSize");
				storageid =true;//有効に
				if(ssize == 0){
					storageid =false;//無効に
				}
			}
		}
		return storageid;
	}
	/**
	 * Modのアイテムが有効かどうか、松明が切れてないかどうか
	 * MoreInventoryMod用処理
	 * @param player　プレイヤー
	 * @return Modお問い合わせ
	 */
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
	/**
	 * used to cycle through icons based on their used duration, i.e. for the bow
	 */
	@SideOnly(Side.CLIENT)
	public IIcon getItemIconForUseDuration(int p_94599_1_)
	{
		return this.iconArray[p_94599_1_];
	}

}