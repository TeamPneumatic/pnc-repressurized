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

package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.inventory.InventorySearcherMenu;
import me.desht.pneumaticcraft.common.item.GPSAreaToolItem;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Predicate;

public class InventorySearcherScreen extends AbstractContainerScreen<InventorySearcherMenu> {
    private static final int SEARCH_SLOT = 36;

    private final ItemStackHandler inventory = new ItemStackHandler(1);
    private final Screen parentScreen;
    private Predicate<ItemStack> stackPredicate = itemStack -> true;
    private WidgetLabel label;
    private int clickedMouseButton;
    private long lastClickTime = 0L;

    public InventorySearcherScreen(InventorySearcherMenu container, Inventory inv, Component title) {
        super(container, inv, title);

        inv.player.containerMenu = container;
//        passEvents = true;
        imageHeight = 176;
        parentScreen = Minecraft.getInstance().screen;
        container.init(inventory);
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(label = new WidgetLabel(leftPos + 105, topPos + 28, Component.empty(), 0xFF404080));
    }

    @Override
    public void onClose() {
        if (parentScreen != null) {
            ClientUtils.closeContainerGui(parentScreen);
        } else {
            super.onClose();
        }
    }

    public void setStackPredicate(Predicate<ItemStack> predicate) {
        stackPredicate = predicate;
    }

    @Nonnull
    public ItemStack getSearchStack() {
        return inventory.getStackInSlot(0);
    }

    public void setSearchStack(@Nonnull ItemStack stack) {
        if (!stack.isEmpty() && stackPredicate.test(stack)) {
            inventory.setStackInSlot(0, stack.copyWithCount(1));
        }
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType clickType) {
        if (slot != null) {
            if (slot.index == SEARCH_SLOT) {
                clickedMouseButton = 0;
                slot.set(ItemStack.EMPTY);
            } else {
                long now = Util.getMillis();
                if (now - lastClickTime < 250 && ItemStack.isSameItemSameComponents(getSearchStack(), slot.getItem())) {
                    onClose();
                } else {
                    clickedMouseButton = mouseButton;
                    setSearchStack(slot.getItem());
                }
                lastClickTime = now;
            }
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (inventory.getStackInSlot(0).getItem() instanceof IPositionProvider) {
            label.setMessage(Component.literal(PneumaticCraftUtils.posToString(getBlockPos())));
        } else {
            label.setMessage(Component.empty());
        }
    }

    /**
     * Special case for when the searched item is a position provider
     * @return the selected blockpos, or null if the search item is not a position provider
     */
    public BlockPos getBlockPos() {
        ItemStack stack = inventory.getStackInSlot(0);
        if (stack.getItem() instanceof IPositionProvider pp) {
            List<BlockPos> posList = pp.getRawStoredPositions(ClientUtils.getClientPlayer(), stack);
            int posIdx = getPosIdx(stack);
            if (!posList.isEmpty()) {
                return posList.get(Math.min(posIdx, posList.size() - 1));
            }
        }
        return null;
    }

    private int getPosIdx(ItemStack stack) {
        if (stack.getItem() instanceof GPSAreaToolItem) {
            // for gps area tool, RMB is idx 0, LMB is idx 1
            return switch (clickedMouseButton) {
                case 0 -> 1;  // LMB
                case 1 -> 0;  // RMB
                default -> 1;  // any other button
            };
        } else {
            return clickedMouseButton;
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;
        graphics.blit(Textures.GUI_INVENTORY_SEARCHER, xStart, yStart, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, getTitle(), this.width / 2, 5, 0x404040, false);

        // darken out all non-matching slots
        for (int i = 0; i < this.menu.slots.size() - 1; ++i) {
            Slot slot = this.menu.slots.get(i);
            if (!stackPredicate.test(slot.getItem())) {
                RenderSystem.colorMask(true, true, true, false);
                graphics.fillGradient(slot.x, slot.y, slot.x + 16, slot.y + 16, 0xC0202020, 0xC0202020);
                RenderSystem.colorMask(true, true, true, true);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int par1, int par2, float par3) {
        super.render(graphics, par1, par2, par3);

        if (this.hoveredSlot != null && stackPredicate.test(this.hoveredSlot.getItem())) {
            renderTooltip(graphics, par1, par2);
        }
    }
}
