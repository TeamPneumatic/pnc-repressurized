package pneumaticCraft.api.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

/**
 * @Deprecated Access via {@link pneumaticCraft.api.recipe.IPneumaticRecipeRegistry}
 */
@Deprecated
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

}
