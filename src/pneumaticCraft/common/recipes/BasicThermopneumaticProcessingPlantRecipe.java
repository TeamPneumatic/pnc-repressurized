package pneumaticCraft.common.recipes;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.api.recipe.IThermopneumaticProcessingPlantRecipe;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class BasicThermopneumaticProcessingPlantRecipe implements IThermopneumaticProcessingPlantRecipe{

    private final FluidStack inputLiquid, outputLiquid;
    private final ItemStack inputItem;
    private final float requiredPressure;
    private final double requiredTemperature;

    public BasicThermopneumaticProcessingPlantRecipe(FluidStack inputLiquid, ItemStack inputItem,
            FluidStack outputLiquid, double requiredTemperature, float requiredPressure){
        this.inputItem = inputItem;
        this.inputLiquid = inputLiquid;
        this.outputLiquid = outputLiquid;
        this.requiredTemperature = requiredTemperature;
        this.requiredPressure = requiredPressure;
    }

    @Override
    public boolean isValidRecipe(FluidStack inputTank, ItemStack inputItem){
        if(inputLiquid != null) {
            if(inputTank == null) return false;
            if(!Fluids.areFluidsEqual(inputTank.getFluid(), inputLiquid.getFluid())) return false;
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

    @Override
    public double getRequiredTemperature(FluidStack inputTank, ItemStack inputItem){
        return requiredTemperature;
    }

    @Override
    public float getRequiredPressure(FluidStack inputTank, ItemStack inputItem){
        return requiredPressure;
    }

    @Override
    public double heatUsed(FluidStack inputTank, ItemStack inputItem){
        return (requiredTemperature - 295) / 10D;
    }

    @Override
    public int airUsed(FluidStack inputTank, ItemStack inputItem){
        return (int)(requiredPressure * 50);
    }

    public FluidStack getInputLiquid(){
        return inputLiquid;
    }

    public FluidStack getOutputLiquid(){
        return outputLiquid;
    }

    public ItemStack getInputItem(){
        return inputItem;
    }
}
