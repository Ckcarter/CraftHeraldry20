package com.example.craftheraldry.common.item;

import com.example.craftheraldry.common.block.BannerBlock;
import com.example.craftheraldry.common.block.TapestryBannerBlock;
import com.example.craftheraldry.common.blockentity.BannerBlockEntity;
import com.example.craftheraldry.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class TapestryBannerItem extends BlockItem {

    public TapestryBannerItem(net.minecraft.world.level.block.Block block, Properties props) {
        super(block, props);
    }

    /**
     * Banner -> Tapestry conversion (preserves crest/lock NBT):
     * Sneak + Right-click any placed BannerBlock with this item to convert it into a tapestry banner.
     */
    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (player == null) return super.useOn(ctx);

        if (!player.isShiftKeyDown()) return super.useOn(ctx);

        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);

        // Only convert normal banners (not already a tapestry)
        if (!(state.getBlock() instanceof BannerBlock) || (state.getBlock() instanceof TapestryBannerBlock)) {
            return super.useOn(ctx);
        }

        // If clicked upper half, redirect to lower
        if (state.hasProperty(BannerBlock.HALF) && state.getValue(BannerBlock.HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
            state = level.getBlockState(pos);
        }

        if (!(level.getBlockEntity(pos) instanceof BannerBlockEntity be)) {
            return super.useOn(ctx);
        }

        if (!level.isClientSide) {
            ItemStack held = ctx.getItemInHand();

            // Copy crest/lock from banner BE -> item NBT (keeps it simple)
            be.writeToItem(held);

            Direction facing = state.getValue(BannerBlock.FACING);

            // Replace with tapestry (one-block hang or wall tapestry block)
            BlockState newState = ModBlocks.TAPESTRY_BANNER.get().defaultBlockState();

            if (newState.hasProperty(BannerBlock.FACING)) {
                newState = newState.setValue(BannerBlock.FACING, facing);
            }
            if (newState.hasProperty(BannerBlock.HALF)) {
                newState = newState.setValue(BannerBlock.HALF, DoubleBlockHalf.LOWER);
            }

            level.setBlock(pos, newState, 3);

            if (level.getBlockEntity(pos) instanceof BannerBlockEntity newBe) {
                newBe.readFromItem(held);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
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
