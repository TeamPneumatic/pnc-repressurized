package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
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
    private final AmadronRecipe offer;
    private final List<Widget> subWidgets = new ArrayList<>();
    private int shoppingAmount;
    private boolean canBuy;
    private final Rectangle2d[] tooltipRectangles = new Rectangle2d[2];
    private boolean renderBackground = true;

    public WidgetAmadronOffer(int x, int y, AmadronRecipe offer) {
        super(x, y, 73, 35, StringTextComponent.EMPTY);
        this.offer = offer;
        if (offer.getInput().getType() == AmadronTradeResource.Type.FLUID) {
            subWidgets.add(new WidgetFluidStack(x + 6, y + 15, offer.getInput().getFluid().copy(), null));
        }
        if (offer.getOutput().getType() == AmadronTradeResource.Type.FLUID) {
            subWidgets.add(new WidgetFluidStack(x + 51, y + 15, offer.getOutput().getFluid().copy(), null));
        }
        if (offer.isRemovableBy(Minecraft.getInstance().player)) {
            List<ITextComponent> l = new ArrayList<>(GuiUtils.xlateAndSplit("pneumaticcraft.gui.amadron.amadronWidget.sneakRightClickToRemove"));
            l.add(StringTextComponent.EMPTY);
            subWidgets.add(new WidgetButtonExtended(x + 57, y + 1, 11, 11, new StringTextComponent(TextFormatting.RED + "x"),
                    b -> NetworkHandler.sendToServer(new PacketGuiButton("remove:" + offer.getId())))
                    .setTooltipText(l));
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return subWidgets.stream().anyMatch(w -> w.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        if (visible) {
            FontRenderer fr = Minecraft.getInstance().font;
            if (renderBackground) {
                Minecraft.getInstance().getTextureManager().bind(Textures.WIDGET_AMADRON_OFFER);
                AbstractGui.blit(matrixStack, x, y, 0, 0, width, height, 256, 256);
            }
            IReorderingProcessor r = fr.split(offer.getVendorName(), 73).get(0);
            fr.draw(matrixStack, r, x + 2, y + 2, 0xFF000000);
            if (shoppingAmount > 0) {
                String str = Integer.toString(shoppingAmount);
                fr.draw(matrixStack,str, x + 36 - fr.width(str) / 2f, y + (offer.getStock() >= 0 ? 15 : 20), 0xFF000000);
            }
            if (offer.getStock() >= 0) {
                String str = TextFormatting.DARK_BLUE.toString() + offer.getStock();
                fr.draw(matrixStack, str, x + 36 - fr.width(str) / 2f, y + 25, 0xFF000000);
            }
            if (!canBuy) {
                AbstractGui.fill(matrixStack, x, y, x + width, y + height, 0xC0804040);
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
            curTip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.vendor", offer.getVendorName()));
            curTip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.selling", offer.getOutput().toString()));
            curTip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.buying", offer.getInput().toString()));
            if (offer.getStock() >= 0) curTip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.stock", offer.getStock()));
            curTip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.inBasket", shoppingAmount));

            if (Minecraft.getInstance().options.advancedItemTooltips) {
                curTip.add(new StringTextComponent(offer.getId().toString()).withStyle(TextFormatting.DARK_GRAY));
            }
        }
    }

    public AmadronRecipe getOffer() {
        return offer;
    }

    public void setShoppingAmount(int amount) {
        shoppingAmount = amount;
    }
}
