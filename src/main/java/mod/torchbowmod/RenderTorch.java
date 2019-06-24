package mod.torchbowmod;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class RenderTorch<T extends EntityTorch> extends ArrowRenderer<T> {
    private static final ResourceLocation TorchTextures = new ResourceLocation("torchbowmod:textures/entity/torch.png");

    public RenderTorch(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return TorchTextures;
    }
}