package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import me.desht.pneumaticcraft.api.recipe.IThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.common.recipes.BasicThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CraftTweaker;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.Helper;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.ListAddition;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.ListRemoval;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.RemoveAllRecipes;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.List;

@ZenClass("mods.pneumaticcraft.thermopneumaticprocessingplant")
@ZenRegister
public class ThermopneumaticProcessingPlant {
	
	public static final String name = "PneumaticCraft Thermopneumatic Processing Plant";
	
	@ZenMethod
	public static void addRecipe(ILiquidStack liquidInput, IItemStack itemInput, double pressure, double temperature, ILiquidStack output) {
		CraftTweaker.ADDITIONS.add(new Add(new BasicThermopneumaticProcessingPlantRecipe(Helper.toFluid(liquidInput), Helper.toStack(itemInput), Helper.toFluid(output), temperature, (float) pressure)));
	}

	@ZenMethod
	public static void addRecipe(IItemStack itemInput, double pressure, double temperature, ILiquidStack output) {
		addRecipe(null, itemInput, pressure, temperature, output);
	}

	@ZenMethod
    public static void removeRecipe(IIngredient output)
    {
		CraftTweaker.REMOVALS.add(new Remove(BasicThermopneumaticProcessingPlantRecipe.recipes, output));
    }

	@ZenMethod
	public static void removeAllRecipes() {
		CraftTweaker.REMOVALS.add(new RemoveAllRecipes<>(name, BasicThermopneumaticProcessingPlantRecipe.recipes));
	}
	
    private static class Add extends ListAddition<IThermopneumaticProcessingPlantRecipe> {
        public Add(IThermopneumaticProcessingPlantRecipe recipe) {
            super(ThermopneumaticProcessingPlant.name, BasicThermopneumaticProcessingPlantRecipe.recipes, recipe);
        }
    }
    
    private static class Remove extends ListRemoval<IThermopneumaticProcessingPlantRecipe> {
    	private final IIngredient output;
    	
        public Remove(List<IThermopneumaticProcessingPlantRecipe> list, IIngredient output) {
            super(ThermopneumaticProcessingPlant.name, list);
            this.output = output;
        }
        
        @Override
        public void apply() {
        	addRecipes();
        	
        	super.apply();
        }

        private void addRecipes() {
            for (IThermopneumaticProcessingPlantRecipe r : recipes) {
                if (Helper.matches(output,  Helper.toILiquidStack(r.getRecipeOutput(null, ItemStack.EMPTY)))) {
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
