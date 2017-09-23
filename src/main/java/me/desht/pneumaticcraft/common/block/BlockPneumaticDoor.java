package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoorBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockPneumaticDoor extends BlockPneumaticCraftModeled {
    private static final PropertyBool TOP_DOOR = PropertyBool.create("top_door");

    // true when the Pneumatic Door Base is determining if it should open the door dependent
    // on the player watched block.
    public boolean isTrackingPlayerEye;

    BlockPneumaticDoor() {
        super(Material.IRON, "pneumatic_door");
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TOP_DOOR, ROTATION);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta).withProperty(TOP_DOOR, (meta & 0x08) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state) + (state.getValue(TOP_DOOR) ? 8 : 0);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity te = PneumaticCraftUtils.getTileEntitySafely(worldIn, pos);
        if (te instanceof TileEntityPneumaticDoor) {
            TileEntityPneumaticDoor teDoor = (TileEntityPneumaticDoor) te;
            if (teDoor.rotationAngle != 0) {
                EnumFacing facing = teDoor.rightGoing ? state.getValue(ROTATION).rotateY() : state.getValue(ROTATION).rotateYCCW();
                state = state.withProperty(ROTATION, facing);
            }
        }
        return state;
    }

    public static boolean isTopDoor(IBlockState state) {
        return state.getValue(TOP_DOOR);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccess, BlockPos pos) {
        if (!blockAccess.getBlockState(pos).getPropertyKeys().contains(ROTATION)) {
            // getBoundingBox() can be called during placement (from World#mayPlace), before the
            // block is actually placed; handle this, or we'll crash with an IllegalArgumentException
            return FULL_BLOCK_AABB;
        }

        if (isTrackingPlayerEye) {
            return FULL_BLOCK_AABB;
        } else {
            float xMin = 0;
            float zMin = 0;
            float xMax = 1;
            float zMax = 1;
            TileEntity te = blockAccess.getTileEntity(pos);
            EnumFacing rotation = getRotation(blockAccess, pos);
            if (te instanceof TileEntityPneumaticDoor) {
                TileEntityPneumaticDoor door = (TileEntityPneumaticDoor) te;
                float cosinus = 13 / 16F - MathHelper.sin((float)Math.toRadians(door.rotationAngle)) * 13 / 16F;
                float sinus = 13 / 16F - MathHelper.cos((float) Math.toRadians(door.rotationAngle)) * 13 / 16F;
                if (door.rightGoing) {
                    switch (rotation) {
                        case NORTH:
                            zMin = cosinus;
                            xMax = 1 - sinus;
                            break;
                        case WEST:
                            xMin = cosinus;
                            zMin = sinus;
                            break;
                        case SOUTH:
                            zMax = 1 - cosinus;
                            xMin = sinus;
                            break;
                        case EAST:
                            xMax = 1 - cosinus;
                            zMax = 1 - sinus;
                            break;

                    }
                } else {
                    switch (rotation) {
                        case NORTH:
                            zMin = cosinus;
                            xMin = sinus;
                            break;
                        case WEST:
                            xMin = cosinus;
                            zMax = 1 - sinus;
                            break;
                        case SOUTH:
                            zMax = 1 - cosinus;
                            xMax = 1 - sinus;
                            break;
                        case EAST:
                            xMax = 1 - cosinus;
                            zMin = sinus;
                            break;

                    }
                }
            }
            boolean topDoor = isTopDoor(blockAccess.getBlockState(pos));
            return new AxisAlignedBB(xMin, topDoor ? -1 : 0, zMin, xMax, topDoor ? 1 : 2, zMax);
        }
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return getBoundingBox(blockState, worldIn, pos);
    }

//    @Override
//    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity) {
//        setBlockBoundsBasedOnState(world, pos);
//        super.addCollisionBoxesToList(world, pos, state, axisalignedbb, arraylist, par7Entity);
//        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
//    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPneumaticDoor.class;
    }

    /**
     * Checks to see if its valid to put this block at the specified coordinates. Args: world, x, y, z
     */
    @Override
    public boolean canPlaceBlockAt(World par1World, BlockPos pos) {
        return super.canPlaceBlockAt(par1World, pos) && par1World.isAirBlock(pos.offset(EnumFacing.UP));
    }

    @Override
    public void onBlockPlacedBy(World par1World, BlockPos pos, IBlockState state, EntityLivingBase par5EntityLiving, ItemStack par6ItemStack) {
        super.onBlockPlacedBy(par1World, pos, state, par5EntityLiving, par6ItemStack);
        par1World.setBlockState(pos.offset(EnumFacing.UP), par1World.getBlockState(pos).withProperty(TOP_DOOR, true), 3);
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public boolean rotateBlock(World world, EntityPlayer player, BlockPos pos, EnumFacing face) {
        IBlockState state = world.getBlockState(pos);
        if (isTopDoor(state)) {
            return rotateBlock(world, player, pos.offset(EnumFacing.DOWN), face);
        } else {
            super.rotateBlock(world, player, pos, face);
            IBlockState newState = world.getBlockState(pos);
            world.setBlockState(pos.offset(EnumFacing.UP), newState.withProperty(TOP_DOOR, true), 3);
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityPneumaticDoor) {
                ((TileEntityPneumaticDoor) te).rightGoing = true;
                ((TileEntityPneumaticDoor) te).setRotationAngle(0);
                TileEntity topDoor = world.getTileEntity(pos.offset(EnumFacing.UP));
                if (topDoor instanceof TileEntityPneumaticDoor) {
                    ((TileEntityPneumaticDoor) topDoor).sendDescriptionPacket();
                }
            }
        }
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float par7, float par8, float par9) {
        TileEntityPneumaticDoorBase doorBase = getDoorBase(world, pos);
        if (!world.isRemote && doorBase != null && doorBase.redstoneMode == 2 && doorBase.getPressure() >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR && hand == EnumHand.MAIN_HAND) {
            doorBase.setOpening(!doorBase.isOpening());
            doorBase.setNeighborOpening(doorBase.isOpening());
            return true;
        }
        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (isTopDoor(state)) {
            BlockPos lowerPos = pos.offset(EnumFacing.DOWN);
            if (world.getBlockState(lowerPos).getBlock() == Blockss.PNEUMATIC_DOOR)
                dropBlockAsItem(world, lowerPos, world.getBlockState(lowerPos), 0);
            world.setBlockToAir(lowerPos);
        } else {
            world.setBlockToAir(pos.offset(EnumFacing.UP));
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        return isTopDoor(state) ? 0 : 1;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        boolean powered = world.isBlockIndirectlyGettingPowered(pos) > 0;
        if (!powered) {
            powered = world.isBlockIndirectlyGettingPowered(pos.offset(isTopDoor(state) ? EnumFacing.DOWN : EnumFacing.UP)) > 0;
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
            return getDoorBase(world, pos.offset(EnumFacing.UP));
        } else {
            EnumFacing dir = getRotation(world, pos);
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
}
