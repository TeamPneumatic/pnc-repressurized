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

package me.desht.pneumaticcraft.client.gui.remote;

import com.google.gson.JsonElement;
import me.desht.pneumaticcraft.api.remote.IRemoteWidget;
import me.desht.pneumaticcraft.client.gui.InventorySearcherScreen;
import me.desht.pneumaticcraft.client.gui.PastebinScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.inventory.RemoteMenu;
import me.desht.pneumaticcraft.common.item.RemoteItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateRemoteLayout;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.remote.*;
import me.desht.pneumaticcraft.common.util.legacyconv.ConversionType;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class RemoteEditorScreen extends AbstractRemoteScreen {
    private static final List<? extends IRemoteWidget> TRAY_WIDGETS = List.of(
            RemoteWidgetCheckbox.TRAY.get(),
            RemoteWidgetLabel.TRAY.get(),
            RemoteWidgetButton.TRAY.get(),
            RemoteWidgetDropdown.TRAY.get()
    );
    private static final int PROGRAMMING_AREA_WIDTH = 183;
    private static final Rect2i PROGRAMMING_AREA = new Rect2i(3, 18, 177, 181);
    private static final Rect2i TRAY_AREA = new Rect2i(195, 18, 80, 78);

    private InventorySearcherScreen invSearchGui;
    private PastebinScreen pastebinGui;
    private IRemoteWidget draggingRemoteWidget;
    private AbstractWidget draggingMCWidget;
    private AbstractWidget configuringMCWidget;

    public RemoteEditorScreen(RemoteMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        imageWidth = 283;
        imageHeight = 202;
    }

    @Override
    public void init() {
        super.init();

        if (pastebinGui != null && pastebinGui.getOutput() != null) {
            // returning from the pastebin gui; load widgets from retrieved Json
            addWidgetsFromLayout(SavedRemoteLayout.fromJson(registryAccess(), pastebinGui.getOutput()));
        } else if (invSearchGui != null && invSearchGui.getSearchStack().getItem() == ModItems.REMOTE.get()) {
            // return from item selection GUI; copy widgets from other remote
            if (RemoteItem.hasSameSecuritySettings(remoteItem, invSearchGui.getSearchStack())) {
                addWidgetsFromLayout(SavedRemoteLayout.fromItem(invSearchGui.getSearchStack()));
            } else {
                ClientUtils.getClientPlayer().displayClientMessage(Component.literal("pneumaticcraft.gui.remote.differentSecuritySettings"), false);
            }
        } else if (widgetMap.isEmpty()) {
            // initial opening of the editor screen; load saved widgets from the remote item data component
            addWidgetsFromLayout(SavedRemoteLayout.fromItem(remoteItem));
        } else {
            // doing a widget rebuild; re-create the mc widgets and update the mapping for the new mc widgets
            Map<AbstractWidget,IRemoteWidget> newMap = new LinkedHashMap<>();
            widgetMap.values().forEach(remoteWidget -> {
                AbstractWidget newMCWidget = RemoteClientRegistry.INSTANCE.createMinecraftWidget(remoteWidget, this);
                newMap.put(newMCWidget, remoteWidget);
                addRenderableWidget(newMCWidget);
            });
            widgetMap.clear();
            widgetMap.putAll(newMap);
        }

        var importBtn = new WidgetButtonExtended(leftPos - 24, topPos, 20, 20, Component.empty(), b -> openImportScreen())
                .setRenderStacks(new ItemStack(ModItems.REMOTE.get()));
        importBtn.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.remote.button.importRemoteButton")));
        addRenderableWidget(importBtn);

        var pastebinBtn = new WidgetButtonExtended(leftPos - 24, topPos + 22, 20, 20, Component.empty(), b -> openPastebinScreen())
                .setRenderedIcon(Textures.GUI_PASTEBIN_ICON_LOCATION);
        pastebinBtn.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.remote.button.pastebinButton")));
        addRenderableWidget(pastebinBtn);

        WidgetCheckBox snapCheck = new WidgetCheckBox(leftPos + 194, topPos + 105, 0xFF404040, xlate("pneumaticcraft.gui.misc.snapToGrid"),
                b -> ConfigHelper.setGuiRemoteGridSnap(b.checked));
        snapCheck.checked = ConfigHelper.client().general.guiRemoteGridSnap.get();
        addRenderableWidget(snapCheck);

        addRenderableWidget(new WidgetLabel(leftPos + 234, topPos + 7, xlate("pneumaticcraft.gui.remote.widgetTray").withStyle(ChatFormatting.DARK_BLUE)).setAlignment(WidgetLabel.Alignment.CENTRE));
    }

    private void addWidgetsFromLayout(SavedRemoteLayout layout) {
        widgetMap.clear();

        buildMinecraftWidgetList(layout.getWidgets(), this, false)
                .forEach(this::addRemoteWidget);

        for (IRemoteWidget remoteWidget : TRAY_WIDGETS) {
            addRemoteWidget(RemoteClientRegistry.INSTANCE.createMinecraftWidget(remoteWidget, this), remoteWidget);
        }
    }

    private void openImportScreen() {
        ClientUtils.openContainerGui(ModMenuTypes.INVENTORY_SEARCHER.get(), Component.translatable("pneumaticcraft.gui.amadron.addTrade.invSearch"));
        if (minecraft.screen instanceof InventorySearcherScreen) {
            invSearchGui = (InventorySearcherScreen) minecraft.screen;
            invSearchGui.setStackPredicate(s -> s.getItem() == ModItems.REMOTE.get());
        }
    }

    private void openPastebinScreen() {
        JsonElement json = makeNewLayout().toJson(registryAccess());
        minecraft.setScreen(pastebinGui = new PastebinScreen(this, json, ConversionType.ACTION_WIDGET));
    }

    private SavedRemoteLayout makeNewLayout() {
        List<IRemoteWidget> list = Util.make(new ArrayList<>(), l -> widgetMap.forEach((mcWidget, remoteWidget) -> {
            if (isInProgrammingArea(mcWidget)) {
                l.add(remoteWidget);
            }
        }));
        return new SavedRemoteLayout(list);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_REMOTE_EDITOR;
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

    private boolean isInTrayArea(AbstractWidget widget) {
        return TRAY_AREA.contains(widget.getX() - getGuiLeft(), widget.getY() - getGuiTop());
    }

    private boolean isInProgrammingArea(AbstractWidget widget) {
        return PROGRAMMING_AREA.contains(widget.getX() - getGuiLeft(), widget.getY() - getGuiTop());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        switch (mouseButton) {
            case 0 -> {
                // left click - drag widget (with copy if it's a tray widget)
                for (Map.Entry<AbstractWidget, IRemoteWidget> entry : widgetMap.entrySet()) {
                    AbstractWidget mcWidget = entry.getKey();
                    boolean trayWidget = isInTrayArea(mcWidget);
                    if (mcWidget.isHovered() && (isInProgrammingArea(mcWidget) || trayWidget)) {
                        IRemoteWidget remoteWidget = entry.getValue();
                        AbstractWidget mcDrag = trayWidget ?
                                RemoteClientRegistry.INSTANCE.createMinecraftWidget(remoteWidget, this) :
                                mcWidget;
                        startDrag(remoteWidget.copy(), mcDrag, trayWidget);
                        return true;
                    }
                }
            }
            case 1 -> {
                // right click - configure widget
                for (Map.Entry<AbstractWidget, IRemoteWidget> entry : widgetMap.entrySet()) {
                    AbstractWidget mcWidget = entry.getKey();
                    if (mcWidget.isHovered() && isInProgrammingArea(mcWidget)) {
                        IRemoteWidget remoteWidget = entry.getValue();
                        Screen screen = RemoteClientRegistry.INSTANCE.createConfigurationScreen(remoteWidget, this);
                        if (screen != null) {
                            configuringMCWidget = mcWidget;
                            minecraft.setScreen(screen);
                            return true;
                        }
                    }
                }
            }
            case 2 -> {
                // middle click - copy & drag widget
                for (Map.Entry<AbstractWidget, IRemoteWidget> entry : widgetMap.entrySet()) {
                    AbstractWidget mcWidget = entry.getKey();
                    if (mcWidget.isHovered() && isInProgrammingArea(mcWidget)) {
                        IRemoteWidget remoteWidget = entry.getValue();
                        AbstractWidget newMCWidget = RemoteClientRegistry.INSTANCE.createMinecraftWidget(remoteWidget, this);
                        startDrag(remoteWidget.copy(), newMCWidget, true);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void startDrag(IRemoteWidget remoteWidget, AbstractWidget mcWidget, boolean copying) {
        draggingRemoteWidget = remoteWidget;
        draggingMCWidget = mcWidget;

        if (copying) {
            addRenderableWidget(draggingMCWidget);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingRemoteWidget != null && draggingMCWidget != null) {
            if (!isInProgrammingArea(draggingMCWidget)) {
                // dragged off the screen - delete it
                removeWidget(draggingMCWidget);
                widgetMap.remove(draggingMCWidget);
            } else {
                // dragged to a new position
                widgetMap.put(draggingMCWidget, draggingRemoteWidget.copyToPos(draggingMCWidget.getX() - getGuiLeft(), draggingMCWidget.getY() - getGuiTop()));
            }
            draggingRemoteWidget = null;
            draggingMCWidget = null;
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        if (draggingMCWidget != null) {
            int x1 = (int) mouseX;
            int y1 = (int) mouseY;
            if (ConfigHelper.client().general.guiRemoteGridSnap.get()) {
                x1 = (x1 / 4) * 4;
                y1 = (y1 / 4) * 4;
            }
            draggingMCWidget.setPosition(x1, y1);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
    }

    @Override
    public void onClose() {
        ItemStack stack = ClientUtils.getClientPlayer().getItemInHand(menu.getHand());
        if (stack.getItem() == ModItems.REMOTE.get()) {
            SavedRemoteLayout layout = makeNewLayout();
            RemoteItem.saveToItem(stack, layout);
            NetworkHandler.sendToServer(new PacketUpdateRemoteLayout(layout, menu.getHand()));
        }

        super.onClose();
    }

    public void updateWidgetFromConfigScreen(IRemoteWidget newWidget) {
        if (configuringMCWidget != null) {
            IRemoteWidget current = widgetMap.get(configuringMCWidget);
            if (current != null && !current.equals(newWidget)) {
                widgetMap.put(configuringMCWidget, newWidget);
                configuringMCWidget = null;
                rebuildWidgets();
            }
        }
    }
}
