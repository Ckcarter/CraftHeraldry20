package com.example.craftheraldry.client;

import com.example.craftheraldry.CrestData;
import com.example.craftheraldry.CraftHeraldry;
import com.example.craftheraldry.HeraldryBannerBlock;
import com.example.craftheraldry.HeraldryBannerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class HeraldryBannerRenderer implements BlockEntityRenderer<HeraldryBannerBlockEntity> {

    private static final ResourceLocation UNUSED = new ResourceLocation(CraftHeraldry.MODID, "textures/entity/banner_dummy.png");

    public HeraldryBannerRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(HeraldryBannerBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        var state = be.getBlockState();
        Direction facing = state.getValue(HeraldryBannerBlock.FACING);

        CrestData crest = be.getCrest();

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        float rotY = -facing.toYRot();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotY));
        poseStack.translate(-0.5, -0.5, -0.49);

        // Render handled by HeraldryRender
        Matrix4f mat = poseStack.last().pose();

        HeraldryRender.renderCrestOnQuad(buffers, poseStack, crest, 0.1f, 0.1f, 0.9f, 0.9f, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private static void 
        vc.vertex(mat, x1, y1, 0).color(r, g, b, a).uv(1, 1).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
        vc.vertex(mat, x1, y0, 0).color(r, g, b, a).uv(1, 0).overlayCoords(overlay).uv2(light).normal(0, 0, 1).normal(0, 0, 1).endVertex();
        vc.vertex(mat, x0, y0, 0).color(r, g, b, a).uv(0, 0).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
    }
}
