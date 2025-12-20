package com.example.craftheraldry.common;

import com.example.craftheraldry.CraftHeraldry;
import com.example.craftheraldry.common.network.CapeSyncPacket;
import com.example.craftheraldry.common.network.ModNetwork;
import com.example.craftheraldry.common.util.CapeData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * Keeps clients in sync about which players have the cosmetic cape equipped.
 */
@Mod.EventBusSubscriber(modid = CraftHeraldry.MODID)
public class CapeSyncEvents {

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        // 1) Always sync the joining player's own cape state to themselves.
        var selfCrest = CapeData.getCapeCrest(sp);
        if (selfCrest != null && selfCrest.icon() >= 0) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new CapeSyncPacket(sp.getUUID(), selfCrest));
        } else {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new CapeSyncPacket(sp.getUUID(), false, null));
        }

        // 2) Also push currently-online players' capes to the joining player.
        // Without this, you might not see existing capes until tracking kicks in.
        for (ServerPlayer other : sp.serverLevel().players()) {
            if (other == sp) continue;
            var crest = CapeData.getCapeCrest(other);
            if (crest != null && crest.icon() >= 0) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new CapeSyncPacket(other.getUUID(), crest));
            }
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        // Re-sync after respawn; some modpacks reset client state aggressively.
        var crest = CapeData.getCapeCrest(sp);
        if (crest != null && crest.icon() >= 0) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new CapeSyncPacket(sp.getUUID(), crest));
        } else {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new CapeSyncPacket(sp.getUUID(), false, null));
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        // Re-sync after changing dimensions so the client cache stays correct.
        var crest = CapeData.getCapeCrest(sp);
        if (crest != null && crest.icon() >= 0) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new CapeSyncPacket(sp.getUUID(), crest));
        } else {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new CapeSyncPacket(sp.getUUID(), false, null));
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer tracker)) return;
        if (!(event.getTarget() instanceof ServerPlayer target)) return;

        var crest = CapeData.getCapeCrest(target);
        if (crest != null && crest.icon() >= 0) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> tracker), new CapeSyncPacket(target.getUUID(), crest));
        } else {
            // Ensure tracker clears stale cache entry if any
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> tracker), new CapeSyncPacket(target.getUUID(), false, null));
        }
    }
}
