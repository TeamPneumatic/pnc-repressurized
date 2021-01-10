package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class WidgetAmadronOffer extends Widget implements ITooltipProvider {
    private final AmadronOffer offer;
    private final List<Widget> subWidgets = new ArrayList<>();
    private int shoppingAmount;
    private boolean canBuy;
    private final Rectangle2d[] tooltipRectangles = new Rectangle2d[2];
    private boolean renderBackground = true;

    public WidgetAmadronOffer(int x, int y, AmadronOffer offer) {
        super(x, y, 73, 35, StringTextComponent.EMPTY);
        this.offer = offer;
        if (offer.getInput().getType() == AmadronTradeResource.Type.FLUID) {
            subWidgets.add(new WidgetFluidStack(x + 6, y + 15, offer.getInput().getFluid(), null));
        }
        if (offer.getOutput().getType() == AmadronTradeResource.Type.FLUID) {
            subWidgets.add(new WidgetFluidStack(x + 51, y + 15, offer.getOutput().getFluid(), null));
        }
        tooltipRectangles[0] = new Rectangle2d(x + 5, y + 14, 18, 18);
        tooltipRectangles[1] = new Rectangle2d(x + 50, y + 14, 18, 18);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        subWidgets.forEach(w -> w.render(matrixStack, mouseX, mouseY, partialTicks));
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        if (visible) {
            FontRenderer fr = Minecraft.getInstance().fontRenderer;
            if (renderBackground) {
                Minecraft.getInstance().getTextureManager().bindTexture(Textures.WIDGET_AMADRON_OFFER);
                RenderSystem.color4f(1f, canBuy ? 1f : 0.4f, canBuy ? 1f : 0.4f, canBuy ? 0.75f : 1f);
                AbstractGui.blit(matrixStack, x, y, 0, 0, width, height, 256, 256);
            }
            IReorderingProcessor r = fr.trimStringToWidth(new StringTextComponent(offer.getVendor()).mergeStyle(canBuy ? TextFormatting.BLACK : TextFormatting.DARK_GRAY), 73).get(0);
            fr.func_238422_b_(matrixStack, r, x + 2, y + 2, 0xFF000000);
            if (shoppingAmount > 0) {
                String str = "" + shoppingAmount;
                fr.drawString(matrixStack,str, x + 36 - fr.getStringWidth(str) / 2f, y + (offer.getMaxStock() > 0 ? 15 : 20), 0xFF000000);
            }
            if (offer.getStock() >= 0) {
                String str = TextFormatting.DARK_BLUE.toString() + offer.getStock();
                fr.drawString(matrixStack,str, x + 36 - fr.getStringWidth(str) / 2f, y + 25, 0xFF000000);
            }
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
    public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTip, boolean shiftPressed) {
        int l = curTip.size();
        for (Widget widget : subWidgets) {
            if (widget.isHovered() && widget instanceof ITooltipProvider) {
                ((ITooltipProvider) widget).addTooltip(mouseX, mouseY, curTip, shiftPressed);
            }
        }
        boolean isInBounds = false;
        for (Rectangle2d rect : tooltipRectangles) {
            if (rect.contains((int)mouseX, (int)mouseY)) {
                isInBounds = true;
            }
        }
        if (!isInBounds) {
            curTip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.vendor", offer.getVendor()));
            curTip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.selling", offer.getOutput().toString()));
            curTip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.buying", offer.getInput().toString()));
            curTip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.inBasket", shoppingAmount));
            if (offer.getStock() >= 0) curTip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.stock", offer.getStock()));
            if (AmadronPlayerOffer.isPlayerOffer(offer, Minecraft.getInstance().player)) {
                curTip.add(StringTextComponent.EMPTY);
                curTip.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.amadron.amadronWidget.sneakRightClickToRemove"));
            }
            if (Minecraft.getInstance().gameSettings.advancedItemTooltips) {
                curTip.add(new StringTextComponent(offer.getId().toString()).mergeStyle(TextFormatting.DARK_GRAY));
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
