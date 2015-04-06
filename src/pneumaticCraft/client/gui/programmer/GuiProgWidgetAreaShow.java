package pneumaticCraft.client.gui.programmer;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import pneumaticCraft.client.AreaShowManager;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.common.progwidgets.IAreaProvider;
import pneumaticCraft.common.progwidgets.IProgWidget;

public class GuiProgWidgetAreaShow<Widget extends IProgWidget> extends GuiProgWidgetOptionBase<Widget>{

    public GuiProgWidgetAreaShow(Widget widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui(){
        super.initGui();

        if(widget instanceof IAreaProvider) {
            buttonList.add(new GuiButton(1000, guiLeft + xSize / 2 - 50, guiTop + 150, 100, 20, I18n.format("gui.programmer.button.showArea")));
            if(AreaShowManager.getInstance().isShowing(guiProgrammer.te)) buttonList.add(new GuiButton(1001, guiLeft + xSize / 2 - 50, guiTop + 175, 100, 20, I18n.format("gui.programmer.button.stopShowingArea")));
        }
    }

    @Override
    public void actionPerformed(GuiButton button){
        if(widget instanceof IAreaProvider) {
            if(button.id == 1000) {
                if(!AreaShowManager.getInstance().isShowing(guiProgrammer.te)) buttonList.add(new GuiButton(1001, guiLeft + xSize / 2 - 50, guiTop + 175, 100, 20, I18n.format("gui.programmer.button.stopShowingArea")));
                guiProgrammer.te.previewArea(widget.getX(), widget.getY());
                return;
            } else if(button.id == 1001) {
                AreaShowManager.getInstance().removeHandlers(guiProgrammer.te);
                buttonList.remove(button);
                return;
            }
        }
        // PacketDispatcher.sendPacketToServer(PacketHandlerPneumaticCraft.showDroneArea(guiProgrammer.te.xCoord, guiProgrammer.te.yCoord, guiProgrammer.te.zCoord, widget.getX(), widget.getY()));
        super.actionPerformed(button);
    }
}
