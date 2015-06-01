package pneumaticCraft.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IThermopneumaticProcessingPlantRecipe{
    public boolean isValidRecipe(FluidStack inputTank, ItemStack inputItem);

    public FluidStack getRecipeOutput(FluidStack inputTank, ItemStack inputItem);

    public void useRecipeItems(FluidStack inputTank, ItemStack inputItem);
}
