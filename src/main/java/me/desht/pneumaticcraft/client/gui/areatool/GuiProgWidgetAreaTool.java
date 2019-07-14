package me.desht.pneumaticcraft.client.gui.areatool;

import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetArea;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateGPSAreaTool;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import net.minecraft.util.Hand;

public class GuiProgWidgetAreaTool extends GuiProgWidgetArea {
    private final Runnable returnAction;
    
    GuiProgWidgetAreaTool(ProgWidgetArea widget, Runnable returnAction){
        super(widget, null);
        this.returnAction = returnAction;
    }

    @Override
    public void onClose() {
        super.onClose();
        NetworkHandler.sendToServer(new PacketUpdateGPSAreaTool(progWidget, Hand.MAIN_HAND));
        returnAction.run();
    }

    @Override
    public boolean showShowAreaButtons(){
        return false;
    }
}
