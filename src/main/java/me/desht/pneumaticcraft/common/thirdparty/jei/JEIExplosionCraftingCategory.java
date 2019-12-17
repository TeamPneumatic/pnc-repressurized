package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.recipe.IExplosionCraftingRecipe;
import me.desht.pneumaticcraft.api.recipe.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class JEIExplosionCraftingCategory extends PneumaticCraftCategory<JEIExplosionCraftingCategory.ExplosionCraftingWrapper> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    private static IDrawable iconStatic;

    JEIExplosionCraftingCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);

        localizedName = I18n.format("gui.nei.title.explosionCrafting");
        background = new ResourceDrawable(Textures.GUI_JEI_MISC_RECIPES, 40, 0, 0, 0, 82, 18) {
            @Override
            public int getWidth() {
                return 160;
            }
        };
        icon = jeiHelpers.getGuiHelper()
                .drawableBuilder(Textures.JEI_EXPLOSION, 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
        if (iconStatic == null) iconStatic = icon;
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.EXPLOSION_CRAFTING;
    }

    @Override
    public Class<? extends JEIExplosionCraftingCategory.ExplosionCraftingWrapper> getRecipeClass() {
        return ExplosionCraftingWrapper.class;
    }

    @Override
    public String getTitle() {
        return localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    static Collection<ExplosionCraftingWrapper> getAllRecipes() {
        List<ExplosionCraftingWrapper> recipes = new ArrayList<>();
        PneumaticCraftRecipes.explosionCraftingRecipes.forEach((recipeName, ecr) -> {
            try {
                recipes.add(new ExplosionCraftingWrapper(ecr));
            } catch (IllegalArgumentException e) {
                Log.warning("can't add recipe " + recipeName + ": " + e.getMessage());
            }
        });
        return recipes;
    }

    static class ExplosionCraftingWrapper extends PneumaticCraftCategory.AbstractCategoryExtension {
        private final int lossRate;

        ExplosionCraftingWrapper(IExplosionCraftingRecipe recipe) {
            this.lossRate = recipe.getLossRate();
            List<ItemStack> inputList = new ArrayList<>(Arrays.asList(recipe.getInput().getMatchingStacks()));
            if (!inputList.isEmpty()) {
                this.addInputItem(PositionedStack.of(inputList, 41, 1));
                this.addOutputItem(PositionedStack.of(recipe.getOutputs(), 105, 1));
            } else {
                throw new IllegalArgumentException("recipe has no valid input");
            }
        }

        @Override
        public void drawInfo(int recipeWidth, int recipeHeight, double mouseX, double mouseY) {
            drawIconAt(iconStatic,73, 0);
        }

        @Nonnull
        @Override
        public List<String> getTooltipStrings(double mouseX, double mouseY) {
            List<String> res = super.getTooltipStrings(mouseX, mouseY);
            if (mouseX >= 63 && mouseX <= 100) {
                res.addAll(PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.nei.recipe.explosionCrafting", lossRate), 32));
            }
            return res;
        }
    }
}
