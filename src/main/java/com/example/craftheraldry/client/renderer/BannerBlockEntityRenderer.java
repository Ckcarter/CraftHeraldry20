package com.example.craftheraldry.client.renderer;

import com.example.craftheraldry.CraftHeraldry;
import com.example.craftheraldry.common.block.BannerBlock;
import com.example.craftheraldry.common.block.WallBannerBlock;
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

    private static final ResourceLocation CLOTH_BASE =
        new ResourceLocation("craftheraldry", "textures/entity/banner_cloth.png");

    private static final ResourceLocation WOOD_ROD_TEXTURE = new ResourceLocation("minecraft", "textures/block/oak_log.png");
    private static final ResourceLocation WOOD_ROD_CAP_TEXTURE = new ResourceLocation("minecraft", "textures/block/oak_log_top.png");
    private static final ResourceLocation SHEET0 =
        new ResourceLocation(CraftHeraldry.MODID, "textures/icons/icon_sheet_0.png");
    private static final ResourceLocation SHEET1 =
        new ResourceLocation(CraftHeraldry.MODID, "textures/icons/icon_sheet_1.png");

    public BannerBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(BannerBlockEntity be, float partialTicks, PoseStack ps,
                       MultiBufferSource buf, int light, int overlay) {

        ps.pushPose();

        BlockState state = be.getBlockState();
        Direction facing = state.hasProperty(BannerBlock.FACING)
                ? state.getValue(BannerBlock.FACING)
                : Direction.NORTH;
        boolean isWallBanner = state.getBlock() instanceof WallBannerBlock;
        boolean hasCap = isWallBanner && state.hasProperty(WallBannerBlock.HAS_CAP) && state.getValue(WallBannerBlock.HAS_CAP);        ps.translate(0.5, 0.5, 0.5);
        float rotY = switch (facing) {
            case SOUTH -> 180f;
            case WEST -> 90f;
            case EAST -> -90f;
            default -> 0f;
        };
        ps.mulPose(Axis.YP.rotationDegrees(rotY));
        ps.translate(-0.5, -0.5, -0.5);

        if (isWallBanner) {
            // Move 2-block-tall cloth + crest down one full block
            ps.translate(0.0, -1.0, 0.0);
        }


        float x0 = 1f / 16f, x1 = 15f / 16f;

// Standing banner: cloth hangs from crossbar.
// Wall banner: TWO blocks tall (cloth hangs down), flush to the wall.
// Tapestry: TWO blocks tall, flush to the wall.
float y0 = isWallBanner ? 0f : 11f / 16f;
float y1 = isWallBanner ? (32f / 16f) : (31f / 16f);

// Local Z (before rotation): NORTH face is at z=16/16.

float z  = isWallBanner ? (15.85f / 16f) : (6.5f / 16f);


        
// === Wall banner top rod (rendered here because the block uses RenderShape.INVISIBLE) ===
if (isWallBanner) {
    VertexConsumer rodSide = buf.getBuffer(RenderType.entitySolid(WOOD_ROD_TEXTURE));
    VertexConsumer rodCap  = buf.getBuffer(RenderType.entitySolid(WOOD_ROD_CAP_TEXTURE));

    var pose = ps.last().pose();
    var normal = ps.last().normal();
    int ov = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

    // Rod dimensions (in block units)
    float rodThicknessY = 2f / 16f;
    float rodDepthZ = 3f / 16f;     // protrusion towards the player

    // Place rod at very top of the cloth
    float ry0 = y1 - rodThicknessY;
    float ry1 = y1;

    // Protrude from the wall
    float rz1 = z;          // near wall face
    float rz0 = z - rodDepthZ;

    // Make rod extend slightly past the cloth width
    float rx0 = x0 - (1f / 16f);
    float rx1 = x1 + (1f / 16f);

    // Simple UVs (0..1).
    float ru0 = 0f, ru1 = 1f;
    float rv0 = 0f, rv1 = 1f;

    // Front face
    rodSide.vertex(pose, rx0, ry1, rz1).color(1f, 1f, 1f, 1f).uv(ru0, rv0).overlayCoords(ov).uv2(light).normal(normal, 0, 0, -1).endVertex();
    rodSide.vertex(pose, rx1, ry1, rz1).color(1f, 1f, 1f, 1f).uv(ru1, rv0).overlayCoords(ov).uv2(light).normal(normal, 0, 0, -1).endVertex();
    rodSide.vertex(pose, rx1, ry0, rz1).color(1f, 1f, 1f, 1f).uv(ru1, rv1).overlayCoords(ov).uv2(light).normal(normal, 0, 0, -1).endVertex();
    rodSide.vertex(pose, rx0, ry0, rz1).color(1f, 1f, 1f, 1f).uv(ru0, rv1).overlayCoords(ov).uv2(light).normal(normal, 0, 0, -1).endVertex();

    // Back face
    rodSide.vertex(pose, rx1, ry1, rz0).color(1f, 1f, 1f, 1f).uv(ru0, rv0).overlayCoords(ov).uv2(light).normal(normal, 0, 0, 1).endVertex();
    rodSide.vertex(pose, rx0, ry1, rz0).color(1f, 1f, 1f, 1f).uv(ru1, rv0).overlayCoords(ov).uv2(light).normal(normal, 0, 0, 1).endVertex();
    rodSide.vertex(pose, rx0, ry0, rz0).color(1f, 1f, 1f, 1f).uv(ru1, rv1).overlayCoords(ov).uv2(light).normal(normal, 0, 0, 1).endVertex();
    rodSide.vertex(pose, rx1, ry0, rz0).color(1f, 1f, 1f, 1f).uv(ru0, rv1).overlayCoords(ov).uv2(light).normal(normal, 0, 0, 1).endVertex();

    // Top face
    rodSide.vertex(pose, rx0, ry1, rz0).color(1f, 1f, 1f, 1f).uv(ru0, rv0).overlayCoords(ov).uv2(light).normal(normal, 0, 1, 0).endVertex();
    rodSide.vertex(pose, rx1, ry1, rz0).color(1f, 1f, 1f, 1f).uv(ru1, rv0).overlayCoords(ov).uv2(light).normal(normal, 0, 1, 0).endVertex();
    rodSide.vertex(pose, rx1, ry1, rz1).color(1f, 1f, 1f, 1f).uv(ru1, rv1).overlayCoords(ov).uv2(light).normal(normal, 0, 1, 0).endVertex();
    rodSide.vertex(pose, rx0, ry1, rz1).color(1f, 1f, 1f, 1f).uv(ru0, rv1).overlayCoords(ov).uv2(light).normal(normal, 0, 1, 0).endVertex();

    // Bottom face
    rodSide.vertex(pose, rx0, ry0, rz1).color(1f, 1f, 1f, 1f).uv(ru0, rv0).overlayCoords(ov).uv2(light).normal(normal, 0, -1, 0).endVertex();
    rodSide.vertex(pose, rx1, ry0, rz1).color(1f, 1f, 1f, 1f).uv(ru1, rv0).overlayCoords(ov).uv2(light).normal(normal, 0, -1, 0).endVertex();
    rodSide.vertex(pose, rx1, ry0, rz0).color(1f, 1f, 1f, 1f).uv(ru1, rv1).overlayCoords(ov).uv2(light).normal(normal, 0, -1, 0).endVertex();
    rodSide.vertex(pose, rx0, ry0, rz0).color(1f, 1f, 1f, 1f).uv(ru0, rv1).overlayCoords(ov).uv2(light).normal(normal, 0, -1, 0).endVertex();

    // Left end cap (oak_log_top)
    rodCap.vertex(pose, rx0, ry1, rz0).color(1f, 1f, 1f, 1f).uv(ru0, rv0).overlayCoords(ov).uv2(light).normal(normal, -1, 0, 0).endVertex();
    rodCap.vertex(pose, rx0, ry1, rz1).color(1f, 1f, 1f, 1f).uv(ru1, rv0).overlayCoords(ov).uv2(light).normal(normal, -1, 0, 0).endVertex();
    rodCap.vertex(pose, rx0, ry0, rz1).color(1f, 1f, 1f, 1f).uv(ru1, rv1).overlayCoords(ov).uv2(light).normal(normal, -1, 0, 0).endVertex();
    rodCap.vertex(pose, rx0, ry0, rz0).color(1f, 1f, 1f, 1f).uv(ru0, rv1).overlayCoords(ov).uv2(light).normal(normal, -1, 0, 0).endVertex();

    // Right end cap (oak_log_top)
    rodCap.vertex(pose, rx1, ry1, rz1).color(1f, 1f, 1f, 1f).uv(ru0, rv0).overlayCoords(ov).uv2(light).normal(normal, 1, 0, 0).endVertex();
    rodCap.vertex(pose, rx1, ry1, rz0).color(1f, 1f, 1f, 1f).uv(ru1, rv0).overlayCoords(ov).uv2(light).normal(normal, 1, 0, 0).endVertex();
    rodCap.vertex(pose, rx1, ry0, rz0).color(1f, 1f, 1f, 1f).uv(ru1, rv1).overlayCoords(ov).uv2(light).normal(normal, 1, 0, 0).endVertex();
    rodCap.vertex(pose, rx1, ry0, rz1).color(1f, 1f, 1f, 1f).uv(ru0, rv1).overlayCoords(ov).uv2(light).normal(normal, 1, 0, 0).endVertex();

    // Optional decorative finials (caps) on the rod ends
    if (hasCap) {
        float f = 2f / 16f;      // finial size
        float protrude = 1f / 16f;

        float fx0L = rx0 - f;
        float fx1L = rx0;
        float fx0R = rx1;
        float fx1R = rx1 + f;

        float fy0 = ry0;
        float fy1 = ry1;

        float fz0 = rz0 - protrude;
        float fz1 = rz1;

        // Left finial cube
        // Front
        rodCap.vertex(pose, fx0L, fy1, fz1).color(1f,1f,1f,1f).uv(0f,0f).overlayCoords(ov).uv2(light).normal(normal, 0,0,-1).endVertex();
        rodCap.vertex(pose, fx1L, fy1, fz1).color(1f,1f,1f,1f).uv(1f,0f).overlayCoords(ov).uv2(light).normal(normal, 0,0,-1).endVertex();
        rodCap.vertex(pose, fx1L, fy0, fz1).color(1f,1f,1f,1f).uv(1f,1f).overlayCoords(ov).uv2(light).normal(normal, 0,0,-1).endVertex();
        rodCap.vertex(pose, fx0L, fy0, fz1).color(1f,1f,1f,1f).uv(0f,1f).overlayCoords(ov).uv2(light).normal(normal, 0,0,-1).endVertex();
        // Back
        rodCap.vertex(pose, fx1L, fy1, fz0).color(1f,1f,1f,1f).uv(0f,0f).overlayCoords(ov).uv2(light).normal(normal, 0,0,1).endVertex();
        rodCap.vertex(pose, fx0L, fy1, fz0).color(1f,1f,1f,1f).uv(1f,0f).overlayCoords(ov).uv2(light).normal(normal, 0,0,1).endVertex();
        rodCap.vertex(pose, fx0L, fy0, fz0).color(1f,1f,1f,1f).uv(1f,1f).overlayCoords(ov).uv2(light).normal(normal, 0,0,1).endVertex();
        rodCap.vertex(pose, fx1L, fy0, fz0).color(1f,1f,1f,1f).uv(0f,1f).overlayCoords(ov).uv2(light).normal(normal, 0,0,1).endVertex();
        // Left face
        rodCap.vertex(pose, fx0L, fy1, fz0).color(1f,1f,1f,1f).uv(0f,0f).overlayCoords(ov).uv2(light).normal(normal, -1,0,0).endVertex();
        rodCap.vertex(pose, fx0L, fy1, fz1).color(1f,1f,1f,1f).uv(1f,0f).overlayCoords(ov).uv2(light).normal(normal, -1,0,0).endVertex();
        rodCap.vertex(pose, fx0L, fy0, fz1).color(1f,1f,1f,1f).uv(1f,1f).overlayCoords(ov).uv2(light).normal(normal, -1,0,0).endVertex();
        rodCap.vertex(pose, fx0L, fy0, fz0).color(1f,1f,1f,1f).uv(0f,1f).overlayCoords(ov).uv2(light).normal(normal, -1,0,0).endVertex();
        // Right face
        rodCap.vertex(pose, fx1L, fy1, fz1).color(1f,1f,1f,1f).uv(0f,0f).overlayCoords(ov).uv2(light).normal(normal, 1,0,0).endVertex();
        rodCap.vertex(pose, fx1L, fy1, fz0).color(1f,1f,1f,1f).uv(1f,0f).overlayCoords(ov).uv2(light).normal(normal, 1,0,0).endVertex();
        rodCap.vertex(pose, fx1L, fy0, fz0).color(1f,1f,1f,1f).uv(1f,1f).overlayCoords(ov).uv2(light).normal(normal, 1,0,0).endVertex();
        rodCap.vertex(pose, fx1L, fy0, fz1).color(1f,1f,1f,1f).uv(0f,1f).overlayCoords(ov).uv2(light).normal(normal, 1,0,0).endVertex();
        // Top
        rodCap.vertex(pose, fx0L, fy1, fz0).color(1f,1f,1f,1f).uv(0f,0f).overlayCoords(ov).uv2(light).normal(normal, 0,1,0).endVertex();
        rodCap.vertex(pose, fx1L, fy1, fz0).color(1f,1f,1f,1f).uv(1f,0f).overlayCoords(ov).uv2(light).normal(normal, 0,1,0).endVertex();
        rodCap.vertex(pose, fx1L, fy1, fz1).color(1f,1f,1f,1f).uv(1f,1f).overlayCoords(ov).uv2(light).normal(normal, 0,1,0).endVertex();
        rodCap.vertex(pose, fx0L, fy1, fz1).color(1f,1f,1f,1f).uv(0f,1f).overlayCoords(ov).uv2(light).normal(normal, 0,1,0).endVertex();
        // Bottom
        rodCap.vertex(pose, fx0L, fy0, fz1).color(1f,1f,1f,1f).uv(0f,0f).overlayCoords(ov).uv2(light).normal(normal, 0,-1,0).endVertex();
        rodCap.vertex(pose, fx1L, fy0, fz1).color(1f,1f,1f,1f).uv(1f,0f).overlayCoords(ov).uv2(light).normal(normal, 0,-1,0).endVertex();
        rodCap.vertex(pose, fx1L, fy0, fz0).color(1f,1f,1f,1f).uv(1f,1f).overlayCoords(ov).uv2(light).normal(normal, 0,-1,0).endVertex();
        rodCap.vertex(pose, fx0L, fy0, fz0).color(1f,1f,1f,1f).uv(0f,1f).overlayCoords(ov).uv2(light).normal(normal, 0,-1,0).endVertex();

        // Right finial cube
        // Front
        rodCap.vertex(pose, fx0R, fy1, fz1).color(1f,1f,1f,1f).uv(0f,0f).overlayCoords(ov).uv2(light).normal(normal, 0,0,-1).endVertex();
        rodCap.vertex(pose, fx1R, fy1, fz1).color(1f,1f,1f,1f).uv(1f,0f).overlayCoords(ov).uv2(light).normal(normal, 0,0,-1).endVertex();
        rodCap.vertex(pose, fx1R, fy0, fz1).color(1f,1f,1f,1f).uv(1f,1f).overlayCoords(ov).uv2(light).normal(normal, 0,0,-1).endVertex();
        rodCap.vertex(pose, fx0R, fy0, fz1).color(1f,1f,1f,1f).uv(0f,1f).overlayCoords(ov).uv2(light).normal(normal, 0,0,-1).endVertex();
        // Back
        rodCap.vertex(pose, fx1R, fy1, fz0).color(1f,1f,1f,1f).uv(0f,0f).overlayCoords(ov).uv2(light).normal(normal, 0,0,1).endVertex();
        rodCap.vertex(pose, fx0R, fy1, fz0).color(1f,1f,1f,1f).uv(1f,0f).overlayCoords(ov).uv2(light).normal(normal, 0,0,1).endVertex();
        rodCap.vertex(pose, fx0R, fy0, fz0).color(1f,1f,1f,1f).uv(1f,1f).overlayCoords(ov).uv2(light).normal(normal, 0,0,1).endVertex();
        rodCap.vertex(pose, fx1R, fy0, fz0).color(1f,1f,1f,1f).uv(0f,1f).overlayCoords(ov).uv2(light).normal(normal, 0,0,1).endVertex();
        // Left face
        rodCap.vertex(pose, fx0R, fy1, fz0).color(1f,1f,1f,1f).uv(0f,0f).overlayCoords(ov).uv2(light).normal(normal, -1,0,0).endVertex();
        rodCap.vertex(pose, fx0R, fy1, fz1).color(1f,1f,1f,1f).uv(1f,0f).overlayCoords(ov).uv2(light).normal(normal, -1,0,0).endVertex();
        rodCap.vertex(pose, fx0R, fy0, fz1).color(1f,1f,1f,1f).uv(1f,1f).overlayCoords(ov).uv2(light).normal(normal, -1,0,0).endVertex();
        rodCap.vertex(pose, fx0R, fy0, fz0).color(1f,1f,1f,1f).uv(0f,1f).overlayCoords(ov).uv2(light).normal(normal, -1,0,0).endVertex();
        // Right face
        rodCap.vertex(pose, fx1R, fy1, fz1).color(1f,1f,1f,1f).uv(0f,0f).overlayCoords(ov).uv2(light).normal(normal, 1,0,0).endVertex();
        rodCap.vertex(pose, fx1R, fy1, fz0).color(1f,1f,1f,1f).uv(1f,0f).overlayCoords(ov).uv2(light).normal(normal, 1,0,0).endVertex();
        rodCap.vertex(pose, fx1R, fy0, fz0).color(1f,1f,1f,1f).uv(1f,1f).overlayCoords(ov).uv2(light).normal(normal, 1,0,0).endVertex();
        rodCap.vertex(pose, fx1R, fy0, fz1).color(1f,1f,1f,1f).uv(0f,1f).overlayCoords(ov).uv2(light).normal(normal, 1,0,0).endVertex();
        // Top
        rodCap.vertex(pose, fx0R, fy1, fz0).color(1f,1f,1f,1f).uv(0f,0f).overlayCoords(ov).uv2(light).normal(normal, 0,1,0).endVertex();
        rodCap.vertex(pose, fx1R, fy1, fz0).color(1f,1f,1f,1f).uv(1f,0f).overlayCoords(ov).uv2(light).normal(normal, 0,1,0).endVertex();
        rodCap.vertex(pose, fx1R, fy1, fz1).color(1f,1f,1f,1f).uv(1f,1f).overlayCoords(ov).uv2(light).normal(normal, 0,1,0).endVertex();
        rodCap.vertex(pose, fx0R, fy1, fz1).color(1f,1f,1f,1f).uv(0f,1f).overlayCoords(ov).uv2(light).normal(normal, 0,1,0).endVertex();
        // Bottom
        rodCap.vertex(pose, fx0R, fy0, fz1).color(1f,1f,1f,1f).uv(0f,0f).overlayCoords(ov).uv2(light).normal(normal, 0,-1,0).endVertex();
        rodCap.vertex(pose, fx1R, fy0, fz1).color(1f,1f,1f,1f).uv(1f,0f).overlayCoords(ov).uv2(light).normal(normal, 0,-1,0).endVertex();
        rodCap.vertex(pose, fx1R, fy0, fz0).color(1f,1f,1f,1f).uv(1f,1f).overlayCoords(ov).uv2(light).normal(normal, 0,-1,0).endVertex();
        rodCap.vertex(pose, fx0R, fy0, fz0).color(1f,1f,1f,1f).uv(0f,1f).overlayCoords(ov).uv2(light).normal(normal, 0,-1,0).endVertex();
    }
}

CrestData crest = be.getCrest();

// If NO crest is set yet: show the plain white cloth.
        if (crest == null || crest.icon() < 0) {
            VertexConsumer base = buf.getBuffer(RenderType.entityCutoutNoCull(CLOTH_BASE));
            // front
            putQuad(ps, base, x0, y0, x1, y1, z, 0f, 0f, 1f, 1f, 0xFFFFFFFF, light);
            // back (swap x to flip winding)
            putQuad(ps, base, x1, y0, x0, y1, z + 0.0010f, 1f, 0f, 0f, 1f, 0xFFFFFFFF, light);
            ps.popPose();
            return;
        }

        // Crest is set: hide the base cloth and render only the crest layers.
        int icon = crest.icon();
        int col = icon % 32;
        int row = icon / 32;
        float u0 = (col * 64f) / 2048f;
        float v0 = (row * 64f) / 4096f;
        float u1 = ((col + 1) * 64f) / 2048f;
        float v1 = ((row + 1) * 64f) / 4096f;

        VertexConsumer vc0 = buf.getBuffer(RenderType.entityCutoutNoCull(SHEET0));
        putQuad(ps, vc0, x0, y0, x1, y1, z + 0.0008f, u1, v0, u0, v1, crest.color1(), light);

        VertexConsumer vc1 = buf.getBuffer(RenderType.entityCutoutNoCull(SHEET1));
        putQuad(ps, vc1, x0, y0, x1, y1, z + 0.0016f, u1, v0, u0, v1, crest.color2(), light);

        // back side (see crest from behind)
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

        vc.vertex(pose, x0, y1, z).color(r, g, b, 1f).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
        vc.vertex(pose, x1, y1, z).color(r, g, b, 1f).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
        vc.vertex(pose, x1, y0, z).color(r, g, b, 1f).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
        vc.vertex(pose, x0, y0, z).color(r, g, b, 1f).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
    }
}
