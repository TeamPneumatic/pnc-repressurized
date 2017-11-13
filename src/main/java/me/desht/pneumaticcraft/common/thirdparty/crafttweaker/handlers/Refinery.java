package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import java.util.stream.Stream;

import com.blamejared.mtlib.helpers.InputHelper;
import com.blamejared.mtlib.helpers.LogHelper;
import com.blamejared.mtlib.helpers.StackHelper;
import com.blamejared.mtlib.utils.BaseListAddition;
import com.blamejared.mtlib.utils.BaseListRemoval;

import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.liquid.ILiquidStack;
import me.desht.pneumaticcraft.common.recipes.RefineryRecipe;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CraftTweaker;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.RemoveAllRecipes;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.pneumaticcraft.refinery")
@ModOnly("mtlib")
@ZenRegister
public class Refinery {
public static final String name = "PneumaticCraft Refinery";
	
	@ZenMethod
	public static void addRecipe(ILiquidStack input, ILiquidStack[] outputs) {
		CraftTweaker.ADDITIONS.add(new Add(new RefineryRecipe(InputHelper.toFluid(input), InputHelper.toFluids(outputs))));
	}
	
	@ZenMethod
	public static void removeRecipes(ILiquidStack input) {
		CraftTweaker.REMOVALS.add(new RemoveInput(input));
	}
	
	@ZenMethod
	public static void removeRecipe(IIngredient[] outputs) {
		CraftTweaker.REMOVALS.add(new RemoveOutput(outputs));
	}
	
	@ZenMethod
	public static void removeAllRecipes() {
		CraftTweaker.REMOVALS.add(new RemoveAllRecipes<RefineryRecipe>(Refinery.name, RefineryRecipe.recipes));
	}
	
    private static class Add extends BaseListAddition<RefineryRecipe> {
        public Add(RefineryRecipe recipe) {
            super(Refinery.name, RefineryRecipe.recipes);
            recipes.add(recipe);
        }

        @Override
        public String getRecipeInfo(RefineryRecipe recipe) {
            return LogHelper.getStackDescription(recipe.input);
        }
    }
    
    private static class RemoveInput extends BaseListRemoval<RefineryRecipe> {
    	private final ILiquidStack input;
    	
        public RemoveInput(ILiquidStack input) {
            super(Refinery.name, RefineryRecipe.recipes);
            this.input = input;
        }
        
        @Override
        public void apply() {
        	addRecipes();
        	
        	super.apply();
        }
        
        private void addRecipes() {
            for (RefineryRecipe r : list) {
            	
            	if(StackHelper.areEqual(r.input, InputHelper.toFluid(input))) {
            		recipes.add(r);
            	}
            }
            
            if(recipes.isEmpty()) {
            	LogHelper.logWarning(String.format("No %s Recipe found for %s. Command ignored!", name, LogHelper.getStackDescription(input)));
            } else {
            	LogHelper.logInfo(String.format("Found %d %s Recipe(s) for %s.", recipes.size(), name, LogHelper.getStackDescription(input)));
            }
		}
        
        @Override
        public String getRecipeInfo(RefineryRecipe recipe) {
            return LogHelper.getStackDescription(recipe.input);
        }
        
		@Override
		public String describe() {
			return String.format("Removing %s Recipe(s) for %s", this.name, LogHelper.getStackDescription(input));
		}
    }
    
    private static class RemoveOutput extends BaseListRemoval<RefineryRecipe> {
    	private final IIngredient[] outputs;
    	
        public RemoveOutput(IIngredient[] outputs) {
            super(Refinery.name, RefineryRecipe.recipes);
            this.outputs = outputs;
        }
        
        @Override
        public void apply() {
        	addRecipes();
        	
        	super.apply();
        }
        
        private void addRecipes() {
            for (RefineryRecipe r : list) {
            	if(Stream.of(outputs).allMatch(o -> Stream.of(r.outputs).anyMatch(ro -> StackHelper.matches(o, InputHelper.toILiquidStack(ro))))) {
            		recipes.add(r);
            	}
            }
            
            if(recipes.isEmpty()) {
            	LogHelper.logWarning(String.format("No %s Recipe found for outputs. Command ignored!", name));
            } else {
            	LogHelper.logInfo(String.format("Found %d %s Recipe(s) for outputs.", recipes.size(), name));
            }
		}
        
        @Override
        public String getRecipeInfo(RefineryRecipe recipe) {
            return LogHelper.getStackDescription(recipe.input);
        }
        
		@Override
		public String describe() {
			return String.format("Removing %s Recipe(s)", this.name);
		}
    }
}
