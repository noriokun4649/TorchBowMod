package mod.torchbowmod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class RenderTorch extends ArrowRenderer<@NotNull EntityTorch, @NotNull TorchRenderState> {
    private static final Identifier TorchTextures = Identifier.fromNamespaceAndPath("torchbowmod","textures/entity/torch.png");

    public RenderTorch(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public @NotNull TorchRenderState createRenderState() {
        return new TorchRenderState();
    }

    @Override
    public void extractRenderState(@NotNull EntityTorch entityTorch, @NotNull TorchRenderState torchRenderState, float partialTick) {
        super.extractRenderState(entityTorch, torchRenderState, partialTick);
        if(entityTorch.getTorchItem().getItem() instanceof BlockItem blockItem){
            torchRenderState.blockState = blockItem.getBlock().defaultBlockState();
        }else{
            torchRenderState.blockState = Blocks.TORCH.defaultBlockState();
        }
    }

    @Override
    protected @NotNull Identifier getTextureLocation(@NotNull TorchRenderState renderState) {
        return TorchTextures;
    }

    @Override
    public void submit(TorchRenderState torchRenderState, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState cameraRenderState) {
        BlockState blockState = torchRenderState.blockState;

        if (!blockState.isAir()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(torchRenderState.yRot - 90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(torchRenderState.xRot - 90.0F ));
            poseStack.translate(-0.5, 0, 0.5);
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        submitNodeCollector.submitBlock(poseStack,blockState,torchRenderState.lightCoords,OverlayTexture.NO_OVERLAY,torchRenderState.outlineColor);
        poseStack.popPose();
    }
}