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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class HeatFrameCoolingRecipeBuilder extends PneumaticCraftRecipeBuilder<HeatFrameCoolingRecipeBuilder> {
    private final Ingredient input;
    private final int temperature;
    private final ItemStack output;
    private final float bonusMultiplier;
    private final float bonusLimit;

    protected HeatFrameCoolingRecipeBuilder(Ingredient input, int temperature, ItemStack output) {
        this(input, temperature, output, 0f, 0f);
    }

    public HeatFrameCoolingRecipeBuilder(Ingredient input, int temperature, ItemStack output, float bonusMultiplier, float bonusLimit) {
        super(RL(PneumaticCraftRecipeTypes.HEAT_FRAME_COOLING));

        this.input = input;
        this.temperature = temperature;
        this.output = output;
        this.bonusMultiplier = bonusMultiplier;
        this.bonusLimit = bonusLimit;
    }

    @Override
    protected RecipeResult getResult(ResourceLocation id) {
        return new HeatFrameCoolingRecipeResult(id);
    }

    public class HeatFrameCoolingRecipeResult extends RecipeResult {
        HeatFrameCoolingRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("input", input.toJson());
            json.addProperty("max_temp", temperature);
            json.add("result", SerializerHelper.serializeOneItemStack(output));
            if (bonusMultiplier > 0f || bonusLimit > 0f) {
                JsonObject bonus = new JsonObject();
                bonus.addProperty("multiplier", bonusMultiplier);
                bonus.addProperty("limit", bonusLimit);
                json.add("bonus_output", bonus);
            }
        }
    }
}
