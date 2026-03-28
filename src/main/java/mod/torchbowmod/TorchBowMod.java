package mod.torchbowmod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static mod.torchbowmod.TorchBow.TORCH_ITEMS;

@Mod(TorchBowMod.MODID)
public class TorchBowMod {
    public static final String MODID = "torchbowmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    private static final DeferredRegister<CreativeModeTab> TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static RegistryObject<Block> CeilingTorch = RegistryObject.create(Identifier.fromNamespaceAndPath("ceilingtorch", "torch"), ForgeRegistries.BLOCKS);
    public static RegistryObject<Block> CeilingSoulTorch = RegistryObject.create(Identifier.fromNamespaceAndPath("ceilingtorch", "soul_torch"), ForgeRegistries.BLOCKS);
    public static RegistryObject<Item> torchbow = ITEMS.register("torchbow", () -> new TorchBow(new Item.Properties().setId(ITEMS.key("torchbow")).durability(384)));
    public static RegistryObject<Item> multiTorch = ITEMS.register("multitorch", () -> new Item(new Item.Properties().setId(ITEMS.key("multitorch")).stacksTo(64)));
    public static RegistryObject<Item> torchArrow = ITEMS.register("torcharrow", () -> new TorchArrow(new Item.Properties().setId(ITEMS.key("torcharrow")).stacksTo(64)));

    public static RegistryObject<EntityType<EntityTorch>> entityTorch = ENTITY_TYPES.register("entitytorch", () ->
            EntityType.Builder.<EntityTorch>of(EntityTorch::new, MobCategory.MISC)
                    .setCustomClientFactory(EntityTorch::new)
                    .setTrackingRange(60)
                    .setUpdateInterval(5)
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(0.5F, 0.5F)
                    .build(ENTITY_TYPES.key("entitytorch")));

    public static final Map<BlockItem, WallTorchBlock> ITEM_TO_WALL_BLOCK = new HashMap<>();

    public TorchBowMod(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();
        ITEMS.register(modBusGroup);
        ENTITY_TYPES.register(modBusGroup);
        TAB.register(modBusGroup);
        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::preInit);
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
        event.enqueueWork(() -> {
        Map<String, Integer> modCountMap = new HashMap<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof WallTorchBlock wallBlock) {
                Item asItem = block.asItem();
                if (asItem instanceof BlockItem blockItem) {
                    ITEM_TO_WALL_BLOCK.put(blockItem, wallBlock);
                    TORCH_ITEMS.add(blockItem);
                    String namespace = BuiltInRegistries.ITEM.getKey(asItem).getNamespace();
                    modCountMap.merge(namespace, 1, Integer::sum);
                }
            }
        }
        LOGGER.info("==== TorchBowMod Torch Item Auto-Registration Stats ====");
        LOGGER.info("Total registered pairs: {}", ITEM_TO_WALL_BLOCK.size());
        for (Map.Entry<String, Integer> entry : modCountMap.entrySet()) {
            LOGGER.info("Namespace '{}' has {} torch items", entry.getKey(), entry.getValue());
        }
        LOGGER.info("========================================================");
    });
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
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

