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

import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import net.minecraft.world.level.material.Fluid;

public abstract class FuelQualityRecipe extends PneumaticCraftRecipe {
    /**
     * Does the given fluid match this recipe?
     * @param inputFluid the fluid to test
     * @return true if it matches, false otherwise
     */
    public abstract boolean matchesFluid(Fluid inputFluid);

    /**
     * Get the fuel for this recipe
     * @return the fuel
     */
    public abstract FluidIngredient getFuel();

    /**
     * Get the amount of compressed air (in mL) produced by burning 1000mB of this fuel in a liquid compressor,
     * with no speed upgrades.
     * @return the amount of air produced by this fuel fluid
     */
    public abstract int getAirPerBucket();

    /**
     * Get the burn rate for this fuel. Burn rate affects the speed at which the fuel is consumed (and compressed
     * air produced), without affecting the overall quantity of air produced.
     * @return the burn rate; 1.0f is the baseline
     */
    public abstract float getBurnRate();
}
