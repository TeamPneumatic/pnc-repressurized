package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.block.BlockPneumaticDoor;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import net.minecraft.block.DoorBlock;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.IItemHandler;

public class TileEntityPneumaticDoor extends TileEntityTickableBase {
    @DescSynced
    @LazySynced
    public float rotationAngle;
    public float oldRotationAngle;
    @DescSynced
    public boolean rightGoing;  // true = door rotates clockwise when door base arm extends
    @DescSynced
    public int color = DyeColor.WHITE.getId();

    public TileEntityPneumaticDoor() {
        super(ModTileEntities.PNEUMATIC_DOOR.get());
    }

    public void setRotationAngle(float rotationAngle) {
        if (oldRotationAngle == rotationAngle) return;

        oldRotationAngle = this.rotationAngle;
        this.rotationAngle = rotationAngle;

        if (oldRotationAngle < 90f && rotationAngle == 90f) {
            world.setBlockState(pos, getBlockState().with(DoorBlock.OPEN, true));
        } else if (oldRotationAngle == 90f && rotationAngle < 90f) {
            world.setBlockState(pos, getBlockState().with(DoorBlock.OPEN, false));
        }

        // also rotate the TE for the other half of the door
        TileEntity otherTE = getWorld().getTileEntity(getPos().offset(isTopDoor() ? Direction.DOWN : Direction.UP));
        if (otherTE instanceof TileEntityPneumaticDoor) {
            TileEntityPneumaticDoor otherDoorHalf = (TileEntityPneumaticDoor) otherTE;
            otherDoorHalf.rightGoing = rightGoing;
            otherDoorHalf.markDirty();
            if (rotationAngle != otherDoorHalf.rotationAngle) {
                otherDoorHalf.setRotationAngle(rotationAngle);
            }
        }
    }

    public boolean setColor(DyeColor dyeColor) {
        if (color != dyeColor.getId() && !getBlockState().get(BlockPneumaticDoor.TOP_DOOR)) {
            color = (byte) dyeColor.getId();
            TileEntity topHalf = getWorld().getTileEntity(getPos().up());
            if (topHalf instanceof TileEntityPneumaticDoor) {
                ((TileEntityPneumaticDoor) topHalf).color = color;
            }
            if (!world.isRemote) {
                markDirty();
                topHalf.markDirty();
                sendDescriptionPacket();
            }
            return true;
        }
        return false;
    }

    private boolean isTopDoor() {
        return BlockPneumaticDoor.isTopDoor(getWorld().getBlockState(getPos()));
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        tag.putBoolean("rightGoing", rightGoing);
        tag.putInt("color", color);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        rightGoing = tag.getBoolean("rightGoing");
        color = tag.getInt("color");
    }

    @Override
    public void serializeExtraItemData(CompoundNBT blockEntityTag) {
        super.serializeExtraItemData(blockEntityTag);

        blockEntityTag.putInt("color", color);
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(),
                getPos().getX() + 1, getPos().getY() + 2, getPos().getZ() + 1);
    }

    @Override
    public boolean canRenderBreaking() {
        return true;
    }

    @Override
    public boolean shouldPreserveStateOnBreak() {
        // keep color even if pickaxed
        return true;
    }
}
