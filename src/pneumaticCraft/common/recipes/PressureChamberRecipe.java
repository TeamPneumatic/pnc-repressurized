/*
 * This class is highly based on Buildcraft's AssemblyRecipe class.
 */

package pneumaticCraft.common.recipes;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import pneumaticCraft.api.recipe.IPressureChamberRecipe;

public class PressureChamberRecipe{
    public static LinkedList<PressureChamberRecipe> chamberRecipes = new LinkedList<PressureChamberRecipe>();
    public static LinkedList<IPressureChamberRecipe> specialRecipes = new LinkedList<IPressureChamberRecipe>();

    public final ItemStack[] input;
    public final ItemStack[] output;
    public final float pressure;
    public final boolean outputAsBlock;

    public PressureChamberRecipe(ItemStack[] input, float pressureRequired, ItemStack[] output, boolean outputAsBlock){
        this.input = input;
        this.output = output;
        pressure = pressureRequired;
        this.outputAsBlock = outputAsBlock;
    }

    public boolean canBeDone(ItemStack[] items){
        for(ItemStack in : input) {

            if(in == null) {
                continue;
            }

            int found = 0; // Amount of ingredient found in inventory

            for(ItemStack item : items) {
                if(item == null) {
                    continue;
                }

                if(item.isItemEqual(in)) {
                    found += item.stackSize; // Adds quantity of stack to amount
                                             // found
                }
            }
            // System.out.println("found amount: " + found +
            // ", input stacksize: " + in.stackSize);
            if(found < in.stackSize) return false; // Return false if the amount
                                                   // of ingredient found
                                                   // is not enough
        }

        return true;
    }
}
