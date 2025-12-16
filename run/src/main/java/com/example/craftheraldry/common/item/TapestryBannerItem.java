package com.example.craftheraldry.common.item;

import com.example.craftheraldry.common.blockentity.BannerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TapestryBannerItem extends BlockItem {

    public TapestryBannerItem(Block block, Properties props) {
        super(block, props);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, Player player,
                                                 ItemStack stack, BlockState state) {
        if (level.getBlockEntity(pos) instanceof BannerBlockEntity be) {
            be.readFromItem(stack);
            return true;
        }
        return false;
    }
}
