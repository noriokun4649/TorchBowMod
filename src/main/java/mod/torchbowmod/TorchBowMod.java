package mod.torchbowmod;


import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TorchBowMod.MODID)
public class TorchBowMod {
    public static final String MODID = "torchbowmod";
    public static Logger LOGGER = LogManager.getLogger("TorchBowMod");
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    @ObjectHolder(registryName = "torchbandolier:torch_bandolier", value = "torch_bandolier")
    public static Item torchbinder = null;
    @ObjectHolder(registryName = "storagebox:storagebox", value = "storagebox")
    public static Item StorageBox = null;
    @ObjectHolder(registryName = "ceilingtorch:torch", value = "ceilingtorch")
    public static Block CeilingTorch = null;

    public static RegistryObject<Item> torchbow = ITEMS.register("torchbow", () -> new TorchBow(new Item.Properties().defaultDurability(384)));
    public static RegistryObject<Item> multiTorch = ITEMS.register("multitorch", () -> new Item(new Item.Properties().stacksTo(64)));
    public static RegistryObject<Item> torchArrow = ITEMS.register("torcharrow", () -> new TorchArrow(new Item.Properties().stacksTo(64)));

    public static RegistryObject<EntityType<EntityTorch>> entityTorch = ENTITY_TYPES.register("entitytorch", () ->
            EntityType.Builder.<EntityTorch>of(EntityTorch::new, MobCategory.MISC)
                    .setCustomClientFactory(EntityTorch::new)
                    .setTrackingRange(60)
                    .setUpdateInterval(5)
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(0.5F, 0.5F)
                    .build(MODID + ":entitytorch"));

    public TorchBowMod() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::initClient);
    }

    private void preInit(final FMLCommonSetupEvent event) {
    }

    private void initClient(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        event.enqueueWork(() ->
        {
            ItemProperties.register(torchbow.get(),
                    new ResourceLocation("pull"), (itemStack, world, livingEntity, num) -> {
                        if (livingEntity == null) {
                            return 0.0F;
                        } else {
                            return livingEntity.getUseItem() != itemStack ? 0.0F : (float) (itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) / 20.0F;
                        }
                    });
            ItemProperties.register(torchbow.get(), new ResourceLocation("pulling"), (itemStack, world, livingEntity, num)
                    -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F);
        });
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(entityTorch.get(), RenderTorch::new);
        }
        @SubscribeEvent
        public static void registerCreativeModeTab(final CreativeModeTabEvent.Register event) {
            event.registerCreativeModeTab(new ResourceLocation(MODID, "torchbowmodtab"), builder ->
                    builder.title(Component.translatable("itemGroup.torchBowModTab"))
                            .icon(() -> new ItemStack(torchbow.get()))
                            .displayItems((parameters,output) -> {
                                output.accept(torchbow.get());
                                output.accept(multiTorch.get());
                                output.accept(torchArrow.get());
                            })
            );
        }
    }

}

