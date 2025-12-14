package com.example.craftheraldry;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Network {
    private Network() {}

    private static final String PROTOCOL = "1";
    public static SimpleChannel CHANNEL;

    public static void init() {
        CHANNEL = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(CraftHeraldry.MODID, "main"))
                .clientAcceptedVersions(PROTOCOL::equals)
                .serverAcceptedVersions(PROTOCOL::equals)
                .networkProtocolVersion(() -> PROTOCOL)
                .simpleChannel();

            int id = 0;

    CHANNEL.messageBuilder(PacketChangeCrest.class, id++)
            .encoder(PacketChangeCrest::encode)
            .decoder(PacketChangeCrest::decode)
            .consumerMainThread(PacketChangeCrest::handle)
            .add();

    CHANNEL.messageBuilder(PacketChangeCrestItem.class, id++)
            .encoder(PacketChangeCrestItem::encode)
            .decoder(PacketChangeCrestItem::decode)
            .consumerMainThread(PacketChangeCrestItem::handle)
            .add();

    CHANNEL.messageBuilder(PacketChangeCrestBlock.class, id++)
            .encoder(PacketChangeCrestBlock::encode)
            .decoder(PacketChangeCrestBlock::decode)
            .consumerMainThread(PacketChangeCrestBlock::handle)
            .add();
}

}
