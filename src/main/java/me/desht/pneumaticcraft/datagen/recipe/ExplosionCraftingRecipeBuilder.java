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
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ExplosionCraftingRecipeBuilder extends PneumaticCraftRecipeBuilder<ExplosionCraftingRecipeBuilder> {
    private final Ingredient input;
    private final int lossRate;
    private final ItemStack[] outputs;

    public ExplosionCraftingRecipeBuilder(Ingredient input, int lossRate, ItemStack... outputs) {
        super(RL(PneumaticCraftRecipeTypes.EXPLOSION_CRAFTING));

        this.input = input;
        this.lossRate = lossRate;
        this.outputs = outputs;
    }

    @Override
    protected RecipeResult getResult(ResourceLocation id) {
        return new ExplosionCraftingRecipeResult(id);
    }

    public class ExplosionCraftingRecipeResult extends RecipeResult {
        ExplosionCraftingRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("input", input.toJson());
            json.add("results", SerializerHelper.serializeItemStacks(outputs));
            json.addProperty("loss_rate", lossRate);
        }
    }
}
