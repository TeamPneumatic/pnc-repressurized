package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.recipes.UVLightBoxRecipe;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JEIUVLightBoxCategory extends PneumaticCraftCategory<JEIUVLightBoxCategory.UVLightBoxRecipeWrapper> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;

    private static final List<UVLightBoxRecipe> UV_LIGHT_BOX_RECIPES;
    static {
        ItemStack out = new ItemStack(ModItems.EMPTY_PCB);
        TileEntityUVLightBox.setExposureProgress(out, 100);
        UVLightBoxRecipe recipe = new UVLightBoxRecipe(Ingredient.fromItems(ModItems.EMPTY_PCB), out);
        UV_LIGHT_BOX_RECIPES = Collections.singletonList(recipe);
    }

    JEIUVLightBoxCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);

        localizedName = I18n.format(ModBlocks.UV_LIGHT_BOX.getTranslationKey());
        background = new ResourceDrawable(Textures.GUI_JEI_MISC_RECIPES, 40, 0, 0, 0, 82, 18) {
            @Override
            public int getWidth() {
                return 160;
            }
        };
        icon = jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModBlocks.UV_LIGHT_BOX));
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.UV_LIGHT_BOX;
    }

    @Override
    public Class<? extends UVLightBoxRecipeWrapper> getRecipeClass() {
        return UVLightBoxRecipeWrapper.class;
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

    @Override
    public void draw(UVLightBoxRecipeWrapper recipe, double mouseX, double mouseY) {
        drawIconAt(icon, 73, -2);
        drawTextAt("gui.nei.recipe.uvLightBox", 5, 24);
    }

    static Collection<UVLightBoxRecipeWrapper> getAllRecipes() {
        return UV_LIGHT_BOX_RECIPES.stream()
                .map(UVLightBoxRecipeWrapper::new)
                .collect(Collectors.toList());
    }

    static class UVLightBoxRecipeWrapper extends PneumaticCraftCategory.AbstractCategoryExtension {
        UVLightBoxRecipeWrapper(UVLightBoxRecipe recipe) {
            this.addInputItem(PositionedStack.of(recipe.getIn().getMatchingStacks(), 41, 0));
            this.addInputItem(PositionedStack.of(new ItemStack(ModBlocks.UV_LIGHT_BOX), 73, -2));
            this.addOutputItem(PositionedStack.of(recipe.getOut(), 105, 0));
        }
    }
}
