package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.client.gui.widget.WidgetAmadronOffer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.stream.Collectors;

public class JEIAmadronTradeCategory extends PneumaticCraftCategory<JEIAmadronTradeCategory.AmadronOfferWrapper> {
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
    public Class<? extends JEIAmadronTradeCategory.AmadronOfferWrapper> getRecipeClass() {
        return AmadronOfferWrapper.class;
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

    static Collection<AmadronOfferWrapper> getAllRecipes() {
        return AmadronOfferManager.getInstance().getAllOffers().stream()
                .map(AmadronOfferWrapper::new)
                .collect(Collectors.toList());
    }

    static class AmadronOfferWrapper extends PneumaticCraftCategory.AbstractCategoryExtension {
        AmadronOfferWrapper(AmadronOffer offer) {
            if (offer.getInput().getType() == AmadronOffer.TradeResource.Type.ITEM)
                addInputItem(PositionedStack.of(offer.getInput().getItem(), 6, 15));
            if (offer.getOutput().getType() == AmadronOffer.TradeResource.Type.ITEM)
                addOutputItem(PositionedStack.of(offer.getOutput().getItem(), 51, 15));
            if (offer.getInput().getType() == AmadronOffer.TradeResource.Type.FLUID)
                addInputFluid(new WidgetCustomTank(6, 15, offer.getInput().getFluid()));
            if (offer.getOutput().getType() == AmadronOffer.TradeResource.Type.FLUID)
                addInputFluid(new WidgetCustomTank(51, 15, offer.getOutput().getFluid()));
            WidgetAmadronOffer widget = new WidgetAmadronOffer(0, 0, offer).setDrawBackground(false);
            widget.setCanBuy(true);
            addWidget(widget);
        }

    }

    private static class WidgetCustomTank extends WidgetTank {
        WidgetCustomTank(int x, int y, FluidStack stack) {
            super(x, y, 16, 16, stack);
        }

        @Override
        public void renderButton(int mouseX, int mouseY, float partialTick) {
        }
    }
}
