package me.desht.pneumaticcraft.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Get an instance of this via {@link PneumaticRegistry.IPneumaticCraftInterface#getRecipeRegistry()}.  Note that all
 * recipe types may also be manipulated via CraftTweaker.
 *
 * @author MineMaarten
 */
public interface IPneumaticRecipeRegistry {
    /**
     * @param requiredFluid       fluid input; may be null
     * @param requiredItem        item input; may be ItemStack.EMPTY
     * @param requiredTemperature in degrees Kelvin
     * @param requiredPressure    required pressure
     * @param output              the output of the recipe; must not be null
     */
    void registerThermopneumaticProcessingPlantRecipe(FluidStack requiredFluid, ItemStack requiredItem, FluidStack output, double requiredTemperature, float requiredPressure);

    /**
     * Allows for registry of a recipe which allows for all your custom needs.
     *
     * @param recipe custom thermopneumatic recipe implementation
     */
    void registerThermopneumaticProcessingPlantRecipe(IThermopneumaticProcessingPlantRecipe recipe);

    /**
     * Add a recipe that needs an item to be drilled in an Assembly set-up to get the output.
     *
     * @param input  valid types: Block, Item, ItemStack
     * @param output valid types: Block, Item, ItemStack
     */
    void addAssemblyDrillRecipe(Object input, Object output);

    /**
     * Add a recipe that needs an item to be lasered in an Assembly set-up to get the output.
     *
     * @param input  valid types: Block, Item, ItemStack
     * @param output valid types: Block, Item, ItemStack
     */
    void addAssemblyLaserRecipe(Object input, Object output);

    /**
     * Adds a recipe to the Pressure Chamber.
     *
     * @param input array of input items
     * @param pressureRequired negative pressures for negative pressure needs.
     * @param output array of output items
     */
    void registerPressureChamberRecipe(ItemIngredient[] input, float pressureRequired, ItemStack[] output);

    /**
     * Allows for registry of a recipe which allows for all your custom needs.
     *
     * @param recipe a custom pressure chamber recipe implementation
     */
    void registerPressureChamberRecipe(IPressureChamberRecipe recipe);

    /**
     * Add an explosion crafting recipe. Any explosion will convert the input item into the output item, with the given
     * (average) loss rate.
     *
     * @param input the input item stack
     * @param output the resulting item stack
     * @param lossRate percentage of input items lost, on average
     */
    void registerExplosionCraftingRecipe(ItemStack input, ItemStack output, int lossRate);

    /**
     * Add an explosion crafting recipe. Any explosion will convert the input item into the output item, with the given
     * (average) loss rate.
     *
     * @param oreDictKey ore dictionary key for the input item
     * @param output the resulting item stack
     * @param lossRate percentage of input items lost, on average
     */
    void registerExplosionCraftingRecipe(String oreDictKey, ItemStack output, int lossRate);

    /**
     * Adds an Amadron offer. Both the input and output can either be ItemStack or FluidStack.
     * This is a default offer, meaning it will be put in a clean config load. After that the user can change it at will
     * to remove this added recipe. It's a static offer, meaning if it exists in the instance, it will be there forever
     * (like the Emerald --> PCB Blueprint offer).
     *
     * @param input an ItemStack or FluidStack
     * @param output an ItemStack or FluidStack
     * @throws IllegalArgumentException if the input and output are not both either an ItemStack or FluidStack
     */
    void registerDefaultStaticAmadronOffer(Object input, Object output);

    /**
     * Adds an Amadron offer. Both the input and output can either be ItemStack or FluidStack.
     * This is a default offer, meaning it will be put in a clean config load. After that the user can change it at will
     * to remove this added recipe. It's a periodic offer, meaning it will be shuffled (by default) once per day between
     * other periodic offers, like the villager trade offers.
     *
     * @param input an ItemStack or FluidStack
     * @param output an ItemStack or FluidStack
     * @throws IllegalArgumentException if the input and output are not both either an ItemStack or FluidStack
     */
    void registerDefaultPeriodicAmadronOffer(Object input, Object output);

    /**
     * Adds a behaviour for when an inventory is framed with a Heat Frame, and is cooled below 0 degrees C. If the
     * input item is a container item it will be returned as well.
     *
     * @param input the input ingredient
     * @param output the returned item
     */
    void registerHeatFrameCoolRecipe(ItemIngredient input, ItemStack output);
    
    /**
     * Adds a recipe to the Refinery. Multiple recipes for the same input can be defined, the most suitable recipe
     * depending on the size of the Refinery is used.
     *  
     * @param input fluid and amount drained per work (default: 10)
     * @param outputs the output fluids and amount produced per work
     */
	void registerRefineryRecipe(FluidStack input, FluidStack... outputs);

    /**
     * Adds a recipe to the Refinery. Multiple recipes for the same input can be defined; the recipe which produces the
     * most output for the Refinery's current size will be used.
     *
     * @param minimumTemperature the minimum temperature (in Kelvin) for refining to start
     * @param input the input fluid and amount consumed per work cycle
     * @param outputs the output fluids and amount produced per work cycle
     */
    void registerRefineryRecipe(int minimumTemperature, FluidStack input, FluidStack... outputs);

    /**
     * Add a melting and/or solidifying recipe to the Plastic Mixer.
     *
     * @param fluidPlastic the fluid; the FluidStack must include the amount required
     * @param solidPlastic the solid item; this should support dye coloring as metadata (or ignore metadata), or nonsensical results may ensue
     * @param temperature the temperature (Kelvin) at which the item should melt (irrelevant if allowMelting is false)
     * @param allowMelting should this recipe allow melting of items to fluids?
     * @param allowSolidifying should this recipe allow solidifying of fluids to items?
     */
    void registerPlasticMixerRecipe(FluidStack fluidPlastic, ItemStack solidPlastic, int temperature, boolean allowMelting, boolean allowSolidifying);
}
