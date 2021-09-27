package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

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

        offer.getInput().accept(
                itemStack -> subWidgets.add(new WidgetItemStack(x + 6, y + 13, itemStack)),
                fluidStack -> subWidgets.add(new WidgetFluidStack(x + 6, y + 15, fluidStack.copy(), null))
        );
        offer.getOutput().accept(
                itemStack -> subWidgets.add(new WidgetItemStack(x + 51, y + 13, itemStack)),
                fluidStack -> subWidgets.add(new WidgetFluidStack(x + 51, y + 15, fluidStack.copy(), null))
        );

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
            boolean availableHere = offer.isUseableByPlayer(ClientUtils.getClientPlayer());
            if (offer.isLocationLimited()) {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GuiUtils.drawTexture(matrixStack, availableHere ? Textures.GUI_OK_LOCATION : Textures.GUI_BAD_LOCATION, x + width - 15, y - 1);
                RenderSystem.disableBlend();
            }
            if (!canBuy || !availableHere) {
                AbstractGui.fill(matrixStack, x, y, x + width, y + height, 0xC0804040);
            }
        }
    }

    public WidgetAmadronOffer setDrawBackground(boolean drawBackground) {
        renderBackground = drawBackground;
        return this;
    }

    public WidgetAmadronOffer setAffordable(boolean canBuy) {
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
            addTooltip(offer, curTip, shoppingAmount);
        }
    }

    public AmadronRecipe getOffer() {
        return offer;
    }

    public void setShoppingAmount(int amount) {
        shoppingAmount = amount;
    }

    public static void addTooltip(AmadronRecipe offer, List<ITextComponent> curTip, int shoppingAmount) {
        curTip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.vendor",
                offer.getVendorName().copy().withStyle(TextFormatting.WHITE))
                .withStyle(TextFormatting.YELLOW));
        curTip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.selling",
                new StringTextComponent(offer.getOutput().toString()).withStyle(TextFormatting.WHITE))
                .withStyle(TextFormatting.YELLOW));
        curTip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.buying",
                new StringTextComponent(offer.getInput().toString()).withStyle(TextFormatting.WHITE))
                .withStyle(TextFormatting.YELLOW));
        if (shoppingAmount >= 0) {
            if (offer.getStock() >= 0) {
                curTip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.stock",
                        new StringTextComponent(Integer.toString(offer.getStock())).withStyle(TextFormatting.WHITE))
                        .withStyle(TextFormatting.AQUA));
            }
            curTip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.inBasket",
                    new StringTextComponent(Integer.toString(shoppingAmount)).withStyle(TextFormatting.WHITE))
                    .withStyle(TextFormatting.AQUA));
        }
        if (!offer.isUseableByPlayer(ClientUtils.getClientPlayer())) {
            curTip.add(xlate("pneumaticcraft.gui.amadron.location.unavailable").withStyle(TextFormatting.RED));
        }
        offer.addAvailabilityData(curTip);
        if (Minecraft.getInstance().options.advancedItemTooltips) {
            curTip.add(new StringTextComponent(offer.getId().toString()).withStyle(TextFormatting.DARK_GRAY));
        }
    }

    private static class WidgetItemStack extends WidgetButtonExtended {
        public WidgetItemStack(int startX, int startY, ItemStack stack) {
            super(startX, startY, 16, 16);
            setRenderStacks(stack);
            setTooltipText(stack.getTooltipLines(Minecraft.getInstance().player, ITooltipFlag.TooltipFlags.NORMAL));
            setVisible(false);
            setRenderStackSize(true);
        }

        @Override
        protected boolean isValidClickButton(int pButton) {
            return false;  // not a clickable widget; just for display
        }
    }
}
