package me.desht.pneumaticcraft.api.crafting;

import me.desht.pneumaticcraft.api.crafting.recipe.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Get an instance of this via {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#getRecipeRegistry()}.
 * <p>
 * Note that machine recipes are now loaded from datapacks, and via an event: {@link RegisterMachineRecipesEvent}.
 *
 * @author MineMaarten, desht
 */
public interface IPneumaticRecipeRegistry {
    /**
     * Register a recipe serializer for the given recipe type, which is the value of {@link IModRecipe#getRecipeType()}.
     * The serializer will be used for loading recipes from JSON data packs, as well as sync'ing recipes from server
     * to client.
     *
     * @param recipeType a recipe type resource location
     * @param serializer a serializer for this recipe type
     */
    void registerSerializer(ResourceLocation recipeType, Supplier<IModRecipeSerializer<? extends IModRecipe>> serializer);

    /**
     * Create a standard item lasering recipe.  See {@link me.desht.pneumaticcraft.api.crafting.StackedIngredient} if
     * you need a recipe taking multiples of an input item.
     *
     * @param id a unique recipe ID
     * @param input the input ingredient
     * @param output the output item
     * @return a lasering recipe
     */
    IAssemblyRecipe assemblyLaserRecipe(ResourceLocation id, @Nonnull Ingredient input, @Nonnull ItemStack output);

    /**
     * Create a standard item drilling recipe.  See {@link me.desht.pneumaticcraft.api.crafting.StackedIngredient} if
     * you need a recipe taking multiples of an input item.
     *
     * @param id a unique recipe ID
     * @param input the input ingredient
     * @param output the output item
     * @return a drilling recipe
     */
    IAssemblyRecipe assemblyDrillRecipe(ResourceLocation id, @Nonnull Ingredient input, @Nonnull ItemStack output);

    /**
     * Create a basic explosion crafting recipe.  This uses in-world explosions to convert nearby items on the ground
     * (in item entity form) to one or more other items.  See {@link me.desht.pneumaticcraft.api.crafting.StackedIngredient}
     * if you need a recipe taking multiples of an input item.
     *
     * @param id unique ID for the recipe
     * @param input the input ingredient
     * @param lossRate the average item loss rate, as a percentage
     * @param outputs the output items
     * @return a basic Explosion Crafting recipe
     */
    IExplosionCraftingRecipe explosionCraftingRecipe(ResourceLocation id, Ingredient input, int lossRate, ItemStack... outputs);

    /**
     * Create a standard Heat Frame cooling recipe.
     *
     * @param id unique ID for the recipe
     * @param input the input ingredient
     * @param temperature the temperature (Kelvin) below which the cooling process occurs
     * @param output the output item
     * @return a basic Heat Frame cooling recipe
     */
    IHeatFrameCoolingRecipe heatFrameCoolingRecipe(ResourceLocation id, Ingredient input, int temperature, ItemStack output);

    /**
     * Create a standard Pressure Chamber recipe. See also {@link StackedIngredient}, which may be helpful if you
     * want to add a recipe taking multiples of the same input item.
     *
     * @param id unique recipe ID
     * @param inputs a list of input ingredients
     * @param pressureRequired the pressure require (this is a minimum if positive, and a maximum if negative)
     * @param outputs the output item(s)
     * @return a recipe suitable for adding via {@link RegisterMachineRecipesEvent#getPressureChamber()}
     */
    IPressureChamberRecipe pressureChamberRecipe(ResourceLocation id, List<Ingredient> inputs, float pressureRequired, ItemStack... outputs);

    /**
     * Create a standard Refinery recipe.  Note that multiple recipes with the same input fluid may exist, provided that
     * each recipe has a different number of output fluids; the Refinery will use the recipe with the largest number
     * of outputs, limited by the number of output tanks in the Refinery multiblock.
     *
     * @param id unique ID for this recipe
     * @param input the input fluid
     * @param operatingTemp a temperature range required for the recipe to craft
     * @param outputs the output fluids
     * @return a basic Refinery recipe
     */
    IRefineryRecipe refineryRecipe(ResourceLocation id, FluidIngredient input, TemperatureRange operatingTemp, FluidStack... outputs);

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
    IThermopneumaticProcessingPlantRecipe thermoPlantRecipe(
            ResourceLocation id, @Nonnull FluidIngredient inputFluid, @Nullable Ingredient inputItem,
            FluidStack outputFluid, TemperatureRange operatingTemperature, float requiredPressure);

    /**
     * Create a standard exothermic Thermopneumatic Processing Plant recipe.  Exothermic recipes produce heat rather than
     * consume it.  See {@link IThermopneumaticProcessingPlantRecipe#isExothermic()}.
     *
     * @param id a unique ID for this recipe
     * @param inputFluid the input fluid
     * @param inputItem the input ingredient, may be null
     * @param outputFluid the output fluid
     * @param operatingTemperature the operating temperature range
     * @param requiredPressure the minimum pressure required (pass 0 if no specific pressure is required)
     * @return a Thermopneumatic Processing Plant recipe (pass {@link TemperatureRange#any()} if no specific temperature is required)
     */
    IThermopneumaticProcessingPlantRecipe exothermicThermoPlantRecipe(
            ResourceLocation id, @Nonnull FluidIngredient inputFluid, @Nullable Ingredient inputItem,
            FluidStack outputFluid, TemperatureRange operatingTemperature, float requiredPressure);
}
