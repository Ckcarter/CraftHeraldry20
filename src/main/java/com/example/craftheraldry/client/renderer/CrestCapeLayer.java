package com.example.craftheraldry.client.renderer;

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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;

/**
 * Vanilla-style cape layer that renders a thin cloth cape with the same sway/bob math as Mojang's
 * CapeLayer, but using the crest synced from the server.
 */
public class CrestCapeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

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

        if (!player.isModelPartShown(PlayerModelPart.CAPE)) return;
        if (player.isInvisible()) return;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof ElytraItem) return;

        CrestData crest = CapeClientCache.get(player.getUUID());
        if (crest == null || crest.icon() < 0) return;

        ResourceLocation capeTex = CapeClientCache.getCapeTexture(player.getUUID());
        if (capeTex == null) return;

        ps.pushPose();

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

        // Walk bounce (safe: uses render params)
        f1 += Mth.sin(limbSwing * 6.0F) * 32.0F * limbSwingAmount;

        if (player.isCrouching()) {
            f1 += 25.0F;
            ps.translate(0.0F, 0.125F, 0.0F);
        }

        ps.mulPose(Axis.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
        ps.mulPose(Axis.ZP.rotationDegrees(f3 / 2.0F));
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - f3 / 2.0F));

        VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucent(capeTex));

        // ✅ FIX: call PlayerModel’s built-in cloak renderer (no private field access)
        this.getParentModel().renderCloak(ps, vc, light, OverlayTexture.NO_OVERLAY);

        ps.popPose();
    }
}
