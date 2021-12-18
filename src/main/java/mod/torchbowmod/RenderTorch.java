package mod.torchbowmod;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderTorch extends ArrowRenderer<EntityTorch> {
    private static final ResourceLocation TorchTextures = new ResourceLocation("torchbowmod:textures/entity/torch.png");

    public RenderTorch(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTorch entity) {
        return TorchTextures;
    }
}