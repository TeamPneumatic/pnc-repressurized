package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.heat.SyncedTemperature;
import me.desht.pneumaticcraft.common.inventory.ContainerThermalCompressor;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class TileEntityThermalCompressor extends TileEntityPneumaticBase
        implements IHeatTinted, IRedstoneControl<TileEntityThermalCompressor>, INamedContainerProvider {
    private static final double AIR_GEN_MULTIPLIER = 0.05;  // mL per degree of difference

    // track running air generation amounts; won't be added to the air handler until > 1.0
    private final double[] airGenerated = new double[2];

    @GuiSynced
    private final IHeatExchangerLogic[] heatExchangers = new IHeatExchangerLogic[4];
    private final List<LazyOptional<IHeatExchangerLogic>> heatCaps = new ArrayList<>(4);
    private final IHeatExchangerLogic connector1;
    private final IHeatExchangerLogic connector2;

    private final IHeatExchangerLogic dummyExchanger;  // never does anything; gets returned from the "null" face

    @DescSynced
    private final SyncedTemperature[] syncedTemperatures = new SyncedTemperature[4];  // S-W-N-E

    @GuiSynced
    private final RedstoneController<TileEntityThermalCompressor> rsController = new RedstoneController<>(this);

    public TileEntityThermalCompressor() {
        super(ModTileEntities.THERMAL_COMPRESSOR.get(), PneumaticValues.DANGER_PRESSURE_THERMAL_COMPRESSOR, PneumaticValues.MAX_PRESSURE_THERMAL_COMPRESSOR, PneumaticValues.VOLUME_THERMAL_COMPRESSOR, 4);

        IntStream.range(0, heatExchangers.length).forEach(i -> {
            heatExchangers[i] = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
            heatExchangers[i].setThermalCapacity(2);
            heatCaps.add(LazyOptional.of(() -> heatExchangers[i]));
        });

        for (int i = 0; i < syncedTemperatures.length; i++) {
            syncedTemperatures[i] = new SyncedTemperature(heatExchangers[i]);
        }

        connector1 = makeConnector(Direction.NORTH);
        connector2 = makeConnector(Direction.EAST);

        dummyExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    }

    private IHeatExchangerLogic makeConnector(Direction side) {
        IHeatExchangerLogic connector = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
        connector.setThermalResistance(200);
        connector.addConnectedExchanger(getHeatExchanger(side));
        connector.addConnectedExchanger(getHeatExchanger(side.getOpposite()));
        return connector;
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isRemote) {
            for (IHeatExchangerLogic heatExchanger : heatExchangers) {
                heatExchanger.tick();
            }

            if (rsController.shouldRun()) {
                connector1.tick();
                connector2.tick();

                equaliseHeat(Direction.NORTH, generatePressure(Direction.NORTH));
                equaliseHeat(Direction.EAST, generatePressure(Direction.EAST));
            }

            for (int i = 0; i < heatExchangers.length; i++) {
                syncedTemperatures[i].tick();
            }
        }
    }

    public IHeatExchangerLogic getHeatExchanger(Direction side) {
        return heatExchangers[side.getHorizontalIndex()];
    }

    private void equaliseHeat(Direction side, double airProduced) {
        // vortex tube adds 1 heat to its hot side and -1 heat to its cold side for every 10 air used
        // thermal compressor must be no more efficient than that or there will be positive feedback loops = infinite air exploits
        double heatToAdd = airProduced / 5.0;

        IHeatExchangerLogic h1 = getHeatExchanger(side);
        IHeatExchangerLogic h2 = getHeatExchanger(side.getOpposite());

        if (h1.getTemperature() > h2.getTemperature()) {
            h1.addHeat(-heatToAdd);
            h2.addHeat(heatToAdd);
        } else {
            h1.addHeat(heatToAdd);
            h2.addHeat(-heatToAdd);
        }
    }

    public double airProduced(Direction side) {
        if (world.isRemote) {
            // clientside: just for GUI display purposes
            double diff = Math.abs(getHeatExchanger(side).getTemperatureAsInt() - getHeatExchanger(side.getOpposite()).getTemperatureAsInt());
            return diff < 10 ? 0 : diff * AIR_GEN_MULTIPLIER;
        } else {
            // server side: the actual calculation
            double diff = Math.abs(getHeatExchanger(side).getTemperature() - getHeatExchanger(side.getOpposite()).getTemperature());
            return diff < 10 ? 0 : diff * AIR_GEN_MULTIPLIER;
        }
    }

    private double generatePressure(Direction side) {
        double airProduced = airProduced(side);
        int sideIdx = side.getAxis() == Direction.Axis.Z ? 1 : 0;
        airGenerated[sideIdx] += airProduced;
        if (airGenerated[sideIdx] > 1.0) {
            int toAdd = (int) airGenerated[sideIdx];
            addAir(toAdd);
            airGenerated[sideIdx] -= toAdd;
        }
        return airProduced;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side.getAxis() == Direction.Axis.Y;
    }

    @Override
    public TintColor getColorForTintIndex(int tintIndex) {
        return HeatUtil.getColourForTemperature(syncedTemperatures[tintIndex].getSyncedTemp());
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        for (int i = 0; i < 4; i++) {
            tag.put("side" + i, heatExchangers[i].serializeNBT());
        }
        tag.put("connector1", connector1.serializeNBT());
        tag.put("connector2", connector2.serializeNBT());
        return tag;
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        for (int i = 0; i < 4; i++) {
            heatExchangers[i].deserializeNBT(tag.getCompound("side" + i));
        }
        connector1.deserializeNBT(tag.getCompound("connector1"));
        connector2.deserializeNBT(tag.getCompound("connector2"));
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        rsController.parseRedstoneMode(tag);
    }

    @Override
    public RedstoneController<TileEntityThermalCompressor> getRedstoneController() {
        return rsController;
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerThermalCompressor(i, playerInventory, getPos());
    }

    @Override
    public LazyOptional<IHeatExchangerLogic> getHeatCap(Direction side) {
        if (side == null) {
            return LazyOptional.of(() -> dummyExchanger);
        } else {
            return side.getAxis() == Direction.Axis.Y ? LazyOptional.empty() : heatCaps.get(side.getHorizontalIndex());
        }
    }
}
