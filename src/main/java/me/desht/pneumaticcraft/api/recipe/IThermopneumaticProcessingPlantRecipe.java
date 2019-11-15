package me.desht.pneumaticcraft.api.recipe;

import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import me.desht.pneumaticcraft.common.recipes.BasicThermopneumaticProcessingPlantRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IThermopneumaticProcessingPlantRecipe extends IModRecipe {
    /**
     * Check if this recipe matches the given input fluid and item.  This does not take any required temperature and
     * pressure into account.
     *
     * @param inputFluid the input fluid
     * @param inputItem the input item
     * @return true if this recipe matches
     */
    boolean matches(FluidStack inputFluid, ItemStack inputItem);

    /**
     * Get the temperature range required for processing to occur.
     *
     * @return temperature range, in degrees Kelvin.
     */
    TemperatureRange getOperatingTemperature();

    /**
     * Get the minimum pressure required for processing to occur.
     *
     * @return pressure, in bar.
     */
    float getRequiredPressure();

    /**
     * Get the base heat used each time the processing plant produces some output.  This value will be subtracted from
     * the machine's current heat.  This could be negative if the recipe is an exothermic recipe, i.e. it produces
     * heat; see {@link #isExothermic()}.
     *
     * @return heat used
     */
    default double heatUsed() {
        TemperatureRange range = getOperatingTemperature();
        if (range.isAny()) return 0;  // don't care about temperature; don't consume or produce heat

        double ambient = HeatExchangerLogicAmbient.BASE_AMBIENT_TEMP; // TODO get the actual machine's ambient temp
        double used;
        if (range.getMin() > ambient) {
            used = (range.getMin() - ambient) / 10D;
        } else if (range.getMax() < ambient) {
            used = (ambient - range.getMax()) / 10D;
        } else {
            if (isExothermic()) {
                used = (range.getMax() - ambient) / 10D;
            } else {
                used = (ambient - range.getMin()) / 10D;
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
    default int airUsed() {
        return (int) (50 * getRequiredPressure());
    }

    Ingredient getInputItem();

    FluidStack getInputFluid();

    FluidStack getOutputFluid();

    /**
     * Check if this recipe is exothermic, i.e. produces heat rather than requiring it. Such recipes generally
     * have a maximum temperature defined, instead of (or as well as) a minimum temperature.
     *
     * @return true if this is an exothermic recipe.
     */
    boolean isExothermic();

    /**
     * Create a standard Thermopneumatic Processing Plant recipe.  Such recipes generally have a minimum temperature
     * requirement.
     *
     * @param id a unique ID for this recipe
     * @param inputFluid the input fluid
     * @param inputItem the input ingredient, may be null
     * @param outputFluid the output fluid
     * @param operatingTemperature the operating temperature range
     * @param requiredPressure the minimum pressure required (pass 0 if no specific pressure is required)
     * @return a Thermopneumatic Processing Plant recipe (pass {@link TemperatureRange#any()} if no specific temperature
     * is required)
     */
    static IThermopneumaticProcessingPlantRecipe basicRecipe(ResourceLocation id, @Nonnull FluidStack inputFluid, @Nullable Ingredient inputItem,
                                                             FluidStack outputFluid, TemperatureRange operatingTemperature, float requiredPressure) {
        return new BasicThermopneumaticProcessingPlantRecipe(id, inputFluid, inputItem, outputFluid, operatingTemperature, requiredPressure, false);
    }

    /**
     * Create a standard exothermic Thermopneumatic Processing Plant recipe.  Exothermic recipes produce heat rather than
     * consume it.  See {@link #isExothermic()}.
     *
     * @param id a unique ID for this recipe
     * @param inputFluid the input fluid
     * @param inputItem the input ingredient, may be null
     * @param outputFluid the output fluid
     * @param operatingTemperature the operating temperature range
     * @param requiredPressure the minimum pressure required (pass 0 if no specific pressure is required)
     * @return a Thermopneumatic Processing Plant recipe (pass {@link TemperatureRange#any()} if no specific temperature is required)
     */
    static IThermopneumaticProcessingPlantRecipe basicExothermicRecipe(ResourceLocation id, @Nonnull FluidStack inputFluid, @Nullable Ingredient inputItem,
                                                             FluidStack outputFluid, TemperatureRange operatingTemperature, float requiredPressure) {
        return new BasicThermopneumaticProcessingPlantRecipe(id, inputFluid, inputItem, outputFluid, operatingTemperature, requiredPressure, true);
    }

    /**
     * Used for client-side sync'ing of recipes: do not call directly!
     * @param buf a packet buffer
     * @return a deserialised recipe
     */
    static IThermopneumaticProcessingPlantRecipe read(PacketBuffer buf) {
        ResourceLocation id = buf.readResourceLocation();
        TemperatureRange range = TemperatureRange.of(buf.readVarInt(), buf.readVarInt());
        float pressure = buf.readFloat();
        Ingredient input = Ingredient.read(buf);
        FluidStack fluidIn = FluidStack.readFromPacket(buf);
        FluidStack fluidOut = FluidStack.readFromPacket(buf);
        boolean exothermic = buf.readBoolean();
        return new BasicThermopneumaticProcessingPlantRecipe(id, fluidIn, input, fluidOut, range, pressure, exothermic);
    }

}
