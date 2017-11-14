package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import java.util.List;
import java.util.stream.Stream;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import me.desht.pneumaticcraft.common.recipes.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CraftTweaker;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.Helper;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.ListAddition;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.ListRemoval;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.RemoveAllRecipes;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.pneumaticcraft.pressurechamber")
@ZenRegister
public class PressureChamber {
	
	public static final String name = "PneumaticCraft Pressure Chamber";
	
	@ZenMethod
	public static void addRecipe(IIngredient[] input, double pressure, IItemStack[] output) {
		CraftTweaker.ADDITIONS.add(new Add(new PressureChamberRecipe(Helper.toInput(input), (float)pressure, Helper.toStacks(output))));
	}
    
    @ZenMethod
    public static void removeRecipe(IIngredient[] output)
    {
    	CraftTweaker.REMOVALS.add(new Remove(PressureChamberRecipe.chamberRecipes, output));
    }
    
    @ZenMethod
    public static void removeAllRecipes() {
        CraftTweaker.REMOVALS.add(new RemoveAllRecipes<PressureChamberRecipe>(PressureChamber.name, PressureChamberRecipe.chamberRecipes));
    }   
    
    private static class Add extends ListAddition<PressureChamberRecipe> {
        public Add(PressureChamberRecipe recipe) {
            super(PressureChamber.name, PressureChamberRecipe.chamberRecipes, recipe);
        }
    }
    
    private static class Remove extends ListRemoval<PressureChamberRecipe> {
    	private final IIngredient[] output;
    	
        public Remove(List<PressureChamberRecipe> recipes, IIngredient[] output) {
            super(PressureChamber.name, PressureChamberRecipe.chamberRecipes, recipes);
            this.output = output;
        }
        
        @Override
        public void apply() {
        	addRecipes();
        	
        	super.apply();
        }
        
        private void addRecipes() {
            for (PressureChamberRecipe r : recipes) {
            	
            	if(Stream.of(output).allMatch(o -> Stream.of(r.output).anyMatch(ro -> Helper.matches(o, Helper.toIItemStack(ro))))) {
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
