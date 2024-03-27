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

package me.desht.pneumaticcraft.common.drone.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIBlockCondition;
import me.desht.pneumaticcraft.common.registry.ModProgWidgets;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;

public class ProgWidgetEnergyCondition extends ProgWidgetCondition {
    public ProgWidgetEnergyCondition() {
        super(ModProgWidgets.CONDITION_RF.get());
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_ENERGY;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA.get(), ModProgWidgets.TEXT.get());
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDroneBase drone, IProgWidget widget) {
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase) widget) {
            @Override
            protected boolean evaluate(BlockPos pos) {
                BlockEntity te = drone.world().getBlockEntity(pos);
                if (te == null) return false;
                int energy = 0;
                for (Direction face : DirectionUtil.VALUES) {
                    if (getSides()[face.get3DDataValue()]) {
                        energy = Math.max(energy, getEnergy(te, face));
                    }
                }
                maybeRecordMeasuredVal(drone, energy);
                return ((ICondition) progWidget).getOperator().evaluate(energy,((ICondition) progWidget).getRequiredCount());
            }

            private int getEnergy(BlockEntity te, Direction side) {
                return IOHelper.getEnergyStorageForBlock(te, side).map(IEnergyStorage::getEnergyStored).orElse(0);
            }
        };
    }
}
