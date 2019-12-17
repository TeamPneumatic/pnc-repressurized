package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer.TradeResource;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferCustom;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.text.WordUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WidgetAmadronOffer extends Widget implements ITooltipProvider {
    private final AmadronOffer offer;
    private final List<Widget> subWidgets = new ArrayList<>();
    private int shoppingAmount;
    private boolean canBuy;
    private final Rectangle[] tooltipRectangles = new Rectangle[2];
    private boolean renderBackground = true;

    public WidgetAmadronOffer(int x, int y, AmadronOffer offer) {
        super(x, y, 73, 35, "");
        this.offer = offer;
        if (offer.getInput().getType() == TradeResource.Type.FLUID) {
            subWidgets.add(new WidgetFluidStack(x + 6, y + 15, offer.getInput().getFluid(), null));
        }
        if (offer.getOutput().getType() == TradeResource.Type.FLUID) {
            subWidgets.add(new WidgetFluidStack(x + 51, y + 15, offer.getOutput().getFluid(), null));
        }
        tooltipRectangles[0] = new Rectangle(x + 6, y + 15, 16, 16);
        tooltipRectangles[1] = new Rectangle(x + 51, y + 15, 16, 16);
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTick) {
        FontRenderer fr = Minecraft.getInstance().fontRenderer;
        if (renderBackground) {
            Minecraft.getInstance().getTextureManager().bindTexture(Textures.WIDGET_AMADRON_OFFER);
            GlStateManager.color4f(1f, canBuy ? 1f : 0.4f, canBuy ? 1f : 0.4f, canBuy ? 0.75f : 1f);
            AbstractGui.blit(x, y, 0, 0, width, height, 256, 256);
        }
        for (Widget widget : subWidgets) {
            widget.render(mouseX, mouseY, partialTick);
        }
        fr.drawString(offer.getVendor(), x + 2, y + 2, 0xFF000000);
        boolean customOffer = offer instanceof AmadronOfferCustom;
        if (shoppingAmount > 0) {
            fr.drawString(TextFormatting.BLACK.toString() + shoppingAmount, x + 36 - fr.getStringWidth("" + shoppingAmount) / 2f, y + (customOffer ? 15 : 20), 0xFF000000);
        }
        if (customOffer) {
            AmadronOfferCustom custom = (AmadronOfferCustom) offer;
            fr.drawString(TextFormatting.DARK_BLUE.toString() + custom.getStock(), x + 36 - fr.getStringWidth("" + custom.getStock()) / 2f, y + 25, 0xFF000000);
        }
    }

    public WidgetAmadronOffer setDrawBackground(boolean drawBackground) {
        renderBackground = drawBackground;
        return this;
    }

    public WidgetAmadronOffer setCanBuy(boolean canBuy) {
        this.canBuy = canBuy;
        return this;
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<String> curTip, boolean shiftPressed) {
        for (Widget widget : subWidgets) {
            if (widget.isHovered() && widget instanceof ITooltipProvider) {
                ((ITooltipProvider) widget).addTooltip(mouseX, mouseY, curTip, shiftPressed);
            }
        }
        boolean isInBounds = false;
        for (Rectangle rect : tooltipRectangles) {
            if (rect.contains(mouseX, mouseY)) {
                isInBounds = true;
            }
        }
        if (!isInBounds) {
            curTip.add(I18n.format("gui.amadron.amadronWidget.vendor", offer.getVendor()));
            curTip.add(I18n.format("gui.amadron.amadronWidget.selling", offer.getOutput().toString()));
            curTip.add(I18n.format("gui.amadron.amadronWidget.buying", offer.getInput().toString()));
            curTip.add(I18n.format("gui.amadron.amadronWidget.inBasket", shoppingAmount));
            if (offer.getStock() >= 0) curTip.add(I18n.format("gui.amadron.amadronWidget.stock", offer.getStock()));
            // todo we should be using UUID here
            if (offer.getVendor().equals(Minecraft.getInstance().player.getGameProfile().getName())) {
                curTip.addAll(Arrays.asList(WordUtils.wrap(I18n.format("gui.amadron.amadronWidget.sneakRightClickToRemove"), 40).split(System.getProperty("line.separator"))));
            }
        }
    }


    public AmadronOffer getOffer() {
        return offer;
    }

    public void setShoppingAmount(int amount) {
        shoppingAmount = amount;
    }
}
