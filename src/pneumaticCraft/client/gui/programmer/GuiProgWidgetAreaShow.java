package pneumaticCraft.client.gui.programmer;

import net.minecraft.client.gui.GuiButton;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.common.progwidgets.IProgWidget;

public class GuiProgWidgetAreaShow extends GuiProgWidgetOptionBase{

    public GuiProgWidgetAreaShow(IProgWidget widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui(){
        super.initGui();

        buttonList.add(new GuiButton(0, guiLeft + 50, guiTop + 150, 80, 20, "Show area"));
    }

    @Override
    public void actionPerformed(GuiButton button){
        guiProgrammer.te.previewArea(widget.getX(), widget.getY());
        // PacketDispatcher.sendPacketToServer(PacketHandlerPneumaticCraft.showDroneArea(guiProgrammer.te.xCoord, guiProgrammer.te.yCoord, guiProgrammer.te.zCoord, widget.getX(), widget.getY()));
        super.actionPerformed(button);
    }

}
