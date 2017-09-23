package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.common.inventory.ContainerRemote;
import me.desht.pneumaticcraft.common.remote.ActionWidget;
import me.desht.pneumaticcraft.common.remote.ActionWidgetVariable;
import me.desht.pneumaticcraft.common.remote.RemoteLayout;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class GuiRemote extends GuiPneumaticContainerBase {

    protected RemoteLayout remoteLayout;
    protected final ItemStack remote;

    public GuiRemote(ItemStack remote, String texture) {
        super(new ContainerRemote(remote), null, texture);
        xSize = 183;
        ySize = 202;
        this.remote = remote;
    }

    public GuiRemote(ItemStack remote) {
        this(remote, Textures.GUI_WIDGET_OPTIONS_STRING);
    }

    @Override
    public void initGui() {
        remoteLayout = null;
        super.initGui();
        if (remoteLayout == null) remoteLayout = new RemoteLayout(remote, guiLeft, guiTop);
        addWidgets(remoteLayout.getWidgets(!(this instanceof GuiRemoteEditor)));
    }

    @Override
    protected Point getInvTextOffset() {
        return null;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }

    @Override
    public void actionPerformed(IGuiWidget widget) {
        for (ActionWidget actionWidget : remoteLayout.getActionWidgets()) {
            if (actionWidget.getWidget() == widget && actionWidget instanceof ActionWidgetVariable) {
                onActionPerformed((ActionWidgetVariable) actionWidget);
            }
        }
    }

    protected void onActionPerformed(ActionWidgetVariable actionWidget) {
        actionWidget.onActionPerformed();
    }

    @Override
    public void onKeyTyped(IGuiWidget widget) {
        super.onKeyTyped(widget);
        for (ActionWidget actionWidget : remoteLayout.getActionWidgets()) {
            if (actionWidget.getWidget() == widget && actionWidget instanceof ActionWidgetVariable) {
                onKeyTyped((ActionWidgetVariable) actionWidget);
            }
        }
    }

    protected void onKeyTyped(ActionWidgetVariable actionWidget) {
        actionWidget.onKeyTyped();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void onGlobalVariableChange(String variable) {
        widgets.clear();
        initGui();
        for (ActionWidget actionWidget : remoteLayout.getActionWidgets()) {
            if (actionWidget instanceof ActionWidgetVariable) {
                ((ActionWidgetVariable) actionWidget).onVariableChange();
            }
        }
    }

    /* @Override
     * TODO NEI dep
     @Optional.Method(modid = ModIds.NEI)
     public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility){
         currentVisibility.showNEI = false;
         return currentVisibility;
     }*/

}
