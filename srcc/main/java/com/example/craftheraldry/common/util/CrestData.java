package com.example.craftheraldry.common.util;

import net.minecraft.nbt.CompoundTag;

public record CrestData(int color1, int color2, short icon) {

    public static final String NBT_KEY = "Crest";

    public static final String TAG_COLOR1 = "color1";
    public static final String TAG_COLOR2 = "color2";
    public static final String TAG_ICON = "icon";

    public static CrestData defaultBlank() {
        return new CrestData(0xFFFFFF, 0xFFFFFF, (short) -1);
    }

    public CrestData copy() {
        return new CrestData(color1, color2, icon);
    }

    public CrestData withColors(int c1, int c2) {
        return new CrestData(c1 & 0xFFFFFF, c2 & 0xFFFFFF, icon);
    }

    public CrestData withIcon(short i) {
        return new CrestData(color1, color2, i);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_COLOR1, color1);
        tag.putInt(TAG_COLOR2, color2);
        tag.putShort(TAG_ICON, icon);
        return tag;
    }

    public static CrestData fromTag(CompoundTag tag) {
        if (tag == null) return defaultBlank();
        int c1 = tag.contains(TAG_COLOR1) ? tag.getInt(TAG_COLOR1) : 0xFFFFFF;
        int c2 = tag.contains(TAG_COLOR2) ? tag.getInt(TAG_COLOR2) : 0xFFFFFF;
        short ic = tag.contains(TAG_ICON) ? tag.getShort(TAG_ICON) : (short) -1;
        return new CrestData(c1, c2, ic);
    }
}
