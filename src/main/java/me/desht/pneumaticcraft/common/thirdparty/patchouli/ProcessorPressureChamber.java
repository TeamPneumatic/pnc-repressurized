package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProcessorPressureChamber implements IComponentProcessor {
    private PressureChamberRecipe recipe = null;

    @Override
    public void setup(IVariableProvider iVariableProvider) {
        String recipeId = iVariableProvider.get("recipe").asString();
        this.recipe = PneumaticCraftRecipeType.PRESSURE_CHAMBER.getRecipe(Minecraft.getInstance().world, new ResourceLocation(recipeId));
    }

    @Override
    public IVariable process(String s) {
        if (recipe == null) return null;

        if (s.startsWith("input")) {
            int index = Integer.parseInt(s.substring(5)) - 1;
            if (index >= 0 && index < recipe.getInputsForDisplay().size()) {
                return Patchouli.Util.getStacks(recipe.getInputsForDisplay().get(index));
            }
        } else if (s.startsWith("output")) {
            int index = Integer.parseInt(s.substring(6)) - 1;
            if (index >= 0 && index < recipe.getResultsForDisplay().size()) {
                return IVariable.from(recipe.getResultsForDisplay().get(index));
            }
        } else if (s.equals("pressure")) {
            return IVariable.wrap(xlate("pneumaticcraft.gui.tooltip.pressure", recipe.getCraftingPressure()).getString());
        }

        return null;
    }
}
