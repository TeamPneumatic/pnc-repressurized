package me.desht.pneumaticcraft.common.recipes;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class RecipeFluid extends AbstractRecipe {

    private final ShapedOreRecipe recipe;
    private final int fluidIndex;
    private final FluidStack fluidStack;
    private final ItemStack originalStack;
    private static int recipeIndex = 0;

    public RecipeFluid(ShapedOreRecipe recipe, int fluidIndex) {
        super("fluid_recipe_" + recipeIndex++);
        this.recipe = recipe;
        Ingredient ingredient = recipe.getIngredients().get(fluidIndex);
        originalStack = ingredient.getMatchingStacks()[0];
        fluidStack = FluidUtil.getFluidContained(originalStack);
        if (fluidStack == null)
            throw new IllegalArgumentException("Recipe doesn't have fluid item at index " + fluidIndex + ". Item: " + originalStack);
        this.fluidIndex = fluidIndex;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        if (fluidIndex >= inv.getSizeInventory()) return false;
        ItemStack stack = inv.getStackInSlot(fluidIndex);
        FluidStack otherFluid = FluidUtil.getFluidContained(stack);
        if (otherFluid != null && otherFluid.isFluidEqual(fluidStack) && otherFluid.amount == fluidStack.amount) {
//            recipe.getInput()[fluidIndex] = stack.copy();
            recipe.getIngredients().set(fluidIndex, Ingredient.fromStacks(stack));
            boolean matches = recipe.matches(inv, world);
            recipe.getIngredients().set(fluidIndex, Ingredient.fromStacks(originalStack));
//            recipe.getInput()[fluidIndex] = originalStack;
            return matches;
        } else {
            return false;
        }
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return recipe.getCraftingResult(inv);
    }

    @Override
    public boolean canFit(int width, int height) {
        return true;
    }

//    @Override
//    public int getRecipeSize() {
//        return recipe.getRecipeSize();
//    }

    @Override
    public ItemStack getRecipeOutput() {
        return recipe.getRecipeOutput();
    }

}
