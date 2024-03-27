/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.heat.SyncedTemperature;
import me.desht.pneumaticcraft.common.inventory.ThermalCompressorMenu;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class ThermalCompressorBlockEntity extends AbstractAirHandlingBlockEntity
        implements IHeatTinted, IRedstoneControl<ThermalCompressorBlockEntity>, MenuProvider, IHeatExchangingTE {
    private static final double AIR_GEN_MULTIPLIER = 0.05;  // mL per degree of difference

    // track running air generation amounts; won't be added to the air handler until > 1.0
    private final double[] airGenerated = new double[2];

    @GuiSynced
    private final IHeatExchangerLogic[] heatExchangers = new IHeatExchangerLogic[4];
    private final IHeatExchangerLogic connector1;
    private final IHeatExchangerLogic connector2;

    private final IHeatExchangerLogic dummyExchanger;  // never does anything; gets returned from the "null" face

    @DescSynced
    private final SyncedTemperature[] syncedTemperatures = new SyncedTemperature[4];  // S-W-N-E

    @GuiSynced
    private final RedstoneController<ThermalCompressorBlockEntity> rsController = new RedstoneController<>(this);

    public ThermalCompressorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.THERMAL_COMPRESSOR.get(), pos, state, PressureTier.TIER_ONE_HALF, PneumaticValues.VOLUME_THERMAL_COMPRESSOR, 4);

        IntStream.range(0, heatExchangers.length).forEach(i -> {
            heatExchangers[i] = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
            heatExchangers[i].setThermalCapacity(2);
        });

        for (int i = 0; i < syncedTemperatures.length; i++) {
            syncedTemperatures[i] = new SyncedTemperature(heatExchangers[i]);
        }

        connector1 = makeConnector(Direction.NORTH);
        connector2 = makeConnector(Direction.EAST);

        dummyExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    private IHeatExchangerLogic makeConnector(Direction side) {
        IHeatExchangerLogic connector = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
        connector.setThermalResistance(200);
        connector.addConnectedExchanger(getHeatExchanger(side));
        connector.addConnectedExchanger(getHeatExchanger(side.getOpposite()));
        return connector;
    }

    @Override
    public void tickServer() {
        super.tickServer();

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

    @Override
    public IHeatExchangerLogic getHeatExchanger(Direction side) {
        if (side == null) return dummyExchanger;
        return side.getAxis() == Direction.Axis.Y ? null : heatExchangers[side.get2DDataValue()];
    }

    @Override
    public void initHeatExchangersOnPlacement(Level world, BlockPos pos) {
        double temp = HeatExchangerLogicAmbient.getAmbientTemperature(world, pos);
        for (IHeatExchangerLogic logic : heatExchangers) {
            logic.setTemperature(temp);
        }
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
        if (nonNullLevel().isClientSide) {
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
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        for (int i = 0; i < 4; i++) {
            tag.put("side" + i, heatExchangers[i].serializeNBT());
        }
        tag.put("connector1", connector1.serializeNBT());
        tag.put("connector2", connector2.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

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
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        rsController.parseRedstoneMode(tag);
    }

    @Override
    public RedstoneController<ThermalCompressorBlockEntity> getRedstoneController() {
        return rsController;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new ThermalCompressorMenu(i, playerInventory, getBlockPos());
    }
}
