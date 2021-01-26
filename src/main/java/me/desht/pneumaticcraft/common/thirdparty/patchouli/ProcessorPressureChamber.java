package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

@SuppressWarnings("unused")
public class ProcessorPressureChamber implements IComponentProcessor {
    private PressureChamberRecipe recipe = null;
    private String header = null;

    @Override
    public void setup(IVariableProvider iVariableProvider) {
        ResourceLocation recipeId = new ResourceLocation(iVariableProvider.get("recipe").asString());
        this.recipe = PneumaticCraftRecipeType.PRESSURE_CHAMBER.getRecipe(Minecraft.getInstance().world, recipeId);
        this.header = iVariableProvider.has("header") ? iVariableProvider.get("header").asString() : "";
    }

    @Override
    public IVariable process(String s) {
        if (recipe == null) return null;

        if (s.equals("header")) {
            return IVariable.wrap(header.isEmpty() ? defaultHeader() : header);
        } else if (s.startsWith("input")) {
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
            String pr = PneumaticCraftUtils.roundNumberTo(recipe.getCraftingPressure(), 1);
            return IVariable.wrap(I18n.format("pneumaticcraft.patchouli.processor.pressureChamber.desc", pr));
        }

        return null;
    }

    private String defaultHeader() {
        // note: only returns first item. use a custom "header" if needed
        if (!recipe.getResultsForDisplay().isEmpty()) {
            return recipe.getResultsForDisplay().get(0).getDisplayName().getString();
        } else {
            return "";
        }
    }
}
