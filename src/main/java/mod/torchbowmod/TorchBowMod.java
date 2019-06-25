package mod.torchbowmod;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = TorchBowMod.MODID, name = TorchBowMod.name, version = TorchBowMod.VERSION)
public class TorchBowMod {
    public static final String MODID = "torchbowmod";
    public static final String name = "TorchBowMod";
    public static final String VERSION = "1.4";
    public static Logger loggers = LogManager.getLogger("TorchBowMod");
    @SidedProxy(clientSide = "mod.torchbowmod.ClientProxy", serverSide = "mod.torchbowmod.ServerProxy")
    public static CommonProxy proxy;
    public static Item torchbow;
    public static Item StorageBox;
    //public static Item torchholder;
    public static Item torchbinder;
    public static Block glowstonetorch;
    //public static int EntityTorchRenderID;
    //public static boolean LittleMaidMob = false;
    public static CreativeTabs TorchBowModTab;

    private static final LanguageMap fallbackTranslator = new LanguageMap();

    public static DamageSource causeEntityTorchDamage(EntityTorch EntityTorch, Entity par1Entity) {
        return (new EntityDamageSourceIndirect("EntityTorch", EntityTorch, par1Entity)).setProjectile();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        TorchBowModTab = new CreativeTabs("TorchBowModTab") {
            public ItemStack getTabIconItem() {
                return new ItemStack(torchbow);
            }

//master
        };

        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "entitytorch"), EntityTorch.class, "EntityTorch", 3/*PlasticConfig.PlasticArrowEntityID*/, this, 60, 5, true);
        proxy.registerRenderes();
        torchbow = new TorchBow();
        //GameRegistry.addShapelessRecipe(new ItemStack(torchbow),new ItemStack(Items.BOW),new ItemStack(Items.FLINT_AND_STEEL));
        //EntityTorchRenderID = proxy.getNewRenderType();

        MinecraftForge.EVENT_BUS.register(this);
        proxy.registerRenderThings();
    }


    //アイテムを登録するイベント。旧preinitのタイミングで発火する。
    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(torchbow);
    }


    //モデルを登録するイベント。SideOnlyによってクライアント側のみ呼ばれる。旧preinitのタイミングで発火する。
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(torchbow, 0, new ModelResourceLocation("torchbowmod:torchbow"));
    }

    private String Translation(String key) {
        return new TextComponentTranslation(key).getUnformattedComponentText();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        loggers.info(Translation("log.sb.check"));
        if (Loader.isModLoaded("storagebox")) {
            try {
                this.StorageBox = ForgeRegistries.ITEMS.getValue(new ResourceLocation("storagebox", "storagebox"));
                loggers.info(Translation("log.sb.load"));
            } catch (Throwable t) {
                loggers.warn(Translation("log.sb.error"));
                loggers.warn(t);
            }
        } else {
            loggers.info(Translation("log.sb.unload"));
        }

        loggers.info(Translation("log.sg.check"));
        if (Loader.isModLoaded("silentgems")) {
            try {
                this.torchbinder = ForgeRegistries.ITEMS.getValue(new ResourceLocation("silentgems", "torchbandolier"));
                loggers.info(Translation("log.sg.load"));
            } catch (Throwable t) {
                loggers.warn(Translation("log.sg.error"));
                loggers.warn(t);
            }
        } else {
            loggers.info(Translation("log.sg.unload"));
        }
        loggers.info(Translation("log.gc.check"));
        if (Loader.isModLoaded("galacticraftcore")) {

            try {
                this.glowstonetorch = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("galacticraftcore", "glowstone_torch"));
                loggers.info(Translation("log.gc.load"));
            } catch (Throwable t) {
                loggers.warn(Translation("log.gc.error"));
                loggers.warn(t);
            }
        } else {
            loggers.info(Translation("log.gc.unload"));
        }

		/*
		loggers.info("MoreInventoryMod導入状況問い合わせ中・・・");
		if(Loader.isModLoaded("MoreInventoryMod"))
		{
			try
			{
				this.torchholder = ForgeRegistries.ITEMS.getValue(new ResourceLocation(("MoreInventoryMod", "torchholder"));
				loggers.info("MoreInventoryMod導入を確認。読み込み完了");
			}
			catch (Throwable t)
			{
				loggers.warn("MoreInventoryMod読み込みエラー");
				loggers.warn(t);
			}
		}else{
			loggers.info("MoreInventoryMod非導入。");
		}*/
    }

}

