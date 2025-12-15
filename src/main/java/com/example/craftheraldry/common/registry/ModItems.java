package com.example.craftheraldry.common.registry;

import com.example.craftheraldry.CraftHeraldry;
import com.example.craftheraldry.common.item.ScrollItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, CraftHeraldry.MODID);

    public static final RegistryObject<Item> BANNER =
        ITEMS.register("banner", () -> new BlockItem(ModBlocks.BANNER.get(), new Item.Properties()));

    public static final RegistryObject<Item> SCROLL =
        ITEMS.register("scroll", () -> new ScrollItem(new Item.Properties().stacksTo(1)));
}
