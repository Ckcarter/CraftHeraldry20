package com.example.craftheraldry;

import net.minecraft.nbt.CompoundTag;

public class CrestData {
    public int backgroundColor; // RGB 0xRRGGBB
    public int foregroundColor; // RGB 0xRRGGBB
    public int icon;            // index into icon list

    public CrestData(int backgroundColor, int foregroundColor, int icon) {
        this.backgroundColor = backgroundColor & 0xFFFFFF;
        this.foregroundColor = foregroundColor & 0xFFFFFF;
        this.icon = icon;
    }

    public static CrestData defaultCrest() {
        return new CrestData(0x000000, 0xFFFFFF, 0);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Bg", backgroundColor & 0xFFFFFF);
        tag.putInt("Fg", foregroundColor & 0xFFFFFF);
        tag.putInt("Icon", icon);
        return tag;
    }

    public static CrestData fromTag(CompoundTag tag) {
        if (tag == null) return null;
        return new CrestData(tag.getInt("Bg"), tag.getInt("Fg"), tag.getInt("Icon"));
    }
}
