package com.example.craftheraldry.common.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Stores/retrieves the player's equipped cape crest in persistent player NBT.
 * This does NOT use any equipment slot.
 */
public final class CapeData {
    private CapeData() {}

    public static final String TAG_CAPE = "CraftHeraldryCape";
    public static final String TAG_CREST = "crest";

    public static boolean hasCape(Player player) {
        CompoundTag tag = player.getPersistentData();
        return tag.contains(TAG_CAPE, net.minecraft.nbt.Tag.TAG_COMPOUND);
    }

    public static CrestData getCapeCrest(Player player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains(TAG_CAPE, net.minecraft.nbt.Tag.TAG_COMPOUND)) return null;
        CompoundTag cape = tag.getCompound(TAG_CAPE);
        if (!cape.contains(TAG_CREST, net.minecraft.nbt.Tag.TAG_COMPOUND)) return null;
        return CrestData.fromTag(cape.getCompound(TAG_CREST));
    }

    public static void setCape(Player player, CrestData crest) {
        CompoundTag root = player.getPersistentData();
        CompoundTag cape = new CompoundTag();
        cape.put(TAG_CREST, crest.toTag());
        root.put(TAG_CAPE, cape);
    }

    public static void clearCape(Player player) {
        player.getPersistentData().remove(TAG_CAPE);
    }
}
