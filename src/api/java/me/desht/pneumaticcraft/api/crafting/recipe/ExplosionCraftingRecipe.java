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

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public abstract class ExplosionCraftingRecipe extends PneumaticCraftRecipe {
    protected ExplosionCraftingRecipe(ResourceLocation id) {
        super(id);
    }

    /**
     * Get the input ingredient for this recipe.
     *
     * @return the input ingredient
     */
    public abstract Ingredient getInput();

    /**
     * Get the number of items which will be taken from the input to create the output item.
     * <p>
     * This should be the number of items in any itemstack returned from {@code getInput().getMatchedStacks()}; for
     * basic {@link Ingredient} ingredients, this will always be 1, but custom ingredient subclasses could return a
     * large number.
     *
     * @return the number of items
     */
    public abstract int getAmount();

    /**
     * Get a list of the output item(s).
     *
     * @return the outputs
     */
    public abstract List<ItemStack> getOutputs();

    /**
     * Get the recipe loss rate, as a percentage.
     * @return the loss rate
     */
    public abstract int getLossRate();

    /**
     * Check if the given itemstack matches this recipe.  The stack's item should match, and additionally
     * there should be at least {@link #getAmount()} items in the stack.
     * @param stack the itemstack to check
     * @return true if this itemstack matches this recipe, false otherwise
     */
    public abstract boolean matches(ItemStack stack);
}
