package com.example.craftheraldry.common.network;

import com.example.craftheraldry.common.item.ScrollItem;
import com.example.craftheraldry.common.util.CrestData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ScrollUpdatePacket {
    private final CrestData crest;

    public ScrollUpdatePacket(CrestData crest) {
        this.crest = crest;
    }

    public static void encode(ScrollUpdatePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.crest.color1());
        buf.writeInt(msg.crest.color2());
        buf.writeShort(msg.crest.icon());
    }

    public static ScrollUpdatePacket decode(FriendlyByteBuf buf) {
        int c1 = buf.readInt();
        int c2 = buf.readInt();
        short icon = buf.readShort();
        return new ScrollUpdatePacket(new CrestData(c1, c2, icon));
    }

    public static void handle(ScrollUpdatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var sender = ctx.get().getSender();
            if (sender == null) return;

            var stack = sender.getMainHandItem();
            if (stack.getItem() instanceof ScrollItem) {
                ScrollItem.setCrest(stack, msg.crest);
            } else {
                stack = sender.getOffhandItem();
                if (stack.getItem() instanceof ScrollItem) {
                    ScrollItem.setCrest(stack, msg.crest);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
