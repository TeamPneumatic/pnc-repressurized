package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.common.recipes.HeatFrameCoolingRecipe;
import mezz.jei.api.IJeiHelpers;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class JEIHeatFrameCoolingCategory extends JEISpecialCraftingCategory<JEIHeatFrameCoolingCategory.HeatFrameCoolingRecipeWrapper> {
    JEIHeatFrameCoolingCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);
        setText("gui.nei.recipe.heatFrameCooling");
    }

    @Override
    protected List<MultipleInputOutputRecipeWrapper> getAllRecipes() {
        List<MultipleInputOutputRecipeWrapper> recipes = new ArrayList<>();
        for (HeatFrameCoolingRecipe hfcr : HeatFrameCoolingRecipe.recipes) {
            HeatFrameCoolingRecipeWrapper recipe = new HeatFrameCoolingRecipeWrapper();
            if (hfcr.input instanceof Pair) {
                NonNullList<ItemStack> l = OreDictionary.getOres((String) ((Pair) hfcr.input).getLeft());
                recipe.addIngredient(new PositionedStack(l, 41, 1));
            } else if (hfcr.input instanceof ItemStack) {
                recipe.addIngredient(new PositionedStack((ItemStack) hfcr.input, 41, 1));
            }
            recipe.addOutput(new PositionedStack(hfcr.output, 105, 1));
            recipes.add(recipe);
        }
        return recipes;
    }

    @Override
    public String getUid() {
        return ModCategoryUid.HEAT_FRAME_COOLING;
    }

    @Override
    public String getTitle() {
        return I18n.format("gui.nei.title.heatFrameCooling");
    }

    static class HeatFrameCoolingRecipeWrapper extends PneumaticCraftCategory.MultipleInputOutputRecipeWrapper {

    }
}
