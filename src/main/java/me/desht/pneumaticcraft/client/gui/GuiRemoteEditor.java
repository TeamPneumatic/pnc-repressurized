package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.remote.RemoteLayout;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.*;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerRemote;
import me.desht.pneumaticcraft.common.item.ItemRemote;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateRemoteLayout;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class GuiRemoteEditor extends GuiRemote {
    private GuiInventorySearcher invSearchGui;
    private GuiPastebin pastebinGui;
    private final List<ActionWidget> widgetTray = new ArrayList<>();
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
            CompoundNBT tag = remote.getOrCreateTag();
            tag.put("actionWidgets", pastebinGui.outputTag.getList("main", Constants.NBT.TAG_COMPOUND));
        } else if (remoteLayout != null) {
            CompoundNBT tag = remote.getOrCreateTag();
            tag.put("actionWidgets", remoteLayout.toNBT(oldGuiLeft, oldGuiTop).getList("actionWidgets", Constants.NBT.TAG_COMPOUND));
        }

        if (invSearchGui != null && invSearchGui.getSearchStack().getItem() == ModItems.REMOTE.get()) {
            if (ItemRemote.hasSameSecuritySettings(remote, invSearchGui.getSearchStack())) {
                remoteLayout = new RemoteLayout(invSearchGui.getSearchStack(), guiLeft, guiTop);
            } else {
                minecraft.player.sendStatusMessage(new StringTextComponent("gui.remote.differentSecuritySettings"), false);
            }
        }

        super.init();

        oldGuiLeft = guiLeft;
        oldGuiTop = guiTop;

        widgetTray.clear();
        widgetTray.add(new ActionWidgetCheckBox(new WidgetCheckBox(guiLeft + 200, guiTop + 23, 0xFF404040, I18n.format("gui.remote.tray.checkbox.name"))));
        widgetTray.add(new ActionWidgetLabel(new WidgetLabelVariable(guiLeft + 200, guiTop + 38, I18n.format("gui.remote.tray.label.name"))));
        widgetTray.add(new ActionWidgetButton(new WidgetButtonExtended(guiLeft + 200, guiTop + 53, 50, 20, I18n.format("gui.remote.tray.button.name"))));
        widgetTray.add(new ActionWidgetDropdown(new WidgetComboBox(font, guiLeft + 200, guiTop + 80, 70, font.FONT_HEIGHT + 1).setFixedOptions()));

        for (ActionWidget actionWidget : widgetTray) {
            addButton(actionWidget.getWidget());
        }

        addButton(new WidgetButtonExtended(guiLeft - 24, guiTop, 20, 20, "", b -> doImport())
                .setTooltipText(I18n.format("gui.remote.button.importRemoteButton"))
                .setRenderStacks(new ItemStack(ModItems.REMOTE.get()))
        );

        addButton(new WidgetButtonExtended(guiLeft - 24, guiTop + 22, 20, 20, "", b -> doPastebin())
                .setTooltipText(I18n.format("gui.remote.button.pastebinButton"))
                .setRenderedIcon(Textures.GUI_PASTEBIN_ICON_LOCATION)
        );

        WidgetCheckBox snapCheck = new WidgetCheckBox(guiLeft + 194, guiTop + 105, 0xFF404040, I18n.format("gui.remote.snapToGrid"),
                b -> ConfigHelper.setGuiRemoteGridSnap(b.checked));
        snapCheck.checked = PNCConfig.Client.guiRemoteGridSnap;
        addButton(snapCheck);

        addButton(new WidgetLabel(guiLeft + 234, guiTop + 7, TextFormatting.BOLD + I18n.format("gui.remote.widgetTray")).setAlignment(WidgetLabel.Alignment.CENTRE));

        minecraft.keyboardListener.enableRepeatEvents(true);
    }

    private void doImport() {
        ClientUtils.openContainerGui(ModContainers.INVENTORY_SEARCHER.get(), new TranslationTextComponent("gui.amadron.addTrade.invSearch"));
        if (minecraft.currentScreen instanceof GuiInventorySearcher) {
            invSearchGui = (GuiInventorySearcher) minecraft.currentScreen;
            invSearchGui.setStackPredicate(s -> s.getItem() == ModItems.REMOTE.get());
        }
    }

    private void doPastebin() {
        CompoundNBT mainTag = new CompoundNBT();
        mainTag.put("main", remote.hasTag() ? remote.getTag().getList("actionWidgets", Constants.NBT.TAG_COMPOUND) : new CompoundNBT());
        minecraft.displayGuiScreen(pastebinGui = new GuiPastebin(this, mainTag));
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
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
    }

    @Override
    protected PointXY getInvNameOffset() {
        return new PointXY(-50, 0);
    }

    private boolean isOutsideProgrammingArea(ActionWidget actionWidget) {
        Widget w = actionWidget.getWidget();
        return w.x < guiLeft || w.y < guiTop || w.x + w.getWidth() > guiLeft + 183 || w.y + w.getHeight() > guiTop + ySize;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        int x = (int) mouseX;
        int y = (int) mouseY;

        switch (mouseButton) {
            case 0:
                // left click - drag widget
                for (ActionWidget actionWidget : widgetTray) {
                    if (actionWidget.getWidget().isHovered()) {
                        // create new widget from tray
                        startDrag(actionWidget.copy(), x, y);
                        remoteLayout.addWidget(draggingWidget);
                        addButton(draggingWidget.getWidget());
                        return true;
                    }
                }
                if (draggingWidget == null) {
                    for (ActionWidget actionWidget : remoteLayout.getActionWidgets()) {
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
                for (ActionWidget actionWidget : remoteLayout.getActionWidgets()) {
                    if (!isOutsideProgrammingArea(actionWidget)) {
                        if (actionWidget.getWidget().isHovered()) {
                            Screen screen = actionWidget.getGui(this);
                            if (screen != null) minecraft.displayGuiScreen(screen);
                            return true;
                        }
                    }
                }
                break;
            case 2:
                // middle click - copy existing widget
                for (ActionWidget actionWidget : remoteLayout.getActionWidgets()) {
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

    private void startDrag(ActionWidget widget, int x, int y) {
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
            if (PNCConfig.Client.guiRemoteGridSnap) {
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
    public void onClose() {
        ItemStack stack = ClientUtils.getClientPlayer().getHeldItem(container.getHand());
        if (stack.getItem() == ModItems.REMOTE.get()) {
            CompoundNBT nbt = remoteLayout.toNBT(guiLeft, guiTop);
            stack.getOrCreateTag().put("actionWidgets", nbt.getList("actionWidgets", Constants.NBT.TAG_COMPOUND));
            NetworkHandler.sendToServer(new PacketUpdateRemoteLayout(remoteLayout.toNBT(guiLeft, guiTop), container.getHand()));
        }

        minecraft.keyboardListener.enableRepeatEvents(false);

        super.onClose();
    }
}
