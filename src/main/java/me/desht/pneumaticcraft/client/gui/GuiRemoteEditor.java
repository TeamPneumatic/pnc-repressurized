package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.Config;
import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerRemote;
import me.desht.pneumaticcraft.common.item.ItemRemote;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateRemoteLayout;
import me.desht.pneumaticcraft.common.remote.*;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiRemoteEditor extends GuiRemote {
    private GuiInventorySearcher invSearchGui;
    private GuiPastebin pastebinGui;
    private final List<ActionWidget> visibleSpawnWidgets = new ArrayList<>();
    private boolean wasClicking;
    private ActionWidget draggingWidget;
    private int dragMouseStartX, dragMouseStartY;
    private int dragWidgetStartX, dragWidgetStartY;
    private int oldGuiLeft, oldGuiTop;

    public GuiRemoteEditor(ContainerRemote container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        xSize = 283;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_REMOTE_EDITOR;
    }

    @Override
    public void init() {
        if (pastebinGui != null && pastebinGui.outputTag != null) {
            CompoundNBT tag = remote.getTag();
            if (tag == null) {
                tag = new CompoundNBT();
                remote.setTag(tag);
            }
            tag.put("actionWidgets", pastebinGui.outputTag.getList("main", 10));
        } else if (remoteLayout != null) {
            CompoundNBT tag = remote.getTag();
            if (tag == null) {
                tag = new CompoundNBT();
                remote.setTag(tag);
            }
            tag.put("actionWidgets", remoteLayout.toNBT(oldGuiLeft, oldGuiTop).getList("actionWidgets", 10));
        }

        if (invSearchGui != null && invSearchGui.getSearchStack() != null && invSearchGui.getSearchStack().getItem() == ModItems.REMOTE) {
            if (ItemRemote.hasSameSecuritySettings(remote, invSearchGui.getSearchStack())) {
                remoteLayout = new RemoteLayout(invSearchGui.getSearchStack(), guiLeft, guiTop);
            } else {
                minecraft.player.sendStatusMessage(new StringTextComponent("gui.remote.differentSecuritySettings"), false);
            }
        }

        super.init();

        oldGuiLeft = guiLeft;
        oldGuiTop = guiTop;
        visibleSpawnWidgets.clear();
        visibleSpawnWidgets.add(new ActionWidgetCheckBox(new GuiCheckBox(guiLeft + 200, guiTop + 20, 0xFF404040, I18n.format("remote.checkbox.name"))));
        visibleSpawnWidgets.add(new ActionWidgetLabel(new WidgetLabelVariable(guiLeft + 200, guiTop + 35, I18n.format("remote.label.name"))));
        visibleSpawnWidgets.add(new ActionWidgetButton(new GuiButtonSpecial(-guiLeft + 200, guiTop + 50, 50, 20, I18n.format("remote.button.name"))));
        visibleSpawnWidgets.add(new ActionWidgetDropdown(new WidgetComboBox(font, guiLeft + 200, guiTop + 80, 70, font.FONT_HEIGHT + 1).setFixedOptions()));

        for (ActionWidget actionWidget : visibleSpawnWidgets) {
            addButton(actionWidget.getWidget());
        }

        GuiButtonSpecial importRemoteButton = new GuiButtonSpecial(guiLeft - 24, guiTop, 20, 20, "", b -> {
            ClientUtils.openContainerGui(ModContainerTypes.INVENTORY_SEARCHER, new StringTextComponent("Inventory Searcher (Remote)"));
            if (minecraft.currentScreen instanceof GuiInventorySearcher) invSearchGui = (GuiInventorySearcher) minecraft.currentScreen;
        });
        importRemoteButton.setTooltipText(I18n.format("gui.remote.button.importRemoteButton"));
        importRemoteButton.setRenderStacks(new ItemStack(ModItems.REMOTE));
        addButton(importRemoteButton);

        GuiButtonSpecial pastebinButton = new GuiButtonSpecial(guiLeft - 24, guiTop + 22, 20, 20, "", b -> {
            CompoundNBT mainTag = new CompoundNBT();
            mainTag.put("main", remote.getTag() != null ? remote.getTag().getList("actionWidgets", 10) : new CompoundNBT());
            minecraft.displayGuiScreen(pastebinGui = new GuiPastebin(this, mainTag));
        });
        pastebinButton.setTooltipText(I18n.format("gui.remote.button.pastebinButton"));
        pastebinButton.setRenderedIcon(Textures.GUI_PASTEBIN_ICON_LOCATION);
        addButton(pastebinButton);

        GuiCheckBox snapCheck = new GuiCheckBox(guiLeft + 200, guiTop + 100, 0xFF404040, "Snap to Grid", b -> {
            Config.setGuiRemoteGridSnap(b.checked);
            // todo check if this gets called automatically
            Config.Client.guiRemoteGridSnap = b.checked;
        });
        snapCheck.checked = Config.Client.guiRemoteGridSnap;
        addButton(snapCheck);
    }

    @Override
    protected boolean shouldDrawBackground() {
        return false;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
        renderBackground();
        bindGuiTexture();
        blit(guiLeft, guiTop, 0, 0, xSize, ySize, 320, 256);
        super.drawGuiContainerBackgroundLayer(partialTicks, x, y);

        x += guiLeft;
        y += guiTop;

        boolean isLeftClicking = minecraft.gameSettings.keyBindAttack.isKeyDown();
        boolean isMiddleClicking = minecraft.gameSettings.keyBindPickBlock.isKeyDown();

        if (draggingWidget != null) {
            int x1 = x - dragMouseStartX + dragWidgetStartX - guiLeft;
            int y1 = y - dragMouseStartY + dragWidgetStartY - guiTop;
            if (Config.Client.guiRemoteGridSnap) {
                x1 = (x1 / 4) * 4;
                y1 = (y1 / 4) * 4;
            }
            draggingWidget.setWidgetPos(x1, y1);
        }

        if (isLeftClicking && !wasClicking) {
            for (ActionWidget actionWidget : visibleSpawnWidgets) {
                if (actionWidget.getWidget().isHovered()) {
                    draggingWidget = actionWidget.copy();
                    remoteLayout.addWidget(draggingWidget);
                    addButton(draggingWidget.getWidget());
                    dragMouseStartX = x - guiLeft;
                    dragMouseStartY = y - guiTop;
                    dragWidgetStartX = actionWidget.getWidget().x;
                    dragWidgetStartY = actionWidget.getWidget().y;
                    break;
                }
            }
            if (draggingWidget == null) {
                for (ActionWidget actionWidget : remoteLayout.getActionWidgets()) {
                    if (actionWidget.getWidget().isHovered()) {
                        draggingWidget = actionWidget;
                        dragMouseStartX = x - guiLeft;
                        dragMouseStartY = y - guiTop;
                        dragWidgetStartX = actionWidget.getWidget().x;
                        dragWidgetStartY = actionWidget.getWidget().y;
                        break;
                    }
                }
            }
        } else if (isMiddleClicking && !wasClicking) {
            for (ActionWidget actionWidget : remoteLayout.getActionWidgets()) {
                if (actionWidget.getWidget().isHovered()) {
                    draggingWidget = actionWidget.copy();
                    remoteLayout.addWidget(draggingWidget);
                    addButton(draggingWidget.getWidget());
                    dragMouseStartX = 0;
                    dragMouseStartY = 0;
                    dragWidgetStartX = actionWidget.getWidget().x - (x - guiLeft);
                    dragWidgetStartY = actionWidget.getWidget().y - (y - guiTop);
                    break;
                }
            }
        }

        if (!isLeftClicking && !isMiddleClicking && draggingWidget != null) {
            if (isOutsideProgrammingArea(draggingWidget)) {
                remoteLayout.getActionWidgets().remove(draggingWidget);
                removeWidget(draggingWidget.getWidget());
            }
            draggingWidget = null;
        }
        wasClicking = isLeftClicking || isMiddleClicking;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        font.drawString("Widget Tray", 194, 8, 0x404040);
    }

    private boolean isOutsideProgrammingArea(ActionWidget actionWidget) {
        Widget w = actionWidget.getWidget();
        Rectangle bounds = new Rectangle(w.x, w.y, w.getWidth(), w.getHeight());
        return !new Rectangle(guiLeft, guiTop, 183, ySize).contains(bounds);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 1) {
            for (ActionWidget actionWidget : remoteLayout.getActionWidgets()) {
                if (!isOutsideProgrammingArea(actionWidget)) {
                    if (actionWidget.getWidget().isHovered()) {
                        Screen screen = actionWidget.getGui(this);
                        if (screen != null) minecraft.displayGuiScreen(screen);
                    }
                }
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onGlobalVariableChange(String variable) {
    }

    @Override
    public void onClose() {
        super.onClose();
        NetworkHandler.sendToServer(new PacketUpdateRemoteLayout(remoteLayout.toNBT(guiLeft, guiTop)));
    }
}
