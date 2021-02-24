package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorCaller;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Optional;

public class BlockElevatorCaller extends BlockPneumaticCraftCamo {
    public BlockElevatorCaller() {
        super(ModBlocks.defaultProps().notSolid());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityElevatorCaller.class;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityElevatorCaller) {
            TileEntityElevatorCaller teEC = (TileEntityElevatorCaller) te;
            if (!world.isRemote) {
                int floor = getFloorForHit(teEC, brtr.getFace(), brtr.getHitVec().x, brtr.getHitVec().y, brtr.getHitVec().z);
                if (floor >= 0) setSurroundingElevators(world, pos, floor);
            }
        }
        return getRotation(state).getOpposite() == brtr.getFace() ? ActionResultType.SUCCESS : ActionResultType.PASS;
    }

    private int getFloorForHit(TileEntityElevatorCaller teEC, Direction side, double hitX, double hitY, double hitZ) {
        double x;
        switch (side) {
            case NORTH: x = Math.abs(hitX % 1); break;
            case SOUTH: x = 1 - Math.abs(hitX % 1); break;
            case EAST: x = Math.abs(hitZ % 1); break;
            case WEST: x = 1 - Math.abs(hitZ % 1); break;
            default: return -1;
        }
        double y = 1 - (hitY % 1);

        for (TileEntityElevatorCaller.ElevatorButton button : teEC.getFloors()) {
            if (x >= button.posX && x <= button.posX + button.width && y >= button.posY && y <= button.posY + button.height) {
                return button.floorNumber;
            }
        }
        return -1;
    }

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
        return VoxelShapes.fullCube();
    }

    public static void setSurroundingElevators(World world, BlockPos pos, int floor) {
        for (Direction dir : DirectionUtil.HORIZONTALS) {
            getElevatorBase(world, pos.offset(dir).offset(Direction.DOWN, 2)).ifPresent(te -> te.goToFloor(floor));
        }
    }

    @Override
    public void onBlockAdded(BlockState newState, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(newState, world, pos, oldState, isMoving);

        updateElevatorButtons(world, pos);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        updateElevatorButtons(world, pos);

        super.onReplaced(state, world, pos, newState, isMoving);
    }

    /**
     * Called when a caller is added or removed; detect any connected elevator base (by finding an adjacent frame
     * and following it down), and tell it to rescan for elevator callers.
     * @param world the world
     * @param pos the blockpos where the caller has been placed/removed
     */
    private void updateElevatorButtons(World world, BlockPos pos) {
        for (Direction dir : DirectionUtil.HORIZONTALS) {
            boolean ok = getElevatorBase(world, pos.offset(dir).offset(Direction.DOWN, 2)).map(te -> {
                te.updateFloors(true);
                return true;
            }).orElse(false);
            if (ok) break;
        }
    }

    private static Optional<TileEntityElevatorBase> getElevatorBase(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if (block == ModBlocks.ELEVATOR_FRAME.get()) {
            return BlockElevatorFrame.getElevatorBase(world, pos);
        } else if (block == ModBlocks.ELEVATOR_BASE.get()) {
            return PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityElevatorBase.class)
                    .filter(TileEntityElevatorBase::isCoreElevator);
        }
        return Optional.empty();
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(BlockState state, IBlockReader par1IBlockAccess, BlockPos pos, Direction side) {
        TileEntity te = par1IBlockAccess.getTileEntity(pos);
        if (te instanceof TileEntityElevatorCaller) {
            TileEntityElevatorCaller teEc = (TileEntityElevatorCaller) te;
            return teEc.getEmittingRedstone() ? 15 : 0;
        }

        return 0;
    }
}
