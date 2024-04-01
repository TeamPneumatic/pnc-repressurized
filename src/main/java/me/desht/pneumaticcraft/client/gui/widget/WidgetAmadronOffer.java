/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class WidgetAmadronOffer extends AbstractWidget {
    private final AmadronOffer offer;
    private final List<AbstractWidget> subWidgets = new ArrayList<>();
    private int shoppingAmount;
    private boolean canBuy;
    private final Rect2i[] tooltipRectangles = new Rect2i[2];
    private boolean renderBackground = true;

    public WidgetAmadronOffer(int x, int y, AmadronOffer offer) {
        super(x, y, 73, 35, Component.empty());
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
            List<Component> l = new ArrayList<>(GuiUtils.xlateAndSplit("pneumaticcraft.gui.amadron.amadronWidget.sneakRightClickToRemove"));
            l.add(Component.empty());
            WidgetButtonExtended btn = new WidgetButtonExtended(x + 57, y + 1, 11, 11, Component.literal(ChatFormatting.RED + "x"),
                    b -> NetworkHandler.sendToServer(new PacketGuiButton("remove:" + offer.getOfferId())));
            btn.setTooltip(Tooltip.create(PneumaticCraftUtils.combineComponents(l)));
            subWidgets.add(btn);
        }
        tooltipRectangles[0] = new Rect2i(x + 5, y + 14, 18, 18);
        tooltipRectangles[1] = new Rect2i(x + 50, y + 14, 18, 18);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return subWidgets.stream().anyMatch(w -> w.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Font fr = Minecraft.getInstance().font;
        if (renderBackground) {
            graphics.blit(Textures.WIDGET_AMADRON_OFFER, getX(), getY(), 0, 0, width, height, 256, 256);
        }
        FormattedCharSequence r = fr.split(offer.getVendorName(), 73).get(0);
        graphics.drawString(fr, r, getX() + 2, getY() + 2, 0xFF000000, false);
        if (shoppingAmount > 0) {
            String str = Integer.toString(shoppingAmount);
            graphics.drawString(fr, str, getX() + 36 - fr.width(str) / 2f, getY() + (offer.getStock() >= 0 ? 15 : 20), 0xFF000000, false);
        }
        if (offer.getStock() >= 0) {
            String str = ChatFormatting.DARK_BLUE.toString() + offer.getStock();
            graphics.drawString(fr, str, getX() + 36 - fr.width(str) / 2f, getY() + 25, 0xFF000000, false);
        }
        boolean availableHere = offer.isUsableByPlayer(ClientUtils.getClientPlayer());
        if (offer.isLocationLimited()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            graphics.blit(availableHere ? Textures.GUI_OK_LOCATION : Textures.GUI_BAD_LOCATION, getX() + width - 15, getY() - 1, 0, 0, 16, 16);
            RenderSystem.disableBlend();
        }
        if (!canBuy || !availableHere) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xC0804040);
        }

        subWidgets.forEach(w -> w.render(graphics, mouseX, mouseY, partialTick));

        if (isHovered && Arrays.stream(tooltipRectangles).noneMatch(rect -> rect.contains(mouseX, mouseY))) {
            graphics.renderTooltip(Minecraft.getInstance().font, makeTooltip(offer, shoppingAmount), Optional.empty(), mouseX, mouseY);
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

    public AmadronOffer getOffer() {
        return offer;
    }

    public void setShoppingAmount(int amount) {
        shoppingAmount = amount;
    }

    public static List<Component> makeTooltip(AmadronOffer offer, int shoppingAmount) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.vendor",
                offer.getVendorName().copy().withStyle(ChatFormatting.WHITE))
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.selling",
                Component.literal(offer.getOutput().toString()).withStyle(ChatFormatting.WHITE))
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.buying",
                Component.literal(offer.getInput().toString()).withStyle(ChatFormatting.WHITE))
                .withStyle(ChatFormatting.YELLOW));
        if (shoppingAmount >= 0) {
            if (offer.getStock() >= 0) {
                tooltip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.stock",
                        Component.literal(Integer.toString(offer.getStock())).withStyle(ChatFormatting.WHITE))
                        .withStyle(ChatFormatting.AQUA));
            }
            tooltip.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.inBasket",
                    Component.literal(Integer.toString(shoppingAmount)).withStyle(ChatFormatting.WHITE))
                    .withStyle(ChatFormatting.AQUA));
        }
        if (!offer.isUsableByPlayer(ClientUtils.getClientPlayer())) {
            tooltip.add(xlate("pneumaticcraft.playerFilter.unavailable").withStyle(ChatFormatting.RED));
        }
        offer.addAvailabilityData(ClientUtils.getClientPlayer(), tooltip);
        if (Minecraft.getInstance().options.advancedItemTooltips) {
            tooltip.add(Component.literal(offer.getOfferId().toString()).withStyle(ChatFormatting.DARK_GRAY));
        }
        return tooltip;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }

    private static class WidgetItemStack extends WidgetButtonExtended {
        public WidgetItemStack(int startX, int startY, ItemStack stack) {
            super(startX, startY, 16, 16);
            setRenderStacks(stack);
            setTooltip(Tooltip.create(PneumaticCraftUtils.combineComponents(stack.getTooltipLines(Minecraft.getInstance().player, TooltipFlag.Default.NORMAL))));
            setVisible(false);
            setRenderStackSize(true);
        }

        @Override
        protected boolean isValidClickButton(int pButton) {
            return false;  // not a clickable widget; just for display
        }
    }
}
