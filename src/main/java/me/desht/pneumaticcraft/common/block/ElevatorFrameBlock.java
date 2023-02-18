/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.block.entity.ElevatorBaseBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.ElevatorFrameBlockEntity;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Optional;

public class ElevatorFrameBlock extends AbstractPneumaticCraftBlock
        implements SimpleWaterloggedBlock, PneumaticCraftEntityBlock {
    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[16];
    private static final VoxelShape MOSTLY_FULL = Block.box(0.5, 0, 0.5, 15.5, 16, 15.5);

    private static final BooleanProperty NE = BooleanProperty.create("ne");
    private static final BooleanProperty SE = BooleanProperty.create("se");
    private static final BooleanProperty SW = BooleanProperty.create("sw");
    private static final BooleanProperty NW = BooleanProperty.create("nw");

    public ElevatorFrameBlock() {
        super(ModBlocks.defaultProps());

        registerDefaultState(defaultBlockState()
                .setValue(NE, false).setValue(SE, false).setValue(SW, false).setValue(NW, false));
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);

        getElevatorBase(pLevel, pPos).ifPresent(ElevatorBaseBlockEntity::updateMaxElevatorHeight);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NE, SW, SE, NW);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        if (state != null) {
            boolean[] connected = getConnections(ctx.getLevel(), ctx.getClickedPos());
            state.setValue(NE, connected[0]).setValue(SE, connected[1]).setValue(SW, connected[2]).setValue(NW, connected[3]);
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!canSurvive(stateIn, worldIn, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }

        boolean[] connected = getConnections(worldIn, currentPos);
        for (Corner corner : Corner.values()) {
            stateIn = stateIn.setValue(corner.prop, connected[corner.ordinal()]);
        }

        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        return selectionContext.isHoldingItem(this.asItem()) ? MOSTLY_FULL : getCachedShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        double blockHeight = getElevatorBlockHeight(worldIn, pos);
        if (blockHeight > 1) {
            return Shapes.block();
        } else if (blockHeight > 0) {
            double minY = Math.max(0, blockHeight - 0.06125d) * 16d;
            double maxY = blockHeight * 16d;
            return Shapes.or(getCachedShape(state), Block.box(0.001, minY, 0.001, 15.999, maxY, 15.999));
        } else {
            return getCachedShape(state);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // this avoids a crash when trying to render particles (see ParticleManager#addBlockDestroyEffects)
        return getCachedShape(state).isEmpty() ? RenderShape.INVISIBLE : super.getRenderShape(state);
    }

    private VoxelShape getCachedShape(BlockState state) {
        int shapeIdx = (state.getValue(NE) ? 1 : 0) | (state.getValue(SE) ? 2 : 0) | (state.getValue(SW) ? 4 : 0) | (state.getValue(NW) ? 8 : 0);
        if (SHAPE_CACHE[shapeIdx] == null) {
            VoxelShape shape = Shapes.empty();
            for (Corner corner : Corner.values()) {
                if (!state.getValue(corner.prop)) {
                    shape = Shapes.or(shape, corner.shape);
                }
            }
            SHAPE_CACHE[shapeIdx] = shape;
        }
        return SHAPE_CACHE[shapeIdx];
    }

    private boolean[] getConnections(BlockGetter world, BlockPos pos) {
        boolean[] res = new boolean[4];

        boolean frameXPos = world.getBlockState(pos.east()).getBlock() == ModBlocks.ELEVATOR_FRAME.get();
        boolean frameXNeg = world.getBlockState(pos.west()).getBlock() == ModBlocks.ELEVATOR_FRAME.get();
        boolean frameZPos = world.getBlockState(pos.south()).getBlock() == ModBlocks.ELEVATOR_FRAME.get();
        boolean frameZNeg = world.getBlockState(pos.north()).getBlock() == ModBlocks.ELEVATOR_FRAME.get();

        res[Corner.SE.ordinal()]  = frameXPos || frameZPos;
        res[Corner.NE.ordinal()]  = frameXPos || frameZNeg;
        res[Corner.SW.ordinal()]  = frameXNeg || frameZPos;
        res[Corner.NW.ordinal()]  = frameXNeg || frameZNeg;

        return res;
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        getElevatorBase(world, pos).ifPresent(teBase -> {
            if (!teBase.isStopped()) {
                int baseY = teBase.getBlockPos().getY();
                if (entity.getY() >= baseY && entity.getY() < baseY + teBase.extension + 10) {
                    double eX = entity.getX();
                    double eZ = entity.getZ();
                    if (teBase.ticksRunning < 10) {
                        // when departing, nudge the entity onto the platform if they're hanging over the edge
                        int x = pos.getX();
                        int z = pos.getZ();
                        AABB box = entity.getBoundingBox();
                        if (box.minX < x && !(teBase.getCachedNeighbor(Direction.WEST) instanceof ElevatorBaseBlockEntity)
                                || (box.maxX > x + 1 && !(teBase.getCachedNeighbor(Direction.EAST) instanceof ElevatorBaseBlockEntity))) {
                            eX = x + 0.5;
                        }
                        if (box.minZ < z && !(teBase.getCachedNeighbor(Direction.NORTH) instanceof ElevatorBaseBlockEntity)
                                || box.maxZ > z + 1 && !(teBase.getCachedNeighbor(Direction.SOUTH) instanceof ElevatorBaseBlockEntity)) {
                            eZ = z + 0.5;
                        }
                    }
                    entity.setPos(eX, baseY + teBase.extension + 1.2, eZ);
                    if (entity instanceof ServerPlayer && teBase.getUpgrades(ModUpgrades.SPEED.get()) >= 6) {
                        // prevents "<player> moved too quickly" problems when the elevator is fast
                        // note: using this can lead to jerky upward movement, so only doing for fast elevators
                        ((ServerPlayer) entity).connection.resetPosition();
                    }
                    entity.fallDistance = 0;
                }
            }
        });
    }

    static Optional<ElevatorBaseBlockEntity> getElevatorBase(BlockGetter world, BlockPos pos) {
        // caching the elevator base in the frame BE - this should be safe from a caching point of view,
        // since if the base (or any frame below us) is broken, all frames above it - including us - will also break
        return world.getBlockEntity(pos, ModBlockEntities.ELEVATOR_FRAME.get())
                .map(ElevatorFrameBlockEntity::getElevatorBase);
    }

    private double getElevatorBlockHeight(BlockGetter world, BlockPos pos) {
        return getElevatorBase(world, pos)
                .map(te -> Math.max(te.extension - (pos.getY() - te.getBlockPos().getY()) + 1, 0D))
                .orElse(0D);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        getElevatorBase(world, pos).ifPresent(ElevatorBaseBlockEntity::updateMaxElevatorHeight);

        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState below = worldIn.getBlockState(pos.below());
        return below.getBlock() == this || below.getBlock() == ModBlocks.ELEVATOR_BASE.get();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return getElevatorBlockHeight(worldIn, pos) == 0f;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        if (!player.isCrouching() && player.getItemInHand(hand).getItem() == this.asItem()) {
            // build it scaffolding-style
            if (!world.isClientSide) {
                BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
                int worldHeight = world.dimensionType().logicalHeight();
                while (mPos.getY() < worldHeight && world.getBlockState(mPos).getBlock() == this) {
                    mPos.move(Direction.UP);
                }
                if (mPos.getY() < worldHeight && world.getBlockState(mPos).isAir()) {
                    world.setBlockAndUpdate(mPos, this.defaultBlockState());
                    float pitch = Math.min(1.5f, (mPos.getY() - pos.getY()) * 0.05f + 1f);
                    world.playSound(null, mPos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1f, pitch);
                    if (!player.isCreative()) {
                        player.getItemInHand(hand).shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.FAIL;
                }
            } else {
                return InteractionResult.SUCCESS;
            }
        } else {
            return super.use(state, world, pos, player, hand, brtr);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ElevatorFrameBlockEntity(pPos, pState);
    }

    private enum Corner {
        NE( 1,-1, ElevatorFrameBlock.NE, Block.box(14.5f, 0,  0.5f, 15.5f, 16,  1.5f)),
        SE( 1, 1, ElevatorFrameBlock.SE, Block.box(14.5f, 0, 14.5f, 15.5f, 16, 15.5f)),
        SW(-1, 1, ElevatorFrameBlock.SW, Block.box( 0.5f, 0, 14.5f,  1.5f, 16, 15.5f)),
        NW(-1,-1, ElevatorFrameBlock.NW, Block.box( 0.5f, 0,  0.5f,  1.5f, 16,  1.5f));

        final int x;
        final int z;
        final BooleanProperty prop;
        final VoxelShape shape;

        Corner(int x, int z, BooleanProperty prop, VoxelShape shape) {
            this.x = x; this.z = z;
            this.prop = prop;
            this.shape = shape;
        }
    }
}
