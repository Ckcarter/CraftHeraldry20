package com.example.craftheraldry.client;

import com.example.craftheraldry.CraftHeraldry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Simple client-only hook to open the screen via a client command:
 * /cresteditor
 */
@Mod.EventBusSubscriber(modid = CraftHeraldry.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(net.minecraft.commands.Commands.literal("cresteditor")
                .executes(ctx -> {
                    var mc = net.minecraft.client.Minecraft.getInstance();
                    mc.setScreen(new CrestCreatorScreen(mc.player == null ? net.minecraft.world.item.ItemStack.EMPTY : mc.player.getMainHandItem()));
                    return 1;
                }));
    }
}
