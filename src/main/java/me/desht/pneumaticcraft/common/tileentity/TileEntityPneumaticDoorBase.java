package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerPneumaticDoorBase;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.lib.TileEntityConstants.PNEUMATIC_DOOR_EXTENSION;
import static me.desht.pneumaticcraft.lib.TileEntityConstants.PNEUMATIC_DOOR_SPEED_FAST;

public class TileEntityPneumaticDoorBase extends TileEntityPneumaticBase
        implements IRedstoneControl, IMinWorkingPressure, ICamouflageableTE, INamedContainerProvider {
    private static final List<String> REDSTONE_LABELS = ImmutableList.of(
            "gui.tab.redstoneBehaviour.pneumaticDoor.button.playerNearby",
            "gui.tab.redstoneBehaviour.pneumaticDoor.button.playerNearbyAndLooking",
            "gui.tab.redstoneBehaviour.pneumaticDoor.button.woodenDoor"
    );

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
    private ItemStack camoStack = ItemStack.EMPTY;
    private BlockState camoState;
    @GuiSynced
    public int redstoneMode;
    @DescSynced
    private float speedMultiplier;

    public TileEntityPneumaticDoorBase() {
        super(ModTileEntities.PNEUMATIC_DOOR_BASE.get(), PneumaticValues.DANGER_PRESSURE_PNEUMATIC_DOOR, PneumaticValues.MAX_PRESSURE_PNEUMATIC_DOOR, PneumaticValues.VOLUME_PNEUMATIC_DOOR, 4);
    }

    @Override
    public void tick() {
        super.tick();
        oldProgress = progress;
        if (!getWorld().isRemote) {
            if (getPressure() >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR) {
                if ((getWorld().getGameTime() & 0x3f) == 0) {
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
            speedMultiplier = getSpeedMultiplierFromUpgrades();
        }
        float targetProgress = opening ? 1F : 0F;
        if (progress < targetProgress) {
            if (progress > 0.05 && progress < targetProgress - PNEUMATIC_DOOR_EXTENSION) {
                progress += PNEUMATIC_DOOR_SPEED_FAST * speedMultiplier;
            } else {
                progress += Math.min(0.02, TileEntityConstants.PNEUMATIC_DOOR_SPEED_SLOW * speedMultiplier);
            }
            if (progress > targetProgress) progress = targetProgress;
        }
        if (progress > targetProgress) {
            if (progress < 0.95 && progress > targetProgress + PNEUMATIC_DOOR_EXTENSION) {
                progress -= PNEUMATIC_DOOR_SPEED_FAST * speedMultiplier;
            } else {
                progress -= Math.min(0.02, TileEntityConstants.PNEUMATIC_DOOR_SPEED_SLOW * speedMultiplier);
            }
            if (progress < targetProgress) progress = targetProgress;
        }
        if (!getWorld().isRemote) {
            addAir((int) (-Math.abs(oldProgress - progress) * PneumaticValues.USAGE_PNEUMATIC_DOOR * (getSpeedUsageMultiplierFromUpgrades() / speedMultiplier)));
        }
        door = getDoor();
        if (door != null) {
            door.setRotationAngle(progress * 90);
            if (!getWorld().isRemote) rightGoing = door.rightGoing;
        }
    }

    private boolean shouldOpen() {
        if (door == null) return false;
        switch (redstoneMode) {
            case 0:
            case 1:
                int range = TileEntityConstants.RANGE_PNEUMATIC_DOOR_BASE + this.getUpgrades(EnumUpgrade.RANGE);
                AxisAlignedBB aabb = new AxisAlignedBB(getPos()).grow(range);
                for (PlayerEntity player : getWorld().getEntitiesWithinAABB(PlayerEntity.class, aabb)) {
                    if (PneumaticCraftUtils.getProtectingSecurityStations(getWorld(), getPos(), player, false, false) > 0) {
                        continue;
                    }
                    if (redstoneMode == 0) {
                        return true;
                    } else {
                        Vec3d eyePos = player.getEyePosition(0f);
                        Vec3d endPos = eyePos.add(player.getLookVec().normalize().scale(range * 1.4142f));
                        return door.getRenderBoundingBox().rayTrace(eyePos, endPos).isPresent();
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
        if (this.opening != wasOpening) {
            NetworkHandler.sendToAllAround(new PacketPlaySound(ModSounds.PNEUMATIC_DOOR.get(), SoundCategory.BLOCKS, getPos(), 1.0F, 1.0F, false), getWorld());
            sendDescriptionPacket();
        }
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
    public boolean canConnectPneumatic(Direction side) {
        return side != Direction.UP;
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
    public void read(CompoundNBT tag) {
        super.read(tag);
        progress = tag.getFloat("extension");
        opening = tag.getBoolean("opening");
        redstoneMode = tag.getInt("redstoneMode");
        rightGoing = tag.getBoolean("rightGoing");
        camoStack  = ICamouflageableTE.readCamoStackFromNBT(tag);
        camoState = ICamouflageableTE.getStateForStack(camoStack);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putFloat("extension", progress);
        tag.putBoolean("opening", opening);
        tag.putInt("redstoneMode", redstoneMode);
        tag.putBoolean("rightGoing", rightGoing);
        ICamouflageableTE.writeCamoStackToNBT(camoStack, tag);
        return tag;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        }
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public BlockState getCamouflage() {
        return camoState;
    }

    @Override
    public void setCamouflage(BlockState state) {
        camoState = state;
        camoStack = ICamouflageableTE.getStackForState(state);
        sendDescriptionPacket();
        markDirty();
    }

    @Override
    public void onDescUpdate() {
        camoState = ICamouflageableTE.getStateForStack(camoStack);

        super.onDescUpdate();
    }

    @Override
    public String getRedstoneTabTitle() {
        return "gui.tab.redstoneBehaviour.pneumaticDoor.openWhen";
    }


    @Override
    protected List<String> getRedstoneButtonLabels() {
        return REDSTONE_LABELS;
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerPneumaticDoorBase(i, playerInventory, getPos());
    }
}
