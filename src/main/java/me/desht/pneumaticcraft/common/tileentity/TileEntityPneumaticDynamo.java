package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class TileEntityPneumaticDynamo extends TileEntityPneumaticBase implements IRedstoneControlled, IHeatExchanger, IMinWorkingPressure {

    private final PneumaticEnergyStorage energy = new PneumaticEnergyStorage(100000);

    @GuiSynced
    private int rfPerTick;
    @GuiSynced
    private int airPerTick;
    @DescSynced
    public boolean isEnabled;
    @GuiSynced
    private int redstoneMode;
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();

    public TileEntityPneumaticDynamo() {
        this(PneumaticValues.DANGER_PRESSURE_PNEUMATIC_DYNAMO, PneumaticValues.MAX_PRESSURE_PNEUMATIC_DYNAMO, PneumaticValues.VOLUME_PNEUMATIC_DYNAMO, 4);
    }

    public TileEntityPneumaticDynamo(float dangerPressure, float criticalPressure, int volume, int upgradeSlots) {
        super(dangerPressure, criticalPressure, volume, upgradeSlots);
        addApplicableUpgrade(IItemRegistry.EnumUpgrade.SPEED);
        heatExchanger.setThermalCapacity(100);
    }

    public int getEfficiency() {
        return HeatUtil.getEfficiency(heatExchanger.getTemperatureAsInt());
    }

    @Override
    public void update() {
        super.update();

        if (!world.isRemote) {
            if (world.getTotalWorldTime() % 20 == 0) {
                int efficiency = ConfigHandler.machineProperties.pneumaticDynamoEfficiency;
                if (efficiency < 1) efficiency = 1;
                airPerTick = (int) (40 * this.getSpeedUsageMultiplierFromUpgrades() * 100 / efficiency);
                rfPerTick = (int) (40 * this.getSpeedUsageMultiplierFromUpgrades() * getEfficiency() / 100);
            }

            boolean newEnabled;
            if (redstoneAllows() && getPressure() > PneumaticValues.MIN_PRESSURE_PNEUMATIC_DYNAMO && energy.getMaxEnergyStored() - energy.getEnergyStored() >= rfPerTick) {
                this.addAir(-airPerTick);
                heatExchanger.addHeat(airPerTick / 100D);
                energy.receiveEnergy(rfPerTick, false);
                newEnabled = true;
            } else {
                newEnabled = false;
            }
            if (world.getTotalWorldTime() % 20 == 0 && newEnabled != isEnabled) {
                isEnabled = newEnabled;
                sendDescriptionPacket();
            }

            TileEntity receiver = getTileCache()[getRotation().ordinal()].getTileEntity();
            if (receiver != null && receiver.hasCapability(CapabilityEnergy.ENERGY, getRotation().getOpposite())) {
                IEnergyStorage neighborStorage = receiver.getCapability(CapabilityEnergy.ENERGY, getRotation().getOpposite());
                int extracted = energy.extractEnergy(rfPerTick * 2, true);
                int energyPushed = neighborStorage.receiveEnergy(extracted, true);
                if (energyPushed > 0) {
                    neighborStorage.receiveEnergy(energy.extractEnergy(energyPushed, false), false);
                }
            }
        }
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public int getRedstoneMode(){
        return redstoneMode;
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if(buttonID == 0 && ++redstoneMode > 2) redstoneMode = 0;
    }

    @Override
    public String getName() {
        return Blockss.PNEUMATIC_DYNAMO.getTranslationKey();
    }

    @Override
    public boolean isConnectedTo(EnumFacing side) {
        return side == getRotation().getOpposite();
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_PNEUMATIC_DYNAMO;
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(EnumFacing side) {
        return heatExchanger;
    }

    public int getRFRate(){
        return rfPerTick;
    }

    public int getAirRate(){
        return airPerTick;
    }

    public int getInfoEnergyStored() {
        return energy.getEnergyStored();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY && (facing == getRotation() || facing == null)
                || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY && (facing == getRotation() || facing == null) ?
                CapabilityEnergy.ENERGY.cast(energy) :
                super.getCapability(capability, facing);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        energy.writeToNBT(tag);
        tag.setByte("redstoneMode", (byte)redstoneMode);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        energy.readFromNBT(tag);
        redstoneMode = tag.getByte("redstoneMode");
    }
}
