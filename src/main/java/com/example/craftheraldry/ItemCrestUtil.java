package com.example.craftheraldry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class ItemCrestUtil {
    private ItemCrestUtil() {}

    private static final String CREST_KEY = "CraftHeraldryCrest";

    public static CrestData readCrest(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(CREST_KEY)) return null;
        return CrestData.fromTag(tag.getCompound(CREST_KEY));
    }

    public static void writeCrest(ItemStack stack, CrestData crest) {
        if (stack == null || stack.isEmpty() || crest == null) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(CREST_KEY, crest.toTag());
    }
}
