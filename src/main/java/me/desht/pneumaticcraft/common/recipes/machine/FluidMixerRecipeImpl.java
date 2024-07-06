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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.crafting.recipe.FluidMixerRecipe;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class FluidMixerRecipeImpl extends FluidMixerRecipe {
    private final SizedFluidIngredient input1;
    private final SizedFluidIngredient input2;
    private final FluidStack outputFluid;
    private final ItemStack outputItem;
    private final float pressure;
    private final int processingTime;

    public FluidMixerRecipeImpl(SizedFluidIngredient input1, SizedFluidIngredient input2, FluidStack outputFluid, ItemStack outputItem, float pressure, int processingTime) {
        this.input1 = input1;
        this.input2 = input2;
        this.outputFluid = outputFluid;
        this.outputItem = outputItem;
        this.pressure = pressure;
        this.processingTime = processingTime;
    }

    @Override
    public boolean matches(FluidStack fluid1, FluidStack fluid2) {
        return input1.test(fluid1) && input2.test(fluid2)
                || input2.test(fluid1) && input1.test(fluid2);
    }

    @Override
    public SizedFluidIngredient getInput1() {
        return input1;
    }

    @Override
    public SizedFluidIngredient getInput2() {
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


    public interface IFactory<T extends FluidMixerRecipe> {
        T create(SizedFluidIngredient input1, SizedFluidIngredient input2,
                 FluidStack outputFluid, ItemStack outputItem, float pressure, int processingTime);
    }

    public static class Serializer<T extends FluidMixerRecipe> implements RecipeSerializer<T> {
        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf,T> streamCodec;

        public Serializer(IFactory<T> factory) {
            codec = RecordCodecBuilder.mapCodec(builder -> builder.group(
                    SizedFluidIngredient.FLAT_CODEC.fieldOf("input1").forGetter(FluidMixerRecipe::getInput1),
                    SizedFluidIngredient.FLAT_CODEC.fieldOf("input2").forGetter(FluidMixerRecipe::getInput2),
                    FluidStack.OPTIONAL_CODEC.fieldOf("fluid_output").forGetter(FluidMixerRecipe::getOutputFluid),
                    ItemStack.OPTIONAL_CODEC.fieldOf("item_output").forGetter(FluidMixerRecipe::getOutputItem),
                    Codec.FLOAT.fieldOf("pressure").forGetter(FluidMixerRecipe::getRequiredPressure),
                    Codec.INT.fieldOf("time").forGetter(FluidMixerRecipe::getProcessingTime)
            ).apply(builder, factory::create));
            streamCodec = StreamCodec.composite(
                    SizedFluidIngredient.STREAM_CODEC, FluidMixerRecipe::getInput1,
                    SizedFluidIngredient.STREAM_CODEC, FluidMixerRecipe::getInput2,
                    FluidStack.OPTIONAL_STREAM_CODEC, FluidMixerRecipe::getOutputFluid,
                    ItemStack.OPTIONAL_STREAM_CODEC, FluidMixerRecipe::getOutputItem,
                    ByteBufCodecs.FLOAT, FluidMixerRecipe::getRequiredPressure,
                    ByteBufCodecs.VAR_INT, FluidMixerRecipe::getProcessingTime,
                    factory::create
            );
        }

        @Override
        public MapCodec<T> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return streamCodec;
        }
    }
}
