package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.common.recipes.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CraftTweaker;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.Helper;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.ListAddition;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.ListRemoval;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.RemoveAllRecipes;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.List;
import java.util.stream.Stream;

@ZenClass("mods.pneumaticcraft.pressurechamber")
@ZenRegister
public class PressureChamber {
	
	public static final String name = "PneumaticCraft Pressure Chamber";
	
	@ZenMethod
	public static void addRecipe(IIngredient[] input, double pressure, IItemStack[] output) {
		CraftTweaker.ADDITIONS.add(new Add(new PressureChamberRecipe.SimpleRecipe(Helper.toItemIngredients(input), (float)pressure, Helper.toStacks(output))));
	}
    
    @ZenMethod
    public static void removeRecipe(IIngredient[] output)
    {
    	CraftTweaker.REMOVALS.add(new Remove(PressureChamberRecipe.recipes, output));
    }
    
    @ZenMethod
    public static void removeAllRecipes() {
        CraftTweaker.REMOVALS.add(new RemoveAllRecipes<>(PressureChamber.name, PressureChamberRecipe.recipes));
    }   
    
    private static class Add extends ListAddition<IPressureChamberRecipe> {
        public Add(IPressureChamberRecipe recipe) {
            super(PressureChamber.name, PressureChamberRecipe.recipes, recipe);
        }
    }
    
    private static class Remove extends ListRemoval<IPressureChamberRecipe> {
    	private final IIngredient[] output;
    	
        public Remove(List<IPressureChamberRecipe> recipes, IIngredient[] output) {
            super(PressureChamber.name, PressureChamberRecipe.recipes, recipes);
            this.output = output;
        }
        
        @Override
        public void apply() {
        	addRecipes();
        	
        	super.apply();
        }
        
        private void addRecipes() {
            for (IPressureChamberRecipe r : recipes) {
            	if(Stream.of(output).allMatch(o -> r.getResult().stream().anyMatch(ro -> Helper.matches(o, Helper.toIItemStack(ro))))) {
            		entries.add(r);
            	}
            }
            
            if(entries.isEmpty()) {
            	Helper.logWarning(String.format("No %s Recipe found for %s. Command ignored!", name, Helper.getStackDescription(output)));
            } else {
            	Helper.logInfo(String.format("Found %d %s Recipe(s) for %s.", entries.size(), name, Helper.getStackDescription(output)));
            }
		}
        
		@Override
		public String describe() {
			return String.format("Removing %s Recipe(s) for %s", this.name, Helper.getStackDescription(output[0]));
		}
    }
}
