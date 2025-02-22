package mod.torchbowmod;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class RenderTorch extends ArrowRenderer<EntityTorch, ArrowRenderState> {
    private static final ResourceLocation TorchTextures = ResourceLocation.fromNamespaceAndPath("torchbowmod","textures/entity/torch.png");

    public RenderTorch(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    protected @NotNull ResourceLocation getTextureLocation(@NotNull ArrowRenderState arrowRenderState) {
        return TorchTextures;
    }

    @Override
    public @NotNull ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}