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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class ThermoPlantRecipe extends PneumaticCraftRecipe {
    /**
     * Check if this recipe matches the given input fluid and item.  This does not take any required temperature and
     * pressure into account.  It will also match if the input fluid matches but is insufficient.
     *
     * @param inputFluid the input fluid
     * @param inputItem the input item
     * @return true if this recipe matches
     */
    public abstract boolean matches(FluidStack inputFluid, ItemStack inputItem);

    /**
     * Get the temperature range required for processing to occur.
     *
     * @return temperature range, in degrees Kelvin.
     */
    public abstract TemperatureRange getOperatingTemperature();

    /**
     * Get the minimum pressure required for processing to occur.
     *
     * @return pressure, in bar.
     */
    public abstract float getRequiredPressure();

    /**
     * Get the base heat used each time the processing plant produces some output.  This value will be subtracted from
     * the machine's current heat.  This could be negative if the recipe is an exothermic recipe, i.e. it produces
     * heat; see {@link #isExothermic()}.
     *
     * @param ambientTemperature the machine's ambient temperature
     * @return heat used
     */
    public double heatUsed(double ambientTemperature) {
        TemperatureRange range = getOperatingTemperature();
        if (range.isAny()) return 0;  // don't care about temperature; don't consume or produce heat

        double used;
        if (range.getMin() > ambientTemperature) {
            used = (range.getMin() - ambientTemperature) / 10D;
        } else if (range.getMax() < ambientTemperature) {
            used = (ambientTemperature - range.getMax()) / 10D;
        } else {
            if (isExothermic()) {
                used = (range.getMax() - ambientTemperature) / 10D;
            } else {
                used = (ambientTemperature - range.getMin()) / 10D;
            }
        }
        return isExothermic() ? -used : used;
    }

    /**
     * Get the base air used each time the processing plant produces some output.  By default, this is 50mL of air per
     * bar of pressure required.
     *
     * @return air used
     */
    public int airUsed() {
        return (int) (50 * getRequiredPressure());
    }

    public abstract Optional<Ingredient> getInputItem();

    public abstract Optional<SizedFluidIngredient> getInputFluid();

    public abstract FluidStack getOutputFluid();

    public abstract ItemStack getOutputItem();

    public abstract float getRecipeSpeed();

    public abstract float getAirUseMultiplier();

    /**
     * Check if this recipe is exothermic, i.e. produces heat rather than requiring it. Such recipes generally
     * have a maximum temperature defined, instead of (or as well as) a minimum temperature.
     *
     * @return true if this is an exothermic recipe.
     */
    public abstract boolean isExothermic();

    public final int getInputFluidAmount() {
        return getInputFluid().map(SizedFluidIngredient::amount).orElse(0);
    }

    public final boolean testFluid(FluidStack fluid) {
        return getInputFluid().map(i -> i.test(fluid)).orElse(false);
    }

    public final boolean testItem(ItemStack stack) {
        return getInputItem().map(i -> i.test(stack)).orElse(false);
    }

    public abstract Inputs inputs();

    public abstract Outputs outputs();

    public record Inputs(Optional<SizedFluidIngredient> inputFluid, Optional<Ingredient> inputItem) {
        public static final Codec<Inputs> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                SizedFluidIngredient.FLAT_CODEC.optionalFieldOf("fluid")
                        .forGetter(Inputs::inputFluid),
                Ingredient.CODEC.optionalFieldOf("item")
                        .forGetter(Inputs::inputItem)
        ).apply(builder, Inputs::new));
        public static StreamCodec<RegistryFriendlyByteBuf, Inputs> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(SizedFluidIngredient.STREAM_CODEC), Inputs::inputFluid,
                ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), Inputs::inputItem,
                Inputs::new
        );

        public static Inputs of(@Nullable SizedFluidIngredient inputFluid, @Nullable Ingredient inputItem) {
            return new Inputs(Optional.ofNullable(inputFluid), Optional.ofNullable(inputItem));
        }
    }

    public record Outputs(FluidStack outputFluid, ItemStack outputItem) {
        public static final Codec<Outputs> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                FluidStack.CODEC.optionalFieldOf("fluid_output", FluidStack.EMPTY)
                        .forGetter(Outputs::outputFluid),
                ItemStack.CODEC.optionalFieldOf("item_output", ItemStack.EMPTY)
                        .forGetter(Outputs::outputItem)
        ).apply(builder, Outputs::new));
        public static StreamCodec<RegistryFriendlyByteBuf, Outputs> STREAM_CODEC = StreamCodec.composite(
                FluidStack.STREAM_CODEC, Outputs::outputFluid,
                ItemStack.STREAM_CODEC, Outputs::outputItem,
                Outputs::new
        );
    }

}
