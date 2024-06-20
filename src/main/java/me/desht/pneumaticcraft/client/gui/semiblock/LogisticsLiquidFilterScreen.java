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

package me.desht.pneumaticcraft.client.gui.semiblock;

import me.desht.pneumaticcraft.client.gui.programmer.ProgWidgetLiquidFilterScreen;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetLiquidFilter;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public class LogisticsLiquidFilterScreen extends ProgWidgetLiquidFilterScreen {
    private final Screen parentScreen;

    public LogisticsLiquidFilterScreen(Screen parentScreen) {
        super(new ProgWidgetLiquidFilter(), null);
        this.parentScreen = parentScreen;
    }

    public Fluid getFilter() {
        return progWidget.getFluidStack().getFluid();
    }

    public void setFilter(Fluid fluid) {
        progWidget.setFluidStack(new FluidStack(fluid, 1000));
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parentScreen);
    }
}
