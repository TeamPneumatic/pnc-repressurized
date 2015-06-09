package pneumaticCraft.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Interface accessed via PneumaticRegistry.getInstance().getRecipeRegistry(), used to register recipes to PneumaticCraft.
 * @author MineMaarten
 */
public interface IPneumaticRecipeRegistry{
    /**
     * 
     * @param requiredFluid can be null
     * @param requiredItem can be null
     * @param requiredTemperature in degrees Kelvin.
     * @param requiredPressure required pressure.
     * @param output the output of the recipe. cannot be null.
     */
    public void registerThermopneumaticProcessingPlantRecipe(FluidStack requiredFluid, ItemStack requiredItem, FluidStack output, double requiredTemperature, float requiredPressure);

    /**
     * Allows for registry of a recipe which allows for all your custom needs.
     * @param recipe
     */
    public void registerThermopneumaticProcessingPlantRecipe(IThermopneumaticProcessingPlantRecipe recipe);

    /**
     * Add a recipe that needs an item to be drilled in an Assembly set-up to get the output.
     * @param input valid types: Block, Item, ItemStack
     * @param output valid types: Block, Item, ItemStack
     */
    public void addAssemblyDrillRecipe(Object input, Object output);

    /**
     * Add a recipe that needs an item to be lasered in an Assembly set-up to get the output.
     * @param input valid types: Block, Item, ItemStack
     * @param output valid types: Block, Item, ItemStack
     */
    public void addAssemblyLaserRecipe(Object input, Object output);

    /**
     * Adds a recipe to the Pressure Chamber.
     * @param input
     * @param pressureRequired negative pressures for negative pressure needs.
     * @param output
     */
    public void registerPressureChamberRecipe(ItemStack[] input, float pressureRequired, ItemStack[] output);

    /**
     * Allows for registry of a recipe which allows for all your custom needs.
     * @param recipe
     */
    public void registerPressureChamberRecipe(IPressureChamberRecipe recipe);
}
