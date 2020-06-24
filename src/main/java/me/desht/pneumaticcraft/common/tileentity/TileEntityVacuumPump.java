package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.capabilities.MachineAirHandler;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerVacuumPump;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.NBTKeys;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class TileEntityVacuumPump extends TileEntityPneumaticBase implements IRedstoneControlled, IManoMeasurable, INamedContainerProvider {
    @GuiSynced
    private final MachineAirHandler vacuumHandler;
    private final LazyOptional<IAirHandlerMachine> vacuumCap;
    public int rotation;
    public int oldRotation;
    private int turnTimer = -1;
    @DescSynced
    public boolean turning = false;
    private int rotationSpeed;
    @GuiSynced
    public int redstoneMode;

    public TileEntityVacuumPump() {
        super(ModTileEntities.VACUUM_PUMP.get(), PneumaticValues.DANGER_PRESSURE_VACUUM_PUMP, PneumaticValues.MAX_PRESSURE_VACUUM_PUMP, PneumaticValues.VOLUME_VACUUM_PUMP, 4);

        this.vacuumHandler  = new MachineAirHandler(PneumaticValues.DANGER_PRESSURE_TIER_ONE, PneumaticValues.MAX_PRESSURE_TIER_ONE, PneumaticValues.VOLUME_VACUUM_PUMP);
        this.vacuumCap = LazyOptional.of(() -> vacuumHandler);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY) {
            if (side == getVacuumSide()) {
                return PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY.orEmpty(cap, vacuumCap);
            } else if (side != getInputSide() && side != null) {
                return LazyOptional.empty();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public IItemHandler getPrimaryInventory() {
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
        super.tick();

        if (!getWorld().isRemote) {
            if (turnTimer >= 0) turnTimer--;

            if (airHandler.getPressure() > PneumaticValues.MIN_PRESSURE_VACUUM_PUMP && vacuumHandler.getPressure() > -0.99F
                    && redstoneAllows()) {
                if (turnTimer == -1) {
                    turning = true;
                }
                airHandler.addAir((int) (-PneumaticValues.USAGE_VACUUM_PUMP * getSpeedUsageMultiplierFromUpgrades()));
                // negative because it's creating a vacuum.
                vacuumHandler.addAir((int) (-PneumaticValues.PRODUCTION_VACUUM_PUMP * getSpeedMultiplierFromUpgrades()));
                turnTimer = 40;
            }
            if (turnTimer == 0) {
                turning = false;
            }
            airHandler.setSideLeaking(airHandler.getConnectedAirHandlers(this).isEmpty() ? getInputSide() : null);
            vacuumHandler.setSideLeaking(vacuumHandler.getConnectedAirHandlers(this).isEmpty() ? getVacuumSide() : null);
        } else {
            oldRotation = rotation;
            if (turning) {
                rotationSpeed = Math.min(rotationSpeed + 1, 20);
            } else {
                rotationSpeed = Math.max(rotationSpeed - 1, 0);
            }
            rotation += rotationSpeed;
        }
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
        tag.putInt(NBTKeys.NBT_REDSTONE_MODE, redstoneMode);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        vacuumHandler.deserializeNBT(tag.getCompound("vacuum"));
        turning = tag.getBoolean("turning");
        redstoneMode = tag.getInt(NBTKeys.NBT_REDSTONE_MODE);
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        }
    }

    @Override
    public void printManometerMessage(PlayerEntity player, List<ITextComponent> curInfo) {
        String input = PneumaticCraftUtils.roundNumberTo(airHandler.getPressure(), 1);
        String vac = PneumaticCraftUtils.roundNumberTo(vacuumHandler.getPressure(), 1);
        curInfo.add(xlate("pneumaticcraft.message.vacuum_pump.manometer", input, vac).applyTextStyle(TextFormatting.GREEN));
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
