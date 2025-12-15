package com.example.craftheraldry.client;

import com.example.craftheraldry.CraftHeraldry;
import com.example.craftheraldry.client.renderer.BannerBlockEntityRenderer;
import com.example.craftheraldry.common.registry.ModBlockEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

@Mod.EventBusSubscriber(modid = CraftHeraldry.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInit {
    public static void init() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> BlockEntityRenderers.register(ModBlockEntities.BANNER.get(), BannerBlockEntityRenderer::new));
    }
}
