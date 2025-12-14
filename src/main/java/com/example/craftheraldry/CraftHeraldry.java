package com.example.craftheraldry;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.IEventBus;

@Mod("craftheraldry")
public class CraftHeraldry {
    public static final String MODID = "craftheraldry";

    public CraftHeraldry() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Registry.init(bus);
        Network.init();
        // This project is a scaffold: register items/blocks/etc. as needed.
    }
}
