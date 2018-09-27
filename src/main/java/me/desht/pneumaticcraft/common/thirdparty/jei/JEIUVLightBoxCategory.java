package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.thirdparty.jei.JEIUVLightBoxCategory.UVLightBoxRecipeWrapper;
import mezz.jei.api.IJeiHelpers;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class JEIUVLightBoxCategory extends JEISpecialCraftingCategory<UVLightBoxRecipeWrapper> {

    JEIUVLightBoxCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);
        setText("gui.nei.recipe.uvLightBox");
    }

    @Override
    public String getUid() {
        return ModCategoryUid.UV_LIGHT_BOX;
    }

    @Override
    public String getTitle() {
        return I18n.format(Blockss.UV_LIGHT_BOX.getTranslationKey() + ".name");
    }

    @Override
    protected List<MultipleInputOutputRecipeWrapper> getAllRecipes() {
        List<MultipleInputOutputRecipeWrapper> recipes = new ArrayList<>();
        MultipleInputOutputRecipeWrapper recipe = new UVLightBoxRecipeWrapper();
        ItemStack emptyPCB = new ItemStack(Itemss.EMPTY_PCB);
        emptyPCB.setItemDamage(emptyPCB.getMaxDamage());
        recipe.addIngredient(new PositionedStack(emptyPCB, 41, 0));
        recipe.addIngredient(new PositionedStack(new ItemStack(Blockss.UV_LIGHT_BOX), 73, -2));
        recipe.addOutput(new PositionedStack(new ItemStack(Itemss.EMPTY_PCB), 105, 0));
        recipes.add(recipe);
        return recipes;
    }

    static class UVLightBoxRecipeWrapper extends PneumaticCraftCategory.MultipleInputOutputRecipeWrapper {
    }
}
