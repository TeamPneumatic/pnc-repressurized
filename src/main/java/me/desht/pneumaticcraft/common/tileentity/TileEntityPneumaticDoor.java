package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.block.BlockPneumaticDoor;
import me.desht.pneumaticcraft.common.block.BlockPneumaticDoor.DoorState;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
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
        if (oldRotationAngle == rotationAngle) return;

        oldRotationAngle = this.rotationAngle;
        this.rotationAngle = rotationAngle;

        if (rotationAngle != oldRotationAngle &&
                (oldRotationAngle == 0f || oldRotationAngle == 90f || rotationAngle == 0f || rotationAngle == 90f)) {
            if (getWorld().isRemote) {
                // force a redraw to make the static door model appear or disappear
                rerenderTileEntity();
            }
        }

        BlockState state = getBlockState();
        if (rotationAngle == 0) {
            state = state.with(BlockPneumaticDoor.DOOR_STATE, DoorState.CLOSED);
        } else if (rotationAngle == 90) {
            Direction originalRotation = state.get(BlockStateProperties.HORIZONTAL_FACING);
            if (originalRotation != Direction.UP && originalRotation != Direction.DOWN) {
                Direction facing = rightGoing ? originalRotation.rotateY() : originalRotation.rotateYCCW();
                state = state.with(BlockStateProperties.HORIZONTAL_FACING, facing);
            }
            state = state.with(BlockPneumaticDoor.DOOR_STATE, DoorState.OPEN);
        } else {
            state = state.with(BlockPneumaticDoor.DOOR_STATE, DoorState.MOVING);
        }
        world.setBlockState(pos, state);

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
