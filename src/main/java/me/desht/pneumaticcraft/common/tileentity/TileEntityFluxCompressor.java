package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;

public class TileEntityFluxCompressor extends TileEntityPneumaticBase implements IRedstoneControlled, IHeatExchanger {
    private final PneumaticEnergyStorage energy = new PneumaticEnergyStorage(100000);
    @GuiSynced
    private int rfPerTick;
    @GuiSynced
    private int airPerTick;
    @GuiSynced
    private int redstoneMode;
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();

    public TileEntityFluxCompressor() {
        this(PneumaticValues.DANGER_PRESSURE_FLUX_COMPRESSOR,
                PneumaticValues.MAX_PRESSURE_FLUX_COMPRESSOR,
                PneumaticValues.VOLUME_FLUX_COMPRESSOR, 4);
    }

    public TileEntityFluxCompressor(float dangerPressure, float criticalPressure, int volume, int upgradeSlots) {
        super(dangerPressure, criticalPressure, volume, 4);
        addApplicableUpgrade(IItemRegistry.EnumUpgrade.SPEED);
        heatExchanger.setThermalCapacity(100);
    }

    public int getEfficiency(){
        return TileEntityAdvancedAirCompressor.getEfficiency(heatExchanger.getTemperature());
    }

    @Override
    public void update() {
        super.update();

        if (!world.isRemote) {
            if (world.getTotalWorldTime() % 5 == 0) {
                airPerTick = (int) (40 * this.getSpeedUsageMultiplierFromUpgrades() * getEfficiency() * ConfigHandler.machineProperties.fluxCompressorEfficiency / 100 / 100);
                rfPerTick = (int) (40 * this.getSpeedUsageMultiplierFromUpgrades());
            }
            if (redstoneAllows() && energy.getEnergyStored() >= rfPerTick) {
                this.addAir(airPerTick);
                energy.extractEnergy(rfPerTick, false);
                heatExchanger.addHeat(rfPerTick / 100D);
            }
        }

        if (!getWorld().isRemote) {
            List<Pair<EnumFacing, IAirHandler>> teList = getAirHandler(null).getConnectedPneumatics();
            if (teList.size() == 0) getAirHandler(null).airLeak(getRotation().getOpposite());
        }
    }

    @Override
    public String getName() {
        return Blockss.FLUX_COMPRESSOR.getTranslationKey();
    }

    @Override
    public boolean isConnectedTo(EnumFacing side) {
        return side == getRotation().getOpposite();
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        // back face is where pneumatics connect
        return (capability == CapabilityEnergy.ENERGY && facing != getRotation().getOpposite())
                || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != getRotation().getOpposite()) {
            return CapabilityEnergy.ENERGY.cast(energy);
        } else {
            return super.getCapability(capability, facing);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        energy.writeToNBT(tag);
        tag.setByte("redstoneMode", (byte)redstoneMode);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        energy.readFromNBT(tag);
        redstoneMode = tag.getByte("redstoneMode");
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if (buttonID == 0 && ++redstoneMode > 2) redstoneMode = 0;
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(EnumFacing side) {
        return heatExchanger;
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
}
