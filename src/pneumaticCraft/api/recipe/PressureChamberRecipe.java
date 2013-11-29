package pneumaticCraft.api.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

public class PressureChamberRecipe{
    public static List<PressureChamberRecipe> chamberRecipes = new ArrayList<PressureChamberRecipe>();
    public static List<IPressureChamberRecipe> specialRecipes = new ArrayList<IPressureChamberRecipe>();

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

    public boolean canBeCompressed(ItemStack[] items){
        for(ItemStack in : input) {
            if(in != null) {
                int amount = 0;
                for(ItemStack item : items) {
                    if(item != null && item.isItemEqual(in)) amount += item.stackSize;
                }
                if(amount < in.stackSize) return false;
            }
        }

        return true;
    }
}
