package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class PressureChamberRecipe {
    public static List<PressureChamberRecipe> chamberRecipes = new ArrayList<>();
    public static List<IPressureChamberRecipe> specialRecipes = new ArrayList<>();

    public final Object[] input;
    public final ItemStack[] output;
    public final float pressure;

    public PressureChamberRecipe(ItemStack[] input, float pressureRequired, ItemStack[] output, boolean outputAsBlock) {
        this.input = input;
        this.output = output;
        pressure = pressureRequired;
    }

    public PressureChamberRecipe(Object[] input, float pressureRequired, ItemStack[] output) {
        for (Object o : input) {
            if (!(o instanceof ItemStack) && !(o instanceof Pair))
                throw new IllegalArgumentException("Input objects need to be of type ItemStack or (Apache's) Pair<String, Integer>. Violating object: " + o);
            if (o instanceof Pair) {
                Pair pair = (Pair) o;
                if (!(pair.getKey() instanceof String))
                    throw new IllegalArgumentException("Pair key needs to be a String (ore dict entry)");
                if (!(pair.getValue() instanceof Integer))
                    throw new IllegalArgumentException("Value key needs to be an Integer (amount)");
            }
        }
        this.input = input;
        this.output = output;
        pressure = pressureRequired;
    }
}
