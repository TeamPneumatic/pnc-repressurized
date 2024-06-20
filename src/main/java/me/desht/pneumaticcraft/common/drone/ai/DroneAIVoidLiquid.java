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

package me.desht.pneumaticcraft.common.drone.ai;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.progwidgets.ILiquidFiltered;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.world.entity.ai.goal.Goal;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class DroneAIVoidLiquid extends Goal {
    private final IDrone drone;
    private final ILiquidFiltered widget;

    public DroneAIVoidLiquid(IDrone drone, ILiquidFiltered widget) {
        this.drone = drone;
        this.widget = widget;
    }

    @Override
    public boolean canUse() {
        return widget.isFluidValid(drone.getFluidTank().getFluid().getFluid());
    }

    @Override
    public void start() {
        int amount = drone.getFluidTank().getFluidAmount();
        if (amount > 0 && widget.isFluidValid(drone.getFluidTank().getFluid().getFluid())) {
            drone.getFluidTank().drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
            drone.addAirToDrone(Math.max(1, PneumaticValues.DRONE_USAGE_VOID * amount / 20));
        }
    }
}
