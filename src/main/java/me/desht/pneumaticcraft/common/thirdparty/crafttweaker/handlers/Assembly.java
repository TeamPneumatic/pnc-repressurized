package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import java.util.List;

import com.blamejared.mtlib.helpers.InputHelper;
import com.blamejared.mtlib.helpers.LogHelper;
import com.blamejared.mtlib.helpers.StackHelper;
import com.blamejared.mtlib.utils.BaseListAddition;
import com.blamejared.mtlib.utils.BaseListRemoval;

import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;
import me.desht.pneumaticcraft.common.recipes.PneumaticRecipeRegistry;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CraftTweaker;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.RemoveAllRecipes;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.pneumaticcraft.assembly")
@ModOnly("mtlib")
@ZenRegister
public class Assembly {
	public static final String name = "PneumaticCraft Assembly";
	public static final String nameLaser = "PneumaticCraft Assembly (Laser)";
	public static final String nameDrill = "PneumaticCraft Assembly (Drill)";
	public static final String nameDrillLaser = "PneumaticCraft Assembly (Drill Laser)";
	
    @ZenMethod
    public static void addDrillRecipe(IItemStack input, IItemStack output) {
        addRecipe(nameDrill, input, output, PneumaticRecipeRegistry.getInstance().drillRecipes);
    }

    @ZenMethod
    public static void addLaserRecipe(IItemStack input, IItemStack output) {
        addRecipe(nameLaser, input, output, PneumaticRecipeRegistry.getInstance().laserRecipes);
    }

    @ZenMethod
    public static void addDrillLaserRecipe(IItemStack input, IItemStack output) {
        addRecipe(nameDrillLaser, input, output, PneumaticRecipeRegistry.getInstance().drillLaserRecipes);
    }
    
    @ZenMethod
    public static void removeDrillRecipe(IIngredient output) {
        removeRecipe(nameDrill, PneumaticRecipeRegistry.getInstance().drillRecipes, output);
    }

    @ZenMethod
    public static void removeAllDrillRecipes() {
        CraftTweaker.REMOVALS.add(new RemoveAllRecipes<AssemblyRecipe>(nameDrill, PneumaticRecipeRegistry.getInstance().drillRecipes));
    }

    @ZenMethod
    public static void removeLaserRecipe(IIngredient output) {
        removeRecipe(nameLaser, PneumaticRecipeRegistry.getInstance().laserRecipes, output);
    }
    
    @ZenMethod
    public static void removeAllLaserRecipes() {
        CraftTweaker.REMOVALS.add(new RemoveAllRecipes<AssemblyRecipe>(nameLaser, PneumaticRecipeRegistry.getInstance().laserRecipes));
    }

    @ZenMethod
    public static void removeDrillLaserRecipe(IIngredient output) {
        removeRecipe(nameDrillLaser, PneumaticRecipeRegistry.getInstance().drillLaserRecipes, output);
    }
    
    @ZenMethod
    public static void removeAllDrillLaserRecipes() {
        CraftTweaker.REMOVALS.add(new RemoveAllRecipes<AssemblyRecipe>(nameDrillLaser, PneumaticRecipeRegistry.getInstance().drillLaserRecipes));
    }
    
    @ZenMethod
    public static void removeAllRecipes() {
    	removeAllDrillRecipes();
    	removeAllLaserRecipes();
    	removeAllDrillLaserRecipes();
    }
    
    public static void addRecipe(String name, IItemStack input, IItemStack output, List<AssemblyRecipe> list) {
        if(input == null || output == null) {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", name));
            return;
        }
        
        CraftTweaker.ADDITIONS.add(new Add(name, new AssemblyRecipe(InputHelper.toStack(input), InputHelper.toStack(output)), list));
    }
    
    private static class Add extends BaseListAddition<AssemblyRecipe> {
        public Add(String name, AssemblyRecipe recipe, List<AssemblyRecipe> list) {
            super(name, list);
            recipes.add(recipe);
        }

        @Override
        public String getRecipeInfo(AssemblyRecipe recipe) {
            return LogHelper.getStackDescription(recipe.getOutput());
        }
    }
    
    public static void removeRecipe(String name, List<AssemblyRecipe> list, IIngredient output) {
        CraftTweaker.REMOVALS.add(new Remove(name, list, output));
    }
    
    private static class Remove extends BaseListRemoval<AssemblyRecipe> {
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
            for (AssemblyRecipe r : list) {
                if (StackHelper.matches(output,  InputHelper.toIItemStack(r.getOutput()))) {
                    recipes.add(r);
                }
            }
            
            if(recipes.isEmpty()) {
            	LogHelper.logWarning(String.format("No %s Recipe found for %s. Command ignored!", name, LogHelper.getStackDescription(output)));
            } else {
            	LogHelper.logInfo(String.format("Found %d %s Recipe(s) for %s.", recipes.size(), name, LogHelper.getStackDescription(output)));
            }
		}

		@Override
        public String getRecipeInfo(AssemblyRecipe recipe) {
            return LogHelper.getStackDescription(recipe.getOutput());
        }
		
		@Override
		public String describe() {
			return String.format("Removing %s Recipe(s) for %s", this.name, LogHelper.getStackDescription(output));
		}
    }
}
