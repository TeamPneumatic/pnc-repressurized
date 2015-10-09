package pneumaticCraft.common.recipes;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class RecipeFluid implements IRecipe{

    private final ShapedOreRecipe recipe;
    private final int fluidIndex;
    private final FluidStack fluidStack;
    private final ItemStack originalStack;

    public RecipeFluid(ShapedOreRecipe recipe, int fluidIndex){
        this.recipe = recipe;
        originalStack = (ItemStack)recipe.getInput()[fluidIndex];
        fluidStack = FluidContainerRegistry.getFluidForFilledItem(originalStack);
        if(fluidStack == null) throw new IllegalArgumentException("Recipe doesn't have fluid item at index " + fluidIndex + ". Item: " + originalStack);
        this.fluidIndex = fluidIndex;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world){
        if(fluidIndex >= inv.getSizeInventory()) return false;
        ItemStack stack = inv.getStackInSlot(fluidIndex);
        FluidStack otherFluid = FluidContainerRegistry.getFluidForFilledItem(stack);
        if(otherFluid != null && otherFluid.isFluidEqual(fluidStack) && otherFluid.amount == fluidStack.amount) {
            recipe.getInput()[fluidIndex] = stack.copy();
            boolean matches = recipe.matches(inv, world);
            recipe.getInput()[fluidIndex] = originalStack;
            return matches;
        } else {
            return false;
        }
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv){
        return recipe.getCraftingResult(inv);
    }

    @Override
    public int getRecipeSize(){
        return recipe.getRecipeSize();
    }

    @Override
    public ItemStack getRecipeOutput(){
        return recipe.getRecipeOutput();
    }

}
