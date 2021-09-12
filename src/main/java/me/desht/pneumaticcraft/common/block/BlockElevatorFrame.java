package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorFrame;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

import static net.minecraft.state.properties.BlockStateProperties.WATERLOGGED;

public class BlockElevatorFrame extends BlockPneumaticCraft implements IWaterLoggable {
    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[16];
    private static final VoxelShape MOSTLY_FULL = Block.box(0.5, 0, 0.5, 15.5, 16, 15.5);

    private static final BooleanProperty NE = BooleanProperty.create("ne");
    private static final BooleanProperty SE = BooleanProperty.create("se");
    private static final BooleanProperty SW = BooleanProperty.create("sw");
    private static final BooleanProperty NW = BooleanProperty.create("nw");

    public BlockElevatorFrame() {
        super(ModBlocks.defaultProps());

        registerDefaultState(getStateDefinition().any()
                .setValue(NE, false).setValue(SE, false).setValue(SW, false).setValue(NW, false)
                .setValue(WATERLOGGED, false));

    }

    @Override
    public void onPlace(BlockState newState, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(newState, world, pos, oldState, isMoving);

        getElevatorBase(world, pos).ifPresent(TileEntityElevatorBase::updateMaxElevatorHeight);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NE, SW, SE, NW, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        boolean[] connected = getConnections(ctx.getLevel(), ctx.getClickedPos());
        FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
        return super.getStateForPlacement(ctx)
                .setValue(NE, connected[0]).setValue(SE, connected[1]).setValue(SW, connected[2]).setValue(NW, connected[3])
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);

    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!canSurvive(stateIn, worldIn, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }

        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }

        boolean[] connected = getConnections(worldIn, currentPos);
        for (Corner corner : Corner.values()) {
            stateIn = stateIn.setValue(corner.prop, connected[corner.ordinal()]);
        }
        return stateIn;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityElevatorFrame.class;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return selectionContext.isHoldingItem(this.asItem()) ? MOSTLY_FULL : getCachedShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        double blockHeight = getElevatorBlockHeight(worldIn, pos);
        if (blockHeight > 1) {
            return VoxelShapes.block();
        } else if (blockHeight > 0) {
            double minY = Math.max(0, blockHeight - 0.06125d) * 16d;
            double maxY = blockHeight * 16d;
            return VoxelShapes.or(getCachedShape(state), Block.box(0.001, minY, 0.001, 15.999, maxY, 15.999));
        } else {
            return getCachedShape(state);
        }
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        // this avoids a crash when trying to render particles (see ParticleManager#addBlockDestroyEffects)
        return getCachedShape(state).isEmpty() ? BlockRenderType.INVISIBLE : super.getRenderShape(state);
    }

    private VoxelShape getCachedShape(BlockState state) {
        int shapeIdx = (state.getValue(NE) ? 1 : 0) | (state.getValue(SE) ? 2 : 0) | (state.getValue(SW) ? 4 : 0) | (state.getValue(NW) ? 8 : 0);
        if (SHAPE_CACHE[shapeIdx] == null) {
            VoxelShape shape = VoxelShapes.empty();
            for (Corner corner : Corner.values()) {
                if (!state.getValue(corner.prop)) {
                    shape = VoxelShapes.or(shape, corner.shape);
                }
            }
            SHAPE_CACHE[shapeIdx] = shape;
        }
        return SHAPE_CACHE[shapeIdx];
    }

    private boolean[] getConnections(IBlockReader world, BlockPos pos) {
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
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
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
                        AxisAlignedBB box = entity.getBoundingBox();
                        if (box.minX < x && !(teBase.getCachedNeighbor(Direction.WEST) instanceof TileEntityElevatorBase)
                                || (box.maxX > x + 1 && !(teBase.getCachedNeighbor(Direction.EAST) instanceof TileEntityElevatorBase))) {
                            eX = x + 0.5;
                        }
                        if (box.minZ < z && !(teBase.getCachedNeighbor(Direction.NORTH) instanceof TileEntityElevatorBase)
                                || box.maxZ > z + 1 && !(teBase.getCachedNeighbor(Direction.SOUTH) instanceof TileEntityElevatorBase)) {
                            eZ = z + 0.5;
                        }
                    }
                    entity.setPos(eX, baseY + teBase.extension + 1.2, eZ);
                    if (entity instanceof ServerPlayerEntity && teBase.getUpgrades(EnumUpgrade.SPEED) >= 6) {
                        // prevents "<player> moved too quickly" problems when the elevator is fast
                        // note: using this can lead to jerky upward movement, so only doing for fast elevators
                        ((ServerPlayerEntity) entity).connection.resetPosition();
                    }
                    entity.fallDistance = 0;
                }
            }
        });
    }

    static Optional<TileEntityElevatorBase> getElevatorBase(IBlockReader world, BlockPos pos) {
        // caching the elevator base in the frame TE - this should be safe from a caching point of view,
        // since if the base (or any frame below us) is broken, all frames above it - including us - will also break
        return PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityElevatorFrame.class)
                .map(TileEntityElevatorFrame::getElevatorBase);
    }

    private double getElevatorBlockHeight(IBlockReader world, BlockPos pos) {
        return getElevatorBase(world, pos)
                .map(te -> Math.max(te.extension - (pos.getY() - te.getBlockPos().getY()) + 1, 0D))
                .orElse(0D);
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        getElevatorBase(world, pos).ifPresent(TileEntityElevatorBase::updateMaxElevatorHeight);

        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockState below = worldIn.getBlockState(pos.below());
        return below.getBlock() == this || below.getBlock() == ModBlocks.ELEVATOR_BASE.get();
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return getElevatorBlockHeight(worldIn, pos) == 0f;
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        if (!player.isCrouching() && player.getItemInHand(hand).getItem() == this.asItem()) {
            // build it scaffolding-style
            if (!world.isClientSide) {
                BlockPos.Mutable mPos = new BlockPos.Mutable(pos.getX(), pos.getY(), pos.getZ());
                int worldHeight = world.dimensionType().logicalHeight();
                while (mPos.getY() < worldHeight && world.getBlockState(mPos).getBlock() == this) {
                    mPos.move(Direction.UP);
                }
                if (mPos.getY() < worldHeight && world.getBlockState(mPos).isAir(world, mPos)) {
                    world.setBlockAndUpdate(mPos, this.defaultBlockState());
                    float pitch = Math.min(1.5f, (mPos.getY() - pos.getY()) * 0.05f + 1f);
                    world.playSound(null, mPos, SoundEvents.METAL_PLACE, SoundCategory.BLOCKS, 1f, pitch);
                    if (!player.isCreative()) {
                        player.getItemInHand(hand).shrink(1);
                    }
                    return ActionResultType.SUCCESS;
                } else {
                    return ActionResultType.FAIL;
                }
            } else {
                return ActionResultType.SUCCESS;
            }
        } else {
            return super.use(state, world, pos, player, hand, brtr);
        }
    }

    private enum Corner {
        NE( 1,-1, BlockElevatorFrame.NE, Block.box(14.5f, 0,  0.5f, 15.5f, 16,  1.5f)),
        SE( 1, 1, BlockElevatorFrame.SE, Block.box(14.5f, 0, 14.5f, 15.5f, 16, 15.5f)),
        SW(-1, 1, BlockElevatorFrame.SW, Block.box( 0.5f, 0, 14.5f,  1.5f, 16, 15.5f)),
        NW(-1,-1, BlockElevatorFrame.NW, Block.box( 0.5f, 0,  0.5f,  1.5f, 16,  1.5f));

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
