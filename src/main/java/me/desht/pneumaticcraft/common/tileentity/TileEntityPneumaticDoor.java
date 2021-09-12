package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.block.BlockPneumaticDoor;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import net.minecraft.block.BlockState;
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
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(DoorBlock.OPEN, true));
        } else if (oldRotationAngle == 90f && rotationAngle < 90f) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(DoorBlock.OPEN, false));
        }

        // also rotate the TE for the other half of the door
        TileEntity otherTE = getLevel().getBlockEntity(getBlockPos().relative(isTopDoor() ? Direction.DOWN : Direction.UP));
        if (otherTE instanceof TileEntityPneumaticDoor) {
            TileEntityPneumaticDoor otherDoorHalf = (TileEntityPneumaticDoor) otherTE;
            otherDoorHalf.rightGoing = rightGoing;
            otherDoorHalf.setChanged();
            if (rotationAngle != otherDoorHalf.rotationAngle) {
                otherDoorHalf.setRotationAngle(rotationAngle);
            }
        }
    }

    public boolean setColor(DyeColor dyeColor) {
        if (color != dyeColor.getId() && !getBlockState().getValue(BlockPneumaticDoor.TOP_DOOR)) {
            color = (byte) dyeColor.getId();
            TileEntity topHalf = getLevel().getBlockEntity(getBlockPos().above());
            if (topHalf instanceof TileEntityPneumaticDoor) {
                ((TileEntityPneumaticDoor) topHalf).color = color;
            }
            if (!level.isClientSide) {
                setChanged();
                topHalf.setChanged();
                sendDescriptionPacket();
            }
            return true;
        }
        return false;
    }

    private boolean isTopDoor() {
        return BlockPneumaticDoor.isTopDoor(getLevel().getBlockState(getBlockPos()));
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);

        tag.putBoolean("rightGoing", rightGoing);
        tag.putInt("color", color);
        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        rightGoing = tag.getBoolean("rightGoing");
        color = tag.getInt("color");
    }

    @Override
    public void serializeExtraItemData(CompoundNBT blockEntityTag, boolean preserveState) {
        super.serializeExtraItemData(blockEntityTag, preserveState);

        blockEntityTag.putInt("color", color);
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(),
                getBlockPos().getX() + 1, getBlockPos().getY() + 2, getBlockPos().getZ() + 1);
    }

//    @Override
//    public boolean canRenderBreaking() {
//        return true;
//    }

    @Override
    public boolean shouldPreserveStateOnBreak() {
        // keep color even if pickaxed
        return true;
    }
}
