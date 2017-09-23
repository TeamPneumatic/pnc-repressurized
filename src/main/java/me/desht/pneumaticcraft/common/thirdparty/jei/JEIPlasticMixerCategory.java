package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.thirdparty.jei.JEIPlasticMixerCategory.PlasticMixerRecipeWrapper;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.IJeiHelpers;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class JEIPlasticMixerCategory extends PneumaticCraftCategory<PlasticMixerRecipeWrapper> {

    public JEIPlasticMixerCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);
    }

    @Override
    public String getUid() {
        return ModCategoryUid.PLASTIC_MIXER;
    }

    @Override
    public String getTitle() {
        return I18n.format(Blockss.PLASTIC_MIXER.getUnlocalizedName() + ".name");
    }

    @Override
    public ResourceDrawable getGuiTexture() {
        return new ResourceDrawable(Textures.GUI_PLASTIC_MIXER, 0, 0, 6, 3, 166, 79);
    }

    static class PlasticMixerRecipeWrapper extends PneumaticCraftCategory.MultipleInputOutputRecipeWrapper {

        private PlasticMixerRecipeWrapper(ItemStack input, FluidStack output) {

            addOutputLiquid(output, 146, 11);
            addIngredient(new PositionedStack(input, 92, 23));
            setUsedTemperature(76, 22, PneumaticValues.PLASTIC_MIXER_MELTING_TEMP);
        }

        private PlasticMixerRecipeWrapper(FluidStack input, ItemStack output) {
            addInputLiquid(input, 146, 11);
            addIngredient(new PositionedStack(new ItemStack(Items.DYE, 1, 1), 121, 19));
            addIngredient(new PositionedStack(new ItemStack(Items.DYE, 1, 2), 121, 37));
            addIngredient(new PositionedStack(new ItemStack(Items.DYE, 1, 4), 121, 55));
            addOutput(new PositionedStack(output, 92, 55));
            setUsedTemperature(76, 22, PneumaticValues.PLASTIC_MIXER_MELTING_TEMP);
        }
    }

    List<MultipleInputOutputRecipeWrapper> getAllRecipes() {
        List<MultipleInputOutputRecipeWrapper> recipes = new ArrayList<MultipleInputOutputRecipeWrapper>();
        for (int i = 0; i < 16; i++)
            recipes.add(new PlasticMixerRecipeWrapper(new ItemStack(Itemss.PLASTIC, 1, i), new FluidStack(Fluids.PLASTIC, 1000)));
        for (int i = 0; i < 16; i++)
            recipes.add(new PlasticMixerRecipeWrapper(new FluidStack(Fluids.PLASTIC, 1000), new ItemStack(Itemss.PLASTIC, 1, i)));
        return recipes;
    }

//    @Override
//    public Class<PlasticMixerNEIRecipe> getRecipeClass() {
//        return PlasticMixerNEIRecipe.class;
//    }
//
//    @Override
//    public IRecipeWrapper getRecipeWrapper(PlasticMixerNEIRecipe recipe) {
//        return recipe;
//    }
//
//    @Override
//    public boolean isRecipeValid(PlasticMixerNEIRecipe recipe) {
//        return true;
//    }
}
