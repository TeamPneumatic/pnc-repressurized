package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import static com.blamejared.mtlib.helpers.InputHelper.toStacks;

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
import crafttweaker.api.oredict.IOreDictEntry;
import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;
import me.desht.pneumaticcraft.common.recipes.HeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.common.recipes.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CraftTweaker;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.OreDictHelper;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.RemoveAllRecipes;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.pneumaticcraft.heatframecooling")
@ModOnly("mtlib")
@ZenRegister
public class HeatFrameCooling {
	public static final String name = "PneumaticCraft Heat Frame Cooling";
	
	@ZenMethod
	public static void addRecipe(IOreDictEntry input, IItemStack output) {
		CraftTweaker.ADDITIONS.add(new Add(new HeatFrameCoolingRecipe(OreDictHelper.toPair(input), InputHelper.toStack(output))));
	
	}
	
    @ZenMethod
    public static void addRecipe(IItemStack input, IItemStack output)
    {
    	CraftTweaker.ADDITIONS.add(new Add(new HeatFrameCoolingRecipe(InputHelper.toStack(input), InputHelper.toStack(output))));
    }
    
    @ZenMethod
    public static void removeRecipe(IIngredient output)
    {
    	CraftTweaker.REMOVALS.add(new Remove(output));
    }
    
    @ZenMethod
    public static void removeAllRecipes() {
        CraftTweaker.REMOVALS.add(new RemoveAllRecipes<HeatFrameCoolingRecipe>(HeatFrameCooling.name, HeatFrameCoolingRecipe.recipes));
    }   

    private static class Add extends BaseListAddition<HeatFrameCoolingRecipe> {
        public Add(HeatFrameCoolingRecipe recipe) {
            super(PressureChamber.name, HeatFrameCoolingRecipe.recipes);
            recipes.add(recipe);
        }

        @Override
        public String getRecipeInfo(HeatFrameCoolingRecipe recipe) {
            return LogHelper.getStackDescription(recipe.output);
        }
    }
    
    private static class Remove extends BaseListRemoval<HeatFrameCoolingRecipe> {
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
            for (HeatFrameCoolingRecipe r : list) {
                if (StackHelper.matches(output,  InputHelper.toIItemStack(r.output))) {
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
        public String getRecipeInfo(HeatFrameCoolingRecipe recipe) {
            return LogHelper.getStackDescription(recipe.output);
        }
		
		@Override
		public String describe() {
			return String.format("Removing %s Recipe(s) for %s", this.name, LogHelper.getStackDescription(output));
		}
    }

    
}
