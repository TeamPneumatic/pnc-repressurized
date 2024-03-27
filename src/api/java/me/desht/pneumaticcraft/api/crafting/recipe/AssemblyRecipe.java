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

import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import java.util.Locale;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public abstract class AssemblyRecipe extends PneumaticCraftRecipe {
    /**
     * Get the input ingredient.
     * @return the input ingredient
     */
    public abstract Ingredient getInput();

    /**
     * Get the number of items required/used
     * @return the number of items
     */
    public abstract int getInputAmount();

    /**
     * Get the output item for this recipe.
     * @return the output item
     */
    @Nonnull
    public abstract ItemStack getOutput();

    /**
     * Get the program required.
     * @return the program type
     */
    public abstract AssemblyProgramType getProgramType();

    /**
     * Check if the given stack is a valid input for this recipe.
     * @param stack input stack
     * @return true if valid, false otherwise
     */
    public abstract boolean matches(ItemStack stack);

    public enum AssemblyProgramType {
        DRILL, LASER, DRILL_LASER;

        public String getRegistryName() {
            return "assembly_program_" + this.toString().toLowerCase(Locale.ROOT);
        }

        public ResourceLocation getRecipeType() {
            return switch (this) {
                case DRILL -> RL(PneumaticCraftRecipeTypes.ASSEMBLY_DRILL);
                case LASER -> RL(PneumaticCraftRecipeTypes.ASSEMBLY_LASER);
                case DRILL_LASER -> RL(PneumaticCraftRecipeTypes.ASSEMBLY_DRILL_LASER);
            };
        }
    }
}
