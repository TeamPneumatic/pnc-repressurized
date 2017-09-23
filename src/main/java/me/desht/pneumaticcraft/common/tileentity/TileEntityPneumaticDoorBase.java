package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.BlockPneumaticDoor;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public class TileEntityPneumaticDoorBase extends TileEntityPneumaticBase implements IRedstoneControl, IMinWorkingPressure {
    public static final int INVENTORY_SIZE = 1;
    private TileEntityPneumaticDoor door;
    private TileEntityPneumaticDoorBase doubleDoor;
    @DescSynced
    public boolean rightGoing;
    public float oldProgress;
    @DescSynced
    @LazySynced
    public float progress;
    @DescSynced
    private boolean opening;
    public boolean wasPowered;
    @DescSynced
    private ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE);
    @GuiSynced
    public int redstoneMode;
    public static final int CAMO_SLOT = 0;
    private ItemStack oldCamo = ItemStack.EMPTY;

    public TileEntityPneumaticDoorBase() {
        super(PneumaticValues.DANGER_PRESSURE_PNEUMATIC_DOOR, PneumaticValues.MAX_PRESSURE_PNEUMATIC_DOOR, PneumaticValues.VOLUME_PNEUMATIC_DOOR, 4);
        addApplicableUpgrade(EnumUpgrade.SPEED, EnumUpgrade.RANGE);
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    @Override
    public void update() {
        super.update();
        oldProgress = progress;
        if (!getWorld().isRemote) {
            if (getPressure() >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR) {
                if (getWorld().getTotalWorldTime() % 60 == 0) {
                    TileEntity te = getWorld().getTileEntity(getPos().offset(getRotation(), 3));
                    if (te instanceof TileEntityPneumaticDoorBase) {
                        doubleDoor = (TileEntityPneumaticDoorBase) te;
                    } else {
                        doubleDoor = null;
                    }
                }
                setOpening(shouldOpen() || isNeighborOpening());
                setNeighborOpening(isOpening());
            } else {
                setOpening(true);
            }
        }
        float targetProgress = opening ? 1F : 0F;
        float speedMultiplier = getSpeedMultiplierFromUpgrades();
        if (progress < targetProgress) {
            if (progress < targetProgress - TileEntityConstants.PNEUMATIC_DOOR_EXTENSION) {
                progress += TileEntityConstants.PNEUMATIC_DOOR_SPEED_FAST * speedMultiplier;
            } else {
                progress += TileEntityConstants.PNEUMATIC_DOOR_SPEED_SLOW * speedMultiplier;
            }
            if (progress > targetProgress) progress = targetProgress;
        }
        if (progress > targetProgress) {
            if (progress > targetProgress + TileEntityConstants.PNEUMATIC_DOOR_EXTENSION) {
                progress -= TileEntityConstants.PNEUMATIC_DOOR_SPEED_FAST * speedMultiplier;
            } else {
                progress -= TileEntityConstants.PNEUMATIC_DOOR_SPEED_SLOW * speedMultiplier;
            }
            if (progress < targetProgress) progress = targetProgress;
        }
        if (!getWorld().isRemote)
            addAir((int) (-Math.abs(oldProgress - progress) * PneumaticValues.USAGE_PNEUMATIC_DOOR * (getSpeedUsageMultiplierFromUpgrades() / speedMultiplier)));

        door = getDoor();
        if (door != null) {
            door.setRotationAngle(progress * 90);
            if (!getWorld().isRemote) rightGoing = door.rightGoing;
        }

        if (!ItemStack.areItemsEqual(oldCamo, inventory.getStackInSlot(CAMO_SLOT))) {
            oldCamo = inventory.getStackInSlot(CAMO_SLOT);
            //TODO 1.8 fix camo meta     getWorld().setBlockMetadataWithNotify(getPos().getX(), getPos().getY(), getPos().getZ(), inventory[CAMO_SLOT] != null ? inventory[CAMO_SLOT].getItemDamage() % 16 : 0, 2);
            rerenderChunk();
        }
    }

    private boolean shouldOpen() {
        switch (redstoneMode) {
            case 0:
            case 1:
                int range = TileEntityConstants.RANGE_PNEUMATIC_DOOR_BASE + this.getUpgrades(EnumUpgrade.RANGE);
                AxisAlignedBB aabb = new AxisAlignedBB(getPos().getX() - range, getPos().getY() - range, getPos().getZ() - range, getPos().getX() + range + 1, getPos().getY() + range + 1, getPos().getZ() + range + 1);
                List<EntityPlayer> players = getWorld().getEntitiesWithinAABB(EntityPlayer.class, aabb);
                for (EntityPlayer player : players) {
                    if (PneumaticCraftUtils.getProtectingSecurityStations(getWorld(), getPos(), player, false, false) == 0) {
                        if (redstoneMode == 0) {
                            return true;
                        } else {
                            ((BlockPneumaticDoor) Blockss.PNEUMATIC_DOOR).isTrackingPlayerEye = true;
                            BlockPos lookedPosition = PneumaticCraftUtils.getEntityLookedBlock(player, range * 1.41F); //max range = range * sqrt(2).
                            ((BlockPneumaticDoor) Blockss.PNEUMATIC_DOOR).isTrackingPlayerEye = false;
                            if (lookedPosition != null) {
                                if (lookedPosition.equals(new BlockPos(getPos().getX(), getPos().getY(), getPos().getZ()))) {
                                    return true;
                                } else {
                                    if (door != null) {
                                        if (lookedPosition.equals(new BlockPos(door.getPos().getX(), door.getPos().getY(), door.getPos().getZ())))
                                            return true;
                                        if (lookedPosition.equals(new BlockPos(door.getPos().getX(), door.getPos().getY() + (door.isTopDoor() ? -1 : 1), door.getPos().getZ())))
                                            return true;
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            case 2:
                return opening;
        }
        return false;
    }

    public void setOpening(boolean opening) {
        boolean wasOpening = this.opening;
        this.opening = opening;
        if (this.opening != wasOpening) sendDescriptionPacket();
    }

    public boolean isOpening() {
        return opening;
    }

    private boolean isNeighborOpening() {
        return doubleDoor != null && doubleDoor.shouldOpen();
    }

    public void setNeighborOpening(boolean opening) {
        if (doubleDoor != null && doubleDoor.getPressure() >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR) {
            doubleDoor.setOpening(opening);
        }
    }

    @Override
    public boolean isConnectedTo(EnumFacing side) {
        return side != EnumFacing.UP;
    }

    private TileEntityPneumaticDoor getDoor() {
        TileEntity te = getWorld().getTileEntity(getPos().offset(getRotation()).add(0, -1, 0));
        if (te instanceof TileEntityPneumaticDoor) {
            TileEntityPneumaticDoor teDoor = (TileEntityPneumaticDoor) te;
            if (getRotation().rotateY() == teDoor.getRotation() && !teDoor.rightGoing) {
                return (TileEntityPneumaticDoor) te;
            } else if (getRotation().rotateYCCW() == teDoor.getRotation() && teDoor.rightGoing) {
                return (TileEntityPneumaticDoor) te;
            }
        }
        return null;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        progress = tag.getFloat("extension");
        opening = tag.getBoolean("opening");
        redstoneMode = tag.getInteger("redstoneMode");
        rightGoing = tag.getBoolean("rightGoing");
        inventory.deserializeNBT(tag.getCompoundTag("Items"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setFloat("extension", progress);
        tag.setBoolean("opening", opening);
        tag.setInteger("redstoneMode", redstoneMode);
        tag.setBoolean("rightGoing", rightGoing);
        tag.setTag("Items", inventory.serializeNBT());
        return tag;
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        }
    }

    @Override
    public String getName() {
        return Blockss.PNEUMATIC_DOOR_BASE.getUnlocalizedName();
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }
}
