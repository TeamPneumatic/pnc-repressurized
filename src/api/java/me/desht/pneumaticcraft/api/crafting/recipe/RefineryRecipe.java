/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

public abstract class RefineryRecipe extends PneumaticCraftRecipe {
    public static final int MAX_OUTPUTS = 4;

    /**
     * Get the input fluid ingredient for this recipe
     *
     * @return the input fluid
     */
    public abstract SizedFluidIngredient getInput();

    /**
     * Get a list of fluid outputs for this recipe
     * @return a list of output fluidstacks
     */
    public abstract List<FluidStack> getOutputs();

    /**
     * Get the valid operating temperature range for this recipe
     * @return the operating temperature range
     */
    public abstract TemperatureRange getOperatingTemp();
}
