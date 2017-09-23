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

    public JEIUVLightBoxCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);
        setText("gui.nei.recipe.uvLightBox");
    }

    @Override
    public String getUid() {
        return ModCategoryUid.UV_LIGHT_BOX;
    }

    @Override
    public String getTitle() {
        return I18n.format(Blockss.UV_LIGHT_BOX.getUnlocalizedName() + ".name");
    }

    @Override
    protected List<MultipleInputOutputRecipeWrapper> getAllRecipes() {
        List<MultipleInputOutputRecipeWrapper> recipes = new ArrayList<MultipleInputOutputRecipeWrapper>();
        MultipleInputOutputRecipeWrapper recipe = new UVLightBoxRecipeWrapper();
        recipe.addIngredient(new PositionedStack(new ItemStack(Itemss.EMPTY_PCB, 1, Itemss.EMPTY_PCB.getMaxDamage()), 41, 80));
        recipe.addIngredient(new PositionedStack(new ItemStack(Blockss.UV_LIGHT_BOX), 73, 80));
        recipe.addOutput(new PositionedStack(new ItemStack(Itemss.EMPTY_PCB), 105, 80));
        recipes.add(recipe);
        return recipes;
    }

    public static class UVLightBoxRecipeWrapper extends PneumaticCraftCategory.MultipleInputOutputRecipeWrapper {

    }

//    @Override
//    public Class<UVLightBoxRecipeWrapper> getRecipeClass() {
//        return UVLightBoxRecipeWrapper.class;
//    }
//
//    @Override
//    public IRecipeWrapper getRecipeWrapper(UVLightBoxRecipeWrapper recipe) {
//        return recipe;
//    }
//
//    @Override
//    public boolean isRecipeValid(UVLightBoxRecipeWrapper recipe) {
//        return true;
//    }
}
