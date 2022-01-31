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
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityHeatSink extends TileEntityCompressedIronBlock {
    private final IHeatExchangerLogic airExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();

    private double ambientTemp;

    public TileEntityHeatSink(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HEAT_SINK.get(), pos, state);

        heatExchanger.setThermalCapacity(5);
        airExchanger.addConnectedExchanger(heatExchanger);
        airExchanger.setThermalResistance(TileEntityConstants.HEAT_SINK_THERMAL_RESISTANCE);
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
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
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("airExchanger", airExchanger.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
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
    public AABB getRenderBoundingBox() {
        return new AABB(
                getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(),
                getBlockPos().getX() + 1, getBlockPos().getY() + 1, getBlockPos().getZ() + 1
        );
    }

    @Override
    public LazyOptional<IHeatExchangerLogic> getHeatCap(Direction side) {
        return side == null || side == getRotation() ? super.getHeatCap(side) : LazyOptional.empty();
    }
}
