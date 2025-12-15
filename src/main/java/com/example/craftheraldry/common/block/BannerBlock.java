
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class BannerBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            // vertical pole (taller)
            Block.box(7, 0, 7, 9, 48, 9),
            // top crossbar (left-to-right) above the cloth
            Block.box(2, 39, 7, 14, 41, 9),
            // cloth plane volume (wider + taller)
            Block.box(2, 19, 6.5, 14, 39, 7.5)
    );
private static final Map<Direction, VoxelShape> SHAPES = makeShapes();

    private static Map<Direction, VoxelShape> makeShapes() {
        Map<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
        map.put(Direction.NORTH, SHAPE_NORTH);
        map.put(Direction.EAST, rotateShape(SHAPE_NORTH, Rotation.CLOCKWISE_90));
        map.put(Direction.SOUTH, rotateShape(SHAPE_NORTH, Rotation.CLOCKWISE_180));
        map.put(Direction.WEST, rotateShape(SHAPE_NORTH, Rotation.COUNTERCLOCKWISE_90));
        return map;
    }

    private static VoxelShape rotateShape(VoxelShape shape, Rotation rot) {
        VoxelShape[] out = new VoxelShape[]{Shapes.empty()};

        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double nMinX=minX, nMinZ=minZ, nMaxX=maxX, nMaxZ=maxZ;

            switch (rot) {
                case CLOCKWISE_90 -> {
                    nMinX = 1 - maxZ; nMaxX = 1 - minZ;
                    nMinZ = minX;     nMaxZ = maxX;
                }
                case CLOCKWISE_180 -> {
                    nMinX = 1 - maxX; nMaxX = 1 - minX;
                    nMinZ = 1 - maxZ; nMaxZ = 1 - minZ;
                }
                case COUNTERCLOCKWISE_90 -> {
                    nMinX = minZ;     nMaxX = maxZ;
                    nMinZ = 1 - maxX; nMaxZ = 1 - minX;
                }
            }

            out[0] = Shapes.or(out[0], Shapes.box(nMinX, minY, nMinZ, nMaxX, maxY, nMaxZ));
        });

        return out[0];
    }

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

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPES.getOrDefault(state.getValue(FACING), SHAPE_NORTH);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPES.getOrDefault(state.getValue(FACING), SHAPE_NORTH);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
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
