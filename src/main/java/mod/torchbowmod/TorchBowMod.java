package mod.torchbowmod;


import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TorchBowMod.MODID)
public class TorchBowMod {
    public static final String MODID = "torchbowmod";
    public static Logger LOGGER = LogManager.getLogger("TorchBowMod");
    public static CreativeModeTab torchBowModTab = (new CreativeModeTab("torchBowModTab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(torchbow);
        }
    });

    @ObjectHolder("torchbandolier:torch_bandolier")
    public static Item torchbinder = null;
    @ObjectHolder("storagebox:storagebox")
    public static Item StorageBox = null;
    @ObjectHolder("ceilingtorch:torch")
    public static Block CeilingTorch = null;

    public static Item torchbow = new TorchBow(new Item.Properties()
            .tab(torchBowModTab).defaultDurability(384))
            .setRegistryName(new ResourceLocation(MODID, "torchbow"));
    public static Item multiTorch = new Item(new Item.Properties()
            .tab(torchBowModTab).stacksTo(64))
            .setRegistryName(new ResourceLocation(MODID, "multitorch"));
    public static EntityType<EntityTorch> TORCH_ENTITY;

    public TorchBowMod() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::initClient);
    }

    private void preInit(final FMLCommonSetupEvent event) {
    }

    private void initClient(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        torchBowModTab.makeIcon();
        event.enqueueWork(() ->
        {
            ItemProperties.register(torchbow,
                    new ResourceLocation("pull"), (itemStack, world, livingEntity, num) -> {
                        if (livingEntity == null) {
                            return 0.0F;
                        } else {
                            return livingEntity.getUseItem() != itemStack ? 0.0F : (float)(itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) / 20.0F;
                        }
                    });
            ItemProperties.register(torchbow,new ResourceLocation("pulling"), (itemStack, world, livingEntity, num)
                    -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F);
        });
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(torchbow, multiTorch);
        }

        @SubscribeEvent
        public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event)
        {
            event.registerEntityRenderer(TORCH_ENTITY, RenderTorch::new);
        }

        @SubscribeEvent
        public static void registerEntityTypes(final RegistryEvent.Register<EntityType<?>> event) {
            TORCH_ENTITY = EntityType.Builder.<EntityTorch>of(EntityTorch::new, MobCategory.MISC)
                    .setCustomClientFactory(EntityTorch::new)
                    .setTrackingRange(60)
                    .setUpdateInterval(5)
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(0.5F, 0.5F)
                    .build(MODID + ":entitytorch");
            TORCH_ENTITY.setRegistryName(new ResourceLocation(MODID, "entitytorch"));
            event.getRegistry().register(TORCH_ENTITY);
        }
    }

}

