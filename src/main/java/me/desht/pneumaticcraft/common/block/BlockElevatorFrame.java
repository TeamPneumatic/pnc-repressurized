package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorFrame;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
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

import static net.minecraft.state.properties.BlockStateProperties.WATERLOGGED;

public class BlockElevatorFrame extends BlockPneumaticCraft implements IWaterLoggable {
    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[16];
    private static final VoxelShape MOSTLY_FULL = Block.makeCuboidShape(0.5, 0, 0.5, 15.5, 16, 15.5);

    private static final BooleanProperty NE = BooleanProperty.create("ne");
    private static final BooleanProperty SE = BooleanProperty.create("se");
    private static final BooleanProperty SW = BooleanProperty.create("sw");
    private static final BooleanProperty NW = BooleanProperty.create("nw");

    public BlockElevatorFrame() {
        super(ModBlocks.defaultProps());

        setDefaultState(getStateContainer().getBaseState()
                .with(NE, false).with(SE, false).with(SW, false).with(NW, false)
                .with(WATERLOGGED, false));

    }

    @Override
    public void onBlockAdded(BlockState newState, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(newState, world, pos, oldState, isMoving);

        TileEntityElevatorBase elevatorBase = getElevatorTE(world, pos);
        if (elevatorBase != null) {
            elevatorBase.updateMaxElevatorHeight();
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(NE, SW, SE, NW, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        boolean[] connected = getConnections(ctx.getWorld(), ctx.getPos());
        IFluidState fluidState = ctx.getWorld().getFluidState(ctx.getPos());
        return super.getStateForPlacement(ctx)
                .with(NE, connected[0]).with(SE, connected[1]).with(SW, connected[2]).with(NW, connected[3])
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);

    }

    @Override
    public IFluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!isValidPosition(stateIn, worldIn, currentPos)) {
            return Blocks.AIR.getDefaultState();
        }

        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }

        boolean[] connected = getConnections(worldIn, currentPos);
        for (Corner corner : Corner.values()) {
            stateIn = stateIn.with(corner.prop, connected[corner.ordinal()]);
        }
        return stateIn;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityElevatorFrame.class;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return getShapePrivate(state, world, pos, selectionContext, false);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return getShapePrivate(state, worldIn, pos, context, true);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        // this avoids a crash when trying to render particles (see ParticleManager#addBlockDestroyEffects)
        return getCachedShape(state).isEmpty() ? BlockRenderType.INVISIBLE : super.getRenderType(state);
    }

    private VoxelShape getShapePrivate(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext, boolean collision) {
        if (selectionContext.hasItem(this.asItem()) && !collision) {
            // return a (mostly) full bounding box if holding frames, for ease of placement of frames against frames
            return MOSTLY_FULL;
        }

        VoxelShape shape = getCachedShape(state);
        float blockHeight = getElevatorBlockHeight(world, pos);
        if (blockHeight > 0f && blockHeight <= 1f) {
            float minY = Math.max(0f, blockHeight - 0.06125f) * 16f;
            float maxY = blockHeight * 16f;
            shape = VoxelShapes.or(shape, Block.makeCuboidShape(0.001, minY, 0.001, 15.999, maxY, 15.999));
        } else if (blockHeight > 1f) {
            shape = VoxelShapes.or(shape, Block.makeCuboidShape(5, 0, 5, 11, 16, 11));
        }

        return shape;
    }

    private VoxelShape getCachedShape(BlockState state) {
        int shapeIdx = (state.get(NE) ? 1 : 0) | (state.get(SE) ? 2 : 0) | (state.get(SW) ? 4 : 0) | (state.get(NW) ? 8 : 0);
        if (SHAPE_CACHE[shapeIdx] == null) {
            VoxelShape shape = VoxelShapes.empty();
            for (Corner corner : Corner.values()) {
                if (!state.get(corner.prop)) {
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
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        TileEntityElevatorBase te = getElevatorTE(world, pos);
        if (te != null && te.oldExtension != te.extension) {
            if (Math.abs(entity.getPosY() - (te.getPos().getY() + te.extension)) < 2.5) {
                AxisAlignedBB box = entity.getBoundingBox();
                int x = te.getPos().getX();
                int z = te.getPos().getZ();
                // nudge the entity onto the platform if they're hanging over by too much
                if (box.minX < x - 0.1) {
                    entity.addVelocity(0.02, 0, 0);
                } else if (box.maxX > x + 1.1) {
                    entity.addVelocity(-0.02, 0, 0);
                } else if (box.minZ < z - 0.1) {
                    entity.addVelocity(0, 0, 0.02);
                } else if (box.maxZ > z + 1.1) {
                    entity.addVelocity(0, 0, -0.02);
                }
                entity.setPosition(entity.getPosX(), te.getPos().getY() + 1 + te.extension, entity.getPosZ());
            }
            entity.fallDistance = 0;
        }
    }

    static TileEntityElevatorBase getElevatorTE(IBlockReader world, BlockPos pos) {
        // TODO cache the elevator base pos in the frame's TE (careful with cache invalidation!)
        while (true) {
            pos = pos.offset(Direction.DOWN);
            if (world.getBlockState(pos).getBlock() == ModBlocks.ELEVATOR_BASE.get()) break;
            if (world.getBlockState(pos).getBlock() != ModBlocks.ELEVATOR_FRAME.get() || pos.getY() <= 0) return null;
        }
        return (TileEntityElevatorBase) world.getTileEntity(pos);
    }

    private float getElevatorBlockHeight(IBlockReader world, BlockPos pos) {
        TileEntityElevatorBase te = getElevatorTE(world, pos);
        return te == null ? 0F : Math.max(te.extension - (pos.getY() - te.getPos().getY()) + 1, 0F);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        TileEntityElevatorBase elevatorBase = getElevatorTE(world, pos);
        if (elevatorBase != null) {
            elevatorBase.updateMaxElevatorHeight();
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockState below = worldIn.getBlockState(pos.down());
        return below.getBlock() == this || below.getBlock() == ModBlocks.ELEVATOR_BASE.get();
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return true;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        if (!player.isCrouching() && player.getHeldItem(hand).getItem() == this.asItem()) {
            // build it scaffolding-style
            if (!world.isRemote) {
                BlockPos.Mutable mPos = new BlockPos.Mutable(pos);
                while (mPos.getY() < world.getMaxHeight() && world.getBlockState(mPos).getBlock() == this) {
                    mPos.move(Direction.UP);
                }
                if (mPos.getY() < world.getMaxHeight() && world.getBlockState(mPos).isAir(world, mPos)) {
                    world.setBlockState(mPos, this.getDefaultState());
                    float pitch = Math.min(1.5f, (mPos.getY() - pos.getY()) * 0.05f + 1f);
                    world.playSound(null, mPos, SoundEvents.BLOCK_METAL_PLACE, SoundCategory.BLOCKS, 1f, pitch);
                    if (!player.isCreative()) {
                        player.getHeldItem(hand).shrink(1);
                    }
                    return ActionResultType.SUCCESS;
                } else {
                    return ActionResultType.FAIL;
                }
            } else {
                return ActionResultType.SUCCESS;
            }
        } else {
            return super.onBlockActivated(state, world, pos, player, hand, brtr);
        }
    }

    private enum Corner {
        NE( 1,-1, BlockElevatorFrame.NE, Block.makeCuboidShape(14.5f, 0,  0.5f, 15.5f, 16,  1.5f)),
        SE( 1, 1, BlockElevatorFrame.SE, Block.makeCuboidShape(14.5f, 0, 14.5f, 15.5f, 16, 15.5f)),
        SW(-1, 1, BlockElevatorFrame.SW, Block.makeCuboidShape( 0.5f, 0, 14.5f,  1.5f, 16, 15.5f)),
        NW(-1,-1, BlockElevatorFrame.NW, Block.makeCuboidShape( 0.5f, 0,  0.5f,  1.5f, 16,  1.5f));

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
