package com.example.craftheraldry.client.renderer;

import com.example.craftheraldry.CraftHeraldry;
import com.example.craftheraldry.client.cape.CapeClientCache;
import com.example.craftheraldry.common.util.CrestData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Renders a cosmetic cape using the crest stored on the server for the player.
 * Cape data is synced to clients with CapeSyncPacket and cached in CapeClientCache.
 */
public class CrestCapeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation CLOTH_BASE =
            new ResourceLocation(CraftHeraldry.MODID, "textures/entity/banner_cloth.png");
    private static final ResourceLocation SHEET0 =
            new ResourceLocation(CraftHeraldry.MODID, "textures/icons/icon_sheet_0.png");
    private static final ResourceLocation SHEET1 =
            new ResourceLocation(CraftHeraldry.MODID, "textures/icons/icon_sheet_1.png");

    public CrestCapeLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack ps, MultiBufferSource buf, int light, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        CrestData crest = CapeClientCache.get(player.getUUID());
        if (crest == null || crest.icon() < 0) return;
        if (player.isInvisible()) return;

        ps.pushPose();

        // Vanilla-like cape sway/bob (adapted from CapeLayer).
        ps.translate(0.0F, 0.0F, 0.125F);

        double d0 = Mth.lerp(partialTicks, player.xCloakO, player.xCloak) - Mth.lerp(partialTicks, player.xo, player.getX());
        double d1 = Mth.lerp(partialTicks, player.yCloakO, player.yCloak) - Mth.lerp(partialTicks, player.yo, player.getY());
        double d2 = Mth.lerp(partialTicks, player.zCloakO, player.zCloak) - Mth.lerp(partialTicks, player.zo, player.getZ());

        float bodyYaw = Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        float sin = Mth.sin(bodyYaw * ((float)Math.PI / 180F));
        float cos = -Mth.cos(bodyYaw * ((float)Math.PI / 180F));

        float f1 = (float)d1 * 10.0F;
        f1 = Mth.clamp(f1, -6.0F, 32.0F);

        float f2 = (float)(d0 * sin + d2 * cos) * 100.0F;
        f2 = Mth.clamp(f2, 0.0F, 150.0F);

        float f3 = (float)(d0 * cos - d2 * sin) * 100.0F;
        f3 = Mth.clamp(f3, -20.0F, 20.0F);

        if (f2 < 0.0F) f2 = 0.0F;

        float bob = Mth.lerp(partialTicks, player.oBob, player.bob);
        f1 += Mth.sin(Mth.lerp(partialTicks, player.walkDistO, player.walkDist) * 6.0F) * 32.0F * bob;

        if (player.isCrouching()) {
            f1 += 25.0F;
            ps.translate(0.0F, 0.125F, 0.0F);
        }

        ps.mulPose(Axis.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
        ps.mulPose(Axis.ZP.rotationDegrees(f3 / 2.0F));
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - f3 / 2.0F));

        // Cape quad (simple rectangle), rendered as cloth + 2 crest layers, both sides.
        float x0 = -5f / 16f;
        float x1 =  5f / 16f;
        float y0 =  0f / 16f;
        float y1 = 16f / 16f;
        float z  =  0f;

        VertexConsumer base = buf.getBuffer(RenderType.entityCutoutNoCull(CLOTH_BASE));
        // Front face (+Z)
        putQuad(ps, base, x0, y0, x1, y1, z + 0.0006f,  1.0f, 0f, 0f, 1f, 1f, 0xFFFFFFFF, light);
        // Back face (-Z) with flipped U so the texture reads the same from behind
        putQuad(ps, base, x0, y0, x1, y1, z - 0.0006f, -1.0f, 1f, 0f, 0f, 1f, 0xFFFFFFFF, light);

        int icon = crest.icon();
        int col = icon % 32;
        int row = icon / 32;
        float u0 = (col * 64f) / 2048f;
        float v0 = (row * 64f) / 4096f;
        float u1 = ((col + 1) * 64f) / 2048f;
        float v1 = ((row + 1) * 64f) / 4096f;

        VertexConsumer vc0 = buf.getBuffer(RenderType.entityCutoutNoCull(SHEET0));
        // Crest layer 0 (color1) on both sides
        putQuad(ps, vc0, x0, y0, x1, y1, z + 0.0018f,  1.0f, u0, v0, u1, v1, crest.color1(), light);
        putQuad(ps, vc0, x0, y0, x1, y1, z - 0.0018f, -1.0f, u1, v0, u0, v1, crest.color1(), light);

VertexConsumer vc1 = buf.getBuffer(RenderType.entityCutoutNoCull(SHEET1));
        // Crest layer 1 (color2) on both sides
        putQuad(ps, vc1, x0, y0, x1, y1, z + 0.0026f,  1.0f, u0, v0, u1, v1, crest.color2(), light);
        putQuad(ps, vc1, x0, y0, x1, y1, z - 0.0026f, -1.0f, u1, v0, u0, v1, crest.color2(), light);

ps.popPose();
    }

    private 
    // Default quad (front-facing normal +Z)
    static void putQuad(PoseStack ps, VertexConsumer vc,
                        float x0, float y0, float x1, float y1, float z,
                        float u0, float v0, float u1, float v1,
                        int color, int light) {
        putQuad(ps, vc, x0, y0, x1, y1, z, 1.0f, u0, v0, u1, v1, color, light);
    }

    // Quad with explicit normal Z (use -1 for back face so lighting is correct)
    static void putQuad(PoseStack ps, VertexConsumer vc,
                        float x0, float y0, float x1, float y1, float z,
                        float nz,
                        float u0, float v0, float u1, float v1,
                        int color, int light) {

        Matrix4f pose = ps.last().pose();
        Matrix3f normal = ps.last().normal();

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        int ov = OverlayTexture.NO_OVERLAY;

        vc.vertex(pose, x0, y1, z).color(r,g,b,1f).uv(u0,v1).overlayCoords(ov).uv2(light).normal(normal, 0,0,nz).endVertex();
        vc.vertex(pose, x1, y1, z).color(r,g,b,1f).uv(u1,v1).overlayCoords(ov).uv2(light).normal(normal, 0,0,nz).endVertex();
        vc.vertex(pose, x1, y0, z).color(r,g,b,1f).uv(u1,v0).overlayCoords(ov).uv2(light).normal(normal, 0,0,nz).endVertex();

        vc.vertex(pose, x1, y0, z).color(r,g,b,1f).uv(u1,v0).overlayCoords(ov).uv2(light).normal(normal, 0,0,nz).endVertex();
        vc.vertex(pose, x0, y0, z).color(r,g,b,1f).uv(u0,v0).overlayCoords(ov).uv2(light).normal(normal, 0,0,nz).endVertex();
        vc.vertex(pose, x0, y1, z).color(r,g,b,1f).uv(u0,v1).overlayCoords(ov).uv2(light).normal(normal, 0,0,nz).endVertex();
    }
}
