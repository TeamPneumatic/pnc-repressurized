package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import com.google.common.math.IntMath;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerPneumaticDoorBase;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.ReceivingRedstoneMode;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

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
                    te -> true),
            new ReceivingRedstoneMode<>("pneumaticDoor.ironDoor", new ItemStack(Items.IRON_DOOR),
                    te -> true)
    );

    public static final int INVENTORY_SIZE = 1;

    private static final int RS_MODE_NEAR = 0;
    private static final int RS_MODE_NEAR_LOOKING = 1;
    public static final int RS_MODE_WOODEN_DOOR = 2;
    public static final int RS_MODE_IRON_DOOR = 3;

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
    @GuiSynced
    private boolean passSignal;

    private int rangeSq;

    public TileEntityPneumaticDoorBase() {
        super(ModTileEntities.PNEUMATIC_DOOR_BASE.get(), PneumaticValues.DANGER_PRESSURE_PNEUMATIC_DOOR, PneumaticValues.MAX_PRESSURE_PNEUMATIC_DOOR, PneumaticValues.VOLUME_PNEUMATIC_DOOR, 4);
    }

    @Override
    public void tick() {
        super.tick();
        oldProgress = progress;
        if (!getLevel().isClientSide) {
            if (getPressure() >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR) {
                if ((getLevel().getGameTime() & 0x3f) == 0) {
                    TileEntity te = getLevel().getBlockEntity(getBlockPos().relative(getRotation(), 3));
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
        if (!getLevel().isClientSide && !PneumaticCraftUtils.epsilonEquals(oldProgress, progress)) {
            addAir((int) (-Math.abs(oldProgress - progress) * PneumaticValues.USAGE_PNEUMATIC_DOOR * (getSpeedUsageMultiplierFromUpgrades() / speedMultiplier)));
        }
        door = getDoor();
        if (door != null) {
            door.setRotationAngle(progress * 90);
            if (!getLevel().isClientSide) rightGoing = door.rightGoing;
        }
    }

    @Override
    public void onUpgradesChanged() {
        super.onUpgradesChanged();

        rangeSq = IntMath.pow(TileEntityConstants.RANGE_PNEUMATIC_DOOR_BASE + this.getUpgrades(EnumUpgrade.RANGE), 2);
    }

    private boolean shouldOpen() {
        if (door == null) return false;
        switch (rsController.getCurrentMode()) {
            case RS_MODE_NEAR:
                return hasAnyValidPlayer(player -> true);
            case RS_MODE_NEAR_LOOKING:
                return hasAnyValidPlayer(this::isPlayerLookingAtDoor);
            case RS_MODE_WOODEN_DOOR:
            case RS_MODE_IRON_DOOR:
                return rsController.getCurrentRedstonePower() > 0 || opening;
        }
        return false;
    }

    private boolean hasAnyValidPlayer(Predicate<ServerPlayerEntity> pred) {
        if (level != null && level.getServer() != null) {
            Vector3d vec = Vector3d.atCenterOf(getBlockPos().relative(getRotation()));
            return level.getServer().getPlayerList().getPlayers().stream()
                    .filter(player -> player.distanceToSqr(vec) <= rangeSq)
                    .filter(player -> !TileEntitySecurityStation.isProtectedFromPlayer(player, getBlockPos(), false))
                    .anyMatch(pred);
        }
        return false;
    }

    private boolean isPlayerLookingAtDoor(ServerPlayerEntity player) {
        Vector3d eyePos = player.getEyePosition(0f);
        // rangeSq is longer than we need, but we've already done a proximity check, so...
        Vector3d endPos = eyePos.add(player.getLookAngle().scale(rangeSq));
        return door.getRenderBoundingBox().clip(eyePos, endPos).isPresent();
    }

    public void setOpening(boolean opening) {
        boolean wasOpening = this.opening;
        this.opening = opening;
        if (this.opening != wasOpening) {
            NetworkHandler.sendToAllTracking(new PacketPlaySound(ModSounds.PNEUMATIC_DOOR.get(), SoundCategory.BLOCKS, getBlockPos(), 1.0F, 1.0F, false),this);
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
        return PneumaticCraftUtils.getTileEntityAt(getLevel(), getBlockPos().relative(getRotation()).below(), TileEntityPneumaticDoor.class).map(teDoor -> {
            if (getRotation().getClockWise() == teDoor.getRotation() && !teDoor.rightGoing) {
                return teDoor;
            } else if (getRotation().getCounterClockWise() == teDoor.getRotation() && teDoor.rightGoing) {
                return teDoor;
            } else {
                return null;
            }
        }).orElse(null);
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        progress = tag.getFloat("extension");
        opening = tag.getBoolean("opening");
        rightGoing = tag.getBoolean("rightGoing");
        passSignal = tag.getBoolean("passSignal");
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        tag.putFloat("extension", progress);
        tag.putBoolean("opening", opening);
        tag.putBoolean("rightGoing", rightGoing);
        tag.putBoolean("passSignal", passSignal);
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
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        if (rsController.parseRedstoneMode(tag)) return;

        if (tag.equals("pass_signal")) {
            passSignal = !passSignal;
            updateNeighbours();
            setChanged();
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

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerPneumaticDoorBase(i, playerInventory, getBlockPos());
    }

    public boolean shouldPassSignalToDoor() {
        return passSignal;
    }
}
