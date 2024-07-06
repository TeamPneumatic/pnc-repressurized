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

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import me.desht.pneumaticcraft.client.gui.remote.RemoteLayout;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.*;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.inventory.RemoteMenu;
import me.desht.pneumaticcraft.common.item.RemoteItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateRemoteLayout;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.util.legacyconv.ConversionType;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class RemoteEditorScreen extends RemoteScreen {
    private InventorySearcherScreen invSearchGui;
    private PastebinScreen pastebinGui;
    private final List<ActionWidget<?>> widgetTray = new ArrayList<>();
    private ActionWidget<?> draggingWidget;
    private int dragMouseStartX, dragMouseStartY;
    private int dragWidgetStartX, dragWidgetStartY;
    private int oldGuiLeft, oldGuiTop;

    public RemoteEditorScreen(RemoteMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        imageWidth = 283;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_REMOTE_EDITOR;
    }

    @Override
    public void init() {
        if (pastebinGui != null && pastebinGui.getOutput() != null) {
            RemoteLayout layout = RemoteLayout.fromJson(registryAccess(), pastebinGui.getOutput());
            RemoteItem.setSavedLayout(remote, layout.toNBT(registryAccess()));
        } else if (remoteLayout != null) {
            RemoteItem.setSavedLayout(remote, remoteLayout.toNBT(registryAccess()));
        }

        if (invSearchGui != null && invSearchGui.getSearchStack().getItem() == ModItems.REMOTE.get()) {
            if (RemoteItem.hasSameSecuritySettings(remote, invSearchGui.getSearchStack())) {
                CustomData data = invSearchGui.getSearchStack().getOrDefault(ModDataComponents.REMOTE_LAYOUT, CustomData.EMPTY);
                remoteLayout = RemoteLayout.fromNBT(registryAccess(), data.copyTag());
            } else {
                ClientUtils.getClientPlayer().displayClientMessage(Component.literal("pneumaticcraft.gui.remote.differentSecuritySettings"), false);
            }
        }

        super.init();

        oldGuiLeft = leftPos;
        oldGuiTop = topPos;

        widgetTray.clear();
        widgetTray.add(new ActionWidgetCheckBox(this, new WidgetCheckBox(leftPos + 200, topPos + 23, 0xFF404040, xlate("pneumaticcraft.gui.remote.tray.checkbox.name"))));
        widgetTray.add(new ActionWidgetLabel(this, new WidgetLabelVariable(leftPos + 200, topPos + 38, xlate("pneumaticcraft.gui.remote.tray.label.name"))));
        widgetTray.add(new ActionWidgetButton(this, new WidgetButtonExtended(leftPos + 200, topPos + 53, 50, 20, xlate("pneumaticcraft.gui.remote.tray.button.name"))));
        widgetTray.add(new ActionWidgetDropdown(this, new WidgetComboBox(font, leftPos + 200, topPos + 79, 70, font.lineHeight + 3).setFixedOptions(true)));

        for (ActionWidget<?> actionWidget : widgetTray) {
            addRenderableWidget(actionWidget.getOrCreateMinecraftWidget(this));
        }

        var importBtn = new WidgetButtonExtended(leftPos - 24, topPos, 20, 20, Component.empty(), b -> doImport())
                .setRenderStacks(new ItemStack(ModItems.REMOTE.get()));
        importBtn.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.remote.button.importRemoteButton")));
        addRenderableWidget(importBtn);

        var pastebinBtn = new WidgetButtonExtended(leftPos - 24, topPos + 22, 20, 20, Component.empty(), b -> doPastebin())
                .setRenderedIcon(Textures.GUI_PASTEBIN_ICON_LOCATION);
        pastebinBtn.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.remote.button.pastebinButton")));
        addRenderableWidget(pastebinBtn);

        WidgetCheckBox snapCheck = new WidgetCheckBox(leftPos + 194, topPos + 105, 0xFF404040, xlate("pneumaticcraft.gui.misc.snapToGrid"),
                b -> ConfigHelper.setGuiRemoteGridSnap(b.checked));
        snapCheck.checked = ConfigHelper.client().general.guiRemoteGridSnap.get();
        addRenderableWidget(snapCheck);

        addRenderableWidget(new WidgetLabel(leftPos + 234, topPos + 7, xlate("pneumaticcraft.gui.remote.widgetTray").withStyle(ChatFormatting.DARK_BLUE)).setAlignment(WidgetLabel.Alignment.CENTRE));
    }

    private void doImport() {
        ClientUtils.openContainerGui(ModMenuTypes.INVENTORY_SEARCHER.get(), Component.translatable("pneumaticcraft.gui.amadron.addTrade.invSearch"));
        if (minecraft.screen instanceof InventorySearcherScreen) {
            invSearchGui = (InventorySearcherScreen) minecraft.screen;
            invSearchGui.setStackPredicate(s -> s.getItem() == ModItems.REMOTE.get());
        }
    }

    private void doPastebin() {
        JsonElement json = remoteLayout.toJson(registryAccess());
        minecraft.setScreen(pastebinGui = new PastebinScreen(this, json, ConversionType.ACTION_WIDGET));
    }

    @Override
    protected boolean shouldDrawBackground() {
        return false;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        graphics.blit(getGuiTexture(), leftPos, topPos, 0, 0, imageWidth, imageHeight, 320, 256);
    }

    @Override
    protected PointXY getInvNameOffset() {
        return new PointXY(-50, 0);
    }

    private boolean isOutsideProgrammingArea(ActionWidget<?> actionWidget) {
        AbstractWidget w = actionWidget.getOrCreateMinecraftWidget(this);
        return w.getX() < leftPos || w.getY() < topPos || w.getX() + w.getWidth() > leftPos + 183 || w.getY() + w.getHeight() > topPos + imageHeight;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        int x = (int) mouseX;
        int y = (int) mouseY;

        switch (mouseButton) {
            case 0 -> {
                // left click - drag widget
                for (ActionWidget<?> actionWidget : widgetTray) {
                    if (actionWidget.getOrCreateMinecraftWidget(this).isHoveredOrFocused()) {
                        // create new widget from tray
                        startDrag(actionWidget.copy(registryAccess()), x, y);
                        remoteLayout.addActionWidget(draggingWidget);
                        addRenderableWidget(draggingWidget.getOrCreateMinecraftWidget(this));
                        return true;
                    }
                }
                if (draggingWidget == null) {
                    for (ActionWidget<?> actionWidget : remoteLayout.getActionWidgets()) {
                        if (actionWidget.getOrCreateMinecraftWidget(this).isHoveredOrFocused()) {
                            // move existing widget
                            startDrag(actionWidget, x, y);
                            return true;
                        }
                    }
                }
            }
            case 1 -> {
                // right click - configure widget
                for (ActionWidget<?> actionWidget : remoteLayout.getActionWidgets()) {
                    if (!isOutsideProgrammingArea(actionWidget)) {
                        if (actionWidget.getOrCreateMinecraftWidget(this).isHoveredOrFocused()) {
                            Screen screen = actionWidget.createConfigurationGui(this);
                            if (screen != null) minecraft.setScreen(screen);
                            return true;
                        }
                    }
                }
            }
            case 2 -> {
                // middle click - copy existing widget
                for (ActionWidget<?> actionWidget : remoteLayout.getActionWidgets()) {
                    if (actionWidget.getOrCreateMinecraftWidget(this).isHoveredOrFocused()) {
                        startDrag(actionWidget.copy(registryAccess()), x, y);
                        remoteLayout.addActionWidget(draggingWidget);
                        addRenderableWidget(draggingWidget.getOrCreateMinecraftWidget(this));
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void startDrag(ActionWidget<?> widget, int x, int y) {
        draggingWidget = widget;
        dragMouseStartX = x;
        dragMouseStartY = y;
        dragWidgetStartX = widget.getOrCreateMinecraftWidget(this).getX();
        dragWidgetStartY = widget.getOrCreateMinecraftWidget(this).getY();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingWidget != null && isOutsideProgrammingArea(draggingWidget)) {
            remoteLayout.removeActionWidget(draggingWidget);
//            remoteLayout.getActionWidgets().remove(draggingWidget);
            removeWidget(draggingWidget.getOrCreateMinecraftWidget(this));
        }
        draggingWidget = null;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        if (draggingWidget != null) {
            int x = (int) mouseX;
            int y = (int) mouseY;
            int x1 = x - dragMouseStartX + dragWidgetStartX;
            int y1 = y - dragMouseStartY + dragWidgetStartY;
            if (ConfigHelper.client().general.guiRemoteGridSnap.get()) {
                x1 = (x1 / 4) * 4;
                y1 = (y1 / 4) * 4;
            }
            draggingWidget.setWidgetPos(this,x1, y1);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
    }

    @Override
    public void onGlobalVariableChange(String variable) {
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void removed() {
        ItemStack stack = ClientUtils.getClientPlayer().getItemInHand(menu.getHand());
        if (stack.getItem() == ModItems.REMOTE.get()) {
            CompoundTag tag = remoteLayout.toNBT(registryAccess());
            RemoteItem.setSavedLayout(stack, tag);
            NetworkHandler.sendToServer(new PacketUpdateRemoteLayout(tag, menu.getHand()));
        }

        super.removed();
    }
}
