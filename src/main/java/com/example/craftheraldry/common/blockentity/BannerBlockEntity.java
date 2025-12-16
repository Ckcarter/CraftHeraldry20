package com.example.craftheraldry.common.blockentity;

import com.example.craftheraldry.common.registry.ModBlockEntities;
import com.example.craftheraldry.common.util.CrestData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BannerBlockEntity extends BlockEntity {

    private CrestData crest = CrestData.defaultBlank();
    private boolean locked = false;

    public BannerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BANNER.get(), pos, state);
    }

    public CrestData getCrest() {
        return crest;
    }

    public void setCrest(CrestData crest) {
        this.crest = crest == null ? CrestData.defaultBlank() : crest;
    }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
/**
 * Save this banner's crest/lock state onto an itemstack so it can be placed elsewhere (tapestry, banner, etc).
 */
public void writeToItem(ItemStack stack) {
    if (stack == null) return;
    CompoundTag root = stack.getOrCreateTag();
    root.put(CrestData.NBT_KEY, crest.toTag());
    if (locked) root.putBoolean("Locked", true);
    else root.remove("Locked");
}

/**
 * Load crest/lock state from an itemstack onto this block entity.
 * Accepts both the new key (Crest/Locked) and legacy keys (crest/locked).
 */
public void readFromItem(ItemStack stack) {
    if (stack == null) return;
    CompoundTag root = stack.getTag();
    if (root == null) return;

    if (root.contains(CrestData.NBT_KEY)) {
        crest = CrestData.fromTag(root.getCompound(CrestData.NBT_KEY));
    } else if (root.contains("crest")) {
        crest = CrestData.fromTag(root.getCompound("crest"));
    }

    if (root.contains("Locked")) {
        locked = root.getBoolean("Locked");
    } else {
        locked = root.getBoolean("locked");
    }

    setChanged();
}


    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("crest", crest.toTag());
        tag.putBoolean("locked", locked);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("crest")) crest = CrestData.fromTag(tag.getCompound("crest"));
        locked = tag.getBoolean("locked");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("crest", crest.toTag());
        tag.putBoolean("locked", locked);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            if (tag.contains("crest")) crest = CrestData.fromTag(tag.getCompound("crest"));
            locked = tag.getBoolean("locked");
        }
    }
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        return new net.minecraft.world.phys.AABB(getBlockPos(), getBlockPos().offset(1, 2, 1));
    }

}
