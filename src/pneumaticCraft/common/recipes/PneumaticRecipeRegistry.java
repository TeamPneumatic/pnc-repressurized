package pneumaticCraft.common.recipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.api.recipe.IPneumaticRecipeRegistry;
import pneumaticCraft.api.recipe.IThermopneumaticProcessingPlantRecipe;

public class PneumaticRecipeRegistry implements IPneumaticRecipeRegistry{
    public List<IThermopneumaticProcessingPlantRecipe> thermopneumaticProcessingPlantRecipes = new ArrayList<IThermopneumaticProcessingPlantRecipe>();

    private static final PneumaticRecipeRegistry INSTANCE = new PneumaticRecipeRegistry();

    public static PneumaticRecipeRegistry getInstance(){
        return INSTANCE;
    }

    @Override
    public void registerThermopneumaticProcessingPlantRecipe(IThermopneumaticProcessingPlantRecipe recipe){
        if(recipe == null) throw new NullPointerException("Recipe can't be null!");
        thermopneumaticProcessingPlantRecipes.add(recipe);
    }

    @Override
    public void registerThermopneumaticProcessingPlantRecipe(FluidStack requiredFluid, ItemStack requiredItem, FluidStack output){
        if(output == null) throw new NullPointerException("Output can't be null!");
        registerThermopneumaticProcessingPlantRecipe(new BasicThermopneumaticProcessingPlantRecipe(requiredFluid, requiredItem, output));
    }

}
