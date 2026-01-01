package com.example.craftheraldry.common.network;

import com.example.craftheraldry.common.item.ScrollItem;
import com.example.craftheraldry.common.util.CrestData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> Server: updates the crest stored on the scroll currently held by the player.
 */
public class ScrollUpdatePacket {
    private final CrestData crest;

    public ScrollUpdatePacket(CrestData crest) {
        this.crest = crest;
    }

    public static void encode(ScrollUpdatePacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.crest == null ? null : msg.crest.toTag());
    }

    public static ScrollUpdatePacket decode(FriendlyByteBuf buf) {
        var tag = buf.readNbt();
        return new ScrollUpdatePacket(CrestData.fromTag(tag));
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
