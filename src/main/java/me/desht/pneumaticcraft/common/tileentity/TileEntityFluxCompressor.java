package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ContainerEnergy;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityFluxCompressor extends TileEntityPneumaticBase implements IRedstoneControlled, INamedContainerProvider {
    private final PneumaticEnergyStorage energy = new PneumaticEnergyStorage(100000);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    @GuiSynced
    private int rfPerTick;
    @GuiSynced
    private int airPerTick;
    @GuiSynced
    private int redstoneMode;
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    private LazyOptional<IHeatExchangerLogic> heatCap = LazyOptional.of(() -> heatExchanger);

    public TileEntityFluxCompressor() {
        super(ModTileEntities.FLUX_COMPRESSOR.get(), PneumaticValues.DANGER_PRESSURE_FLUX_COMPRESSOR,
                PneumaticValues.MAX_PRESSURE_FLUX_COMPRESSOR,
                PneumaticValues.VOLUME_FLUX_COMPRESSOR, 4);

        heatExchanger.setThermalCapacity(100);
    }

    public int getEfficiency(){
        return HeatUtil.getEfficiency(heatExchanger.getTemperatureAsInt());
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isRemote) {
            if (world.getGameTime() % 5 == 0) {
                airPerTick = (int) (40 * this.getSpeedUsageMultiplierFromUpgrades() * getEfficiency() * PNCConfig.Common.Machines.fluxCompressorEfficiency / 100 / 100);
                rfPerTick = (int) (40 * this.getSpeedUsageMultiplierFromUpgrades());
            }
            if (redstoneAllows() && energy.getEnergyStored() >= rfPerTick) {
                this.addAir(airPerTick);
                energy.extractEnergy(rfPerTick, false);
                heatExchanger.addHeat(rfPerTick / 100D);
            }
            airHandler.setSideLeaking(hasNoConnectedAirHandlers() ? getRotation().getOpposite() : null);
        }
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side == getRotation().getOpposite();
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY && side != getRotation().getOpposite()) {
            return energyCap.cast();
        } else {
            return super.getCapability(cap, side);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag){
        super.write(tag);
        energy.writeToNBT(tag);
        tag.putByte("redstoneMode", (byte)redstoneMode);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag){
        super.read(tag);
        energy.readFromNBT(tag);
        redstoneMode = tag.getByte("redstoneMode");
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG) && ++redstoneMode == 2) {
            redstoneMode = 0;
        }
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    @Override
    public LazyOptional<IHeatExchangerLogic> getHeatCap(Direction side) {
        return heatCap;
    }

    public int getInfoEnergyPerTick() {
        return rfPerTick;
    }

    public int getInfoEnergyStored() {
        return energy.getEnergyStored();
    }

    public int getAirRate() {
        return airPerTick;
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerEnergy(ModContainers.FLUX_COMPRESSOR.get(), i, playerInventory, getPos());
    }
}
