package mod.torchbowmod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class RenderTorch extends ArrowRenderer<EntityTorch, TorchRenderState> {
    private static final ResourceLocation TorchTextures = ResourceLocation.fromNamespaceAndPath("torchbowmod","textures/entity/torch.png");

    public RenderTorch(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public TorchRenderState createRenderState() {
        return new TorchRenderState();
    }

    @Override
    public void extractRenderState(EntityTorch entityTorch, TorchRenderState torchRenderState, float partialTick) {
        super.extractRenderState(entityTorch, torchRenderState, partialTick);
        if(entityTorch.getTorchItem().getItem() instanceof BlockItem blockItem){
            torchRenderState.blockState = blockItem.getBlock().defaultBlockState();
        }else{
            torchRenderState.blockState = Blocks.TORCH.defaultBlockState();
        }
    }

    @Override
    protected ResourceLocation getTextureLocation(TorchRenderState renderState) {
        return TorchTextures;
    }

    @Override
    public void render(TorchRenderState renderState, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        BlockState blockState = renderState.blockState;

        BlockModelShaper modelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();

        if (!blockState.isAir()) {
            var model = modelShaper.getBlockModel(blockState);

            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(renderState.yRot - 90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(renderState.xRot - 90.0F ));

            float f9 = renderState.shake;
            if (f9 > 0.0F) {
                float f10 = Mth.sin(f9 * 3.0F) * f9;
                poseStack.mulPose(Axis.ZP.rotationDegrees(f10));
            }

            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            poseStack.translate(-0.5F, -0.25F, -0.5F);

            for (var rendertype : model.getRenderTypes(blockState, RandomSource.create(), ModelData.EMPTY)) {
                VertexConsumer vertexconsumer = ItemRenderer.getFoilBuffer(buffer, rendertype, true, false);
                PoseStack.Pose posestack$pose = poseStack.last();
                for (BakedQuad bakedquad : model.getQuads(null, null, RandomSource.create(), ModelData.EMPTY, RenderType.cutout())) {
                    vertexconsumer.putBulkData(posestack$pose, bakedquad, 1F, 1F, 1F, 1F, packedLight, OverlayTexture.NO_OVERLAY, true);
                }
            }

            poseStack.popPose();
        }
    }
}