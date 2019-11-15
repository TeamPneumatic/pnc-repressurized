package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorFrame;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import static net.minecraft.state.properties.BlockStateProperties.WATERLOGGED;

public class BlockElevatorFrame extends BlockPneumaticCraft {
    private static final BooleanProperty NE = BooleanProperty.create("ne");
    private static final BooleanProperty SE = BooleanProperty.create("se");
    private static final BooleanProperty SW = BooleanProperty.create("sw");
    private static final BooleanProperty NW = BooleanProperty.create("nw");

    public BlockElevatorFrame() {
        super("elevator_frame");
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
        builder.add(NE, SW, SE, NW);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
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
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        if (selectionContext.hasItem(this.asItem())) {
            // return a full bounding box if holding frames, for ease of placement of frames against frames
            return VoxelShapes.fullCube();
        }

        // TODO can cache these; only 16 possible values here
        VoxelShape shape = VoxelShapes.empty();
        boolean[] connected = getConnections(world, pos);
        for (Corner corner : Corner.values()) {
            if (!connected[corner.ordinal()]) {
                shape = VoxelShapes.or(shape, VoxelShapes.create(corner.aabb));
            }
        }

        float blockHeight = getElevatorBlockHeight(world, pos);
        if (blockHeight > 0) {
            shape = VoxelShapes.or(shape, VoxelShapes.create(new AxisAlignedBB(0, 0, 0, 1, blockHeight, 1)));
        }

        return shape;
    }

    private boolean[] getConnections(IBlockReader world, BlockPos pos) {
        boolean[] res = new boolean[4];

        boolean frameXPos = world.getBlockState(pos.east()).getBlock() == ModBlocks.ELEVATOR_FRAME;
        boolean frameXNeg = world.getBlockState(pos.west()).getBlock() == ModBlocks.ELEVATOR_FRAME;
        boolean frameZPos = world.getBlockState(pos.south()).getBlock() == ModBlocks.ELEVATOR_FRAME;
        boolean frameZNeg = world.getBlockState(pos.north()).getBlock() == ModBlocks.ELEVATOR_FRAME;

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
            if (Math.abs(entity.posY - (te.getPos().getY() + te.extension)) < 2.5) {
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
                entity.setPosition(entity.posX, te.getPos().getY() + 1 + te.extension, entity.posZ);
            }
            entity.fallDistance = 0;
        }
    }

    static TileEntityElevatorBase getElevatorTE(IBlockReader world, BlockPos pos) {
        while (true) {
            pos = pos.offset(Direction.DOWN);
            if (world.getBlockState(pos).getBlock() == ModBlocks.ELEVATOR_BASE) break;
            if (world.getBlockState(pos).getBlock() != ModBlocks.ELEVATOR_FRAME || pos.getY() <= 0) return null;
        }
        return (TileEntityElevatorBase) world.getTileEntity(pos);
    }

    private float getElevatorBlockHeight(IBlockReader world, BlockPos pos) {
        TileEntityElevatorBase te = getElevatorTE(world, pos);
        return te == null ? 0F : MathHelper.clamp(te.extension - (pos.getY() - te.getPos().getY()) + 1, 0F, 1F);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        TileEntityElevatorBase elevatorBase = getElevatorTE(world, pos);
        if (elevatorBase != null) {
            elevatorBase.updateMaxElevatorHeight();
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }

    private enum Corner {
        NE(1, -1, BlockElevatorFrame.NE, new AxisAlignedBB(15f / 16f, 0, 0, 1, 15f/16f, 1f/16f)),
        SE(1, 1, BlockElevatorFrame.SE, new AxisAlignedBB(15f / 16f, 0, 15f / 16f, 1, 15f/16f, 1)),
        SW(-1, 1, BlockElevatorFrame.SW, new AxisAlignedBB(0, 0, 15f / 16f, 1f / 16f, 15f/16f, 1)),
        NW(-1,-1, BlockElevatorFrame.NW, new AxisAlignedBB(0, 0, 0, 1f/16f, 15f/16f, 1f/16f));

        final int x;
        final int z;
        final BooleanProperty prop;
        final AxisAlignedBB aabb;

        Corner(int x, int z, BooleanProperty prop, AxisAlignedBB aabb) {
            this.x = x; this.z = z;
            this.prop = prop;
            this.aabb = aabb;
        }
    }
}
