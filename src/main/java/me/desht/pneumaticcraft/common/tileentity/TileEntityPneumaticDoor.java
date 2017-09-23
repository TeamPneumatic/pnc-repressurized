package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.block.BlockPneumaticDoor;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityPneumaticDoor extends TileEntityBase {
    @DescSynced
    @LazySynced
    public float rotationAngle;
    public float oldRotationAngle;
    @DescSynced
    public boolean rightGoing;

    public void setRotationAngle(float rotationAngle) {
        oldRotationAngle = this.rotationAngle;
        this.rotationAngle = rotationAngle;

        if (oldRotationAngle == 0f && rotationAngle != 0f || oldRotationAngle != 0f && rotationAngle == 0f) {
            IBlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(pos, state, state ,3);
        }

        // also rotate the TE for the other half of the door
        TileEntity otherTE = getWorld().getTileEntity(getPos().offset(isTopDoor() ? EnumFacing.DOWN : EnumFacing.UP));
        if (otherTE instanceof TileEntityPneumaticDoor) {
            TileEntityPneumaticDoor otherDoorHalf = (TileEntityPneumaticDoor) otherTE;
            otherDoorHalf.rightGoing = rightGoing;
            if (rotationAngle != otherDoorHalf.rotationAngle) {
                otherDoorHalf.setRotationAngle(rotationAngle);
            }
        }
    }

    public boolean isTopDoor() {
        return BlockPneumaticDoor.isTopDoor(getWorld().getBlockState(getPos()));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("rightGoing", rightGoing);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        rightGoing = tag.getBoolean("rightGoing");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 2, getPos().getZ() + 1);
    }
}
