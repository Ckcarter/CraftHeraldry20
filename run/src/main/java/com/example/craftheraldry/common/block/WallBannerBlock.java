package com.example.craftheraldry.common.block;

import com.example.craftheraldry.common.blockentity.BannerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Wall-mounted variant of the banner.
 *
 * Placement:
 * - Must be placed on a horizontal face (a wall)
 * - Places a two-block-tall banner (LOWER + UPPER)
 *
 * Rendering:
 * - No block model (RenderShape.INVISIBLE)
 * - Uses BannerBlockEntityRenderer to draw the cloth/crest (BlockEntity on LOWER only)
 */
public class WallBannerBlock extends Block implements EntityBlock {

    // IMPORTANT: reuse the SAME properties as BannerBlock so the BlockEntityRenderer can read them.
    // (BlockState properties are identity-based, not just name-based.)
    public static final DirectionProperty FACING = BannerBlock.FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BannerBlock.HALF;

    public WallBannerBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // No model; rendered entirely by the BlockEntityRenderer.
        // The cloth/crest is rendered by the BlockEntityRenderer.
        return RenderShape.INVISIBLE;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        Level level = ctx.getLevel();

        Direction face = ctx.getClickedFace();
        if (!face.getAxis().isHorizontal()) return null; // must be placed on a wall

        if (pos.getY() >= level.getMaxBuildHeight() - 1) return null;
        if (!level.getBlockState(pos.above()).canBeReplaced(ctx)) return null;

        // Face is the direction the player clicked (the wall face). The banner should face OUT from the wall.
        return this.defaultBlockState()
                .setValue(FACING, face)
                .setValue(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) return;
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        DoubleBlockHalf half = state.getValue(HALF);

        // Needs a sturdy block behind it (opposite the direction it faces).
        BlockPos supportPos = pos.relative(facing.getOpposite());
        if (!level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing)) return false;

        // Upper/lower pairing must match.
        if (half == DoubleBlockHalf.LOWER) {
            BlockState above = level.getBlockState(pos.above());
            return above.is(this)
                    && above.getValue(HALF) == DoubleBlockHalf.UPPER
                    && above.getValue(FACING) == facing;
        } else {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this)
                    && below.getValue(HALF) == DoubleBlockHalf.LOWER
                    && below.getValue(FACING) == facing;
        }
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

    @Nullable
    @Override
    public BannerBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Only store data on LOWER half
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new BannerBlockEntity(pos, state) : null;
    }
}
