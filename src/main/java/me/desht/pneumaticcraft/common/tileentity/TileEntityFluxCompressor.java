package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.block.BlockPneumaticDynamo;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ContainerEnergy;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityFluxCompressor extends TileEntityPneumaticBase
        implements IRedstoneControl<TileEntityFluxCompressor>, INamedContainerProvider, IHeatExchangingTE {
    private static final int BASE_FE_PRODUCTION = 40;
    private final PneumaticEnergyStorage energy = new PneumaticEnergyStorage(100000);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    @GuiSynced
    private int rfPerTick;
    @GuiSynced
    private float airPerTick;
    private float airBuffer;
    private boolean isEnabled;
    @GuiSynced
    private final RedstoneController<TileEntityFluxCompressor> rsController = new RedstoneController<>(this);
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

        if (!level.isClientSide) {
            if (level.getGameTime() % 5 == 0) {
                airPerTick = (BASE_FE_PRODUCTION * this.getSpeedUsageMultiplierFromUpgrades()
                        * (getHeatEfficiency() / 100f)
                        * (ConfigHelper.common().machines.fluxCompressorEfficiency.get() / 100f));
                rfPerTick = (int) (BASE_FE_PRODUCTION * this.getSpeedUsageMultiplierFromUpgrades());
            }
            boolean newEnabled = false;
            if (rsController.shouldRun() && energy.getEnergyStored() >= rfPerTick) {
                airBuffer += airPerTick;
                if (airBuffer >= 1f) {
                    int toAdd = (int) airBuffer;
                    this.addAir(toAdd);
                    airBuffer -= toAdd;
                    heatExchanger.addHeat(toAdd / 20d);
                }
                energy.extractEnergy(rfPerTick, false);
                newEnabled = true;
            }
            if ((level.getGameTime() & 0x7) == 0 && newEnabled != isEnabled) {
                isEnabled = newEnabled;
                BlockState state = level.getBlockState(worldPosition);
                level.setBlockAndUpdate(worldPosition, state.setValue(BlockPneumaticDynamo.ACTIVE, isEnabled));
            }
            airHandler.setSideLeaking(hasNoConnectedAirHandlers() ? getRotation().getOpposite() : null);
        }
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side == getRotation().getOpposite();
    }

    @Override
    public RedstoneController<TileEntityFluxCompressor> getRedstoneController() {
        return rsController;
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
    public CompoundNBT save(CompoundNBT tag){
        super.save(tag);
        energy.writeToNBT(tag);
        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        energy.readFromNBT(tag);
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        rsController.parseRedstoneMode(tag);
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

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerEnergy<>(ModContainers.FLUX_COMPRESSOR.get(), i, playerInventory, getBlockPos());
    }

    @Override
    public IHeatExchangerLogic getHeatExchanger(Direction dir) {
        return heatExchanger;
    }
}
