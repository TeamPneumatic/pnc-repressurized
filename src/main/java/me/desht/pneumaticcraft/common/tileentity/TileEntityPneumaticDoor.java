package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.block.BlockPneumaticDoor;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.IItemHandlerModifiable;

public class TileEntityPneumaticDoor extends TileEntityTickableBase {
    @DescSynced
    @LazySynced
    public float rotationAngle;
    public float oldRotationAngle;
    @DescSynced
    public boolean rightGoing;

    public TileEntityPneumaticDoor() {
        super(ModTileEntityTypes.PNEUMATIC_DOOR);
    }

    public void setRotationAngle(float rotationAngle) {
        oldRotationAngle = this.rotationAngle;
        this.rotationAngle = rotationAngle;

        if (rotationAngle != oldRotationAngle &&
                (oldRotationAngle == 0f || oldRotationAngle == 90f || rotationAngle == 0f || rotationAngle == 90f)) {
            if (getWorld().isRemote) {
                // force a redraw to make the static door model appear or disappear
                getWorld().markForRerender(pos);
            }
        }

        // also rotate the TE for the other half of the door
        TileEntity otherTE = getWorld().getTileEntity(getPos().offset(isTopDoor() ? Direction.DOWN : Direction.UP));
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
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putBoolean("rightGoing", rightGoing);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        rightGoing = tag.getBoolean("rightGoing");
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 2, getPos().getZ() + 1);
    }

    @Override
    public boolean canRenderBreaking() {
        return true;
    }
}
