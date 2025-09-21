package mod.torchbowmod;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static mod.torchbowmod.TorchBow.TORCH_ITEMS;

@Mod(TorchBowMod.MODID)
public class TorchBowMod {
    public static final String MODID = "torchbowmod";
    public static Logger LOGGER = LogManager.getLogger("TorchBowMod");
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    private static final DeferredRegister<CreativeModeTab> TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static RegistryObject<Block> CeilingTorch = RegistryObject.create(ResourceLocation.fromNamespaceAndPath("ceilingtorch", "torch"), ForgeRegistries.BLOCKS);
    public static RegistryObject<Block> CeilingSoulTorch = RegistryObject.create(ResourceLocation.fromNamespaceAndPath("ceilingtorch", "soul_torch"), ForgeRegistries.BLOCKS);
    public static RegistryObject<Item> torchbow = ITEMS.register("torchbow", () -> new TorchBow(new Item.Properties().durability(384)));
    public static RegistryObject<Item> multiTorch = ITEMS.register("multitorch", () -> new Item(new Item.Properties().stacksTo(64)));
    public static RegistryObject<Item> torchArrow = ITEMS.register("torcharrow", () -> new TorchArrow(new Item.Properties().stacksTo(64)));

    public static final Map<BlockItem, WallTorchBlock> ITEM_TO_WALL_BLOCK = new HashMap<>();

    public static RegistryObject<EntityType<EntityTorch>> entityTorch = ENTITY_TYPES.register("entitytorch", () ->
            EntityType.Builder.<EntityTorch>of(EntityTorch::new, MobCategory.MISC)
                    .setCustomClientFactory(EntityTorch::new)
                    .setTrackingRange(60)
                    .setUpdateInterval(5)
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(0.5F, 0.5F)
                    .build("entitytorch"));

    public TorchBowMod(FMLJavaModLoadingContext context) {
        final IEventBus modEventBus = context.getModEventBus();
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        TAB.register(modEventBus);
        modEventBus.addListener(this::preInit);
        TAB.register("torchbowmodtab", () ->
                CreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.torchBowModTab"))
                        .icon(() -> new ItemStack(torchbow.get()))
                        .displayItems((parameters,output) -> {
                            output.accept(torchbow.get());
                            output.accept(multiTorch.get());
                            output.accept(torchArrow.get());
                        }).build());
    }

    private void preInit(final FMLCommonSetupEvent event) {
        Map<String, Integer> modCountMap = new HashMap<>();
        ForgeRegistries.BLOCKS.getValues().stream()
                .filter(block -> block instanceof WallTorchBlock)
                .forEach(block -> {
                    Item asItem = block.asItem();
                    if (asItem instanceof BlockItem blockItem) {
                        ITEM_TO_WALL_BLOCK.put(blockItem, (WallTorchBlock)block);
                        TORCH_ITEMS.add(blockItem);
                        String namespace = BuiltInRegistries.ITEM.getKey(asItem).getNamespace();
                        modCountMap.merge(namespace, 1, Integer::sum);
                    }
                });
        LOGGER.info("==== TorchBowMod Torch Item Auto-Registration Stats ====");
        LOGGER.info("Total registered pairs: {}", ITEM_TO_WALL_BLOCK.size());
        for (Map.Entry<String, Integer> entry : modCountMap.entrySet()) {
            LOGGER.info("Namespace '{}' has {} torch items", entry.getKey(), entry.getValue());
        }
        LOGGER.info("========================================================");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() ->
            {
                ItemProperties.register(torchbow.get(),
                        ResourceLocation.withDefaultNamespace("pull"), (itemStack, world, livingEntity, num) -> {
                            if (livingEntity == null) {
                                return 0.0F;
                            } else {
                                return livingEntity.getUseItem() != itemStack ? 0.0F : (float) (itemStack.getUseDuration(livingEntity) - livingEntity.getUseItemRemainingTicks()) / 20.0F;
                            }
                        });
                ItemProperties.register(torchbow.get(), ResourceLocation.withDefaultNamespace("pulling"), (itemStack, world, livingEntity, num)
                        -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F);
            });
        }
    }
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(entityTorch.get(), RenderTorch::new);
        }
        @SubscribeEvent
        public static void registerCreativeModeTab(final BuildCreativeModeTabContentsEvent event) {
        }
    }

}

