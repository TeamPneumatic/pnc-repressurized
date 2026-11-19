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
import me.desht.pneumaticcraft.common.drone.progwidgets.ILiquidFiltered;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.world.entity.ai.goal.Goal;

public class DroneAIVoidLiquid extends Goal {
    private final IDrone drone;
    private final ILiquidFiltered widget;

    public DroneAIVoidLiquid(IDrone drone, ILiquidFiltered widget) {
        this.drone = drone;
        this.widget = widget;
    }

    @Override
    public boolean canUse() {
        return drone.getFluidTank().getAmountAsInt(0) > 0 && widget.isFluidValid(FluidUtils.getFluid(drone.getFluidTank(), 0).getFluid());
    }

    @Override
    public void start() {
        int amount = drone.getFluidTank().getAmountAsInt(0);
        if (amount > 0 && widget.isFluidValid(FluidUtils.getFluid(drone.getFluidTank(), 0).getFluid())) {
            FluidUtils.drain(drone.getFluidTank(), Integer.MAX_VALUE, false);
            drone.addAirToDrone(Math.max(1, PneumaticValues.DRONE_USAGE_VOID * amount / 20));
        }
    }
}
