package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JEIAmadronTradeCategory implements IRecipeCategory<AmadronOffer> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;

    JEIAmadronTradeCategory() {
        icon = JEIPlugin.jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModItems.AMADRON_TABLET.get()));
        background = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.WIDGET_AMADRON_OFFER, 0, 0, 73, 35);
        localizedName = I18n.format(ModItems.AMADRON_TABLET.get().getTranslationKey());
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.AMADRON_TRADE;
    }

    @Override
    public Class<? extends AmadronOffer> getRecipeClass() {
        return AmadronOffer.class;
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
    public void setIngredients(AmadronOffer recipe, IIngredients ingredients) {
        if (recipe.getInput().getType() == AmadronTradeResource.Type.ITEM) {
            ingredients.setInput(VanillaTypes.ITEM, recipe.getInput().getItem());
        } else if (recipe.getInput().getType() == AmadronTradeResource.Type.FLUID) {
            ingredients.setInput(VanillaTypes.FLUID, recipe.getInput().getFluid());
        }
        if (recipe.getOutput().getType() == AmadronTradeResource.Type.ITEM) {
            ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput().getItem());
        } else if (recipe.getOutput().getType() == AmadronTradeResource.Type.FLUID) {
            ingredients.setOutput(VanillaTypes.FLUID, recipe.getOutput().getFluid());
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, AmadronOffer recipe, IIngredients ingredients) {
        if (recipe.getInput().getType() == AmadronTradeResource.Type.ITEM) {
            recipeLayout.getItemStacks().init(0, true, 5, 14);
            recipeLayout.getItemStacks().set(0, recipe.getInput().getItem());
        } else if (recipe.getInput().getType() == AmadronTradeResource.Type.FLUID) {
            recipeLayout.getFluidStacks().init(0, true, 6, 15);
            recipeLayout.getFluidStacks().set(0, recipe.getInput().getFluid());
        }
        if (recipe.getOutput().getType() == AmadronTradeResource.Type.ITEM) {
            recipeLayout.getItemStacks().init(1, false, 50, 14);
            recipeLayout.getItemStacks().set(1, recipe.getOutput().getItem());
        } else if (recipe.getOutput().getType() == AmadronTradeResource.Type.FLUID) {
            recipeLayout.getFluidStacks().init(1, false, 51, 15);
            recipeLayout.getFluidStacks().set(1, recipe.getOutput().getFluid());
        }
    }

    @Override
    public void draw(AmadronOffer recipe, double mouseX, double mouseY) {
        FontRenderer fr = Minecraft.getInstance().fontRenderer;
        int x = (background.getWidth() - fr.getStringWidth(recipe.getVendor())) / 2;
        fr.drawString(recipe.getVendor(), x, 3, 0xFF404040);

        if (recipe.getInput().getType() == AmadronTradeResource.Type.FLUID) {
            drawFluidOverlay(22, fr, recipe.getInput());
        }
        if (recipe.getOutput().getType() == AmadronTradeResource.Type.FLUID) {
            drawFluidOverlay(67, fr, recipe.getOutput());
        }
    }

    private void drawFluidOverlay(int x, FontRenderer fr, AmadronTradeResource res) {
        String s = res.getAmount() / 1000 + "B";
        GlStateManager.translated(0, 0, 400);
        fr.drawStringWithShadow(s, x - fr.getStringWidth(s), 23, 0xFFFFFFFF);
        GlStateManager.translated(0, 0, -400);
    }

    @Override
    public List<String> getTooltipStrings(AmadronOffer recipe, double mouseX, double mouseY) {
        List<String> res = new ArrayList<>();
        if (mouseX >= 22 && mouseX <= 51 && mouseY >= 12) {
            res.add(I18n.format("gui.amadron.amadronWidget.vendor", recipe.getVendor()));
            res.add(I18n.format("gui.amadron.amadronWidget.selling", recipe.getOutput().toString()));
            res.add(I18n.format("gui.amadron.amadronWidget.buying", recipe.getInput().toString()));
        }
        return res;
    }

    static Collection<AmadronOffer> getAllRecipes() {
        return AmadronOfferManager.getInstance().getAllOffers();
    }
}
