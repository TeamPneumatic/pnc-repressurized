package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoorBase;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPneumaticDoorBase extends BlockPneumaticCraftCamo {

    BlockPneumaticDoorBase() {
        super(Material.IRON, "pneumatic_door_base");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPneumaticDoorBase.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.PNEUMATIC_DOOR;
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);
        TileEntityPneumaticDoorBase doorBase = (TileEntityPneumaticDoorBase) world.getTileEntity(pos);
        updateDoorSide(doorBase);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPneumaticDoorBase) {
            updateDoorSide((TileEntityPneumaticDoorBase) te);
            EnumFacing dir = ((TileEntityPneumaticDoorBase) te).getRotation();
            if (world.getBlockState(pos.offset(dir)).getBlock() == Blockss.PNEUMATIC_DOOR) {
                Blockss.PNEUMATIC_DOOR.neighborChanged(world.getBlockState(pos.offset(dir)), world, pos, block, pos.offset(dir));
            }
        }
    }

    private void updateDoorSide(TileEntityPneumaticDoorBase doorBase) {
        TileEntity teDoor = doorBase.getWorld().getTileEntity(doorBase.getPos().offset(doorBase.getRotation()));
        if (teDoor instanceof TileEntityPneumaticDoor) {
            TileEntityPneumaticDoor door = (TileEntityPneumaticDoor) teDoor;
            if (doorBase.getRotation().rotateY() == door.getRotation() && door.rightGoing || doorBase.getRotation().rotateYCCW() == door.getRotation() && !door.rightGoing) {
                door.rightGoing = !door.rightGoing;
                door.setRotationAngle(0);
            }
        }
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return false;
    }
}
