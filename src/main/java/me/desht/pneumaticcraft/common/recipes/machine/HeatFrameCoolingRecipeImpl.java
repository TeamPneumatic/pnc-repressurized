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

package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Collection;

public class HeatFrameCoolingRecipeImpl extends HeatFrameCoolingRecipe {
    // cache the highest threshold temperature of all recipes, to reduce the recipe searching heat frames need to do
    private static int maxThresholdTemp = Integer.MIN_VALUE;

    public final Ingredient input;
    private final int temperature;
    public final ItemStack output;
    private final float bonusMultiplier;
    private final float bonusLimit;

    public HeatFrameCoolingRecipeImpl(ResourceLocation id, Ingredient input, int temperature, ItemStack output) {
        this(id, input, temperature, output, 0f, 0f);
    }

    public HeatFrameCoolingRecipeImpl(ResourceLocation id, Ingredient input, int temperature, ItemStack output, float bonusMultiplier, float bonusLimit) {
        super(id);

        this.input = input;
        this.temperature = temperature;
        this.output = output;
        this.bonusMultiplier = bonusMultiplier;
        this.bonusLimit = bonusLimit;
    }

    @Override
    public Ingredient getInput() {
        return input;
    }

    @Override
    public ItemStack getOutput() {
        return output;
    }

    @Override
    public int getThresholdTemperature() {
        return temperature;
    }

    @Override
    public float getBonusMultiplier() {
        return bonusMultiplier;
    }

    @Override
    public float getBonusLimit() {
        return bonusLimit;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return input.test(stack);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        input.toNetwork(buffer);
        buffer.writeInt(temperature);
        buffer.writeItem(output);
        buffer.writeFloat(bonusMultiplier);
        buffer.writeFloat(bonusLimit);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.HEAT_FRAME_COOLING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return PneumaticCraftRecipeType.heatFrameCooling;
    }

    public static <T extends Recipe<?>> void cacheMaxThresholdTemp(Collection<T> recipes) {
        maxThresholdTemp = Integer.MIN_VALUE;
        for (T recipe : recipes) {
            if (recipe instanceof HeatFrameCoolingRecipe) {
                if (((HeatFrameCoolingRecipe) recipe).getThresholdTemperature() > maxThresholdTemp) {
                    maxThresholdTemp = ((HeatFrameCoolingRecipe) recipe).getThresholdTemperature();
                }
            }
        }
    }

    public static int getMaxThresholdTemp(Level world) {
        if (maxThresholdTemp == Integer.MIN_VALUE) {
            cacheMaxThresholdTemp(PneumaticCraftRecipeType.heatFrameCooling.getRecipes(world).values());
        }
        return maxThresholdTemp;
    }

    public static class Serializer<T extends HeatFrameCoolingRecipe> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<T> {
        private final IFactory<T> factory;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public T fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient input = Ingredient.fromJson(json.get("input"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int maxTemp = GsonHelper.getAsInt(json,"max_temp", 273);
            float bonusMultiplier = 0f;
            float bonusLimit = 0f;
            if (json.has("bonus_output")) {
                JsonObject bonus = json.getAsJsonObject("bonus_output");
                bonusMultiplier = GsonHelper.getAsFloat(bonus, "multiplier");
                bonusLimit = GsonHelper.getAsFloat(bonus, "limit");
            }
            return factory.create(recipeId, input, maxTemp, result, bonusMultiplier, bonusLimit);
        }

        @Nullable
        @Override
        public T fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            int temperature = buffer.readInt();
            ItemStack out = buffer.readItem();
            float bonusMultiplier = buffer.readFloat();
            float bonusLimit = buffer.readFloat();
            return factory.create(recipeId, input, temperature, out, bonusMultiplier, bonusLimit);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, T recipe) {
            recipe.write(buffer);
        }

        public interface IFactory<T extends HeatFrameCoolingRecipe> {
            T create(ResourceLocation id, Ingredient input, int temperature, ItemStack out, float bonusMultiplier, float bonusLimit);
        }
    }
}
