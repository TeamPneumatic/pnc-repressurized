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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PressureChamberRecipeBuilder extends PneumaticCraftRecipeBuilder<PressureChamberRecipeBuilder> {
    private final List<Ingredient> inputs;
    private final float requiredPressure;
    private final ItemStack[] outputs;

    public PressureChamberRecipeBuilder(List<Ingredient> inputs, float requiredPressure, ItemStack... outputs) {
        super(RL(PneumaticCraftRecipeTypes.PRESSURE_CHAMBER));

        this.inputs = inputs;
        this.requiredPressure = requiredPressure;
        this.outputs = outputs;
    }

    @Override
    protected RecipeResult getResult(ResourceLocation id) {
        return new PressureChamberRecipeResult(id);
    }

    public class PressureChamberRecipeResult extends RecipeResult {
        PressureChamberRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            JsonArray in = new JsonArray();
            for (Ingredient ingr : inputs) {
                in.add(ingr.toJson());
            }
            json.add("inputs", in);
            json.addProperty("pressure", requiredPressure);
            json.add("results", SerializerHelper.serializeItemStacks(outputs));
        }
    }
}
