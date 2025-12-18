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

        // Sync to self for any already-equipped cape (e.g., after reconnect).
        var crest = CapeData.getCapeCrest(sp);
        if (crest != null && crest.icon() >= 0) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new CapeSyncPacket(sp.getUUID(), crest));
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
