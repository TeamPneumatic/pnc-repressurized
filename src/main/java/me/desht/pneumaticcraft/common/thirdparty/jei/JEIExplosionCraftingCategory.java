package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.common.recipes.ExplosionCraftingRecipe;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class JEIExplosionCraftingCategory extends JEISpecialCraftingCategory<JEIExplosionCraftingCategory.ExplosionCraftingWrapper> {
    private static IDrawable icon;

    JEIExplosionCraftingCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);

        icon = jeiHelpers.getGuiHelper()
                .drawableBuilder(Textures.JEI_EXPLOSION, 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
    }

    @Override
    protected List<MultipleInputOutputRecipeWrapper> getAllRecipes() {
        List<MultipleInputOutputRecipeWrapper> recipes = new ArrayList<>();
        for (ExplosionCraftingRecipe ecr : ExplosionCraftingRecipe.recipes) {
            ExplosionCraftingWrapper recipe = new ExplosionCraftingWrapper(ecr.getLossRate());
            List<ItemStack> inputList = new ArrayList<>();
            if (!ecr.getInput().isEmpty()) {
                inputList.add(ecr.getInput());
            } else if (ecr.getOreDictKey() != null) {
                inputList = OreDictionary.getOres(ecr.getOreDictKey());
            }
            if (!inputList.isEmpty()) {
                recipe.addIngredient(new PositionedStack(inputList, 41, 0));
                recipe.addOutput(new PositionedStack(ecr.getOutput(), 105, 0));
                recipes.add(recipe);
            } else {
                Log.warning("could not determine JEI input for explosion crafting recipe " + ecr.getOutput());
            }
        }
        return recipes;
    }

    @Override
    public String getUid() {
        return ModCategoryUid.EXPLOSION_CRAFTING;
    }

    @Override
    public String getTitle() {
        return I18n.format("gui.nei.title.explosionCrafting");
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    static class ExplosionCraftingWrapper extends PneumaticCraftCategory.MultipleInputOutputRecipeWrapper {
        private final int lossRate;

        ExplosionCraftingWrapper(int lossRate) {
            this.lossRate = lossRate;
        }

        @Override
        public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableDepth();
            GlStateManager.enableAlpha();
            icon.draw(Minecraft.getMinecraft(), 73, 0);
            GlStateManager.enableDepth();
            GlStateManager.disableAlpha();
        }

        @Nonnull
        @Override
        public List<String> getTooltipStrings(int mouseX, int mouseY) {
            List<String> res = super.getTooltipStrings(mouseX, mouseY);
            if (mouseX >= 63 && mouseX <= 100) {
                res.addAll(PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.nei.recipe.explosionCrafting", lossRate), 32));
            }
            return res;
        }
    }
}
