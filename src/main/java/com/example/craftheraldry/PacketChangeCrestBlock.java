package com.example.craftheraldry;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketChangeCrestBlock {
    public final int bg, fg, icon;
    public final BlockPos pos;

    public PacketChangeCrestBlock(CrestData crest, BlockPos pos) {
        this.bg = crest.backgroundColor & 0xFFFFFF;
        this.fg = crest.foregroundColor & 0xFFFFFF;
        this.icon = crest.icon;
        this.pos = pos;
    }

    public PacketChangeCrestBlock(int bg, int fg, int icon, BlockPos pos) {
        this.bg = bg & 0xFFFFFF;
        this.fg = fg & 0xFFFFFF;
        this.icon = icon;
        this.pos = pos;
    }

    public static void encode(PacketChangeCrestBlock msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.bg);
        buf.writeInt(msg.fg);
        buf.writeInt(msg.icon);
        buf.writeBlockPos(msg.pos);
    }

    public static PacketChangeCrestBlock decode(FriendlyByteBuf buf) {
        return new PacketChangeCrestBlock(buf.readInt(), buf.readInt(), buf.readInt(), buf.readBlockPos());
    }

    public static void handle(PacketChangeCrestBlock msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            var level = player.level();
            var be = level.getBlockEntity(msg.pos);
            if (be instanceof HeraldryBannerBlockEntity bannerBe) {
                bannerBe.setCrest(new CrestData(msg.bg, msg.fg, msg.icon));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
