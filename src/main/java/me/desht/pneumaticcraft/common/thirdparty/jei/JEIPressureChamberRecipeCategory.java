package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.common.recipes.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.util.OreDictionaryHelper;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.IJeiHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class JEIPressureChamberRecipeCategory extends PneumaticCraftCategory<JEIPressureChamberRecipeCategory.ChamberRecipeWrapper> {
    public JEIPressureChamberRecipeCategory(IJeiHelpers jeiHelpers) {
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

    public static class ChamberRecipeWrapper extends PneumaticCraftCategory.MultipleInputOutputRecipeWrapper {
        float recipePressure;

        public ChamberRecipeWrapper(PressureChamberRecipe recipe) {
            for (int i = 0; i < recipe.input.length; i++) {
                PositionedStack stack;
                int posX = 19 + i % 3 * 17;
                int posY = 93 - i / 3 * 17;

                if (recipe.input[i] instanceof Pair) {
                    List<ItemStack> oreInputs = new ArrayList<ItemStack>();

                    Pair<String, Integer> oreDictEntry = (Pair<String, Integer>) recipe.input[i];
                    for (ItemStack s : OreDictionaryHelper.getOreDictEntries(oreDictEntry.getKey())) {
                        s = s.copy();
                        s.setCount(oreDictEntry.getValue());
                        oreInputs.add(s);
                    }
                    stack = new PositionedStack(oreInputs, posX, posY);
                } else {
                    stack = new PositionedStack((ItemStack) recipe.input[i], posX, posY);
                }
                this.addIngredient(stack);
            }
            for (int i = 0; i < recipe.output.length; i++) {
                PositionedStack stack = new PositionedStack(recipe.output[i], 101 + i % 3 * 18, 59 + i / 3 * 18);
                this.addOutput(stack);
            }
            this.recipePressure = recipe.pressure;
        }

        @Override
        public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
            drawAnimatedPressureGauge(120, 27, -1, recipePressure, PneumaticValues.DANGER_PRESSURE_PRESSURE_CHAMBER, PneumaticValues.MAX_PRESSURE_PRESSURE_CHAMBER);
        }
    }

    //    @Override
//    public Class<PressureChamberRecipe> getRecipeClass() {
//        return PressureChamberRecipe.class;
//    }
//
//    @Override
//    public IRecipeWrapper getRecipeWrapper(PressureChamberRecipe recipe) {
//        return getShape(recipe);
//    }
//
//    @Override
//    public boolean isRecipeValid(PressureChamberRecipe recipe) {
//        return true;
//    }

}
