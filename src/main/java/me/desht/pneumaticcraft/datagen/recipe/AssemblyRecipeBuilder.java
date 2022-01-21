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

package me.desht.pneumaticcraft.datagen.recipe;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Locale;

public class AssemblyRecipeBuilder extends PneumaticCraftRecipeBuilder<AssemblyRecipeBuilder> {
    private final Ingredient input;
    @Nonnull
    private final ItemStack output;
    private final AssemblyRecipe.AssemblyProgramType program;

    public AssemblyRecipeBuilder(Ingredient input, @Nonnull ItemStack output, AssemblyRecipe.AssemblyProgramType program) {
        super(program.getRecipeType());

        this.input = input;
        this.output = output;
        this.program = program;
    }

    @Override
    protected RecipeResult getResult(ResourceLocation id) {
        return new AssemblyRecipeResult(id);
    }

    public class AssemblyRecipeResult extends RecipeResult {
        AssemblyRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("input", input.toJson());
            json.add("result", SerializerHelper.serializeOneItemStack(output));
            json.addProperty("program", program.toString().toLowerCase(Locale.ROOT));
        }
    }
}
