package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.thirdparty.jei.JEIEtchingAcidCategory.EtchingAcidRecipeWrapper;
import mezz.jei.api.IJeiHelpers;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class JEIEtchingAcidCategory extends JEISpecialCraftingCategory<EtchingAcidRecipeWrapper> {

    public JEIEtchingAcidCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);
        setText("gui.nei.recipe.etchingAcid");
    }

    @Override
    public String getUid() {
        return ModCategoryUid.ETCHING_ACID;
    }

    @Override
    public String getTitle() {
        return I18n.format(Fluids.getBlock(Fluids.ETCHING_ACID).getUnlocalizedName() + ".name");
    }

    @Override
    protected List<MultipleInputOutputRecipeWrapper> getAllRecipes() {
        List<MultipleInputOutputRecipeWrapper> recipes = new ArrayList<>();
        MultipleInputOutputRecipeWrapper recipe = new EtchingAcidRecipeWrapper();
        recipe.addIngredient(new PositionedStack(new ItemStack(Itemss.EMPTY_PCB), 41, 80));
        recipe.addIngredient(new PositionedStack(new ItemStack(Fluids.getBucket(Fluids.ETCHING_ACID)), 73, 80));
        recipe.addOutput(new PositionedStack(new ItemStack(Itemss.UNASSEMBLED_PCB), 105, 80));
        recipes.add(recipe);
        return recipes;
    }

    static class EtchingAcidRecipeWrapper extends PneumaticCraftCategory.MultipleInputOutputRecipeWrapper {

    }

//    @Override
//    public Class<EtchingAcidRecipeWrapper> getRecipeClass() {
//        return EtchingAcidRecipeWrapper.class;
//    }
//
//    @Override
//    public IRecipeWrapper getRecipeWrapper(EtchingAcidRecipeWrapper recipe) {
//        return recipe;
//    }
//
//    @Override
//    public boolean isRecipeValid(EtchingAcidRecipeWrapper recipe) {
//        return true;
//    }
}
