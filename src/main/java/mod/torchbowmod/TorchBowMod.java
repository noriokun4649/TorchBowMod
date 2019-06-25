package mod.torchbowmod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraftforge.common.MinecraftForge;

//@Mod(modid = TorchBowMod.MODID,name = TorchBowMod.name,  version = TorchBowMod.VERSION)
public class TorchBowMod
{
	public static final String MODID = "TorchBowMod";
	public static final String name = "TorchBowMod";
	public static final String VERSION = "1.8";
	public static Logger loggers = LogManager.getLogger("TorchBowMod");
	@SidedProxy(clientSide = "mod.torchbowmod.ClientProxy", serverSide = "mod.torchbowmod.ServerProxy")
	public static CommonProxy proxy;
	public static Item torchbow;
	public static Item StorageBox;
	public static Item torchholder;
	public static int EntityTorchRenderID;
	public static boolean LittleMaidMob = false;
	public static CreativeTabs TorchBowModTab;
	public static DamageSource causeEntityTorchDamage(EntityTorch EntityTorch, Entity par1Entity) {
		return (new EntityDamageSourceIndirect("EntityTorch", EntityTorch, par1Entity)).setProjectile();
	}
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		TorchBowModTab = new CreativeTabs("TorchBowModTab")
		{
			public Item getTabIconItem()
			{
				return torchbow;
			}

//master
		};

		EntityRegistry.registerModEntity(EntityTorch.class, "EntityTorch",3/*PlasticConfig.PlasticArrowEntityID*/, this, 60, 5, true);
		proxy.registerRenderes();
		torchbow = new TorchBow().setTextureName("torchbowmod:torchbow").setUnlocalizedName("torchbow");
		GameRegistry.registerItem(torchbow, "torchbow");
		GameRegistry.addShapelessRecipe(new ItemStack(torchbow),new ItemStack(Items.bow),new ItemStack(Items.flint_and_steel));
		EntityTorchRenderID = proxy.getNewRenderType();
		MinecraftForge.EVENT_BUS.register(this);
		proxy.registerRenderThings();
	}
	@EventHandler
	public void postInit (FMLPostInitializationEvent event)
	{
		loggers.info("StorageBox導入状況問い合わせ中・・・");
		if(Loader.isModLoaded("net.minecraft.storagebox.mod_StorageBox"))
		{
			try
			{
				this.StorageBox = GameRegistry.findItem("net.minecraft.storagebox.mod_StorageBox", "Storage Box");
				loggers.info("StorageBox導入を確認。読み込み完了");
			}
			catch (Throwable t)
			{
				loggers.warn("StorageBox読み込みエラー");
				loggers.warn(t);
			}
		}else{
			loggers.info("StorageBox非導入。");
		}
		loggers.info("MoreInventoryMod導入状況問い合わせ中・・・");
		if(Loader.isModLoaded("MoreInventoryMod"))
		{
			try
			{
				this.torchholder = GameRegistry.findItem("MoreInventoryMod", "torchholder");
				loggers.info("MoreInventoryMod導入を確認。読み込み完了");
			}
			catch (Throwable t)
			{
				loggers.warn("MoreInventoryMod読み込みエラー");
				loggers.warn(t);
			}
		}else{
			loggers.info("MoreInventoryMod非導入。");
		}
	}
}

