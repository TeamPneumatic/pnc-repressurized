package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AssemblyRecipe {

    public static final List<AssemblyRecipe> drillRecipes = new ArrayList<>();
    public static final List<AssemblyRecipe> laserRecipes = new ArrayList<>();
    public static final List<AssemblyRecipe> drillLaserRecipes = new ArrayList<>();

    private final ItemStack input;
    private final ItemStack output;
    private final ItemStack programStack;

    public AssemblyRecipe(ItemStack input, ItemStack output, ItemStack programStack) {
        this.input = input;
        this.output = output;
        this.programStack = programStack;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public ItemStack getProgramStack() {
        return programStack;
    }

    static void addDrillRecipe(Object input, Object output) {
        PneumaticRegistry.getInstance().getRecipeRegistry().addAssemblyDrillRecipe(input, output);
    }

    static void addLaserRecipe(Object input, Object output) {
        PneumaticRegistry.getInstance().getRecipeRegistry().addAssemblyLaserRecipe(input, output);
    }

    public static AssemblyRecipe findRecipeForOutput(ItemStack result) {
        for (AssemblyRecipe recipe : AssemblyRecipe.drillLaserRecipes) {
            if (ItemStack.areItemsEqual(result, recipe.getOutput())) {
                return recipe;
            }
        }
        for (AssemblyRecipe recipe : AssemblyRecipe.drillRecipes) {
            if (ItemStack.areItemsEqual(result, recipe.getOutput())) {
                return recipe;
            }
        }
        for (AssemblyRecipe recipe : AssemblyRecipe.laserRecipes) {
            if (ItemStack.areItemsEqual(result, recipe.getOutput())) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * Work out which recipes can be chained.  E.g. if laser recipe makes B from A, and drill recipe makes C from B,
     * then add a laser/drill recipe to make C from A. Takes into account the number of inputs & outputs from each step.
     */
    public static void calculateAssemblyChain() {
        ItemStack drillLaserProgram = new ItemStack(ModItems.ASSEMBLY_PROGRAM_LASER_DRILL);

        for (AssemblyRecipe firstRecipe : drillRecipes) {
            for (AssemblyRecipe secondRecipe : laserRecipes) {
                if (firstRecipe.getOutput().isItemEqual(secondRecipe.getInput())
                        && firstRecipe.getOutput().getCount() % secondRecipe.getInput().getCount() == 0
                        && secondRecipe.getOutput().getMaxStackSize() >= secondRecipe.getOutput().getCount() * (firstRecipe.getOutput().getCount() / secondRecipe.getInput().getCount())) {
                    ItemStack output = secondRecipe.getOutput().copy();
                    output.setCount(output.getCount() * (firstRecipe.getOutput().getCount() / secondRecipe.getInput().getCount()));
                    drillLaserRecipes.add(new AssemblyRecipe(firstRecipe.getInput(), output, drillLaserProgram));
                }
            }
        }
    }
}
