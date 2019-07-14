package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.thirdparty.jei.JEIEtchingAcidCategory.EtchingAcidRecipeWrapper;
import mezz.jei.api.IJeiHelpers;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class JEIEtchingAcidCategory extends JEISpecialCraftingCategory<EtchingAcidRecipeWrapper> {

    JEIEtchingAcidCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);
        setText("gui.nei.recipe.etchingAcid");
    }

    @Override
    public String getUid() {
        return ModCategoryUid.ETCHING_ACID;
    }

    @Override
    public String getTitle() {
        return I18n.format(Fluids.getBlock(Fluids.ETCHING_ACID).getTranslationKey() + ".name");
    }

    @Override
    protected List<MultipleInputOutputRecipeWrapper> getAllRecipes() {
        List<MultipleInputOutputRecipeWrapper> recipes = new ArrayList<>();
        MultipleInputOutputRecipeWrapper recipe = new EtchingAcidRecipeWrapper();
        recipe.addIngredient(new PositionedStack(new ItemStack(ModItems.EMPTY_PCB,1, 32767), 41, 0));
        recipe.addIngredient(new PositionedStack(Fluids.getBucketStack(Fluids.ETCHING_ACID), 73, 0));
        recipe.addOutput(new PositionedStack(new ItemStack(ModItems.UNASSEMBLED_PCB), 105, 0));
        recipes.add(recipe);
        return recipes;
    }

    static class EtchingAcidRecipeWrapper extends PneumaticCraftCategory.MultipleInputOutputRecipeWrapper {
    }
}
