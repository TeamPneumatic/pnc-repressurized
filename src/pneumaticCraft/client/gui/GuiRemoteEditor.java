package pneumaticCraft.client.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;

import org.lwjgl.input.Mouse;

import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.client.gui.widget.WidgetComboBox;
import pneumaticCraft.common.item.ItemRemote;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketUpdateRemoteLayout;
import pneumaticCraft.common.remote.ActionWidget;
import pneumaticCraft.common.remote.ActionWidgetButton;
import pneumaticCraft.common.remote.ActionWidgetCheckBox;
import pneumaticCraft.common.remote.ActionWidgetDropdown;
import pneumaticCraft.common.remote.ActionWidgetLabel;
import pneumaticCraft.common.remote.ActionWidgetVariable;
import pneumaticCraft.common.remote.RemoteLayout;
import pneumaticCraft.common.remote.WidgetLabelVariable;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;

public class GuiRemoteEditor extends GuiRemote{
    private GuiInventorySearcher invSearchGui;
    private GuiPastebin pastebinGui;
    private final List<ActionWidget> visibleSpawnWidgets = new ArrayList<ActionWidget>();
    private boolean wasClicking;
    private ActionWidget draggingWidget;
    private int dragMouseStartX, dragMouseStartY;
    private int dragWidgetStartX, dragWidgetStartY;
    private int oldGuiLeft, oldGuiTop;

    public GuiRemoteEditor(ItemStack remote){
        super(remote, Textures.GUI_REMOTE_EDITOR);
        xSize = 283;
    }

    @Override
    public void initGui(){
        if(pastebinGui != null && pastebinGui.outputTag != null) {
            NBTTagCompound tag = remote.getTagCompound();
            if(tag == null) {
                tag = new NBTTagCompound();
                remote.setTagCompound(tag);
            }
            tag.setTag("actionWidgets", pastebinGui.outputTag.getTagList("main", 10));
        } else if(remoteLayout != null) {
            NBTTagCompound tag = remote.getTagCompound();
            if(tag == null) {
                tag = new NBTTagCompound();
                remote.setTagCompound(tag);
            }
            tag.setTag("actionWidgets", remoteLayout.toNBT(oldGuiLeft, oldGuiTop).getTagList("actionWidgets", 10));
        }

        if(invSearchGui != null && invSearchGui.getSearchStack() != null && invSearchGui.getSearchStack().getItem() == Itemss.remote) {
            if(ItemRemote.hasSameSecuritySettings(remote, invSearchGui.getSearchStack())) {
                remoteLayout = new RemoteLayout(invSearchGui.getSearchStack(), guiLeft, guiTop);
            } else {
                mc.thePlayer.addChatComponentMessage(new ChatComponentTranslation("gui.remote.differentSecuritySettings"));
            }
        }
        super.initGui();
        oldGuiLeft = guiLeft;
        oldGuiTop = guiTop;
        visibleSpawnWidgets.clear();
        visibleSpawnWidgets.add(new ActionWidgetCheckBox(new GuiCheckBox(-1, guiLeft + 200, guiTop + 10, 0xFF000000, I18n.format("remote.checkbox.name"))));
        visibleSpawnWidgets.add(new ActionWidgetLabel(new WidgetLabelVariable(guiLeft + 200, guiTop + 25, I18n.format("remote.label.name"))));
        visibleSpawnWidgets.add(new ActionWidgetButton(new GuiButtonSpecial(-1, guiLeft + 200, guiTop + 40, 50, 20, I18n.format("remote.button.name"))));
        visibleSpawnWidgets.add(new ActionWidgetDropdown(new WidgetComboBox(fontRendererObj, guiLeft + 200, guiTop + 70, 70, fontRendererObj.FONT_HEIGHT + 1).setFixedOptions()));

        for(ActionWidget actionWidget : visibleSpawnWidgets) {
            addWidget(actionWidget.getWidget());
        }

        GuiButtonSpecial importRemoteButton = new GuiButtonSpecial(0, guiLeft - 24, guiTop + 20, 20, 20, "");
        importRemoteButton.setTooltipText(I18n.format("gui.remote.button.importRemoteButton"));
        importRemoteButton.setRenderStacks(new ItemStack(Itemss.remote));
        buttonList.add(importRemoteButton);

        GuiButtonSpecial pastebinButton = new GuiButtonSpecial(1, guiLeft - 24, guiTop + 44, 20, 20, "");
        pastebinButton.setTooltipText(I18n.format("gui.remote.button.pastebinButton"));
        //pastebinButton.setRenderStacks(new ItemStack(Itemss.advancedPCB));
        pastebinButton.setRenderedIcon(Textures.GUI_PASTEBIN_ICON_LOCATION);
        buttonList.add(pastebinButton);

    }

