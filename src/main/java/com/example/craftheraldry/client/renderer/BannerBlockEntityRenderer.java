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
            new ResourceLocation(CraftHeraldry.MODID, "textures/entity/banner_cloth.png");

    // Use a simple wood texture for the banner rod/crossbar.
    // (Oak planks reads closest to vanilla's banner bar in-game.)
    private static final ResourceLocation WOOD_ROD_TEXTURE =
            new ResourceLocation("minecraft", "textures/block/oak_planks.png");

    private static final ResourceLocation SHEET0 =
            new ResourceLocation(CraftHeraldry.MODID, "textures/icons/icon_sheet_0.png");
    private static final ResourceLocation SHEET1 =
            new ResourceLocation(CraftHeraldry.MODID, "textures/icons/icon_sheet_1.png");


    private static final float NOTCH_HEIGHT = 2f / 16f;

    public BannerBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(BannerBlockEntity be, float partialTicks, PoseStack ps,
                       MultiBufferSource buf, int light, int overlay) {

        ps.pushPose();

        BlockState state = be.getBlockState();
        boolean isWallBanner = state.getBlock() instanceof WallBannerBlock;

        // Vanilla-style transforms:
        // - Standing banner uses ROTATION_16
        // - Wall banner uses FACING
        ps.translate(0.5, 0.5, 0.5);
        if (isWallBanner) {
            Direction facing = state.getValue(WallBannerBlock.FACING);
            // Rotate so the cloth plane faces outward in the direction of FACING.
            // This matches vanilla-style block entity rotations and fixes N/S inversion.
            ps.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        } else {
            int rot = state.hasProperty(BannerBlock.ROTATION) ? state.getValue(BannerBlock.ROTATION) : 0;
            // Vanilla standing banner: ROTATION_16 is applied with a negative sign in rendering.
            // This ensures the banner's front faces the placer for all directions.
            ps.mulPose(Axis.YP.rotationDegrees(-rot * 22.5f));
        }
        ps.translate(-0.5, -0.5, -0.5);

        // Wall banner cloth is 2 blocks tall and hangs down from the top block.
        if (isWallBanner) ps.translate(0.0, -1.015625, 0.0);

        // Cloth quad bounds
        float x0 = 1f / 16f, x1 = 15f / 16f;
        // Both banners render a 2-block-tall cloth; the wall banner is shifted down above.
        float y0 = 0f;
        float y1 = 32f / 16f;

        // Standing banner is offset inward; wall banner is flush to wall (near z=1.0).
        float z = isWallBanner ? (1.0f / 16f) : (6.5f / 16f);

        // === Standing banner pole + crossbar ===
        if (!isWallBanner) {
            VertexConsumer rodVc = buf.getBuffer(RenderType.entitySolid(WOOD_ROD_TEXTURE));
            var pose = ps.last().pose();
            var normal = ps.last().normal();
            int ov = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

            // Pole (2px x 2px) from y=0..2 blocks
            float px0 = 7f / 16f;
            float px1 = 9f / 16f;
            float pz0 = 7f / 16f;
            float pz1 = 9f / 16f;
            float py0 = 0f;
            float py1 = 32f / 16f;

            // Crossbar (14px wide, 2px tall, 2px thick) at the top
            float cx0 = 2f / 16f;
            float cx1 = 14f / 16f;
            float cy0 = (32f / 16f) - (2f / 16f);
            float cy1 = 32f / 16f;
            float cz0 = 7f / 16f;
            float cz1 = 9f / 16f;

            // Helper: draw a textured box with normalized UVs (simple 0..1)
            // Pole faces
            box(rodVc, pose, normal, px0, py0, pz0, px1, py1, pz1, 0f, 0f, 1f, 1f, ov, light);
            // Crossbar faces
            box(rodVc, pose, normal, cx0, cy0, cz0, cx1, cy1, cz1, 0f, 0f, 1f, 1f, ov, light);
        }

        // === Wall banner bar + tips (vanilla-like). Rendered here because the wall banner block uses RenderShape.INVISIBLE. ===
        if (isWallBanner) {
            VertexConsumer rodVc = buf.getBuffer(RenderType.entitySolid(WOOD_ROD_TEXTURE));
            var pose = ps.last().pose();
            var normal = ps.last().normal();
            int ov = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

            // The Mojang wall banner has a thin crossbar at the very top of the (2-block-tall) cloth,
            // with little "tips" on each end.
            float barH = 2f / 16f;   // 2px tall
            float barD = 2f / 16f;   // 2px thick

            float by1 = y1;
            float by0 = y1 - barH;

            // Keep the bar just in front of the wall, with a tiny epsilon to avoid z-fighting.
            float bz1 = 2.0f / 16f;        // near the wall
            float bz0 = bz1 - barD;          // towards the player

            // Main bar spans almost the full width.
            float bx0 = 1f / 16f;
            float bx1 = 15f / 16f;
            box(rodVc, pose, normal, bx0, by0, bz0, bx1, by1, bz1, 0f, 0f, 1f, 1f, ov, light);

            // End tips: small cubes that stick out 1px beyond each end.
            float tip = 2f / 16f; // 2px wide
            float tipOut = 1f / 16f;

            // Left tip
            box(rodVc, pose, normal,
                    bx0 - tipOut, by0, bz0,
                    bx0 - tipOut + tip, by1, bz1,
                    0f, 0f, 1f, 1f, ov, light);

            // Right tip
            box(rodVc, pose, normal,
                    bx1 - tip + tipOut, by0, bz0,
                    bx1 + tipOut, by1, bz1,
                    0f, 0f, 1f, 1f, ov, light);
        }

        CrestData crest = be.getCrest();

        // If NO crest is set yet: show the plain cloth.
        if (crest == null || crest.icon() < 0) {
            VertexConsumer base = buf.getBuffer(RenderType.entityCutoutNoCull(CLOTH_BASE));
            if (isWallBanner) {
                // Mojang/vanilla wall banner shape is a simple rectangle (no V-cut).
                putQuad(ps, base, x0, y0, x1, y1, z, 0f, 0f, 1f, 1f, 0xFFFFFFFF, light);
                putQuad(ps, base, x1, y0, x0, y1, z + 0.0010f, 1f, 0f, 0f, 1f, 0xFFFFFFFF, light);
            } else {
                // Standing banner uses the inverted V-cut.
                putInvertedVQuad(ps, base, x0, y0, x1, y1, z, 0f, 0f, 1f, 1f, 0xFFFFFFFF, light);
                // back (swap x to flip winding)
                putInvertedVQuad(ps, base, x1, y0, x0, y1, z + 0.0010f, 1f, 0f, 0f, 1f, 0xFFFFFFFF, light);
            }
            ps.popPose();
            return;
        }

        // Crest is set: render crest layers.
        int icon = crest.icon();
        int col = icon % 32;
        int row = icon / 32;
        float u0 = (col * 64f) / 2048f;
        float v0 = (row * 64f) / 4096f;
        float u1 = ((col + 1) * 64f) / 2048f;
        float v1 = ((row + 1) * 64f) / 4096f;

        VertexConsumer vc0 = buf.getBuffer(RenderType.entityCutoutNoCull(SHEET0));
        if (isWallBanner) {
            putQuad(ps, vc0, x0, y0, x1, y1, z + 0.0008f, u1, v0, u0, v1, crest.color1(), light);
        } else {
            putInvertedVQuad(ps, vc0, x0, y0, x1, y1, z + 0.0008f, u1, v0, u0, v1, crest.color1(), light);
        }

        VertexConsumer vc1 = buf.getBuffer(RenderType.entityCutoutNoCull(SHEET1));
        if (isWallBanner) {
            putQuad(ps, vc1, x0, y0, x1, y1, z + 0.0016f, u1, v0, u0, v1, crest.color2(), light);
        } else {
            putInvertedVQuad(ps, vc1, x0, y0, x1, y1, z + 0.0016f, u1, v0, u0, v1, crest.color2(), light);
        }

        // back side (see crest from behind)
        VertexConsumer back0 = buf.getBuffer(RenderType.entityCutoutNoCull(SHEET0));
        if (isWallBanner) {
            putQuad(ps, back0, x1, y0, x0, y1, z + 0.0025f, u1, v0, u0, v1, crest.color1(), light);
        } else {
            putInvertedVQuad(ps, back0, x1, y0, x0, y1, z + 0.0025f, u1, v0, u0, v1, crest.color1(), light);
        }

        VertexConsumer back1 = buf.getBuffer(RenderType.entityCutoutNoCull(SHEET1));
        if (isWallBanner) {
            putQuad(ps, back1, x1, y0, x0, y1, z + 0.0033f, u1, v0, u0, v1, crest.color2(), light);
        } else {
            putInvertedVQuad(ps, back1, x1, y0, x0, y1, z + 0.0033f, u1, v0, u0, v1, crest.color2(), light);
        }

        ps.popPose();
    }

    /**
     * Minimal "box" builder: emits all 6 faces of an axis-aligned cuboid.
     * Texture UVs are simple 0..1 mapping (good enough for wood/log).
     */
    private static void box(VertexConsumer vc, org.joml.Matrix4f pose, org.joml.Matrix3f normal,
                            float x0, float y0, float z0, float x1, float y1, float z1,
                            float u0, float v0, float u1, float v1,
                            int overlay, int light) {

        // front (-Z)
        vc.vertex(pose, x0, y1, z0).color(1f,1f,1f,1f).uv(u0,v0).overlayCoords(overlay).uv2(light).normal(normal, 0,0,-1).endVertex();
        vc.vertex(pose, x1, y1, z0).color(1f,1f,1f,1f).uv(u1,v0).overlayCoords(overlay).uv2(light).normal(normal, 0,0,-1).endVertex();
        vc.vertex(pose, x1, y0, z0).color(1f,1f,1f,1f).uv(u1,v1).overlayCoords(overlay).uv2(light).normal(normal, 0,0,-1).endVertex();
        vc.vertex(pose, x0, y0, z0).color(1f,1f,1f,1f).uv(u0,v1).overlayCoords(overlay).uv2(light).normal(normal, 0,0,-1).endVertex();

        // back (+Z)
        vc.vertex(pose, x1, y1, z1).color(1f,1f,1f,1f).uv(u0,v0).overlayCoords(overlay).uv2(light).normal(normal, 0,0,1).endVertex();
        vc.vertex(pose, x0, y1, z1).color(1f,1f,1f,1f).uv(u1,v0).overlayCoords(overlay).uv2(light).normal(normal, 0,0,1).endVertex();
        vc.vertex(pose, x0, y0, z1).color(1f,1f,1f,1f).uv(u1,v1).overlayCoords(overlay).uv2(light).normal(normal, 0,0,1).endVertex();
        vc.vertex(pose, x1, y0, z1).color(1f,1f,1f,1f).uv(u0,v1).overlayCoords(overlay).uv2(light).normal(normal, 0,0,1).endVertex();

        // left (-X)
        vc.vertex(pose, x0, y1, z1).color(1f,1f,1f,1f).uv(u0,v0).overlayCoords(overlay).uv2(light).normal(normal, -1,0,0).endVertex();
        vc.vertex(pose, x0, y1, z0).color(1f,1f,1f,1f).uv(u1,v0).overlayCoords(overlay).uv2(light).normal(normal, -1,0,0).endVertex();
        vc.vertex(pose, x0, y0, z0).color(1f,1f,1f,1f).uv(u1,v1).overlayCoords(overlay).uv2(light).normal(normal, -1,0,0).endVertex();
        vc.vertex(pose, x0, y0, z1).color(1f,1f,1f,1f).uv(u0,v1).overlayCoords(overlay).uv2(light).normal(normal, -1,0,0).endVertex();

        // right (+X)
        vc.vertex(pose, x1, y1, z0).color(1f,1f,1f,1f).uv(u0,v0).overlayCoords(overlay).uv2(light).normal(normal, 1,0,0).endVertex();
        vc.vertex(pose, x1, y1, z1).color(1f,1f,1f,1f).uv(u1,v0).overlayCoords(overlay).uv2(light).normal(normal, 1,0,0).endVertex();
        vc.vertex(pose, x1, y0, z1).color(1f,1f,1f,1f).uv(u1,v1).overlayCoords(overlay).uv2(light).normal(normal, 1,0,0).endVertex();
        vc.vertex(pose, x1, y0, z0).color(1f,1f,1f,1f).uv(u0,v1).overlayCoords(overlay).uv2(light).normal(normal, 1,0,0).endVertex();

        // top (+Y)
        vc.vertex(pose, x0, y1, z1).color(1f,1f,1f,1f).uv(u0,v0).overlayCoords(overlay).uv2(light).normal(normal, 0,1,0).endVertex();
        vc.vertex(pose, x1, y1, z1).color(1f,1f,1f,1f).uv(u1,v0).overlayCoords(overlay).uv2(light).normal(normal, 0,1,0).endVertex();
        vc.vertex(pose, x1, y1, z0).color(1f,1f,1f,1f).uv(u1,v1).overlayCoords(overlay).uv2(light).normal(normal, 0,1,0).endVertex();
        vc.vertex(pose, x0, y1, z0).color(1f,1f,1f,1f).uv(u0,v1).overlayCoords(overlay).uv2(light).normal(normal, 0,1,0).endVertex();

        // bottom (-Y)
        vc.vertex(pose, x0, y0, z0).color(1f,1f,1f,1f).uv(u0,v0).overlayCoords(overlay).uv2(light).normal(normal, 0,-1,0).endVertex();
        vc.vertex(pose, x1, y0, z0).color(1f,1f,1f,1f).uv(u1,v0).overlayCoords(overlay).uv2(light).normal(normal, 0,-1,0).endVertex();
        vc.vertex(pose, x1, y0, z1).color(1f,1f,1f,1f).uv(u1,v1).overlayCoords(overlay).uv2(light).normal(normal, 0,-1,0).endVertex();
        vc.vertex(pose, x0, y0, z1).color(1f,1f,1f,1f).uv(u0,v1).overlayCoords(overlay).uv2(light).normal(normal, 0,-1,0).endVertex();
    }

    private static void putInvertedVQuad(PoseStack ps, VertexConsumer vc,
                               float x0, float y0, float x1, float y1, float z,
                               float u0, float v0, float u1, float v1,
                               int color, int light) {

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        var pose = ps.last().pose();
        var normal = ps.last().normal();
        int ov = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;


        float xCenter = (x0 + x1) / 2f;
        float yNotch = y0 + NOTCH_HEIGHT;
        float uCenter = (u0 + u1) / 2f;


        // Left half
        vc.vertex(pose, x0, y1, z).color(r, g, b, 1f).uv(u0, v0).overlayCoords(ov).uv2(light).normal(normal, 0, 0, -1).endVertex();
        vc.vertex(pose, xCenter, y1, z).color(r, g, b, 1f).uv(uCenter, v0).overlayCoords(ov).uv2(light).normal(normal, 0, 0, -1).endVertex();
        vc.vertex(pose, xCenter, yNotch, z).color(r, g, b, 1f).uv(uCenter, v1).overlayCoords(ov).uv2(light).normal(normal, 0, 0, -1).endVertex();
        vc.vertex(pose, x0, y0, z).color(r, g, b, 1f).uv(u0, v1).overlayCoords(ov).uv2(light).normal(normal, 0, 0, -1).endVertex();

        // Right half
        vc.vertex(pose, xCenter, y1, z).color(r, g, b, 1f).uv(uCenter, v0).overlayCoords(ov).uv2(light).normal(normal, 0, 0, -1).endVertex();
        vc.vertex(pose, x1, y1, z).color(r, g, b, 1f).uv(u1, v0).overlayCoords(ov).uv2(light).normal(normal, 0, 0, -1).endVertex();
        vc.vertex(pose, x1, y0, z).color(r, g, b, 1f).uv(u1, v1).overlayCoords(ov).uv2(light).normal(normal, 0, 0, -1).endVertex();
        vc.vertex(pose, xCenter, yNotch, z).color(r, g, b, 1f).uv(uCenter, v1).overlayCoords(ov).uv2(light).normal(normal, 0, 0, -1).endVertex();

    }

    /**
     * Simple rectangular quad (used for Mojang/vanilla wall banners).
     * x0/x1 ordering controls winding and mirrors UVs for the back side.
     */
    private static void putQuad(PoseStack ps, VertexConsumer vc,
                                float x0, float y0, float x1, float y1, float z,
                                float u0, float v0, float u1, float v1,
                                int color, int light) {

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        var pose = ps.last().pose();
        var normal = ps.last().normal();
        int ov = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

        vc.vertex(pose, x0, y1, z).color(r, g, b, 1f).uv(u0, v0).overlayCoords(ov).uv2(light).normal(normal, 0, 0, -1).endVertex();
        vc.vertex(pose, x1, y1, z).color(r, g, b, 1f).uv(u1, v0).overlayCoords(ov).uv2(light).normal(normal, 0, 0, -1).endVertex();
        vc.vertex(pose, x1, y0, z).color(r, g, b, 1f).uv(u1, v1).overlayCoords(ov).uv2(light).normal(normal, 0, 0, -1).endVertex();
        vc.vertex(pose, x0, y0, z).color(r, g, b, 1f).uv(u0, v1).overlayCoords(ov).uv2(light).normal(normal, 0, 0, -1).endVertex();
    }
}
