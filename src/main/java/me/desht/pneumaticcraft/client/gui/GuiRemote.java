package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerRemote;
import me.desht.pneumaticcraft.common.remote.ActionWidget;
import me.desht.pneumaticcraft.common.remote.ActionWidgetVariable;
import me.desht.pneumaticcraft.common.remote.RemoteLayout;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiRemote extends GuiPneumaticContainerBase<ContainerRemote,TileEntityBase> {

    RemoteLayout remoteLayout;
    protected final ItemStack remote;

    public GuiRemote(ContainerRemote container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        xSize = 183;
        ySize = 202;
        this.remote = inv.player.getHeldItem(container.getHand());
    }

    /**
     * Client has received a PacketSetGlobalVariable message; update the remote GUI, if it's open.
     * @param varName variable that changed
     */
    public static void maybeHandleVariableChange(String varName) {
        Screen screen = Minecraft.getInstance().currentScreen;
        if (screen instanceof GuiRemote) {
            ((GuiRemote) screen).onGlobalVariableChange(varName);
        }
    }

    @Override
    public void init() {
        remoteLayout = null;

        super.init();

        if (remoteLayout == null) {
            remoteLayout = new RemoteLayout(remote, guiLeft, guiTop);
        }
        remoteLayout.getWidgets(!(this instanceof GuiRemoteEditor)).forEach(this::addButton);
    }

    @Override
    protected PointXY getInvTextOffset() {
        return null;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_WIDGET_OPTIONS;
    }

    public void onGlobalVariableChange(String variable) {
        buttons.clear();
        children.clear();
        init();
        for (ActionWidget actionWidget : remoteLayout.getActionWidgets()) {
            if (actionWidget instanceof ActionWidgetVariable) {
                ((ActionWidgetVariable) actionWidget).onVariableChange();
            }
        }
    }
}
