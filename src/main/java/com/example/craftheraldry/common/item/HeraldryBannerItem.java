package com.example.craftheraldry.common.item;

import com.example.craftheraldry.common.blockentity.BannerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Banner item that preserves and applies crest NBT to the placed BannerBlockEntity,
 * matching vanilla BannerItem behavior (patterns stored on the item are written to the block entity on placement).
 */
public class HeraldryBannerItem extends StandingAndWallBlockItem {

    public HeraldryBannerItem(Block standing, Block wall, Properties props) {
        // Direction.DOWN matches vanilla BannerItem's wall attachment direction parameter for StandingAndWallBlockItem.
        super(standing, wall, props, net.minecraft.core.Direction.DOWN);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, net.minecraft.world.entity.player.Player player,
                                                 ItemStack stack, BlockState state) {
        boolean did = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if (level.isClientSide) return did;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BannerBlockEntity bannerBe) {
            bannerBe.readFromItem(stack);
            bannerBe.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
        return did;
    }
}
