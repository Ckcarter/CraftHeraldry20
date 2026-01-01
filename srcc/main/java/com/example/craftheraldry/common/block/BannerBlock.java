package com.example.craftheraldry.common.block;

import com.example.craftheraldry.common.blockentity.BannerBlockEntity;
import com.example.craftheraldry.common.item.ScrollItem;
import com.example.craftheraldry.common.util.CrestData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Vanilla-style standing banner:
 * - one block only
 * - ROTATION_16 property
 * - rendered entirely by the BlockEntityRenderer (RenderShape.INVISIBLE)
 * - crest is stored on the shared BannerBlockEntity
 */
public class BannerBlock extends Block implements EntityBlock {

    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;

    // Very thin selection box (vanilla banners are non-solid)
    private static final VoxelShape SHAPE = Block.box(7, 0, 7, 9, 16, 9);

    public BannerBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // Vanilla standing banner placement:
        // ROTATION_16 is derived directly from the placing entity's yaw.
        int rot = Mth.floor((double) (ctx.getRotation() * 16.0F / 360.0F) + 0.5D) & 15;
        return this.defaultBlockState().setValue(ROTATION, rot);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        return below.isFaceSturdy(level, belowPos, Direction.UP);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) return;
        if (level.getBlockEntity(pos) instanceof BannerBlockEntity be) {
            be.readFromItem(stack);
            be.setChanged();
            ((ServerLevel) level).sendBlockUpdated(pos, state, state, 3);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack out = new ItemStack(this.asItem());
        if (level.getBlockEntity(pos) instanceof BannerBlockEntity be) {
            be.writeToItem(out);
        }
        return out;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {

        if (!(level.getBlockEntity(pos) instanceof BannerBlockEntity be)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);
        boolean holdingScroll = held.getItem() instanceof ScrollItem;

        // Shift + empty hand toggles lock; holding scroll applies crest.
        if (!holdingScroll && !(player.isShiftKeyDown() && held.isEmpty())) return InteractionResult.PASS;

        if (!level.isClientSide) {
            if (player.isShiftKeyDown() && held.isEmpty()) {
                be.setLocked(!be.isLocked());
                player.displayClientMessage(
                        Component.translatable(be.isLocked() ? "message.craftheraldry.locked" : "message.craftheraldry.unlocked"),
                        true
                );
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