    @Override
    public void actionPerformed(GuiButton button){
        if(button.id == 0) {
            invSearchGui = new GuiInventorySearcher(FMLClientHandler.instance().getClient().thePlayer);
            FMLClientHandler.instance().showGuiScreen(invSearchGui);
        } else if(button.id == 1) {
            NBTTagCompound mainTag = new NBTTagCompound();
            mainTag.setTag("main", remote.getTagCompound() != null ? remote.getTagCompound().getTagList("actionWidgets", 10) : new NBTTagCompound());
            FMLClientHandler.instance().showGuiScreen(pastebinGui = new GuiPastebin(this, mainTag));
        }
    }

    @Override
    protected boolean shouldDrawBackground(){
        return false;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y){
        bindGuiTexture();
        Gui.func_146110_a(guiLeft, guiTop, 0, 0, xSize, ySize, 320, 256);
        super.drawGuiContainerBackgroundLayer(partialTicks, x, y);

        x += guiLeft;
        y += guiTop;

        boolean isLeftClicking = Mouse.isButtonDown(0);
        boolean isMiddleClicking = Mouse.isButtonDown(2);

        if(draggingWidget != null) {
            draggingWidget.setWidgetPos(x - dragMouseStartX + dragWidgetStartX - guiLeft, y - dragMouseStartY + dragWidgetStartY - guiTop);
        }

        if(isLeftClicking && !wasClicking) {
            for(ActionWidget widget : visibleSpawnWidgets) {
                Rectangle bounds = widget.getWidget().getBounds();
                if(x >= bounds.x + guiLeft && y >= bounds.y + guiTop && x <= bounds.x + guiLeft + bounds.width && y <= bounds.y + guiTop + bounds.height) {
                    draggingWidget = widget.copy();
                    remoteLayout.addWidget(draggingWidget);
                    addWidget(draggingWidget.getWidget());
                    dragMouseStartX = x - guiLeft;
                    dragMouseStartY = y - guiTop;
                    dragWidgetStartX = bounds.x;
                    dragWidgetStartY = bounds.y;
                    break;
                }
            }
            if(draggingWidget == null) {
                for(ActionWidget widget : remoteLayout.getActionWidgets()) {
                    Rectangle bounds = widget.getWidget().getBounds();
                    if(x >= bounds.x + guiLeft && y >= bounds.y + guiTop && x <= bounds.x + guiLeft + bounds.width && y <= bounds.y + guiTop + bounds.height) {
                        draggingWidget = widget;
                        dragMouseStartX = x - guiLeft;
                        dragMouseStartY = y - guiTop;
                        dragWidgetStartX = bounds.x;
                        dragWidgetStartY = bounds.y;
                        break;
                    }
                }
            }
        } else if(isMiddleClicking && !wasClicking) {
            for(ActionWidget widget : remoteLayout.getActionWidgets()) {
                Rectangle bounds = widget.getWidget().getBounds();
                if(x >= bounds.x + guiLeft && y >= bounds.y + guiTop && x <= bounds.x + guiLeft + bounds.width && y <= bounds.y + guiTop + bounds.height) {
                    draggingWidget = widget.copy();
                    remoteLayout.addWidget(draggingWidget);
                    addWidget(draggingWidget.getWidget());
                    dragMouseStartX = 0;
                    dragMouseStartY = 0;
                    dragWidgetStartX = bounds.x - (x - guiLeft);
                    dragWidgetStartY = bounds.y - (y - guiTop);
                    break;
                }
            }
        }

        if(!isLeftClicking && !isMiddleClicking && draggingWidget != null) {
            if(isOutsideProgrammingArea(draggingWidget)) {
                remoteLayout.getActionWidgets().remove(draggingWidget);
                removeWidget(draggingWidget.getWidget());
            }
            draggingWidget = null;
        }
        wasClicking = isLeftClicking || isMiddleClicking;
    }

    private boolean isOutsideProgrammingArea(ActionWidget widget){
        Rectangle bounds = widget.getWidget().getBounds();
        return !new Rectangle(guiLeft, guiTop, 183, ySize).contains(bounds);
    }

    @Override
    protected void mouseClicked(int x, int y, int par3){
        super.mouseClicked(x, y, par3);

        if(par3 == 1) {
            for(ActionWidget widget : remoteLayout.getActionWidgets()) {
                if(!isOutsideProgrammingArea(widget)) {
                    Rectangle bounds = widget.getWidget().getBounds();
                    if(x >= bounds.x && y >= bounds.y && x <= bounds.x + bounds.width && y <= bounds.y + bounds.height) {
                        GuiScreen screen = widget.getGui(this);
                        if(screen != null) mc.displayGuiScreen(screen);
                    }
                }
            }
        }
    }

    @Override
    protected void onActionPerformed(ActionWidgetVariable actionWidget){
        actionWidget.onVariableChange();
    }

    @Override
    public void onGlobalVariableChange(String variable){}

    @Override
    public void onGuiClosed(){
        super.onGuiClosed();
        NetworkHandler.sendToServer(new PacketUpdateRemoteLayout(remoteLayout.toNBT(guiLeft, guiTop)));
    }
}
