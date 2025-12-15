package com.example.craftheraldry.common.block;

import com.example.craftheraldry.common.blockentity.BannerBlockEntity;
import com.example.craftheraldry.common.item.ScrollItem;
import com.example.craftheraldry.common.util.CrestData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BannerBlock extends Block implements EntityBlock {

    // A thin banner-like shape (pole + cloth plane)
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(7, 0, 7, 9, 16, 9),   // pole
            Block.box(1, 2, 7.5, 15, 14, 8.5) // cloth
    );


    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BannerBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BannerBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BannerBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BannerBlockEntity be)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);
        boolean holdingScroll = held.getItem() instanceof ScrollItem;

        if (!level.isClientSide) {
            if (player.isShiftKeyDown() && held.isEmpty()) {
                be.setLocked(!be.isLocked());
                player.displayClientMessage(Component.translatable(be.isLocked() ? "message.craftheraldry.locked" : "message.craftheraldry.unlocked"), true);
                be.setChanged();
                ((ServerLevel) level).sendBlockUpdated(pos, state, state, 3);
                return InteractionResult.CONSUME;
            }

            if (holdingScroll) {
                if (be.isLocked()) {
                    player.displayClientMessage(Component.translatable("message.craftheraldry.banner_is_locked"), true);
                    return InteractionResult.CONSUME;
                }
                CrestData crest = ScrollItem.getCrest(held);
                be.setCrest(crest);
                be.setChanged();
                ((ServerLevel) level).sendBlockUpdated(pos, state, state, 3);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
