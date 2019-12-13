package me.desht.pneumaticcraft.client.gui.areatool;

import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetArea;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateGPSAreaTool;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import net.minecraft.util.Hand;

/**
 * Area widget as used by the GPS Area Tool.
 */
public class GuiProgWidgetAreaTool extends GuiProgWidgetArea {
    private final Hand hand;
    private final Runnable returnAction;
    
    GuiProgWidgetAreaTool(ProgWidgetArea widget, Hand hand, Runnable returnAction) {
        super(widget, null);

        this.hand = hand;
        this.returnAction = returnAction;
    }

    @Override
    public void onClose() {
        NetworkHandler.sendToServer(new PacketUpdateGPSAreaTool(progWidget, hand));
        returnAction.run();

        super.onClose();
    }

    @Override
    public boolean displayShowAreaButtons() {
        return false;
    }
}
