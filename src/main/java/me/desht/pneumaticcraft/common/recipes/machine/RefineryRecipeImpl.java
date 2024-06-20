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

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.recipe.RefineryRecipe;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.util.CodecUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.apache.commons.lang3.Validate;

import java.util.List;

public class RefineryRecipeImpl extends RefineryRecipe {
	public final SizedFluidIngredient input;
	public final List<FluidStack> outputs;
	private final TemperatureRange operatingTemp;

	public RefineryRecipeImpl(SizedFluidIngredient input, TemperatureRange operatingTemp, List<FluidStack> outputs) {
		super();

		this.operatingTemp = operatingTemp;
		Validate.isTrue(outputs.size() >= 2 && outputs.size() <= MAX_OUTPUTS,
				"Recipe must have between 2 and " + MAX_OUTPUTS + " (inclusive) outputs");
		this.input = input;
		this.outputs = ImmutableList.copyOf(outputs);
	}

	@Override
	public SizedFluidIngredient getInput() {
		return input;
	}

	@Override
	public List<FluidStack> getOutputs() {
		return outputs;
	}

	@Override
	public TemperatureRange getOperatingTemp() {
		return operatingTemp;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipeSerializers.REFINERY.get();
	}

	@Override
	public RecipeType<?> getType() {
		return ModRecipeTypes.REFINERY.get();
	}

	@Override
	public String getGroup() {
		return Names.MOD_ID + ":refinery";
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(ModBlocks.REFINERY.get());
	}

	public interface IFactory<T extends RefineryRecipe> {
		T create(SizedFluidIngredient input, TemperatureRange operatingTemp, List<FluidStack> outputs);
	}

	public static class Serializer<T extends RefineryRecipe> implements RecipeSerializer<T> {
		private final MapCodec<T> codec;
		private final StreamCodec<RegistryFriendlyByteBuf,T> streamCodec;

        public Serializer(IFactory<T> factory) {
            this.codec = RecordCodecBuilder.mapCodec(builder -> builder.group(
					SizedFluidIngredient.FLAT_CODEC
							.fieldOf("input")
							.forGetter(RefineryRecipe::getInput),
					TemperatureRange.CODEC
							.optionalFieldOf("temperature", TemperatureRange.min(373))
							.forGetter(RefineryRecipe::getOperatingTemp),
					CodecUtil.listWithSizeBound(FluidStack.CODEC.listOf(), 2, RefineryRecipe.MAX_OUTPUTS)
							.fieldOf("outputs")
							.forGetter(RefineryRecipe::getOutputs)
			).apply(builder, factory::create));
			this.streamCodec = StreamCodec.composite(
					SizedFluidIngredient.STREAM_CODEC, RefineryRecipe::getInput,
					TemperatureRange.STREAM_CODEC, RefineryRecipe::getOperatingTemp,
					FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()), RefineryRecipe::getOutputs,
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
