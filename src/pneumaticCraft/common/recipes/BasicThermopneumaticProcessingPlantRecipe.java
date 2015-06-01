package pneumaticCraft.common.recipes;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.api.recipe.IThermopneumaticProcessingPlantRecipe;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class BasicThermopneumaticProcessingPlantRecipe implements IThermopneumaticProcessingPlantRecipe{

    private final FluidStack inputLiquid, outputLiquid;
    private final ItemStack inputItem;

    public BasicThermopneumaticProcessingPlantRecipe(FluidStack inputLiquid, ItemStack inputItem,
            FluidStack outputLiquid){
        this.inputItem = inputItem;
        this.inputLiquid = inputLiquid;
        this.outputLiquid = outputLiquid;
    }

    @Override
    public boolean isValidRecipe(FluidStack inputTank, ItemStack inputItem){
        if(inputLiquid != null) {
            if(inputTank == null) return false;
            if(inputTank.getFluid() != inputLiquid.getFluid()) return false;
            if(inputTank.amount < inputLiquid.amount) return false;
        }
        if(this.inputItem != null) {
            if(inputItem == null) return false;
            if(!inputItem.isItemEqual(this.inputItem) && !PneumaticCraftUtils.isSameOreDictStack(inputItem, this.inputItem)) return false;
            if(inputItem.stackSize < this.inputItem.stackSize) return false;
        }
        return true;
    }

    @Override
    public FluidStack getRecipeOutput(FluidStack inputTank, ItemStack inputItem){
        return outputLiquid;
    }

    @Override
    public void useRecipeItems(FluidStack inputTank, ItemStack inputItem){
        if(inputLiquid != null) inputTank.amount -= inputLiquid.amount;
        if(this.inputItem != null) inputItem.stackSize -= this.inputItem.stackSize;
    }

}
