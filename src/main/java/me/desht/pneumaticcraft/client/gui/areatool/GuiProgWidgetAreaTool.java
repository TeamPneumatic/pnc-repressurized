package me.desht.pneumaticcraft.client.gui.areatool;

import java.io.IOException;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiScreen;
import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetArea;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateGPSAreaTool;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;

public class GuiProgWidgetAreaTool extends GuiProgWidgetArea{
    private final Runnable returnAction;
    
    public GuiProgWidgetAreaTool(ProgWidgetArea widget, Runnable returnAction){
        super(widget, null);
        this.returnAction = returnAction;
    }

    @Override
    public void keyTyped(char key, int keyCode) throws IOException {
        super.keyTyped(key, keyCode);
        if (keyCode == 1) {
            onGuiClosed();
            NetworkHandler.sendToServer(new PacketUpdateGPSAreaTool(widget));
            returnAction.run();
        }
    }
    
    @Override
    public boolean showShowAreaButtons(){
        return false;
    }
}
