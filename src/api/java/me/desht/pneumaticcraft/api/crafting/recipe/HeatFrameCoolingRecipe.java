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

import com.mojang.datafixers.util.Either;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidContainerIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.concurrent.ThreadLocalRandom;

public abstract class HeatFrameCoolingRecipe extends PneumaticCraftRecipe {
    /**
     * Get the input ingredient. Fluid ingredients will be matched by an item containing that fluid, and which
     * provides an {@link IFluidHandlerItem} capability.
     *
     * @return the input ingredient
     */
    public abstract Either<Ingredient, FluidContainerIngredient> getInput();

    /**
     * Get the output item. This does not take into account any bonus multiplier.
     * @return the output item
     */
    public abstract ItemStack getOutput();

    /**
     * Get the threshold temperature (Kelvin) below which cooling occurs.
     *
     * @return the threshold temperature
     */
    public abstract int getThresholdTemperature();

    /**
     * Get the bonus output multiplier; for every degree below the threshold temperature, this raises the chance of
     * a bonus output by this amount.  E.g. with a multiplier of 0.01, there would be a 50% chance of a bonus output
     * when the current temperature is 50 below the threshold temperature.
     * <p>
     * Note that the calculated bonus chance could be greater than 1; in that case there is a guaranteed second output
     * plus a chance of a third, and so on.
     *
     * @return a bonus multiplier
     */
    public abstract float getBonusMultiplier();

    /**
     * Get the bonus limit; a hard ceiling on the bonus output chance.  E.g. with a limit of 0.8, there will never be
     * a better than 80% chance of a bonus output.
     *
     * @return a bonus limit
     */
    public abstract float getBonusLimit();

    /**
     * Check if the given itemstack is valid for this recipe.
     *
     * @param stack the itemstack
     * @return true if this itemstack is valid for this recipe
     */
    public abstract boolean matches(ItemStack stack);

    /**
     * Calculate an output quantity based on the recipe's bonus settings and the current temperature of the heat frame.
     *
     * @param temperature heat frame's current temperature
     * @return the number of output items
     */
    public final int calculateOutputQuantity(double temperature) {
        if (getBonusMultiplier() <= 0) return 1;
        float delta = getThresholdTemperature() - (float)temperature;
        if (delta < 0) return 1;
        float mul = 1 + Math.min(getBonusLimit(), getBonusMultiplier() * delta);
        int result = (int) mul;
        if (ThreadLocalRandom.current().nextFloat() < mul - result) result++;
        return result;
    }
}
