package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorCaller;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockElevatorCaller extends BlockPneumaticCraftCamo {

    BlockElevatorCaller() {
        super(Material.IRON, "elevator_caller");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityElevatorCaller.class;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityElevatorCaller) {
            TileEntityElevatorCaller teEC = (TileEntityElevatorCaller) te;
            if (!world.isRemote) {
                int floor = getFloorForHit(teEC, side, hitX, hitY, hitZ);
                if (floor >= 0) setSurroundingElevators(world, pos, floor);
            }
        }
        return getRotation(state).getOpposite() == side;
    }

    private int getFloorForHit(TileEntityElevatorCaller teEC, EnumFacing side, float hitX, float hitY, float hitZ) {
        float x;
        switch (side) {
            case NORTH: x = hitX; break;
            case SOUTH: x = 1.0f - hitX; break;
            case EAST: x = hitZ; break;
            case WEST: x = 1.0f - hitZ; break;
            default: return -1;
        }
//        float x = side == EnumFacing.NORTH || side == EnumFacing.SOUTH ? hitX : hitZ;
        float y = 1.0f - hitY;

        for (TileEntityElevatorCaller.ElevatorButton button : teEC.getFloors()) {
            if (x >= button.posX && x <= button.posX + button.width && y >= button.posY && y <= button.posY + button.height) {
                return button.floorNumber;
            }
        }
        return -1;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d origin, Vec3d direction) {
        setBlockBounds(FULL_BLOCK_AABB);
        RayTraceResult rayTrace = super.collisionRayTrace(state, world, pos, origin, direction);
        EnumFacing orientation = getRotation(world, pos).getOpposite();
        if (rayTrace != null && rayTrace.sideHit == orientation) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityElevatorCaller) {
                TileEntityElevatorCaller caller = (TileEntityElevatorCaller) te;
                for (TileEntityElevatorCaller.ElevatorButton button : caller.getFloors()) {
                    float startX = 0, startZ = 0, endX = 0, endZ = 0;
                    switch (orientation) {
                        case NORTH:
                            startZ = 0F;
                            endZ = 0.01F;
                            endX = 1 - (float) button.posX;
                            startX = 1 - ((float) button.posX + (float) button.width);
                            break;
                        case SOUTH:
                            startZ = 0.99F;
                            endZ = 1F;
                            startX = (float) button.posX;
                            endX = (float) button.posX + (float) button.width;
                            break;
                        case WEST:
                            startX = 0F;
                            endX = 0.01F;
                            startZ = (float) button.posX;
                            endZ = (float) button.posX + (float) button.width;
                            break;
                        case EAST:
                            startX = 0.99F;
                            endX = 1F;
                            endZ = 1 - (float) button.posX;
                            startZ = 1 - ((float) button.posX + (float) button.width);
                            break;
                    }

                    setBlockBounds(new AxisAlignedBB(startX, 1 - (float) (button.posY + button.height), startZ, endX, 1 - (float) button.posY, endZ));
                    RayTraceResult buttonTrace = super.collisionRayTrace(state, world, pos, origin, direction);
                    if (buttonTrace != null) {
                        if (startX > 0.01F && startX < 0.98F) startX += 0.01F;
                        if (startZ > 0.01F && startZ < 0.98F) startZ += 0.01F;
                        if (endX > 0.02F && endX < 0.99F) endX -= 0.01F;
                        if (endZ > 0.02F && endZ < 0.99F) endZ -= 0.01F;
                        setBlockBounds(new AxisAlignedBB(startX, 1.01F - (float) (button.posY + button.height), startZ, endX, 0.99F - (float) button.posY, endZ));
                        buttonTrace.subHit = button.floorNumber;
                        return buttonTrace;
                    }
                }
            }
        }

        setBlockBounds(FULL_BLOCK_AABB);
        return rayTrace;
    }

    public static void setSurroundingElevators(World world, BlockPos pos, int floor) {
        for (EnumFacing dir : EnumFacing.HORIZONTALS) {
            TileEntityElevatorBase elevator = getElevatorBase(world, pos.offset(dir));
            if (elevator != null) {
                elevator.goToFloor(floor);
            }
        }
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        updateElevatorButtons(worldIn, pos);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        updateElevatorButtons(world, pos);
        super.breakBlock(world, pos, state);
    }

    private void updateElevatorButtons(World world, BlockPos pos) {
        for (EnumFacing dir : EnumFacing.VALUES) {
            if (dir.getAxis() != Axis.Y) {
                TileEntityElevatorBase elevator = getElevatorBase(world, pos.offset(dir).offset(EnumFacing.DOWN, 2));
                if (elevator != null) {
                    elevator.updateFloors();
                }
            }
        }
    }

    private static TileEntityElevatorBase getElevatorBase(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        TileEntityElevatorBase elevator = null;
        if (block == Blockss.ELEVATOR_FRAME) {
            elevator = BlockElevatorFrame.getElevatorTE(world, pos);
        }
        if (block == Blockss.ELEVATOR_BASE) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityElevatorBase && ((TileEntityElevatorBase) te).isCoreElevator()) {
                elevator = (TileEntityElevatorBase) te;
            }
        }
        return elevator;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false ;//this should return false, because otherwise I can't give color to the rendered elevator buttons for some reason...
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess par1IBlockAccess, BlockPos pos, EnumFacing side) {
        TileEntity te = par1IBlockAccess.getTileEntity(pos);
        if (te instanceof TileEntityElevatorCaller) {
            TileEntityElevatorCaller teEc = (TileEntityElevatorCaller) te;
            return teEc.getEmittingRedstone() ? 15 : 0;
        }

        return 0;
    }
}
