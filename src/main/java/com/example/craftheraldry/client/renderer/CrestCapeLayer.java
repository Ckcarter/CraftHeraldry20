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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Forge-safe custom cape layer that keeps vanilla-like math but adds client-side smoothing
 * so the cape doesn't "flap" unnaturally in modpacks/servers.
 *
 * Key idea:
 * - We compute target rotations (pitch + roll) each frame
 * - Then we exponentially smooth them per-player before applying to the PoseStack.
 *
 * This replaces reliance on vanilla internal bob fields (which are not safely accessible here).
 */
public class CrestCapeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final Map<UUID, SmoothState> SMOOTH = new ConcurrentHashMap<>();

    // Smaller = smoother (less flappy). 0.12-0.22 are good.
    private static final float SMOOTH_ALPHA = 0.16F;

    // Heavier-feel tuning (keeps it natural)
    private static final float WALK_AMP = 14.0F;      // was 18/32
    private static final float F1_MAX = 20.0F;        // tighter clamp
    private static final float F2_MAX = 130.0F;       // slightly less aggressive forward swing

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
        // Vanilla cape spacing: cape is rendered in model space, then nudged back by 0.125.
        // This is the same offset Mojang uses so it sits correctly relative to armor.
        ps.translate(0.0D, 0.0D, 0.125D);

        double d0 = Mth.lerp(partialTicks, player.xCloakO, player.xCloak) - Mth.lerp(partialTicks, player.xo, player.getX());
        double d1 = Mth.lerp(partialTicks, player.yCloakO, player.yCloak) - Mth.lerp(partialTicks, player.yo, player.getY());
        double d2 = Mth.lerp(partialTicks, player.zCloakO, player.zCloak) - Mth.lerp(partialTicks, player.zo, player.getZ());

        float bodyYaw = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        float sin = Mth.sin(bodyYaw * ((float)Math.PI / 180F));
        float cos = -Mth.cos(bodyYaw * ((float)Math.PI / 180F));

        // Base pitch from vertical cloak motion
        float f1 = (float)d1 * 10.0F;
        f1 = Mth.clamp(f1, -6.0F, F1_MAX);

        // Forward/back swing (kept mostly vanilla)
        float f2 = (float)(d0 * sin + d2 * cos) * 100.0F;
        f2 = Mth.clamp(f2, 0.0F, F2_MAX);

        // Side roll
        float f3 = (float)(d0 * cos - d2 * sin) * 100.0F;
        f3 = Mth.clamp(f3, -20.0F, 20.0F);

        // Walk bounce (damped)
        float amt = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        amt *= amt;
        f1 += Mth.sin(limbSwing * 1.0F) * WALK_AMP * amt;

        if (player.isCrouching()) {
            f1 += 25.0F;
            // Vanilla crouch compensation
            ps.translate(0.0D, 0.2D, 0.0D);
        }

        // --- smoothing (per player) ---
        SmoothState s = SMOOTH.computeIfAbsent(player.getUUID(), id -> new SmoothState());
        // reset smoothing when the player is newly created (teleport/dimension changes can jump values)
        if (s.lastAge > ageInTicks + 10.0F || s.lastAge == 0.0F) {
            s.pitch = f1;
            s.roll = f3;
        } else {
            s.pitch = Mth.lerp(SMOOTH_ALPHA, s.pitch, f1);
            s.roll  = Mth.lerp(SMOOTH_ALPHA, s.roll,  f3);
        }
        s.lastAge = ageInTicks;

        // Apply rotations in the same order as vanilla CapeLayer (X, Z, then Y).
        ps.mulPose(Axis.XP.rotationDegrees(6.0F + f2 / 2.0F + s.pitch));
        ps.mulPose(Axis.ZP.rotationDegrees(s.roll / 2.0F));
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - (s.roll / 2.0F)));

        VertexConsumer vc = buf.getBuffer(RenderType.entitySolid(capeTex));
        this.getParentModel().renderCloak(ps, vc, light, OverlayTexture.NO_OVERLAY);

        ps.popPose();
    }

    private static final class SmoothState {
        float pitch;
        float roll;
        float lastAge;
    }
}