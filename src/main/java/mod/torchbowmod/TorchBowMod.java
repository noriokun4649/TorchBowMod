package mod.torchbowmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TorchBowMod implements ModInitializer {

    public static final String MODID = "torchbowmod";
    public static final ItemGroup TORCH_BOW_GROUP = FabricItemGroupBuilder.build(new Identifier(MODID, "torchbowmod_tab"),
            () -> new ItemStack(TorchBowMod.TORCH_BOW_ITEM));
    public static final Item TORCH_BOW_ITEM = new TorchBow(new Item.Settings().group(TorchBowMod.TORCH_BOW_GROUP).maxDamage(384));
    public static final Item MULCH_TORCH_ITEM = new Item(new Item.Settings().group(TorchBowMod.TORCH_BOW_GROUP).maxCount(64));
    public static final EntityType<TorchEntity> TORCH;

    static {
        TORCH = Registry.register(Registry.ENTITY_TYPE,
                new Identifier(MODID, "entitytorch"),
                FabricEntityTypeBuilder.<TorchEntity>create(SpawnGroup.MISC, TorchEntity::new)
                        .trackable(60, 5, true).build());
    }

    @Override
    public void onInitialize() {
        System.out.println("おまんこぶりゅりゅりゅブリュッセル!");
        Registry.register(Registry.ITEM, new Identifier(MODID, "torchbow"), TORCH_BOW_ITEM);
        Registry.register(Registry.ITEM, new Identifier(MODID, "multitorch"), MULCH_TORCH_ITEM);
    }

}
