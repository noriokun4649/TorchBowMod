package mod.torchbowmod;


import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.item.ItemModelsProperties.func_239418_a_;

@Mod(TorchBowMod.MODID)
public class TorchBowMod {
    public static final String MODID = "torchbowmod";
    public static Logger LOGGER = LogManager.getLogger("TorchBowMod");
    public static ItemGroup torchBowModTab = (new ItemGroup("torchBowModTab") {
        @Override
        public ItemStack createIcon() {
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
            .group(torchBowModTab).defaultMaxDamage(384))
            .setRegistryName(new ResourceLocation(MODID, "torchbow"));
    public static Item multiTorch = new Item(new Item.Properties()
            .group(torchBowModTab).maxStackSize(64))
            .setRegistryName(new ResourceLocation(MODID, "multitorch"));
    public static EntityType<EntityTorch> TORCH_ENTITY;

    public TorchBowMod() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::initClient);
    }

    private void preInit(final FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void initClient(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(TORCH_ENTITY, RenderTorch::new);
        torchBowModTab.createIcon();
        func_239418_a_(torchbow, new ResourceLocation("pull"), (itemStack, world, livingEntity) -> {
            if (livingEntity == null) {
                return 0.0F;
            } else {
                return livingEntity.getActiveItemStack() != itemStack ? 0.0F : (float)(itemStack.getUseDuration() - livingEntity.getItemInUseCount()) / 20.0F;
            }
        });
        func_239418_a_(torchbow, new ResourceLocation("pulling"), (itemStack, world, livingEntity)
                -> livingEntity != null && livingEntity.isHandActive() && livingEntity.getActiveItemStack() == itemStack ? 1.0F : 0.0F);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(torchbow, multiTorch);
        }

        @SubscribeEvent
        public static void registerEntityTypes(final RegistryEvent.Register<EntityType<?>> event) {
            TORCH_ENTITY = EntityType.Builder.<EntityTorch>create(EntityTorch::new, EntityClassification.MISC)
                    .setCustomClientFactory(EntityTorch::new)
                    .setTrackingRange(60)
                    .setUpdateInterval(5)
                    .setShouldReceiveVelocityUpdates(true)
                    .size(0.5F, 0.5F)
                    .build(MODID + ":entitytorch");
            TORCH_ENTITY.setRegistryName(new ResourceLocation(MODID, "entitytorch"));
            event.getRegistry().register(TORCH_ENTITY);
        }
    }

}

