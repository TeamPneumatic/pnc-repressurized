package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.api.recipe.ItemIngredient;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.IJeiHelpers;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class JEIPressureChamberRecipeCategory extends PneumaticCraftCategory<JEIPressureChamberRecipeCategory.ChamberRecipeWrapper> {
    JEIPressureChamberRecipeCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);
    }

    @Override
    public String getUid() {
        return ModCategoryUid.PRESSURE_CHAMBER;
    }

    @Override
    public String getTitle() {
        return I18n.format("gui.pressureChamber");
    }

    @Override
    public ResourceDrawable getGuiTexture() {
        return new ResourceDrawable(Textures.GUI_NEI_PRESSURE_CHAMBER_LOCATION, 0, 0, 5, 11, 166, 130);
    }

    static class ChamberRecipeWrapper extends PneumaticCraftCategory.MultipleInputOutputRecipeWrapper {
        ChamberRecipeWrapper(IPressureChamberRecipe recipe) {
            for (int i = 0; i < recipe.getInput().size(); i++) {
                int posX = 19 + i % 3 * 17;
                int posY = 93 - i / 3 * 17;
                ItemIngredient ingredient = recipe.getInput().get(i);
                PositionedStack pStack = new PositionedStack(ingredient.getStacks(), posX, posY)
                        .setTooltipKey(ingredient.getTooltipKey());
                this.addIngredient(pStack);
            }
            for (int i = 0; i < recipe.getResult().size(); i++) {
                ItemStack stack = recipe.getResult().get(i);
                PositionedStack pStack = new PositionedStack(stack, 101 + i % 3 * 18, 59 + i / 3 * 18)
                        .setTooltipKey(IPressureChamberRecipe.getTooltipKey(stack));
                this.addOutput(pStack);
            }
            setUsedPressure(120, 27, recipe.getCraftingPressure(), PneumaticValues.DANGER_PRESSURE_TIER_ONE, PneumaticValues.MAX_PRESSURE_TIER_ONE);
        }
    }
}
