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
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Vanilla-style cape layer that renders a thin cloth cape with the same sway/bob math as Mojang's
 * CapeLayer, but using the crest synced from the server.
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
    public void render(PoseStack ps,
                       MultiBufferSource buf,
                       int light,
                       AbstractClientPlayer player,
                       float limbSwing,
                       float limbSwingAmount,
                       float partialTicks,
                       float ageInTicks,
                       float netHeadYaw,
                       float headPitch) {

        // Must be enabled in Skin Customization.
        if (!player.isModelPartShown(PlayerModelPart.CAPE)) return;
        if (player.isInvisible()) return;

        // Hide when Elytra is equipped (vanilla behavior).
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof ElytraItem) return;

        CrestData crest = CapeClientCache.get(player.getUUID());
        if (crest == null || crest.icon() < 0) return;

        ps.pushPose();

        // --- Vanilla CapeLayer math (1.20.x style) ---
        // Slightly behind the player.
        ps.translate(0.0F, 0.0F, 0.125F);

        double d0 = Mth.lerp(partialTicks, player.xCloakO, player.xCloak) - Mth.lerp(partialTicks, player.xo, player.getX());
        double d1 = Mth.lerp(partialTicks, player.yCloakO, player.yCloak) - Mth.lerp(partialTicks, player.yo, player.getY());
        double d2 = Mth.lerp(partialTicks, player.zCloakO, player.zCloak) - Mth.lerp(partialTicks, player.zo, player.getZ());

        float bodyYaw = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        float sin = Mth.sin(bodyYaw * ((float) Math.PI / 180F));
        float cos = -Mth.cos(bodyYaw * ((float) Math.PI / 180F));

        float f1 = (float) d1 * 10.0F;
        f1 = Mth.clamp(f1, -6.0F, 32.0F);

        float f2 = (float) (d0 * sin + d2 * cos) * 100.0F;
        f2 = Mth.clamp(f2, 0.0F, 150.0F);

        float f3 = (float) (d0 * cos - d2 * sin) * 100.0F;
        f3 = Mth.clamp(f3, -20.0F, 20.0F);

        if (f2 < 0.0F) f2 = 0.0F;

        float bob = Mth.lerp(partialTicks, player.oBob, player.bob);
        float walk = Mth.lerp(partialTicks, player.walkDistO, player.walkDist);
        f1 += Mth.sin(walk * 6.0F) * 32.0F * bob;

        if (player.isCrouching()) {
            f1 += 25.0F;
            ps.translate(0.0F, 0.125F, 0.0F);
        }

        ps.mulPose(Axis.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
        ps.mulPose(Axis.ZP.rotationDegrees(f3 / 2.0F));
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - f3 / 2.0F));

        // --- Geometry: a thin 10px wide x 16px tall cape plane, double-sided ---
        // Vanilla-ish size.
        float x0 = -5f / 16f;
        float x1 =  5f / 16f;
        float y0 =  0f / 16f;
        float y1 = 16f / 16f;
        float z  =  0f;

        // Use translucent so alpha in the icon sheets works correctly.
        VertexConsumer base = buf.getBuffer(RenderType.entityTranslucent(CLOTH_BASE));
        drawDoubleSided(ps, base, x0, y0, x1, y1, z, 0f, 0f, 1f, 1f, 0xFFFFFFFF, light);

        // Crest icon UVs (64x64 icons packed in 2048x4096 sheets).
        int icon = crest.icon();
        int col = icon % 32;
        int row = icon / 32;
        float u0 = (col * 64f) / 2048f;
        float v0 = (row * 64f) / 4096f;
        float u1 = ((col + 1) * 64f) / 2048f;
        float v1 = ((row + 1) * 64f) / 4096f;

        VertexConsumer vc0 = buf.getBuffer(RenderType.entityTranslucent(SHEET0));
        drawDoubleSided(ps, vc0, x0, y0, x1, y1, z, u0, v0, u1, v1, crest.color1(), light);

        VertexConsumer vc1 = buf.getBuffer(RenderType.entityTranslucent(SHEET1));
        drawDoubleSided(ps, vc1, x0, y0, x1, y1, z, u0, v0, u1, v1, crest.color2(), light);

        ps.popPose();
    }

    /**
     * Draws the cape as two very-close quads so it is visible from both sides with correct lighting.
     * Back face flips U so the image reads the same when seen from behind.
     */
    private static void drawDoubleSided(PoseStack ps,
                                        VertexConsumer vc,
                                        float x0, float y0, float x1, float y1,
                                        float z,
                                        float u0, float v0, float u1, float v1,
                                        int color,
                                        int light) {
        // Front (+Z)
        putQuad(ps, vc, x0, y0, x1, y1, z + 0.0006f, 1.0f, u0, v0, u1, v1, color, light);
        // Back (-Z) with flipped U
        putQuad(ps, vc, x0, y0, x1, y1, z - 0.0006f, -1.0f, u1, v0, u0, v1, color, light);
    }

    private static void putQuad(PoseStack ps,
                                VertexConsumer vc,
                                float x0, float y0, float x1, float y1,
                                float z,
                                float nz,
                                float u0, float v0, float u1, float v1,
                                int color,
                                int light) {
        Matrix4f pose = ps.last().pose();
        Matrix3f normal = ps.last().normal();

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        int ov = OverlayTexture.NO_OVERLAY;

        // Two triangles.
        vc.vertex(pose, x0, y1, z).color(r, g, b, 1f).uv(u0, v1).overlayCoords(ov).uv2(light).normal(normal, 0, 0, nz).endVertex();
        vc.vertex(pose, x1, y1, z).color(r, g, b, 1f).uv(u1, v1).overlayCoords(ov).uv2(light).normal(normal, 0, 0, nz).endVertex();
        vc.vertex(pose, x1, y0, z).color(r, g, b, 1f).uv(u1, v0).overlayCoords(ov).uv2(light).normal(normal, 0, 0, nz).endVertex();

        vc.vertex(pose, x1, y0, z).color(r, g, b, 1f).uv(u1, v0).overlayCoords(ov).uv2(light).normal(normal, 0, 0, nz).endVertex();
        vc.vertex(pose, x0, y0, z).color(r, g, b, 1f).uv(u0, v0).overlayCoords(ov).uv2(light).normal(normal, 0, 0, nz).endVertex();
        vc.vertex(pose, x0, y1, z).color(r, g, b, 1f).uv(u0, v1).overlayCoords(ov).uv2(light).normal(normal, 0, 0, nz).endVertex();
    }
}
