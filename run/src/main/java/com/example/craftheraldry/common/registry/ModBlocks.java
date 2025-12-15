package com.example.craftheraldry.common.registry;

import com.example.craftheraldry.CraftHeraldry;
import com.example.craftheraldry.common.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, CraftHeraldry.MODID);

    public static final RegistryObject<Block> BANNER =
        BLOCKS.register("banner", () -> new BannerBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion()
        ));
}
