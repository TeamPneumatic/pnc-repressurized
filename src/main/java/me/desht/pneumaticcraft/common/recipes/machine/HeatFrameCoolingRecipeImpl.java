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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Collection;

public class HeatFrameCoolingRecipeImpl extends HeatFrameCoolingRecipe {
    // cache the highest threshold temperature of all recipes, to reduce the recipe searching heat frames need to do
    private static int maxThresholdTemp = Integer.MIN_VALUE;

    public final Ingredient input;
    private final int temperature;
    public final ItemStack output;
    private final float bonusMultiplier;
    private final float bonusLimit;

    public HeatFrameCoolingRecipeImpl(Ingredient input, int temperature, ItemStack output) {
        this(input, temperature, output, 0f, 0f);
    }

    public HeatFrameCoolingRecipeImpl(Ingredient input, int temperature, ItemStack output, float bonusMultiplier, float bonusLimit) {
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
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.HEAT_FRAME_COOLING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.HEAT_FRAME_COOLING.get();
    }

    public static <T extends Recipe<?>> void cacheMaxThresholdTemp(Collection<RecipeHolder<T>> recipes) {
        maxThresholdTemp = Integer.MIN_VALUE;
        for (var holder : recipes) {
            if (holder.value() instanceof HeatFrameCoolingRecipe hfcr) {
                if (hfcr.getThresholdTemperature() > maxThresholdTemp) {
                    maxThresholdTemp = hfcr.getThresholdTemperature();
                }
            }
        }
    }

    public static int getMaxThresholdTemp(Level world) {
        if (maxThresholdTemp == Integer.MIN_VALUE) {
            cacheMaxThresholdTemp(ModRecipeTypes.getRecipes(world, ModRecipeTypes.HEAT_FRAME_COOLING));
        }
        return maxThresholdTemp;
    }

    public static class Serializer<T extends HeatFrameCoolingRecipe> implements RecipeSerializer<T> {
        private final IFactory<T> factory;
        private final Codec<T> codec;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;

            codec = RecordCodecBuilder.create(inst -> inst.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("input")
                            .forGetter(HeatFrameCoolingRecipe::getInput),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("temperature")
                            .forGetter(HeatFrameCoolingRecipe::getThresholdTemperature),
                    ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("output")
                            .forGetter(HeatFrameCoolingRecipe::getOutput),
                    Codec.floatRange(0f, Float.MAX_VALUE).optionalFieldOf("bonusMultiplier", 0f)
                            .forGetter(HeatFrameCoolingRecipe::getBonusMultiplier),
                    Codec.floatRange(0f, Float.MAX_VALUE).optionalFieldOf("bonusLimit", 0f)
                            .forGetter(HeatFrameCoolingRecipe::getBonusLimit)
            ).apply(inst, factory::create));
        }

        @Override
        public Codec<T> codec() {
            return codec;
        }

        @Override
        public T fromNetwork(FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            int temperature = buffer.readInt();
            ItemStack out = buffer.readItem();
            float bonusMultiplier = buffer.readFloat();
            float bonusLimit = buffer.readFloat();
            return factory.create(input, temperature, out, bonusMultiplier, bonusLimit);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, T recipe) {
            recipe.getInput().toNetwork(buffer);
            buffer.writeInt(recipe.getThresholdTemperature());
            buffer.writeItem(recipe.getOutput());
            buffer.writeFloat(recipe.getBonusMultiplier());
            buffer.writeFloat(recipe.getBonusLimit());
        }

        public interface IFactory<T extends HeatFrameCoolingRecipe> {
            T create(Ingredient input, int temperature, ItemStack out, float bonusMultiplier, float bonusLimit);
        }
    }
}
