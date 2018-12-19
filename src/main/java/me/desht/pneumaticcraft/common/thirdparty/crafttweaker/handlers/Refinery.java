package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.liquid.ILiquidStack;
import me.desht.pneumaticcraft.common.recipes.RefineryRecipe;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CraftTweaker;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.Helper;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.ListAddition;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.ListRemoval;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.RemoveAllRecipes;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.stream.Stream;

@ZenClass("mods.pneumaticcraft.refinery")
@ZenRegister
public class Refinery {
public static final String name = "PneumaticCraft Refinery";

	@ZenMethod
	public static void addRecipe(int minTemp, ILiquidStack input, ILiquidStack[] outputs) {
		CraftTweaker.ADDITIONS.add(new Add(new RefineryRecipe(minTemp, Helper.toFluid(input), Helper.toFluids(outputs))));
	}

	@ZenMethod
	public static void addRecipe(ILiquidStack input, ILiquidStack[] outputs) {
		CraftTweaker.ADDITIONS.add(new Add(new RefineryRecipe(373, Helper.toFluid(input), Helper.toFluids(outputs))));
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
		CraftTweaker.REMOVALS.add(new RemoveAllRecipes<>(Refinery.name, RefineryRecipe.recipes));
	}
	
    private static class Add extends ListAddition<RefineryRecipe> {
        public Add(RefineryRecipe recipe) {
            super(Refinery.name, RefineryRecipe.recipes, recipe);
        }
    }
    
    private static class RemoveInput extends ListRemoval<RefineryRecipe> {
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
            for (RefineryRecipe r : recipes) {
            	
            	if(Helper.areEqual(r.input, Helper.toFluid(input))) {
            		entries.add(r);
            	}
            }
            
            if(entries.isEmpty()) {
            	Helper.logWarning(String.format("No %s Recipe found for %s. Command ignored!", name, Helper.getStackDescription(input)));
            } else {
            	Helper.logInfo(String.format("Found %d %s Recipe(s) for %s.", entries.size(), name, Helper.getStackDescription(input)));
            }
		}

        
		@Override
		public String describe() {
			return String.format("Removing %s Recipe(s) for %s", this.name, Helper.getStackDescription(input));
		}
    }
    
    private static class RemoveOutput extends ListRemoval<RefineryRecipe> {
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
            for (RefineryRecipe r : recipes) {
            	if(Stream.of(outputs).allMatch(o -> Stream.of(r.outputs).anyMatch(ro -> Helper.matches(o, Helper.toILiquidStack(ro))))) {
            		entries.add(r);
            	}
            }
            
            if(entries.isEmpty()) {
            	Helper.logWarning(String.format("No %s Recipe found for outputs. Command ignored!", name));
            } else {
            	Helper.logInfo(String.format("Found %d %s Recipe(s) for outputs.", entries.size(), name));
            }
		}
        
		@Override
		public String describe() {
			return String.format("Removing %s Recipe(s)", this.name);
		}
    }
}
