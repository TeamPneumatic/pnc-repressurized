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

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.remote.RemoteLayout;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.*;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerRemote;
import me.desht.pneumaticcraft.common.item.ItemRemote;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateRemoteLayout;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiRemoteEditor extends GuiRemote {
    private GuiInventorySearcher invSearchGui;
    private GuiPastebin pastebinGui;
    private final List<ActionWidget<?>> widgetTray = new ArrayList<>();
    private ActionWidget<?> draggingWidget;
    private int dragMouseStartX, dragMouseStartY;
    private int dragWidgetStartX, dragWidgetStartY;
    private int oldGuiLeft, oldGuiTop;

    public GuiRemoteEditor(ContainerRemote container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        imageWidth = 283;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_REMOTE_EDITOR;
    }

    @Override
    public void init() {
        if (pastebinGui != null && pastebinGui.outputTag != null) {
            CompoundNBT tag = remote.getOrCreateTag();
            tag.put("actionWidgets", pastebinGui.outputTag.getList("main", Constants.NBT.TAG_COMPOUND));
        } else if (remoteLayout != null) {
            CompoundNBT tag = remote.getOrCreateTag();
            tag.put("actionWidgets", remoteLayout.toNBT(oldGuiLeft, oldGuiTop).getList("actionWidgets", Constants.NBT.TAG_COMPOUND));
        }

        if (invSearchGui != null && invSearchGui.getSearchStack().getItem() == ModItems.REMOTE.get()) {
            if (ItemRemote.hasSameSecuritySettings(remote, invSearchGui.getSearchStack())) {
                remoteLayout = new RemoteLayout(invSearchGui.getSearchStack(), leftPos, topPos);
            } else {
                minecraft.player.displayClientMessage(new StringTextComponent("pneumaticcraft.gui.remote.differentSecuritySettings"), false);
            }
        }

        super.init();

        oldGuiLeft = leftPos;
        oldGuiTop = topPos;

        widgetTray.clear();
        widgetTray.add(new ActionWidgetCheckBox(new WidgetCheckBox(leftPos + 200, topPos + 23, 0xFF404040, xlate("pneumaticcraft.gui.remote.tray.checkbox.name"))));
        widgetTray.add(new ActionWidgetLabel(new WidgetLabelVariable(leftPos + 200, topPos + 38, xlate("pneumaticcraft.gui.remote.tray.label.name"))));
        widgetTray.add(new ActionWidgetButton(new WidgetButtonExtended(leftPos + 200, topPos + 53, 50, 20, xlate("pneumaticcraft.gui.remote.tray.button.name"))));
        widgetTray.add(new ActionWidgetDropdown(new WidgetComboBox(font, leftPos + 200, topPos + 80, 70, font.lineHeight + 1).setFixedOptions()));

        for (ActionWidget<?> actionWidget : widgetTray) {
            addButton(actionWidget.getWidget());
        }

        addButton(new WidgetButtonExtended(leftPos - 24, topPos, 20, 20, StringTextComponent.EMPTY, b -> doImport())
                .setTooltipText(xlate("pneumaticcraft.gui.remote.button.importRemoteButton"))
                .setRenderStacks(new ItemStack(ModItems.REMOTE.get()))
        );

        addButton(new WidgetButtonExtended(leftPos - 24, topPos + 22, 20, 20, StringTextComponent.EMPTY, b -> doPastebin())
                .setTooltipText(xlate("pneumaticcraft.gui.remote.button.pastebinButton"))
                .setRenderedIcon(Textures.GUI_PASTEBIN_ICON_LOCATION)
        );

        WidgetCheckBox snapCheck = new WidgetCheckBox(leftPos + 194, topPos + 105, 0xFF404040, xlate("pneumaticcraft.gui.misc.snapToGrid"),
                b -> ConfigHelper.setGuiRemoteGridSnap(b.checked));
        snapCheck.checked = ConfigHelper.client().general.guiRemoteGridSnap.get();
        addButton(snapCheck);

        addButton(new WidgetLabel(leftPos + 234, topPos + 7, xlate("pneumaticcraft.gui.remote.widgetTray").withStyle(TextFormatting.BOLD)).setAlignment(WidgetLabel.Alignment.CENTRE));

        minecraft.keyboardHandler.setSendRepeatsToGui(true);
    }

    private void doImport() {
        ClientUtils.openContainerGui(ModContainers.INVENTORY_SEARCHER.get(), new TranslationTextComponent("pneumaticcraft.gui.amadron.addTrade.invSearch"));
        if (minecraft.screen instanceof GuiInventorySearcher) {
            invSearchGui = (GuiInventorySearcher) minecraft.screen;
            invSearchGui.setStackPredicate(s -> s.getItem() == ModItems.REMOTE.get());
        }
    }

    private void doPastebin() {
        CompoundNBT mainTag = new CompoundNBT();
        mainTag.put("main", remote.hasTag() ? remote.getTag().getList("actionWidgets", Constants.NBT.TAG_COMPOUND) : new CompoundNBT());
        minecraft.setScreen(pastebinGui = new GuiPastebin(this, mainTag));
    }

    @Override
    protected boolean shouldDrawBackground() {
        return false;
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
        renderBackground(matrixStack);
        bindGuiTexture();
        blit(matrixStack, leftPos, topPos, 0, 0, imageWidth, imageHeight, 320, 256);
        super.renderBg(matrixStack, partialTicks, x, y);
    }

    @Override
    protected PointXY getInvNameOffset() {
        return new PointXY(-50, 0);
    }

    private boolean isOutsideProgrammingArea(ActionWidget<?> actionWidget) {
        Widget w = actionWidget.getWidget();
        return w.x < leftPos || w.y < topPos || w.x + w.getWidth() > leftPos + 183 || w.y + w.getHeight() > topPos + imageHeight;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        int x = (int) mouseX;
        int y = (int) mouseY;

        switch (mouseButton) {
            case 0:
                // left click - drag widget
                for (ActionWidget<?> actionWidget : widgetTray) {
                    if (actionWidget.getWidget().isHovered()) {
                        // create new widget from tray
                        startDrag(actionWidget.copy(), x, y);
                        remoteLayout.addWidget(draggingWidget);
                        addButton(draggingWidget.getWidget());
                        return true;
                    }
                }
                if (draggingWidget == null) {
                    for (ActionWidget<?> actionWidget : remoteLayout.getActionWidgets()) {
                        if (actionWidget.getWidget().isHovered()) {
                            // move existing widget
                            startDrag(actionWidget, x, y);
                            return true;
                        }
                    }
                }
                break;
            case 1:
                // right click - configure widget
                for (ActionWidget<?> actionWidget : remoteLayout.getActionWidgets()) {
                    if (!isOutsideProgrammingArea(actionWidget)) {
                        if (actionWidget.getWidget().isHovered()) {
                            Screen screen = actionWidget.getGui(this);
                            if (screen != null) minecraft.setScreen(screen);
                            return true;
                        }
                    }
                }
                break;
            case 2:
                // middle click - copy existing widget
                for (ActionWidget<?> actionWidget : remoteLayout.getActionWidgets()) {
                    if (actionWidget.getWidget().isHovered()) {
                        startDrag(actionWidget.copy(), x, y);
                        remoteLayout.addWidget(draggingWidget);
                        addButton(draggingWidget.getWidget());
                        return true;
                    }
                }
                break;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void startDrag(ActionWidget<?> widget, int x, int y) {
        draggingWidget = widget;
        dragMouseStartX = x;
        dragMouseStartY = y;
        dragWidgetStartX = widget.getWidget().x;
        dragWidgetStartY = widget.getWidget().y;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingWidget != null && isOutsideProgrammingArea(draggingWidget)) {
            remoteLayout.getActionWidgets().remove(draggingWidget);
            removeWidget(draggingWidget.getWidget());
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
            draggingWidget.setWidgetPos(x1, y1);
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
            CompoundNBT nbt = remoteLayout.toNBT(leftPos, topPos);
            stack.getOrCreateTag().put("actionWidgets", nbt.getList("actionWidgets", Constants.NBT.TAG_COMPOUND));
            NetworkHandler.sendToServer(new PacketUpdateRemoteLayout(remoteLayout.toNBT(leftPos, topPos), menu.getHand()));
        }

        minecraft.keyboardHandler.setSendRepeatsToGui(false);

        super.removed();
    }
}
