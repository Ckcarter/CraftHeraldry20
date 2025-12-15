package com.example.craftheraldry.client.renderer;

import com.example.craftheraldry.CraftHeraldry;
import com.example.craftheraldry.common.block.BannerBlock;
import com.example.craftheraldry.common.blockentity.BannerBlockEntity;
import com.example.craftheraldry.common.util.CrestData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class BannerBlockEntityRenderer implements BlockEntityRenderer<BannerBlockEntity> {

    private static final ResourceLocation SHEET0 = new ResourceLocation(CraftHeraldry.MODID, "textures/icons/icon_sheet_0.png");
    private static final ResourceLocation SHEET1 = new ResourceLocation(CraftHeraldry.MODID, "textures/icons/icon_sheet_1.png");

    public BannerBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(BannerBlockEntity be, float partialTicks, PoseStack ps, MultiBufferSource buf, int light, int overlay) {
        CrestData crest = be.getCrest();
        if (crest == null || crest.icon() < 0) return;

        ps.pushPose();

        BlockState state = be.getBlockState();
Direction facing = state.hasProperty(BannerBlock.FACING) ? state.getValue(BannerBlock.FACING) : Direction.NORTH;

        ps.translate(0.5, 0.5, 0.5);
        float rotY = switch (facing) {
            case SOUTH -> 180f;
            case WEST -> 90f;
            case EAST -> -90f;
            default -> 0f;
        };
        ps.mulPose(Axis.YP.rotationDegrees(rotY));
        ps.translate(-0.5, -0.5, -0.5);

        float x0 = 2f/16f, x1 = 14f/16f;
            float y0 = 3f/16f, y1 = 31f/16f;
            float z = 6.5f/16f;

        int icon = crest.icon();
        int col = icon % 32;
        int row = icon / 32;
        float u0 = (col * 64f) / 2048f;
        float v0 = (row * 64f) / 4096f;
        float u1 = ((col + 1) * 64f) / 2048f;
        float v1 = ((row + 1) * 64f) / 4096f;

        VertexConsumer vc0 = buf.getBuffer(RenderType.entityCutoutNoCull(SHEET0));
        putQuad(ps, vc0, x0, y0, x1, y1, z, u1, v0, u0, v1, crest.color1(), light);

        VertexConsumer vc1 = buf.getBuffer(RenderType.entityCutoutNoCull(SHEET1));
        putQuad(ps, vc1, x0, y0, x1, y1, z + 0.0008f, u1, v0, u0, v1, crest.color2(), light);

            // back side (so you can see the crest from behind)
            // draw slightly behind, and flip normal by swapping vertex winding via swapped x0/x1
            VertexConsumer back0 = buf.getBuffer(RenderType.entityCutoutNoCull(SHEET0));
            putQuad(ps, back0, x1, y0, x0, y1, z + 0.0025f, u1, v0, u0, v1, crest.color1(), light);
            VertexConsumer back1 = buf.getBuffer(RenderType.entityCutoutNoCull(SHEET1));
            putQuad(ps, back1, x1, y0, x0, y1, z + 0.0033f, u1, v0, u0, v1, crest.color2(), light);
ps.popPose();
    }

    private static void putQuad(PoseStack ps, VertexConsumer vc,
                               float x0, float y0, float x1, float y1, float z,
                               float u0, float v0, float u1, float v1,
                               int color, int light) {

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        var pose = ps.last().pose();
        var normal = ps.last().normal();
        int overlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

        // Counter-clockwise on north-facing quad
        vc.vertex(pose, x0, y1, z).color(r, g, b, 1f).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
        vc.vertex(pose, x1, y1, z).color(r, g, b, 1f).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
        vc.vertex(pose, x1, y0, z).color(r, g, b, 1f).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
        vc.vertex(pose, x0, y0, z).color(r, g, b, 1f).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
    }
}
