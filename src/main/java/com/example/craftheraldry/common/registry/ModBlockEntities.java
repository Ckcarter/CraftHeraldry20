package com.example.craftheraldry.common.registry;

import com.example.craftheraldry.CraftHeraldry;
import com.example.craftheraldry.common.blockentity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CraftHeraldry.MODID);

    public static final RegistryObject<BlockEntityType<BannerBlockEntity>> BANNER =
        BLOCK_ENTITIES.register("banner",
            () -> BlockEntityType.Builder.of(
                    BannerBlockEntity::new,
                    ModBlocks.BANNER.get(),
                    ModBlocks.WALL_BANNER.get()

                    ).build(null)
        );
}
