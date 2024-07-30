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

package me.desht.pneumaticcraft.common.block.entity.heat;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.lib.BlockEntityConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class HeatSinkBlockEntity extends CompressedIronBlockEntity {
    private final IHeatExchangerLogic airExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();

    private double ambientTemp;

    public HeatSinkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.HEAT_SINK.get(), pos, state);

        heatExchanger.setThermalCapacity(5);
        airExchanger.addConnectedExchanger(heatExchanger);
        airExchanger.setThermalResistance(BlockEntityConstants.HEAT_SINK_THERMAL_RESISTANCE);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (!nonNullLevel().isClientSide) {
            ambientTemp = HeatExchangerLogicAmbient.getAmbientTemperature(getLevel(), getBlockPos());
            airExchanger.setTemperature(ambientTemp);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("airExchanger", airExchanger.serializeNBT());
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        airExchanger.deserializeNBT(tag.getCompound("airExchanger"));
    }

    @Override
    public void tickServer() {
        super.tickServer();

        airExchanger.tick();
        airExchanger.setTemperature(ambientTemp);
    }

    public void onFannedByAirGrate() {
        // called server-side
        heatExchanger.tick();
        airExchanger.setTemperature(ambientTemp);
    }

    @Override
    public IHeatExchangerLogic getHeatExchanger(Direction dir) {
        return dir == null || dir == getRotation() ? super.getHeatExchanger(dir) : null;
    }
}
