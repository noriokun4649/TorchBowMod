package mod.torchbowmod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.client.renderer.entity.ItemRenderer.getFoilBufferDirect;

@OnlyIn(Dist.CLIENT)
public class RenderTorch extends ArrowRenderer<EntityTorch> {
    private static final ResourceLocation TorchTextures = ResourceLocation.fromNamespaceAndPath("torchbowmod","textures/entity/torch.png");

    public RenderTorch(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull EntityTorch entity) {
        return TorchTextures;
    }

    public void render(EntityTorch entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        ItemStack itemStack = entity.getTorchItem();
        if(!(itemStack.getItem() instanceof BlockItem)) itemStack = Blocks.TORCH.asItem().getDefaultInstance();

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        if (!itemStack.isEmpty()) {
            var p_model = itemRenderer.getModel(itemStack, entity.level(), null, 0);
            p_model = p_model.applyTransform(ItemDisplayContext.GROUND,poseStack, false);

            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) - 90.0F ));

            float f9 = (float)entity.shakeTime - partialTicks;
            if (f9 > 0.0F) {
                float f10 = Mth.sin(f9 * 3.0F) * f9;
                poseStack.mulPose(Axis.ZP.rotationDegrees(f10));
            }

            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            poseStack.scale(1.5F, 1.5F, 1.5F);
            poseStack.translate(-0.5F, -0.25F, -0.5F);

            for (var model : p_model.getRenderPasses(itemStack, true)) {
                for (var rendertype : model.getRenderTypes(itemStack, true)) {
                    VertexConsumer vertexconsumer = getFoilBufferDirect(buffer, rendertype, true, itemStack.hasFoil());
                    PoseStack.Pose posestack$pose = poseStack.last();
                    for (BakedQuad bakedquad : model.getQuads(null, null, RandomSource.create(), ModelData.EMPTY, RenderType.cutout())) {
                        vertexconsumer.putBulkData(posestack$pose, bakedquad, 1F, 1F, 1F, 1F, packedLight, OverlayTexture.NO_OVERLAY, true);
                    }
                }
            }

            poseStack.popPose();
        }
    }
}