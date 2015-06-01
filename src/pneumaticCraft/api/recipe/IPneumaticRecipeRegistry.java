package pneumaticCraft.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IPneumaticRecipeRegistry{
    public void registerThermopneumaticProcessingPlantRecipe(IThermopneumaticProcessingPlantRecipe recipe);

    public void registerThermopneumaticProcessingPlantRecipe(FluidStack requiredFluid, ItemStack requiredItem, FluidStack output);
}
