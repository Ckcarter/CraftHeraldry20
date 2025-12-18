package com.example.craftheraldry.common.network;

import com.example.craftheraldry.CraftHeraldry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    public static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(CraftHeraldry.MODID, "main"),
        () -> PROTOCOL,
        PROTOCOL::equals,
        PROTOCOL::equals
    );

    private static int index = 0;

    public static void init() {
        CHANNEL.registerMessage(index++, ScrollUpdatePacket.class, ScrollUpdatePacket::encode, ScrollUpdatePacket::decode, ScrollUpdatePacket::handle);
        CHANNEL.registerMessage(index++, CapeSyncPacket.class, CapeSyncPacket::encode, CapeSyncPacket::decode, CapeSyncPacket::handle);
    }
}
