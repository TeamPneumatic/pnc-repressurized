package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.client.gui.widget.WidgetAmadronOffer;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class JEIAmadronTradeCategory extends JEIPneumaticCraftCategory<AmadronOffer> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;

    JEIAmadronTradeCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);

        icon = jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModItems.AMADRON_TABLET));
        background = jeiHelpers.getGuiHelper().createDrawable(Textures.WIDGET_AMADRON_OFFER, 0, 0, 73, 35);
        localizedName = I18n.format(ModItems.AMADRON_TABLET.getTranslationKey());
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
        AmadronOffer.TradeResource in = recipe.getInput();
        if (!in.getItem().isEmpty()) {
            ingredients.setInput(VanillaTypes.ITEM, in.getItem());
        } else if (!in.getFluid().isEmpty()) {
            ingredients.setInput(VanillaTypes.FLUID, in.getFluid());
        }
        AmadronOffer.TradeResource out = recipe.getOutput();
        if (!out.getItem().isEmpty()) {
            ingredients.setOutput(VanillaTypes.ITEM, out.getItem());
        } else if (!out.getFluid().isEmpty()) {
            ingredients.setOutput(VanillaTypes.FLUID, out.getFluid());
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, AmadronOffer recipe, IIngredients ingredients) {
        AmadronOffer.TradeResource in = recipe.getInput();
        if (!in.getItem().isEmpty()) {
            recipeLayout.getItemStacks().init(0, true, 6, 15);
            recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        } else if (!in.getFluid().isEmpty()) {
            recipeLayout.getFluidStacks().init(0, true, 6, 15);
            recipeLayout.getFluidStacks().set(0, ingredients.getInputs(VanillaTypes.FLUID).get(0));
        }
        AmadronOffer.TradeResource out = recipe.getOutput();
        if (!out.getItem().isEmpty()) {
            recipeLayout.getItemStacks().init(1, false, 51, 15);
            recipeLayout.getItemStacks().set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
        } else if (!out.getFluid().isEmpty()) {
            recipeLayout.getFluidStacks().init(1, false, 51, 15);
            recipeLayout.getFluidStacks().set(1, ingredients.getOutputs(VanillaTypes.FLUID).get(0));
        }

        addWidget(new WidgetAmadronOffer(0, 0, recipe).setDrawBackground(false).setCanBuy(true));
    }
}
