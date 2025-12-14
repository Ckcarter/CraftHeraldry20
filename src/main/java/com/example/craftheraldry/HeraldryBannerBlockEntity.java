package com.example.craftheraldry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HeraldryBannerBlockEntity extends BlockEntity {
    private CrestData crest = CrestData.defaultCrest();

    public HeraldryBannerBlockEntity(BlockPos pos, BlockState state) {
        super(Registry.HERALDRY_BANNER_BE.get(), pos, state);
    }

    public CrestData getCrest() {
        return crest;
    }

    public void setCrest(CrestData crest) {
        this.crest = crest == null ? CrestData.defaultCrest() : crest;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Crest", crest.toTag());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Crest")) {
            CrestData c = CrestData.fromTag(tag.getCompound("Crest"));
            if (c != null) crest = c;
        }
    }
}
