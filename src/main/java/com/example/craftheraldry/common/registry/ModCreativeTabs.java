package com.example.craftheraldry.common.registry;

import com.example.craftheraldry.CraftHeraldry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Creative tab so items show up in Creative inventory and aren't hidden by JEI's
 * "hide items not in creative tabs" setting.
 */
public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CraftHeraldry.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + CraftHeraldry.MODID + ".main"))
                    .icon(() -> new ItemStack(ModItems.SCROLL.get()))
                    .displayItems((params, output) -> {
                        // Items
                        output.accept(ModItems.SCROLL.get());
                        output.accept(ModItems.BANNER.get());
                        // Blocks (so they can be found easily too)
                        output.accept(ModBlocks.BANNER.get());
                        output.accept(ModBlocks.WALL_BANNER.get());
                    })
                    .build()
    );
}
