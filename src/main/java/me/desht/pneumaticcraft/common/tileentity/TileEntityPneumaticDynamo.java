package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.config.Config;
import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ContainerEnergy;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityPneumaticDynamo extends TileEntityPneumaticBase implements IRedstoneControlled, IHeatExchanger, IMinWorkingPressure, INamedContainerProvider {

    private final PneumaticEnergyStorage energy = new PneumaticEnergyStorage(100000);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

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
        super(ModTileEntityTypes.PNEUMATIC_DYNAMO, PneumaticValues.DANGER_PRESSURE_PNEUMATIC_DYNAMO, PneumaticValues.MAX_PRESSURE_PNEUMATIC_DYNAMO, PneumaticValues.VOLUME_PNEUMATIC_DYNAMO, 4);
    }

    public int getEfficiency() {
        return HeatUtil.getEfficiency(heatExchanger.getTemperatureAsInt());
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isRemote) {
            if (world.getGameTime() % 20 == 0) {
                int efficiency = Math.max(1, Config.Common.Machines.pneumaticDynamoEfficiency);
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
            if (world.getGameTime() % 20 == 0 && newEnabled != isEnabled) {
                isEnabled = newEnabled;
                sendDescriptionPacket();
            }

            TileEntity receiver = getTileCache()[getRotation().ordinal()].getTileEntity();
            if (receiver != null) {
                receiver.getCapability(CapabilityEnergy.ENERGY, getRotation().getOpposite()).ifPresent(neighborStorage -> {
                    int extracted = energy.extractEnergy(rfPerTick * 2, true);
                    int energyPushed = neighborStorage.receiveEnergy(extracted, true);
                    if (energyPushed > 0) {
                        neighborStorage.receiveEnergy(energy.extractEnergy(energyPushed, false), false);
                    }
                });
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
    public void handleGUIButtonPress(String tag, PlayerEntity player){
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG) && ++redstoneMode > 2) {
            redstoneMode = 0;
        }
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
    }

    @Override
    public boolean canConnectTo(Direction side) {
        return side == getRotation().getOpposite();
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_PNEUMATIC_DYNAMO;
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(Direction side) {
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        return capability == CapabilityEnergy.ENERGY && (facing == getRotation() || facing == null) ?
                energyCap.cast() :
                super.getCapability(capability, facing);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        energy.writeToNBT(tag);
        tag.putByte("redstoneMode", (byte)redstoneMode);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        energy.readFromNBT(tag);
        redstoneMode = tag.getByte("redstoneMode");
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerEnergy(ModContainerTypes.PNEUMATIC_DYNAMO, i, playerInventory, getPos());
    }
}
