package me.desht.pneumaticcraft.common.recipes;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.item.ItemStack;

public class HeatFrameCoolingRecipe {
	public static List<HeatFrameCoolingRecipe> recipes = new ArrayList<>();
	
	public final Object input;
	public final ItemStack output;
	
	public HeatFrameCoolingRecipe(ItemStack input, ItemStack output) {
		this.input = input;
		this.output = output;
	}
	
	public HeatFrameCoolingRecipe(Object input, ItemStack output) {
        if (input == null) throw new NullPointerException("Input can't be null!");
        if (output == null) throw new NullPointerException("Output can't be null!");
        
        if (!(input instanceof ItemStack) && !(input instanceof Pair))
            throw new IllegalArgumentException("Input needs to be of type ItemStack or org.apache.commons.lang3.tuple.Pair<String, Integer>. Violating object: " + input);
        if (input instanceof Pair) {
            Pair pair = (Pair) input;
            if (!(pair.getKey() instanceof String))
                throw new IllegalArgumentException("Pair key needs to be a String (ore dict entry)");
            if (!(pair.getValue() instanceof Integer))
                throw new IllegalArgumentException("Pair value needs to be an Integer (amount)");
        }
        
        this.input = input;
        this.output = output;
	}
}
