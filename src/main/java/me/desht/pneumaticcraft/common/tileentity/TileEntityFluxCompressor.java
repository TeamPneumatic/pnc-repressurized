package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.config.PNCConfig.Common.Machines;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ContainerEnergy;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.NBTKeys;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
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
    private static final int BASE_FE_PRODUCTION = 40;
    private final PneumaticEnergyStorage energy = new PneumaticEnergyStorage(100000);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    @GuiSynced
    private int rfPerTick;
    @GuiSynced
    private float airPerTick;
    private float airBuffer;
    @GuiSynced
    private int redstoneMode;
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    private final LazyOptional<IHeatExchangerLogic> heatCap = LazyOptional.of(() -> heatExchanger);

    public TileEntityFluxCompressor() {
        super(ModTileEntities.FLUX_COMPRESSOR.get(), PneumaticValues.DANGER_PRESSURE_FLUX_COMPRESSOR,
                PneumaticValues.MAX_PRESSURE_FLUX_COMPRESSOR,
                PneumaticValues.VOLUME_FLUX_COMPRESSOR, 4);

        heatExchanger.setThermalCapacity(100);
    }

    public int getHeatEfficiency(){
        return HeatUtil.getEfficiency(heatExchanger.getTemperatureAsInt());
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isRemote) {
            if (world.getGameTime() % 5 == 0) {
                airPerTick = (BASE_FE_PRODUCTION * this.getSpeedUsageMultiplierFromUpgrades()
                        * (getHeatEfficiency() / 100f)
                        * (Machines.fluxCompressorEfficiency / 100f));
                rfPerTick = (int) (BASE_FE_PRODUCTION * this.getSpeedUsageMultiplierFromUpgrades());
            }
            if (redstoneAllows() && energy.getEnergyStored() >= rfPerTick) {
                airBuffer += airPerTick;
                if (airBuffer >= 1f) {
                    int toAdd = (int) airBuffer;
                    this.addAir(toAdd);
                    airBuffer -= toAdd;
                    heatExchanger.addHeat(toAdd / 20d);
                }
                energy.extractEnergy(rfPerTick, false);
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
        tag.putByte(NBTKeys.NBT_REDSTONE_MODE, (byte)redstoneMode);
        return tag;
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        energy.readFromNBT(tag);
        redstoneMode = tag.getByte(NBTKeys.NBT_REDSTONE_MODE);
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG) && ++redstoneMode == 3) {
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

    public float getAirRate() {
        return airPerTick;
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerEnergy<>(ModContainers.FLUX_COMPRESSOR.get(), i, playerInventory, getPos());
    }
}
