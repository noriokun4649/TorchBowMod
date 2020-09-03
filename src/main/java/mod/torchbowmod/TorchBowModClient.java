package mod.torchbowmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import static mod.torchbowmod.TorchBowMod.MODID;
import static mod.torchbowmod.TorchBowMod.TORCH;

@Environment(EnvType.CLIENT)
public class TorchBowModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.INSTANCE.register(TORCH, (dispatcher, context) -> new TorchEntityRender(dispatcher));
        ClientSidePacketRegistry.INSTANCE.register(new Identifier(MODID, "spawntorch"), this::spawnEntity);
    }

    private void spawnEntity(PacketContext context, PacketByteBuf byteBuf) {
        int entityId = byteBuf.readInt();
        double x = byteBuf.readDouble();
        double y = byteBuf.readDouble();
        double z = byteBuf.readDouble();
        context.getTaskQueue().execute(() -> {
            World world = context.getPlayer().world;
            PlayerEntity playerEntity = context.getPlayer();
            TorchEntity torchEntity = new TorchEntity(world, playerEntity);
            torchEntity.setPos(x, y, z);
            torchEntity.setEntityId(entityId);
            torchEntity.updateTrackedPosition(x, y, z);
            ((ClientWorld) world).addEntity(entityId, torchEntity);
        });

    }
}

