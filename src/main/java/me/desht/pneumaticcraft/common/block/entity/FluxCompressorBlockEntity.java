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
import me.desht.pneumaticcraft.api.block.PNCBlockStateProperties;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.FluxCompressorMenu;
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
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class FluxCompressorBlockEntity extends AbstractAirHandlingBlockEntity
        implements IRedstoneControl<FluxCompressorBlockEntity>, MenuProvider, IHeatExchangingTE {
    private static final int BASE_FE_PRODUCTION = 40;
    private final PneumaticEnergyStorage energy = new PneumaticEnergyStorage(100000);
//    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    @GuiSynced
    private int rfPerTick;
    @GuiSynced
    private float airPerTick;
    private float airBuffer;
    private boolean isEnabled;
    @GuiSynced
    private final RedstoneController<FluxCompressorBlockEntity> rsController = new RedstoneController<>(this);
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
//    private final LazyOptional<IHeatExchangerLogic> heatCap = LazyOptional.of(() -> heatExchanger);

    public FluxCompressorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.FLUX_COMPRESSOR.get(), pos, state, PressureTier.TIER_TWO, PneumaticValues.VOLUME_FLUX_COMPRESSOR, 4);

        heatExchanger.setThermalCapacity(100);
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    @Override
    public boolean hasEnergyCapability() {
        return true;
    }

    @Override
    public IEnergyStorage getEnergyHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return energy;
    }

    public int getHeatEfficiency(){
        return HeatUtil.getEfficiency(heatExchanger.getTemperatureAsInt());
    }

    @Override
    public void tickServer() {
        super.tickServer();

        final Level level = nonNullLevel();
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
            level.setBlockAndUpdate(worldPosition, state.setValue(PNCBlockStateProperties.ACTIVE, isEnabled));
        }
        airHandler.setSideLeaking(hasNoConnectedAirHandlers() ? getRotation().getOpposite() : null);
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side == getRotation().getOpposite();
    }

    @Override
    public RedstoneController<FluxCompressorBlockEntity> getRedstoneController() {
        return rsController;
    }

    @Override
    public void saveAdditional(CompoundTag tag){
        super.saveAdditional(tag);
        energy.writeToNBT(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        energy.readFromNBT(tag);
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        rsController.parseRedstoneMode(tag);
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
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new FluxCompressorMenu(i, playerInventory, getBlockPos());
    }

    @Override
    public IHeatExchangerLogic getHeatExchanger(Direction dir) {
        return heatExchanger;
    }
}
