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
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public abstract class FluidMixerRecipe extends PneumaticCraftRecipe {
    public abstract boolean matches(FluidStack fluid1, FluidStack fluid2);

    public abstract FluidIngredient getInput1();

    public abstract FluidIngredient getInput2();

    public abstract FluidStack getOutputFluid();

    public abstract ItemStack getOutputItem();

    public abstract int getProcessingTime();

    public abstract float getRequiredPressure();
}
