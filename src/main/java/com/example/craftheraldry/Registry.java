package com.example.craftheraldry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class Registry {
    private Registry() {}

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CraftHeraldry.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CraftHeraldry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CraftHeraldry.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CraftHeraldry.MODID);

    public static final RegistryObject<Block> HERALDRY_BANNER_BLOCK = BLOCKS.register("heraldry_banner", HeraldryBannerBlock::new);

    public static final RegistryObject<Item> HERALDRY_SCROLL = ITEMS.register("heraldry_scroll",
            () -> new HeraldryScrollItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> HERALDRY_BANNER_ITEM = ITEMS.register("heraldry_banner",
            () -> new BlockItem(HERALDRY_BANNER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<HeraldryBannerBlockEntity>> HERALDRY_BANNER_BE =
            BLOCK_ENTITIES.register("heraldry_banner",
                    () -> BlockEntityType.Builder.of(HeraldryBannerBlockEntity::new, HERALDRY_BANNER_BLOCK.get()).build(null));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("craftheraldry",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("CraftHeraldry"))
                    .icon(() -> HERALDRY_SCROLL.get().getDefaultInstance())
                    .displayItems((params, out) -> {
                        out.accept(HERALDRY_SCROLL.get());
                        out.accept(HERALDRY_BANNER_ITEM.get());
                    })
                    .build());

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
        TABS.register(bus);
    }
}
