package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.oredict.IOreDictEntry;
import me.desht.pneumaticcraft.api.recipe.ItemIngredient;
import me.desht.pneumaticcraft.common.recipes.HeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CraftTweaker;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.Helper;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.ListAddition;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.ListRemoval;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.RemoveAllRecipes;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.pneumaticcraft.heatframecooling")
@ZenRegister
public class HeatFrameCooling {
	public static final String name = "PneumaticCraft Heat Frame Cooling";
	
	@ZenMethod
	public static void addRecipe(IOreDictEntry input, IItemStack output) {
		CraftTweaker.ADDITIONS.add(new Add(new HeatFrameCoolingRecipe(Helper.toItemIngredient(input), Helper.toStack(output))));
	}
	
    @ZenMethod
    public static void addRecipe(IItemStack input, IItemStack output)
    {
    	CraftTweaker.ADDITIONS.add(new Add(new HeatFrameCoolingRecipe(new ItemIngredient(Helper.toStack(input)), Helper.toStack(output))));
    }
    
    @ZenMethod
    public static void removeRecipe(IIngredient output)
    {
    	CraftTweaker.REMOVALS.add(new Remove(output));
    }
    
    @ZenMethod
    public static void removeAllRecipes() {
        CraftTweaker.REMOVALS.add(new RemoveAllRecipes<>(HeatFrameCooling.name, HeatFrameCoolingRecipe.recipes));
    }   

    private static class Add extends ListAddition<HeatFrameCoolingRecipe> {
        public Add(HeatFrameCoolingRecipe recipe) {
            super(PressureChamber.name, HeatFrameCoolingRecipe.recipes, recipe);
        }
    }
    
    private static class Remove extends ListRemoval<HeatFrameCoolingRecipe> {
    	private final IIngredient output;
    	
        public Remove(IIngredient output) {
            super(HeatFrameCooling.name, HeatFrameCoolingRecipe.recipes);
            this.output = output;
        }
        
        @Override
        public void apply() {
        	addRecipes();
        	super.apply();
        }

        private void addRecipes() {
            for (HeatFrameCoolingRecipe r : recipes) {
                if (Helper.matches(output,  Helper.toIItemStack(r.output))) {
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
			return String.format("Removing %s Recipe(s) for %s", this.name, Helper.getStackDescription(output));
		}
    }

    
}
