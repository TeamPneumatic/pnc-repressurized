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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityPneumaticDynamo extends TileEntityPneumaticBase implements
        IRedstoneControl<TileEntityPneumaticDynamo>, IMinWorkingPressure,
        INamedContainerProvider, IHeatExchangingTE {

    private final PneumaticEnergyStorage energy = new PneumaticEnergyStorage(100000);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    @GuiSynced
    private int rfPerTick;
    @GuiSynced
    private int airPerTick;
    private boolean isEnabled;
    @GuiSynced
    private final RedstoneController<TileEntityPneumaticDynamo> rsController = new RedstoneController<>(this);
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    private final LazyOptional<IHeatExchangerLogic> heatCap = LazyOptional.of(() -> heatExchanger);

    public TileEntityPneumaticDynamo() {
        super(ModTileEntities.PNEUMATIC_DYNAMO.get(), PneumaticValues.DANGER_PRESSURE_PNEUMATIC_DYNAMO, PneumaticValues.MAX_PRESSURE_PNEUMATIC_DYNAMO, PneumaticValues.VOLUME_PNEUMATIC_DYNAMO, 4);
    }

    public int getEfficiency() {
        return HeatUtil.getEfficiency(heatExchanger.getTemperatureAsInt());
    }

    @Override
    public void tick() {
        super.tick();

        if (!level.isClientSide) {
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
                level.setBlockAndUpdate(worldPosition, state.setValue(BlockPneumaticDynamo.ACTIVE, isEnabled));
            }

            TileEntity receiver = getCachedNeighbor(getRotation());
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
    public RedstoneController<TileEntityPneumaticDynamo> getRedstoneController() {
        return rsController;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player){
        rsController.parseRedstoneMode(tag);
    }

    @Override
    public IItemHandler getPrimaryInventory() {
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

    @Override
    public LazyOptional<IHeatExchangerLogic> getHeatCap(Direction side) {
        return heatCap;
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
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);

        energy.writeToNBT(tag);
        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        energy.readFromNBT(tag);
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerEnergy<TileEntityPneumaticDynamo>(ModContainers.PNEUMATIC_DYNAMO.get(), i, playerInventory, getBlockPos());
    }

    @Nullable
    @Override
    public IHeatExchangerLogic getHeatExchanger(Direction dir) {
        return heatExchanger;
    }
}
