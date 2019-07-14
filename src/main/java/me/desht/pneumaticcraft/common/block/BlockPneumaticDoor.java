package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoorBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockPneumaticDoor extends BlockPneumaticCraftModeled {
    private static final PropertyBool TOP_DOOR = PropertyBool.create("top_door");
    private static final PropertyEnum<DoorState> DOOR_STATE = PropertyEnum.create("door_state", DoorState.class);

    // true when the Pneumatic Door Base is determining if it should open the door dependent
    // on the player watched block.
    public boolean isTrackingPlayerEye;
    private int thickness = 13;  // 13/16 for bounding box, 15/16 for collision box

    public BlockPneumaticDoor() {
        super(Material.IRON, "pneumatic_door");
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TOP_DOOR, ROTATION, DOOR_STATE);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta).withProperty(TOP_DOOR, (meta & 0x08) != 0);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return super.getMetaFromState(state) + (state.getValue(TOP_DOOR) ? 8 : 0);
    }

    @Override
    public BlockState getActualState(BlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity te = PneumaticCraftUtils.getTileEntitySafely(worldIn, pos);
        if (te instanceof TileEntityPneumaticDoor) {
            TileEntityPneumaticDoor teDoor = (TileEntityPneumaticDoor) te;
            if (teDoor.rotationAngle == 90) {
                Direction originalRotation = state.getValue(ROTATION);
                if(originalRotation != Direction.UP && originalRotation != Direction.DOWN){
                    Direction facing = teDoor.rightGoing ? originalRotation.rotateY() : originalRotation.rotateYCCW();
                    state = state.withProperty(ROTATION, facing);
                }
                state = state.withProperty(DOOR_STATE, DoorState.OPEN);
            } else if (teDoor.rotationAngle == 0) {
                state = state.withProperty(DOOR_STATE, DoorState.CLOSED);
            } else if (teDoor.rotationAngle > 0) {
                // currently rotating - hide the static model; TESR will show the rotating door
                state = state.withProperty(DOOR_STATE, DoorState.MOVING);
            }
        }
        return state;
    }

    public static boolean isTopDoor(BlockState state) {
        return state.getValue(TOP_DOOR);
    }

    @Override
    public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess blockAccess, BlockPos pos) {
        if (isTrackingPlayerEye) {
            return FULL_BLOCK_AABB;
        } else {
            float xMin = 0;
            float zMin = 0;
            float xMax = 1;
            float zMax = 1;
            TileEntity te = blockAccess.getTileEntity(pos);
            Direction rotation = state.getValue(ROTATION);
            if (te instanceof TileEntityPneumaticDoor) {
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
            boolean topDoor = state.getValue(TOP_DOOR);
            return new AxisAlignedBB(xMin, topDoor ? -1 : 0, zMin, xMax, topDoor ? 1 : 2, zMax);
        }
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(BlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        thickness = 15;
        AxisAlignedBB aabb = getBoundingBox(blockState, worldIn, pos);
        thickness = 13;
        return aabb;
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return !state.getValue(TOP_DOOR) && super.canRenderInLayer(state, layer);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPneumaticDoor.class;
    }

    @Override
    public boolean canPlaceBlockAt(World par1World, BlockPos pos) {
        return super.canPlaceBlockAt(par1World, pos) && par1World.isAirBlock(pos.offset(Direction.UP));
    }

    @Override
    public void onBlockPlacedBy(World par1World, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack par6ItemStack) {
        super.onBlockPlacedBy(par1World, pos, state, par5EntityLiving, par6ItemStack);
        par1World.setBlockState(pos.offset(Direction.UP), par1World.getBlockState(pos).withProperty(TOP_DOOR, true), 3);
    }

    public Item getItemDropped(BlockState state, Random rand, int fortune) {
        return isTopDoor(state) ? Items.AIR : super.getItemDropped(state, rand, fortune);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos posDown = pos.down();
        BlockPos posUp = pos.up();
        if (player.capabilities.isCreativeMode && isTopDoor(state) && worldIn.getBlockState(posDown).getBlock() == this) {
            worldIn.setBlockToAir(posDown);
        }
        if (!isTopDoor(state) && worldIn.getBlockState(posUp).getBlock() == this) {
            if (player.capabilities.isCreativeMode) {
                worldIn.setBlockToAir(pos);
            }
            worldIn.setBlockToAir(posUp);
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
            world.setBlockState(pos.offset(Direction.UP), newState.withProperty(TOP_DOOR, true), 3);
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
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float par7, float par8, float par9) {
        TileEntityPneumaticDoorBase doorBase = getDoorBase(world, pos);
        if (!world.isRemote && doorBase != null && doorBase.redstoneMode == 2 && doorBase.getPressure() >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR && hand == Hand.MAIN_HAND) {
            doorBase.setOpening(!doorBase.isOpening());
            doorBase.setNeighborOpening(doorBase.isOpening());
            return true;
        }
        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, BlockState state) {
        if (isTopDoor(state)) {
            BlockPos lowerPos = pos.offset(Direction.DOWN);
            if (world.getBlockState(lowerPos).getBlock() == ModBlocks.PNEUMATIC_DOOR)
                dropBlockAsItem(world, lowerPos, world.getBlockState(lowerPos), 0);
            world.setBlockToAir(lowerPos);
        } else {
            world.setBlockToAir(pos.offset(Direction.UP));
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public int quantityDropped(BlockState state, int fortune, Random random) {
        return isTopDoor(state) ? 0 : 1;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
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
