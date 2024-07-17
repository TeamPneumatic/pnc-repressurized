package me.desht.pneumaticcraft.client.gui.remote;

import me.desht.pneumaticcraft.api.remote.IRemoteWidget;
import me.desht.pneumaticcraft.client.gui.AbstractPneumaticCraftContainerScreen;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.inventory.RemoteMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractRemoteScreen extends AbstractPneumaticCraftContainerScreen<RemoteMenu, AbstractPneumaticCraftBlockEntity> {
    protected final ItemStack remoteItem;
    protected final Map<AbstractWidget, IRemoteWidget> widgetMap = new LinkedHashMap<>();

    public AbstractRemoteScreen(RemoteMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        remoteItem = inv.player.getItemInHand(container.getHand());
    }

    protected void addRemoteWidget(AbstractWidget mcWidget, IRemoteWidget remoteWidget) {
        widgetMap.put(mcWidget, remoteWidget);
        addRenderableWidget(mcWidget);
    }

    /**
     * Client has received a PacketSetGlobalVariable message; update the remote GUI, if it's open.
     * @param varName variable that changed
     */
    public static void handleVariableChangeIfOpen(String varName) {
        if (Minecraft.getInstance().screen instanceof RemoteScreen r) {
            r.onGlobalVariableChanged(varName);
        }
    }

    /**
     * Given a list of remote widgets, build an identity hash map of minecraft widget -> remote widget.
     *
     * @param remoteWidgets the remote widgets
     * @param screen the screen on which the minecraft widgets will be placed
     * @param filterWidgets if true, make invisible any widgets which have a non-matching enable variable
     * @return the widget map
     */
    static Map<AbstractWidget, IRemoteWidget> buildMinecraftWidgetList(List<IRemoteWidget> remoteWidgets, AbstractRemoteScreen screen, boolean filterWidgets) {
        // Subclasses of AbstractWidget don't override hashCode() and equals(), which is fine for our purposes here
        // We basically want an identity map of mc widget -> remote widget, and linked is nice to preserve widget order
        Map<AbstractWidget,IRemoteWidget> map = new LinkedHashMap<>();
        remoteWidgets.forEach(remoteWidget -> {
            AbstractWidget mcWidget = RemoteClientRegistry.INSTANCE.createMinecraftWidget(remoteWidget, screen);
            mcWidget.visible = !filterWidgets || remoteWidget.isEnabled(Minecraft.getInstance().player);
            map.put(mcWidget, remoteWidget);
        });
        return map;
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
    protected boolean shouldParseVariablesInTooltips() {
        return true;
    }
}
