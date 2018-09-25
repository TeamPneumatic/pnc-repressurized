package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CraftTweaker;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.Helper;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.ListAddition;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.ListRemoval;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.RemoveAllRecipes;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.List;

@ZenClass("mods.pneumaticcraft.assembly")
@ZenRegister
public class Assembly {
	public static final String name = "PneumaticCraft Assembly";
	public static final String nameLaser = "PneumaticCraft Assembly (Laser)";
	public static final String nameDrill = "PneumaticCraft Assembly (Drill)";
	public static final String nameDrillLaser = "PneumaticCraft Assembly (Drill Laser)";
	
    @ZenMethod
    public static void addDrillRecipe(IItemStack input, IItemStack output) {
        addRecipe(nameDrill, input, output, AssemblyRecipe.drillRecipes);
    }

    @ZenMethod
    public static void addLaserRecipe(IItemStack input, IItemStack output) {
        addRecipe(nameLaser, input, output, AssemblyRecipe.laserRecipes);
    }

    @ZenMethod
    public static void addDrillLaserRecipe(IItemStack input, IItemStack output) {
        addRecipe(nameDrillLaser, input, output, AssemblyRecipe.drillLaserRecipes);
    }
    
    @ZenMethod
    public static void removeDrillRecipe(IIngredient output) {
        removeRecipe(nameDrill, AssemblyRecipe.drillRecipes, output);
    }

    @ZenMethod
    public static void removeAllDrillRecipes() {
        CraftTweaker.REMOVALS.add(new RemoveAllRecipes<>(nameDrill, AssemblyRecipe.drillRecipes));
    }

    @ZenMethod
    public static void removeLaserRecipe(IIngredient output) {
        removeRecipe(nameLaser, AssemblyRecipe.laserRecipes, output);
    }
    
    @ZenMethod
    public static void removeAllLaserRecipes() {
        CraftTweaker.REMOVALS.add(new RemoveAllRecipes<>(nameLaser, AssemblyRecipe.laserRecipes));
    }

    @ZenMethod
    public static void removeDrillLaserRecipe(IIngredient output) {
        removeRecipe(nameDrillLaser, AssemblyRecipe.drillLaserRecipes, output);
    }
    
    @ZenMethod
    public static void removeAllDrillLaserRecipes() {
        CraftTweaker.REMOVALS.add(new RemoveAllRecipes<>(nameDrillLaser, AssemblyRecipe.drillLaserRecipes));
    }
    
    @ZenMethod
    public static void removeAllRecipes() {
    	removeAllDrillRecipes();
    	removeAllLaserRecipes();
    	removeAllDrillLaserRecipes();
    }
    
    public static void addRecipe(String name, IItemStack input, IItemStack output, List<AssemblyRecipe> list) {
        if(input == null || output == null) {
            Helper.logError(String.format("Required parameters missing for %s Recipe.", name));
            return;
        }
        
        CraftTweaker.ADDITIONS.add(new Add(name, new AssemblyRecipe(Helper.toStack(input), Helper.toStack(output)), list));
    }
        
    public static void removeRecipe(String name, List<AssemblyRecipe> list, IIngredient output) {
        CraftTweaker.REMOVALS.add(new Remove(name, list, output));
    }
    
    private static class Add extends ListAddition<AssemblyRecipe> {
        public Add(String name, AssemblyRecipe recipe, List<AssemblyRecipe> list) {
            super(name, list, recipe);
        }
    }
    
    private static class Remove extends ListRemoval<AssemblyRecipe> {
    	private final IIngredient output;
    	
        public Remove(String name, List<AssemblyRecipe> list, IIngredient output) {
            super(name, list);
            this.output = output;
        }
        
        @Override
        public void apply() {
        	addRecipes();
        	super.apply();
        }

        private void addRecipes() {
            for (AssemblyRecipe r : recipes) {
                if (Helper.matches(output,  Helper.toIItemStack(r.getOutput()))) {
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
