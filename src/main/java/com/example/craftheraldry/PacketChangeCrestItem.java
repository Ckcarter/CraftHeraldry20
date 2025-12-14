package com.example.craftheraldry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketChangeCrestItem {
    public final int bg, fg, icon;
    public final int handOrdinal;

    public PacketChangeCrestItem(CrestData crest, InteractionHand hand) {
        this.bg = crest.backgroundColor & 0xFFFFFF;
        this.fg = crest.foregroundColor & 0xFFFFFF;
        this.icon = crest.icon;
        this.handOrdinal = hand.ordinal();
    }

    public PacketChangeCrestItem(int bg, int fg, int icon, int handOrdinal) {
        this.bg = bg & 0xFFFFFF;
        this.fg = fg & 0xFFFFFF;
        this.icon = icon;
        this.handOrdinal = handOrdinal;
    }

    public static void encode(PacketChangeCrestItem msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.bg);
        buf.writeInt(msg.fg);
        buf.writeInt(msg.icon);
        buf.writeInt(msg.handOrdinal);
    }

    public static PacketChangeCrestItem decode(FriendlyByteBuf buf) {
        return new PacketChangeCrestItem(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void handle(PacketChangeCrestItem msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            InteractionHand hand = msg.handOrdinal == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            var stack = player.getItemInHand(hand);
            if (stack.isEmpty()) return;

            CrestData crest = new CrestData(msg.bg, msg.fg, msg.icon);
            ItemCrestUtil.writeCrest(stack, crest);
        });
        ctx.get().setPacketHandled(true);
    }
}
