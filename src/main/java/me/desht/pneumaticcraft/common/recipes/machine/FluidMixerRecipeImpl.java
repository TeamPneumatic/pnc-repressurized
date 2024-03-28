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
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.FluidMixerRecipe;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidMixerRecipeImpl extends FluidMixerRecipe {
    private final FluidIngredient input1;
    private final FluidIngredient input2;
    private final FluidStack outputFluid;
    private final ItemStack outputItem;
    private final float pressure;
    private final int processingTime;

    public FluidMixerRecipeImpl(FluidIngredient input1, FluidIngredient input2, FluidStack outputFluid, ItemStack outputItem, float pressure, int processingTime) {
        this.input1 = input1;
        this.input2 = input2;
        this.outputFluid = outputFluid;
        this.outputItem = outputItem;
        this.pressure = pressure;
        this.processingTime = processingTime;
    }

    @Override
    public boolean matches(FluidStack fluid1, FluidStack fluid2) {
        return input1.testFluid(fluid1) && input2.testFluid(fluid2)
                || input2.testFluid(fluid1) && input1.testFluid(fluid2);
    }

    @Override
    public FluidIngredient getInput1() {
        return input1;
    }

    @Override
    public FluidIngredient getInput2() {
        return input2;
    }

    @Override
    public FluidStack getOutputFluid() {
        return outputFluid;
    }

    @Override
    public ItemStack getOutputItem() {
        return outputItem;
    }

    @Override
    public int getProcessingTime() {
        return processingTime;
    }

    @Override
    public float getRequiredPressure() {
        return pressure;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.FLUID_MIXER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.FLUID_MIXER.get();
    }

    public static class Serializer<T extends FluidMixerRecipe> implements RecipeSerializer<T> {
        private final IFactory<T> factory;
        private final Codec<T> codec;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;

            codec = RecordCodecBuilder.create(builder -> builder.group(
                    FluidIngredient.FLUID_CODEC.fieldOf("input1").forGetter(FluidMixerRecipe::getInput1),
                    FluidIngredient.FLUID_CODEC.fieldOf("input2").forGetter(FluidMixerRecipe::getInput2),
                    FluidStack.CODEC.fieldOf("fluid_output").forGetter(FluidMixerRecipe::getOutputFluid),
                    ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("item_output").forGetter(FluidMixerRecipe::getOutputItem),
                    Codec.FLOAT.fieldOf("pressure").forGetter(FluidMixerRecipe::getRequiredPressure),
                    Codec.INT.fieldOf("time").forGetter(FluidMixerRecipe::getProcessingTime)
            ).apply(builder, factory::create));
        }

        @Override
        public Codec<T> codec() {
            return codec;
        }

        @Override
        public T fromNetwork(FriendlyByteBuf buffer) {
            FluidIngredient input1 = (FluidIngredient) Ingredient.fromNetwork(buffer);
            FluidIngredient input2 = (FluidIngredient) Ingredient.fromNetwork(buffer);
            FluidStack outputFluid = FluidStack.readFromPacket(buffer);
            ItemStack outputItem = buffer.readItem();
            float pressure = buffer.readFloat();
            int processingTime = buffer.readVarInt();

            return factory.create(input1, input2, outputFluid, outputItem, pressure, processingTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, T recipe) {
            recipe.getInput1().toNetwork(buffer);
            recipe.getInput2().toNetwork(buffer);
            recipe.getOutputFluid().writeToPacket(buffer);
            buffer.writeItem(recipe.getOutputItem());
            buffer.writeFloat(recipe.getRequiredPressure());
            buffer.writeVarInt(recipe.getProcessingTime());
        }

        public interface IFactory<T extends FluidMixerRecipe> {
            T create(FluidIngredient input1, FluidIngredient input2,
                     FluidStack outputFluid, ItemStack outputItem, float pressure, int processingTime);
        }
    }
}
