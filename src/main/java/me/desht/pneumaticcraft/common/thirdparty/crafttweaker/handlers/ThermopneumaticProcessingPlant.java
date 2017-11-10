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
import me.desht.pneumaticcraft.api.recipe.IThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;
import me.desht.pneumaticcraft.common.recipes.BasicThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.common.recipes.PneumaticRecipeRegistry;
import me.desht.pneumaticcraft.common.recipes.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CraftTweaker;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.RemoveAllRecipes;
import net.minecraft.item.ItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.pneumaticcraft.thermopneumaticprocessingplant")
@ModOnly("mtlib")
@ZenRegister
public class ThermopneumaticProcessingPlant {
	
	public static final String name = "PneumaticCraft Thermopneumatic Processing Plant";
	
	@ZenMethod
	public static void addRecipe(ILiquidStack liquidInput, IItemStack itemInput, double pressure, double temperature, ILiquidStack output) {
		CraftTweaker.ADDITIONS.add(new Add(new BasicThermopneumaticProcessingPlantRecipe(InputHelper.toFluid(liquidInput), InputHelper.toStack(itemInput), InputHelper.toFluid(output), temperature, (float) pressure), output));
	}

	@ZenMethod
	public static void addRecipe(IItemStack itemInput, double pressure, double temperature, ILiquidStack output) {
		addRecipe(null, itemInput, pressure, temperature, output);
	}

	@ZenMethod
    public static void removeRecipe(IIngredient output)
    {
		CraftTweaker.REMOVALS.add(new Remove(PneumaticRecipeRegistry.getInstance().thermopneumaticProcessingPlantRecipes, output));
    }

	@ZenMethod
	public static void removeAllRecipes() {
		CraftTweaker.REMOVALS.add(new RemoveAllRecipes<IThermopneumaticProcessingPlantRecipe>(name, PneumaticRecipeRegistry.getInstance().thermopneumaticProcessingPlantRecipes));
	}
	
    private static class Add extends BaseListAddition<IThermopneumaticProcessingPlantRecipe> {
    	private final ILiquidStack output;
    	
        public Add(IThermopneumaticProcessingPlantRecipe recipe, ILiquidStack output) {
            super(ThermopneumaticProcessingPlant.name, PneumaticRecipeRegistry.getInstance().thermopneumaticProcessingPlantRecipes);
            this.output = output;
            recipes.add(recipe);
        }

        @Override
        public String getRecipeInfo(IThermopneumaticProcessingPlantRecipe recipe) {
            return LogHelper.getStackDescription(output);
        }
    }
    
    private static class Remove extends BaseListRemoval<IThermopneumaticProcessingPlantRecipe> {
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
            for (IThermopneumaticProcessingPlantRecipe r : list) {
                if (StackHelper.matches(output,  InputHelper.toILiquidStack(r.getRecipeOutput(null, ItemStack.EMPTY)))) {
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
        public String getRecipeInfo(IThermopneumaticProcessingPlantRecipe recipe) {
            return LogHelper.getStackDescription(output);
        }
		
		@Override
		public String describe() {
			return String.format("Removing %s Recipe(s) for %s", this.name, LogHelper.getStackDescription(output));
		}
    }
}
