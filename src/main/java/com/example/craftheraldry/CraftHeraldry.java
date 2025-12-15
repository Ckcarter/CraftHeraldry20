package com.example.craftheraldry;

import com.example.craftheraldry.client.ClientInit;
import com.example.craftheraldry.common.network.ModNetwork;
import com.example.craftheraldry.common.registry.ModBlockEntities;
import com.example.craftheraldry.common.registry.ModBlocks;
import com.example.craftheraldry.common.registry.ModItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CraftHeraldry.MODID)
public class CraftHeraldry {
    public static final String MODID = "craftheraldry";

    public CraftHeraldry() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModBlockEntities.BLOCK_ENTITIES.register(bus);

        ModNetwork.init();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientInit::init);
    }
}
