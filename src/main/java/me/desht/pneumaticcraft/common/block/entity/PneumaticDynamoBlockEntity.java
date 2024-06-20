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
import me.desht.pneumaticcraft.common.inventory.PneumaticDynamoMenu;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;

public class PneumaticDynamoBlockEntity extends AbstractAirHandlingBlockEntity implements
        IRedstoneControl<PneumaticDynamoBlockEntity>, IMinWorkingPressure,
        MenuProvider, IHeatExchangingTE {

    private final PneumaticEnergyStorage energy = new PneumaticEnergyStorage(100000);

    @GuiSynced
    private int rfPerTick;
    @GuiSynced
    private int airPerTick;
    private boolean isEnabled;
    @GuiSynced
    private final RedstoneController<PneumaticDynamoBlockEntity> rsController = new RedstoneController<>(this);
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();

    public PneumaticDynamoBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.PNEUMATIC_DYNAMO.get(), pos, state, PressureTier.TIER_TWO, PneumaticValues.VOLUME_PNEUMATIC_DYNAMO, 4);
    }

    public int getEfficiency() {
        return HeatUtil.getEfficiency(heatExchanger.getTemperatureAsInt());
    }

    @Override
    public void tickServer() {
        super.tickServer();

        final Level level = nonNullLevel();
        if (level.getGameTime() % 20 == 0) {
            int efficiency = Math.max(1, ConfigHelper.common().machines.pneumaticDynamoEfficiency.get());
            airPerTick = (int) (40 * this.getSpeedUsageMultiplierFromUpgrades() * 100 / efficiency);
            rfPerTick = (int) (40 * this.getSpeedUsageMultiplierFromUpgrades() * getEfficiency() / 100);
        }

        boolean newEnabled;
        if (rsController.shouldRun() && getPressure() > getMinWorkingPressure() && energy.getMaxEnergyStored() - energy.getEnergyStored() >= rfPerTick) {
            this.addAir(-airPerTick);
            heatExchanger.addHeat(airPerTick / 100D);
            energy.receiveEnergy(rfPerTick, false);
            newEnabled = true;
        } else {
            newEnabled = false;
        }
        if ((level.getGameTime() & 0xf) == 0 && newEnabled != isEnabled) {
            isEnabled = newEnabled;
            BlockState state = level.getBlockState(worldPosition);
            level.setBlockAndUpdate(worldPosition, state.setValue(PNCBlockStateProperties.ACTIVE, isEnabled));
        }

        BlockEntity receiver = getCachedNeighbor(getRotation());
        if (receiver != null) {
            IOHelper.getEnergyStorageForBlock(receiver, getRotation().getOpposite()).ifPresent(neighborStorage -> {
                int extracted = energy.extractEnergy(rfPerTick * 2, true);
                int energyPushed = neighborStorage.receiveEnergy(extracted, true);
                if (energyPushed > 0) {
                    neighborStorage.receiveEnergy(energy.extractEnergy(energyPushed, false), false);
                }
            });
        }
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public RedstoneController<PneumaticDynamoBlockEntity> getRedstoneController() {
        return rsController;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player){
        rsController.parseRedstoneMode(tag);
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return null;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side == getRotation().getOpposite();
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_PNEUMATIC_DYNAMO;
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
    public boolean hasEnergyCapability() {
        return true;
    }

    @Override
    public IEnergyStorage getEnergyHandler(@Nullable Direction dir) {
        return dir == getRotation() || dir == null ? energy : null;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        energy.writeToNBT(tag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        energy.readFromNBT(tag);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
        return new PneumaticDynamoMenu(windowId, playerInventory, getBlockPos());
    }

    @Nullable
    @Override
    public IHeatExchangerLogic getHeatExchanger(Direction dir) {
        return heatExchanger;
    }
}
