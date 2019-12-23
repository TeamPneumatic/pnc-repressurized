package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.inventory.ContainerVacuumPump;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class TileEntityVacuumPump extends TileEntityPneumaticBase implements IRedstoneControlled, IManoMeasurable, INamedContainerProvider {
    @GuiSynced
    private final IAirHandlerMachine vacuumHandler;
    public int rotation;
    public int oldRotation;
    private int turnTimer = -1;
    @DescSynced
    public boolean turning = false;
    private int rotationSpeed;
    @GuiSynced
    public int redstoneMode;

    public TileEntityVacuumPump() {
        super(ModTileEntityTypes.VACUUM_PUMP, PneumaticValues.DANGER_PRESSURE_VACUUM_PUMP, PneumaticValues.MAX_PRESSURE_VACUUM_PUMP, PneumaticValues.VOLUME_VACUUM_PUMP, 4);

        this.vacuumHandler  = PneumaticRegistry.getInstance().getAirHandlerSupplier().createTierOneAirHandler(PneumaticValues.VOLUME_VACUUM_PUMP);
        addApplicableUpgrade(EnumUpgrade.SPEED);
    }

    @Override
    public IAirHandlerMachine getAirHandler(Direction side) {
        if (side == null || side == getInputSide()) {
            return super.getAirHandler(side);
        } else if (side == getVacuumSide()) {
            return vacuumHandler;
        } else {
            return null;
        }
    }

    @Override
    public void validate() {
        super.validate();
        vacuumHandler.validate(this);
    }

    @Override
    public void onNeighborTileUpdate() {
        super.onNeighborTileUpdate();
        vacuumHandler.onNeighborChange();
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
    }

    public Direction getInputSide() {
        return getVacuumSide().getOpposite();
    }

    public Direction getVacuumSide() {
        return getRotation();
    }

    @Override
    public void tick() {
        if (!getWorld().isRemote && turnTimer >= 0) {
            turnTimer--;
        }
        if (!getWorld().isRemote
                && getAirHandler(getInputSide()).getPressure() > PneumaticValues.MIN_PRESSURE_VACUUM_PUMP
                && getAirHandler(getVacuumSide()).getPressure() > -1F
                && redstoneAllows()) {
            if (!getWorld().isRemote && turnTimer == -1) {
                turning = true;
            }
            getAirHandler(getVacuumSide()).addAir((int) (-PneumaticValues.PRODUCTION_VACUUM_PUMP * getSpeedMultiplierFromUpgrades())); // negative because it's pulling a vacuum.
            getAirHandler(getInputSide()).addAir((int) (-PneumaticValues.USAGE_VACUUM_PUMP * getSpeedUsageMultiplierFromUpgrades()));
            turnTimer = 40;
        }
        if (turnTimer == 0) {
            turning = false;
        }
        oldRotation = rotation;
        if (getWorld().isRemote) {
            if (turning) {
                rotationSpeed = Math.min(rotationSpeed + 1, 20);
            } else {
                rotationSpeed = Math.max(rotationSpeed - 1, 0);
            }
            rotation += rotationSpeed;
        }

        super.tick();
        vacuumHandler.tick();

        IAirHandlerMachine inputHandler = getAirHandler(getInputSide());
        List<Pair<Direction, IAirHandlerMachine>> teList = inputHandler.getConnectedPneumatics();
        if (teList.size() == 0) inputHandler.airLeak(getInputSide());
        teList = vacuumHandler.getConnectedPneumatics();
        if (teList.size() == 0) vacuumHandler.airLeak(getVacuumSide());

    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.put("vacuum", vacuumHandler.serializeNBT());
        tag.putBoolean("turning", turning);
        tag.putInt("redstoneMode", redstoneMode);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        vacuumHandler.deserializeNBT(tag.getCompound("vacuum"));
        turning = tag.getBoolean("turning");
        redstoneMode = tag.getInt("redstoneMode");
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        }
    }

    @Override
    public void printManometerMessage(PlayerEntity player, List<ITextComponent> curInfo) {
        String input = PneumaticCraftUtils.roundNumberTo(getAirHandler(getInputSide()).getPressure(), 1);
        String vac = PneumaticCraftUtils.roundNumberTo(getAirHandler(getVacuumSide()).getPressure(), 1);
        curInfo.add(xlate("message.vacuum_pump.manometer", input, vac).applyTextStyle(TextFormatting.GREEN));
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerVacuumPump(i, playerInventory, getPos());
    }
}
