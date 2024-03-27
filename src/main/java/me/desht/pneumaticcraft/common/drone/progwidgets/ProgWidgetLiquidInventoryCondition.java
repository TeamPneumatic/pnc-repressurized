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
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class ProgWidgetLiquidInventoryCondition extends ProgWidgetCondition {

    public ProgWidgetLiquidInventoryCondition() {
        super(ModProgWidgets.CONDITION_LIQUID_INVENTORY.get());
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA.get(), ModProgWidgets.LIQUID_FILTER.get(), ModProgWidgets.TEXT.get());
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDroneBase drone, IProgWidget widget) {
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase) widget) {

            @Override
            protected boolean evaluate(BlockPos pos) {
                BlockEntity te = drone.world().getBlockEntity(pos);
                int count = te == null ? countFluid(drone.world(), pos) : countFluid(te);
                maybeRecordMeasuredVal(drone, count);
                return ((ICondition) progWidget).getOperator().evaluate(count, ((ICondition) progWidget).getRequiredCount());
            }

            private int countFluid(Level world, BlockPos pos) {
                FluidState state = world.getFluidState(pos);
                if (state.getType() != Fluids.EMPTY && ProgWidgetLiquidFilter.isLiquidValid(state.getType(), progWidget, 1)) {
                    return 1000;
                } else {
                    return 0;
                }
            }

            private int countFluid(BlockEntity te) {
                return IOHelper.getFluidHandlerForBlock(te).map(handler -> {
                    int total = 0;
                    for (int i = 0; i < handler.getTanks(); i++) {
                        FluidStack stack = handler.getFluidInTank(i);
                        if (!stack.isEmpty() && ProgWidgetLiquidFilter.isLiquidValid(stack.getFluid(), progWidget, 1)) {
                            total += stack.getAmount();
                        }
                    }
                    return total;
                }).orElse(0);
            }
        };
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_LIQUID_INVENTORY;
    }

}
