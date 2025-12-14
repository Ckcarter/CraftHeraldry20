package com.example.craftheraldry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Minimal "send crest to server" packet.
 * In a real mod you would also include WHICH item/slot is being edited,
 * and validate permissions and bounds.
 */
public class PacketChangeCrest {
    public final int bg;
    public final int fg;
    public final int icon;

    public PacketChangeCrest(CrestData crest) {
        this.bg = crest.backgroundColor & 0xFFFFFF;
        this.fg = crest.foregroundColor & 0xFFFFFF;
        this.icon = crest.icon;
    }

    public PacketChangeCrest(int bg, int fg, int icon) {
        this.bg = bg & 0xFFFFFF;
        this.fg = fg & 0xFFFFFF;
        this.icon = icon;
    }

    public static void encode(PacketChangeCrest msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.bg);
        buf.writeInt(msg.fg);
        buf.writeInt(msg.icon);
    }

    public static PacketChangeCrest decode(FriendlyByteBuf buf) {
        return new PacketChangeCrest(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void handle(PacketChangeCrest msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // TODO: apply to an item / tile / capability.
            // For now, just store it on the player's persistent NBT as a demo.
            var data = player.getPersistentData();
            var crest = new CrestData(msg.bg, msg.fg, msg.icon);
            data.put("CraftHeraldry_LastCrest", crest.toTag());
        });
        ctx.get().setPacketHandled(true);
    }
}
