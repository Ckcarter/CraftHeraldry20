package com.example.craftheraldry.common.network;

import com.example.craftheraldry.common.util.CrestData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Syncs a player's equipped cape crest to clients.
 * If 'hasCape' is false, the client should clear that player's cape entry.
 */
public class CapeSyncPacket {
    private final UUID playerId;
    private final boolean hasCape;
    private final CrestData crest;

    public CapeSyncPacket(UUID playerId, CrestData crest) {
        this.playerId = playerId;
        this.hasCape = crest != null && crest.icon() >= 0;
        this.crest = crest;
    }

    public CapeSyncPacket(UUID playerId, boolean hasCape, CrestData crest) {
        this.playerId = playerId;
        this.hasCape = hasCape;
        this.crest = crest;
    }

    public static void encode(CapeSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerId);
        buf.writeBoolean(msg.hasCape);
        if (msg.hasCape && msg.crest != null) {
            buf.writeNbt(msg.crest.toTag());
        }
    }

    public static CapeSyncPacket decode(FriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        boolean has = buf.readBoolean();
        CrestData crest = null;
        if (has) {
            var tag = buf.readNbt();
            crest = CrestData.fromTag(tag);
        }
        return new CapeSyncPacket(id, has, crest);
    }

    public static void handle(CapeSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> com.example.craftheraldry.client.cape.CapeClientCache.applySync(msg.playerId, msg.hasCape, msg.crest)));
        ctx.get().setPacketHandled(true);
    }
}
