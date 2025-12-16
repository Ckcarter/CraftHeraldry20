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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * One-block wall-mounted banner (vanilla wall-banner style).
 *
 * - Places as ONE block (no DoubleBlockHalf).
 * - Stores crest data in BannerBlockEntity at its own position.
 * - Right-click with a ScrollItem applies the crest (same behavior as BannerBlock).
 * - Shift + empty hand toggles lock (same behavior as BannerBlock).
 */
public class WallBannerBlock extends Block implements EntityBlock {

    // Reuse the SAME property instance as the standing banner for renderer compatibility.
    public static final DirectionProperty FACING = BannerBlock.FACING;

    public WallBannerBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction face = ctx.getClickedFace();
        if (!face.getAxis().isHorizontal()) return null; // must be placed on a wall
        // Face is the direction of the wall face you clicked; the banner should face outward.
        return this.defaultBlockState().setValue(FACING, face);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, false);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        // Thin wall-hanging collision/outline (2px thick)
        Direction f = state.getValue(FACING);
        return switch (f) {
            case NORTH -> Block.box(1, 0, 14, 15, 16, 16);
            case SOUTH -> Block.box(1, 0, 0, 15, 16, 2);
            case WEST -> Block.box(14, 0, 1, 16, 16, 15);
            case EAST -> Block.box(0, 0, 1, 2, 16, 15);
            default -> Shapes.block();
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return getShape(state, level, pos, ctx);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {

        if (!(level.getBlockEntity(pos) instanceof BannerBlockEntity be)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);
        boolean holdingScroll = held.getItem() instanceof ScrollItem;

        // Match BannerBlock: only handle scroll apply, or shift+empty lock toggle.
        if (!holdingScroll && !(player.isShiftKeyDown() && held.isEmpty())) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            // Shift + empty hand toggles lock
            if (player.isShiftKeyDown() && held.isEmpty()) {
                be.setLocked(!be.isLocked());
                player.displayClientMessage(
                        Component.translatable(be.isLocked()
                                ? "message.craftheraldry.locked"
                                : "message.craftheraldry.unlocked"),
                        true
                );
                be.setChanged();
                ((ServerLevel) level).sendBlockUpdated(pos, state, state, 3);
                return InteractionResult.CONSUME;
            }

            // Apply crest from scroll
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BannerBlockEntity(pos, state);
    }
}
