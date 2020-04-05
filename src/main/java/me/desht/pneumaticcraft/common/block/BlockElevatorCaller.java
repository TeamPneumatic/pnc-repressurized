package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorCaller;
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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockElevatorCaller extends BlockPneumaticCraftCamo {
    public BlockElevatorCaller() {
        super(ModBlocks.defaultProps());
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

    // todo 1.14 figure out raytracing
//    @Override
//    public RayTraceResult collisionRayTrace(BlockState state, World world, BlockPos pos, Vec3d origin, Vec3d direction) {
//        setBlockBounds(FULL_BLOCK_AABB);
//        RayTraceResult rayTrace = super.collisionRayTrace(state, world, pos, origin, direction);
//        Direction orientation = getRotation(world, pos).getOpposite();
//        if (rayTrace != null && rayTrace.sideHit == orientation) {
//            TileEntity te = world.getTileEntity(pos);
//            if (te instanceof TileEntityElevatorCaller) {
//                TileEntityElevatorCaller caller = (TileEntityElevatorCaller) te;
//                for (TileEntityElevatorCaller.ElevatorButton button : caller.getFloors()) {
//                    float startX = 0, startZ = 0, endX = 0, endZ = 0;
//                    switch (orientation) {
//                        case NORTH:
//                            startZ = 0F;
//                            endZ = 0.01F;
//                            endX = 1 - (float) button.posX;
//                            startX = 1 - ((float) button.posX + (float) button.width);
//                            break;
//                        case SOUTH:
//                            startZ = 0.99F;
//                            endZ = 1F;
//                            startX = (float) button.posX;
//                            endX = (float) button.posX + (float) button.width;
//                            break;
//                        case WEST:
//                            startX = 0F;
//                            endX = 0.01F;
//                            startZ = (float) button.posX;
//                            endZ = (float) button.posX + (float) button.width;
//                            break;
//                        case EAST:
//                            startX = 0.99F;
//                            endX = 1F;
//                            endZ = 1 - (float) button.posX;
//                            startZ = 1 - ((float) button.posX + (float) button.width);
//                            break;
//                    }
//
//                    setBlockBounds(new AxisAlignedBB(startX, 1 - (float) (button.posY + button.height), startZ, endX, 1 - (float) button.posY, endZ));
//                    RayTraceResult buttonTrace = super.collisionRayTrace(state, world, pos, origin, direction);
//                    if (buttonTrace != null) {
//                        if (startX > 0.01F && startX < 0.98F) startX += 0.01F;
//                        if (startZ > 0.01F && startZ < 0.98F) startZ += 0.01F;
//                        if (endX > 0.02F && endX < 0.99F) endX -= 0.01F;
//                        if (endZ > 0.02F && endZ < 0.99F) endZ -= 0.01F;
//                        setBlockBounds(new AxisAlignedBB(startX, 1.01F - (float) (button.posY + button.height), startZ, endX, 0.99F - (float) button.posY, endZ));
//                        buttonTrace.subHit = button.floorNumber;
//                        return buttonTrace;
//                    }
//                }
//            }
//        }
//
//        setBlockBounds(FULL_BLOCK_AABB);
//        return rayTrace;
//    }

    @Override
    protected boolean doesCamoOverrideBounds() {
        return false;  // need to be able to highlight the buttons
    }

    public static void setSurroundingElevators(World world, BlockPos pos, int floor) {
        for (Direction dir : PneumaticCraftUtils.HORIZONTALS) {
            TileEntityElevatorBase elevator = getElevatorBase(world, pos.offset(dir).offset(Direction.DOWN, 2));
            if (elevator != null) {
                elevator.goToFloor(floor);
            }
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

    private void updateElevatorButtons(World world, BlockPos pos) {
        for (Direction dir : PneumaticCraftUtils.HORIZONTALS) {
            TileEntityElevatorBase elevator = getElevatorBase(world, pos.offset(dir).offset(Direction.DOWN, 2));
            if (elevator != null) {
                elevator.updateFloors();
                break;
            }
        }
    }

    private static TileEntityElevatorBase getElevatorBase(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        TileEntityElevatorBase elevator = null;
        if (block == ModBlocks.ELEVATOR_FRAME.get()) {
            elevator = BlockElevatorFrame.getElevatorTE(world, pos);
        }
        if (block == ModBlocks.ELEVATOR_BASE.get()) {
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

//    @OnlyIn(Dist.CLIENT)
//    public int getPackedLightmapCoords(BlockState state, IEnviromentBlockReader worldIn, BlockPos pos) {
//        int i = worldIn.getLightFor(LightType.SKY, pos);
//        return (i << 20) | 0xF0;
//    }

//    @Override
//    public boolean isOpaqueCube(BlockState state) {
//        return false ;//this should return false, because otherwise I can't give color to the rendered elevator buttons for some reason...
//    }

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
