package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariableProvider;
import vazkii.patchouli.common.util.ItemStackUtil;

public class ProcessorPressureChamber implements IComponentProcessor {
    private PressureChamberRecipe recipe = null;

    @Override
    public void setup(IVariableProvider<String> iVariableProvider) {
        String recipeName = iVariableProvider.get("recipe");
        this.recipe = PneumaticCraftRecipeType.PRESSURE_CHAMBER.getRecipe(Minecraft.getInstance().world, new ResourceLocation(recipeName));
    }

    @Override
    public String process(String s) {
        if (recipe == null) return null;

        if (s.startsWith("input")) {
            int index = Integer.parseInt(s.substring(5)) - 1;
            if (index >= 0 && index < recipe.getInputsForDisplay().size()) {
                return ItemStackUtil.serializeIngredient(recipe.getInputsForDisplay().get(index));
            }
        } else if (s.startsWith("output")) {
            int index = Integer.parseInt(s.substring(6)) - 1;
            if (index >= 0 && index < recipe.getResultsForDisplay().size()) {
                return ItemStackUtil.serializeStack(recipe.getResultsForDisplay().get(index));
            }
        } else if (s.equals("pressure")) {
            return String.format("Required pressure: %.1f bar", recipe.getCraftingPressure());
        }

        return null;
    }
}
