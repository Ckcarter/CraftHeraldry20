package com.example.craftheraldry.client;

import com.example.craftheraldry.CraftHeraldry;
import com.example.craftheraldry.CrestData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * Renders a crest by compositing:
 *  - iconSheet0 tinted with backgroundColor
 *  - iconSheet1 tinted with foregroundColor
 *
 * Uses the same icon index UVs on both sheets.
 *
 * Original sheet sizes (from the uploaded CraftHeraldry assets): 2048x4096 with 64x64 tiles.
 */
public final class HeraldryRender {

    public static final int ICON_TILE = 64;
    public static final int SHEET_W = 2048;
    public static final int SHEET_H = 4096;
    public static final int COLS = SHEET_W / ICON_TILE; // 32

    public static final ResourceLocation SHEET0 = new ResourceLocation(CraftHeraldry.MODID, "textures/icons/iconSheet0.png");
    public static final ResourceLocation SHEET1 = new ResourceLocation(CraftHeraldry.MODID, "textures/icons/iconSheet1.png");

    private HeraldryRender() {}

    public static void renderCrestInGui(GuiGraphics gfx, CrestData crest, int x, int y, int size) {
        int icon = Math.max(0, crest.icon);
        int col = icon % COLS;
        int row = icon / COLS;

        float u0 = (col * ICON_TILE) / (float) SHEET_W;
        float u1 = ((col + 1) * ICON_TILE) / (float) SHEET_W;
        float v0 = (row * ICON_TILE) / (float) SHEET_H;
        float v1 = ((row + 1) * ICON_TILE) / (float) SHEET_H;

        // background
        setTint(crest.backgroundColor);
        RenderSystem.setShaderTexture(0, SHEET0);
        blitUv(gfx, x, y, size, size, u0, v0, u1, v1);

        // foreground
        setTint(crest.foregroundColor);
        RenderSystem.setShaderTexture(0, SHEET1);
        blitUv(gfx, x, y, size, size, u0, v0, u1, v1);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static void setTint(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        RenderSystem.setShaderColor(r, g, b, 1f);
    }

    private static void blitUv(GuiGraphics gfx, int x, int y, int w, int h, float u0, float v0, float u1, float v1) {
        // GuiGraphics#blit takes pixel UV sizes; easiest is use the overload with u/v in pixels.
        int pxU = (int) (u0 * SHEET_W);
        int pxV = (int) (v0 * SHEET_H);
        gfx.blit(RenderSystem.getShaderTexture(0), x, y, pxU, pxV, w, h, SHEET_W, SHEET_H);
    }

    public static void renderCrestOnQuad(MultiBufferSource buffers, PoseStack poseStack, CrestData crest,
                                        float x0, float y0, float x1, float y1,
                                        int packedLight, int packedOverlay) {

        int icon = Math.max(0, crest.icon);
        int col = icon % COLS;
        int row = icon / COLS;

        float u0 = (col * ICON_TILE) / (float) SHEET_W;
        float u1 = ((col + 1) * ICON_TILE) / (float) SHEET_W;
        float v0 = (row * ICON_TILE) / (float) SHEET_H;
        float v1 = ((row + 1) * ICON_TILE) / (float) SHEET_H;

        Matrix4f mat = poseStack.last().pose();

        // background layer
        VertexConsumer bg = buffers.getBuffer(RenderType.entityCutoutNoCull(SHEET0));
        quad(bg, mat, x0, y0, x1, y1, u0, v0, u1, v1, packedLight, packedOverlay, crest.backgroundColor);

        // foreground layer
        VertexConsumer fg = buffers.getBuffer(RenderType.entityCutoutNoCull(SHEET1));
        quad(fg, mat, x0, y0, x1, y1, u0, v0, u1, v1, packedLight, packedOverlay, crest.foregroundColor);
    }

    private static void quad(VertexConsumer vc, Matrix4f mat, float x0, float y0, float x1, float y1,
                             float u0, float v0, float u1, float v1,
                             int light, int overlay, int rgb) {

        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;

        vc.vertex(mat, x0, y1, 0).color(r, g, b, 1f).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
        vc.vertex(mat, x1, y1, 0).color(r, g, b, 1f).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
        vc.vertex(mat, x1, y0, 0).color(r, g, b, 1f).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
        vc.vertex(mat, x0, y0, 0).color(r, g, b, 1f).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
    }
}
