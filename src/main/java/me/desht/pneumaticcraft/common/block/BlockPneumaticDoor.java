package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoorBase;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockPneumaticDoor extends BlockPneumaticCraft {
    private static final BooleanProperty TOP_DOOR = BooleanProperty.create("top_door");
    public static final EnumProperty<DoorState> DOOR_STATE = EnumProperty.create("door_state", DoorState.class);

    // true when the Pneumatic Door Base is determining if it should open the door dependent
    // on the player watched block.
    public boolean isTrackingPlayerEye;

    public BlockPneumaticDoor() {
        super(Material.IRON, "pneumatic_door");
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(TOP_DOOR, DOOR_STATE);
    }

    public static boolean isTopDoor(BlockState state) {
        return state.get(TOP_DOOR);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        if (isTrackingPlayerEye) {
            return VoxelShapes.fullCube();
        } else {
            return calculateVoxelShape(state, world, pos, 15);
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        if (isTrackingPlayerEye) {
            return VoxelShapes.fullCube();
        } else {
            return calculateVoxelShape(state, world, pos, 13);
        }
    }

    private VoxelShape calculateVoxelShape(BlockState state, IBlockReader world, BlockPos pos, int thickness) {
        float xMin = 0;
        float zMin = 0;
        float xMax = 1;
        float zMax = 1;
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPneumaticDoor) {
            Direction rotation = state.get(ROTATION);
            TileEntityPneumaticDoor door = (TileEntityPneumaticDoor) te;
            float cosinus = thickness / 16F - MathHelper.sin((float)Math.toRadians(door.rotationAngle)) * thickness / 16F;
            float sinus = thickness / 16F - MathHelper.cos((float) Math.toRadians(door.rotationAngle)) * thickness / 16F;
            if (door.rightGoing) {
                switch (rotation) {
                    case NORTH:
                        zMin = cosinus; xMax = 1 - sinus;
                        break;
                    case WEST:
                        xMin = cosinus; zMin = sinus;
                        break;
                    case SOUTH:
                        zMax = 1 - cosinus; xMin = sinus;
                        break;
                    case EAST:
                        xMax = 1 - cosinus; zMax = 1 - sinus;
                        break;
                }
            } else {
                switch (rotation) {
                    case NORTH:
                        zMin = cosinus; xMin = sinus;
                        break;
                    case WEST:
                        xMin = cosinus; zMax = 1 - sinus;
                        break;
                    case SOUTH:
                        zMax = 1 - cosinus; xMax = 1 - sinus;
                        break;
                    case EAST:
                        xMax = 1 - cosinus; zMin = sinus;
                        break;
                }
            }
        }
        boolean topDoor = state.get(TOP_DOOR);
        return VoxelShapes.create(new AxisAlignedBB(xMin, topDoor ? -1 : 0, zMin, xMax, topDoor ? 1 : 2, zMax));
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return !state.get(TOP_DOOR) && super.canRenderInLayer(state, layer);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPneumaticDoor.class;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        BlockPos down = pos.down();
        BlockState belowState = world.getBlockState(down);
        if (state.get(TOP_DOOR)) {
            return belowState.getBlock() == this;
        } else {
            return Block.hasSolidSide(belowState, world, down, Direction.UP);
        }
    }

    @Override
    public void onBlockPlacedBy(World par1World, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack par6ItemStack) {
        super.onBlockPlacedBy(par1World, pos, state, par5EntityLiving, par6ItemStack);

        par1World.setBlockState(pos.offset(Direction.UP), par1World.getBlockState(pos).with(TOP_DOOR, true), 3);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos posDown = pos.down();
        BlockPos posUp = pos.up();
        if (player.isCreative() && isTopDoor(state) && worldIn.getBlockState(posDown).getBlock() == this) {
            worldIn.removeBlock(posDown, false);
        }
        if (!isTopDoor(state) && worldIn.getBlockState(posUp).getBlock() == this) {
            if (player.isCreative()) {
                worldIn.removeBlock(pos, false);
            }
            worldIn.removeBlock(posUp, false);
        }
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public boolean onWrenched(World world, PlayerEntity player, BlockPos pos, Direction face, Hand hand) {
        BlockState state = world.getBlockState(pos);
        if (isTopDoor(state)) {
            return onWrenched(world, player, pos.offset(Direction.DOWN), face, hand);
        } else {
            super.onWrenched(world, player, pos, face, hand);
            BlockState newState = world.getBlockState(pos);
            world.setBlockState(pos.offset(Direction.UP), newState.with(TOP_DOOR, true), 3);
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityPneumaticDoor) {
                ((TileEntityPneumaticDoor) te).rightGoing = true;
                ((TileEntityPneumaticDoor) te).setRotationAngle(0);
                TileEntity topDoor = world.getTileEntity(pos.offset(Direction.UP));
                if (topDoor instanceof TileEntityPneumaticDoor) {
                    ((TileEntityPneumaticDoor) topDoor).sendDescriptionPacket();
                }
            }
        }
        return true;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        TileEntityPneumaticDoorBase doorBase = getDoorBase(world, pos);
        if (!world.isRemote && doorBase != null && doorBase.redstoneMode == 2 && doorBase.getPressure() >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR && hand == Hand.MAIN_HAND) {
            doorBase.setOpening(!doorBase.isOpening());
            doorBase.setNeighborOpening(doorBase.isOpening());
            return true;
        }
        return false;
    }

    // todo 1.14 loot table
//    public Item getItemDropped(BlockState state, Random rand, int fortune) {
//        return isTopDoor(state) ? Items.AIR : super.getItemDropped(state, rand, fortune);
//    }
//
//    @Override
//    public void breakBlock(World world, BlockPos pos, BlockState state) {
//        if (isTopDoor(state)) {
//            BlockPos lowerPos = pos.offset(Direction.DOWN);
//            if (world.getBlockState(lowerPos).getBlock() == ModBlocks.PNEUMATIC_DOOR)
//                dropBlockAsItem(world, lowerPos, world.getBlockState(lowerPos), 0);
//            world.setBlockToAir(lowerPos);
//        } else {
//            world.setBlockToAir(pos.offset(Direction.UP));
//        }
//        super.breakBlock(world, pos, state);
//    }
//
//    @Override
//    public int quantityDropped(BlockState state, int fortune, Random random) {
//        return isTopDoor(state) ? 0 : 1;
//    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean b) {
        boolean powered = world.getRedstonePowerFromNeighbors(pos) > 0;
        if (!powered) {
            powered = world.getRedstonePowerFromNeighbors(pos.offset(isTopDoor(state) ? Direction.DOWN : Direction.UP)) > 0;
        }
        TileEntityPneumaticDoorBase doorBase = getDoorBase(world, pos);
        if (!world.isRemote && doorBase != null && doorBase.getPressure() >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR) {
            if (powered != doorBase.wasPowered) {
                doorBase.wasPowered = powered;
                doorBase.setOpening(powered);
            }
        }
    }

    private TileEntityPneumaticDoorBase getDoorBase(World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() != this) return null;
        if (!isTopDoor(world.getBlockState(pos))) {
            return getDoorBase(world, pos.offset(Direction.UP));
        } else {
            Direction dir = getRotation(world, pos);
            if (dir.getAxis() == Direction.Axis.Y) {
                // should never happen, but see https://github.com/TeamPneumatic/pnc-repressurized/issues/284
                return null;
            }
            TileEntity te1 = world.getTileEntity(pos.offset(dir.rotateY()));
            if (te1 instanceof TileEntityPneumaticDoorBase) {
                TileEntityPneumaticDoorBase door = (TileEntityPneumaticDoorBase) te1;
                if (door.getRotation() == dir.rotateYCCW()) {
                    return door;
                }
            }
            TileEntity te2 = world.getTileEntity(pos.offset(dir.rotateYCCW()));
            if (te2 instanceof TileEntityPneumaticDoorBase) {
                TileEntityPneumaticDoorBase door = (TileEntityPneumaticDoorBase) te2;
                if (door.getRotation() == dir.rotateY()) {
                    return door;
                }
            }
            return null;
        }
    }

    public enum DoorState implements IStringSerializable {
        CLOSED("closed"), MOVING("moving"), OPEN("open");

        private final String name;

        DoorState(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
