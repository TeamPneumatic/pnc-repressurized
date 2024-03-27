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
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.heat.SyncedTemperature;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class CompressedIronBlockBlockEntity extends AbstractTickingBlockEntity implements IComparatorSupport, IHeatTinted, IHeatExchangingTE {
    protected final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    private int comparatorOutput = 0;
    @DescSynced
    protected final SyncedTemperature syncedTemperature = new SyncedTemperature(heatExchanger);

    public CompressedIronBlockBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntityTypes.COMPRESSED_IRON_BLOCK.get(), pos, state);
    }

    CompressedIronBlockBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        heatExchanger.setThermalCapacity(10);
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    @Override
    public void tickServer() {
        super.tickServer();

        syncedTemperature.tick();

        int newComparatorOutput = HeatUtil.getComparatorOutput((int) heatExchanger.getTemperature());
        if (comparatorOutput != newComparatorOutput) {
            comparatorOutput = newComparatorOutput;
            nonNullLevel().updateNeighbourForOutputSignal(getBlockPos(), getBlockState().getBlock());
        }
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return null;
    }

    @Override
    public int getComparatorValue() {
        return comparatorOutput;
    }

    @Override
    public TintColor getColorForTintIndex(int tintIndex) {
        return HeatUtil.getColourForTemperature(syncedTemperature.getSyncedTemp());
    }

    @Override
    public IHeatExchangerLogic getHeatExchanger(Direction dir) {
        return heatExchanger;
    }
}
