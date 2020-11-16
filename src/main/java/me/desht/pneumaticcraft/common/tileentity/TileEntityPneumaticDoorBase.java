package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerPneumaticDoorBase;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.ReceivingRedstoneMode;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static me.desht.pneumaticcraft.lib.TileEntityConstants.PNEUMATIC_DOOR_EXTENSION;
import static me.desht.pneumaticcraft.lib.TileEntityConstants.PNEUMATIC_DOOR_SPEED_FAST;

public class TileEntityPneumaticDoorBase extends TileEntityPneumaticBase implements
        IRedstoneControl<TileEntityPneumaticDoorBase>, IMinWorkingPressure, ICamouflageableTE, INamedContainerProvider {
    private static final List<RedstoneMode<TileEntityPneumaticDoorBase>> REDSTONE_MODES = ImmutableList.of(
            new ReceivingRedstoneMode<>("pneumaticDoor.playerNearby", new ItemStack(Items.OBSERVER),
                    te -> true),
            new ReceivingRedstoneMode<>("pneumaticDoor.playerNearbyAndLooking", new ItemStack(Items.ENDER_EYE),
                    te -> true),
            new ReceivingRedstoneMode<>("pneumaticDoor.woodenDoor", new ItemStack(Items.OAK_DOOR),
                    te -> true)
    );

    public static final int INVENTORY_SIZE = 1;
    private static final int RS_MODE_NEAR = 0;
    private static final int RS_MODE_NEAR_LOOKING = 1;
    public static final int RS_MODE_WOODEN_DOOR = 2;

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
    private BlockState camoState;
    @GuiSynced
    public final RedstoneController<TileEntityPneumaticDoorBase> rsController = new RedstoneController<>(this, REDSTONE_MODES);
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
        switch (rsController.getCurrentMode()) {
            case RS_MODE_NEAR:
            case RS_MODE_NEAR_LOOKING:
                int range = TileEntityConstants.RANGE_PNEUMATIC_DOOR_BASE + this.getUpgrades(EnumUpgrade.RANGE);
                AxisAlignedBB aabb = new AxisAlignedBB(getPos()).grow(range);
                for (PlayerEntity player : getWorld().getEntitiesWithinAABB(PlayerEntity.class, aabb)) {
                    if (TileEntitySecurityStation.getProtectingSecurityStations(player, getPos(), false, false) > 0) {
                        continue;
                    }
                    if (rsController.getCurrentMode() == RS_MODE_NEAR) {
                        return true;
                    } else {
                        Vector3d eyePos = player.getEyePosition(0f);
                        Vector3d endPos = eyePos.add(player.getLookVec().normalize().scale(range * 1.4142f));
                        return door.getRenderBoundingBox().rayTrace(eyePos, endPos).isPresent();
                    }
                }
                return false;
            case RS_MODE_WOODEN_DOOR:
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
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        progress = tag.getFloat("extension");
        opening = tag.getBoolean("opening");
        rightGoing = tag.getBoolean("rightGoing");
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putFloat("extension", progress);
        tag.putBoolean("opening", opening);
        tag.putBoolean("rightGoing", rightGoing);
        return tag;
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);

        ICamouflageableTE.writeCamo(tag, camoState);
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);

        camoState = ICamouflageableTE.readCamo(tag);
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        rsController.parseRedstoneMode(tag);
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
    public RedstoneController<TileEntityPneumaticDoorBase> getRedstoneController() {
        return rsController;
    }

    @Override
    public BlockState getCamouflage() {
        return camoState;
    }

    @Override
    public void setCamouflage(BlockState state) {
        camoState = state;
        ICamouflageableTE.syncToClient(this);
    }

    @Override
    public IFormattableTextComponent getRedstoneTabTitle() {
        return xlate("pneumaticcraft.gui.tab.redstoneBehaviour.pneumaticDoor.openWhen");
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
