package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.client.gui.widget.WidgetAmadronOffer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.IJeiHelpers;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class JEIAmadronTradeCategory extends PneumaticCraftCategory<JEIAmadronTradeCategory.AmadronOfferWrapper> {
    public JEIAmadronTradeCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);
    }

    @Override
    public String getUid() {
        return ModCategoryUid.AMADRON_TRADE;
    }

    @Override
    public String getTitle() {
        return I18n.format(Itemss.AMADRON_TABLET.getUnlocalizedName() + ".name");
    }

    @Override
    public ResourceDrawable getGuiTexture() {
        return new ResourceDrawable(Textures.WIDGET_AMADRON_OFFER_STRING, 0, 0, 0, 0, 73, 35);
    }

    static class AmadronOfferWrapper extends PneumaticCraftCategory.MultipleInputOutputRecipeWrapper {
        private AmadronOfferWrapper(AmadronOffer offer) {
            if (offer.getInput() instanceof ItemStack)
                addIngredient(new PositionedStack((ItemStack) offer.getInput(), 6, 15));
            if (offer.getOutput() instanceof ItemStack)
                addOutput(new PositionedStack((ItemStack) offer.getOutput(), 51, 15));
            if (offer.getInput() instanceof FluidStack)
                addInputLiquid(new WidgetCustomTank(6, 15, (FluidStack) offer.getInput()));
            if (offer.getOutput() instanceof FluidStack)
                addOutputLiquid(new WidgetCustomTank(51, 15, (FluidStack) offer.getOutput()));
            WidgetAmadronOffer widget = new WidgetAmadronOffer(0, 0, 0, offer).setDrawBackground(false);
            widget.setCanBuy(true);
            addWidget(widget);
        }
    }

    protected List<MultipleInputOutputRecipeWrapper> getAllRecipes() {
        List<MultipleInputOutputRecipeWrapper> recipes = new ArrayList<MultipleInputOutputRecipeWrapper>();
        for (AmadronOffer recipe : AmadronOfferManager.getInstance().getAllOffers()) {
            recipes.add(new AmadronOfferWrapper(recipe));
        }
        return recipes;
    }

    private static class WidgetCustomTank extends WidgetTank {

        public WidgetCustomTank(int x, int y, FluidStack stack) {
            super(x, y, 16, 16, stack);
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTick) {

        }

    }

    //    @Override
//    public Class<AmadronOffer> getRecipeClass() {
//        return AmadronOffer.class;
//    }
//
//    @Override
//    public IRecipeWrapper getRecipeWrapper(AmadronOffer recipe) {
//        return new AmadronOfferWrapper(recipe);
//    }
//
//    @Override
//    public boolean isRecipeValid(AmadronOffer recipe) {
//        return true;
//    }

}
