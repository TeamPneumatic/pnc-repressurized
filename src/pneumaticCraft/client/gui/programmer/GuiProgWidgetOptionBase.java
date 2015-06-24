package pneumaticCraft.client.gui.programmer;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.gui.GuiPneumaticScreenBase;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketProgrammerUpdate;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.lib.Textures;

public class GuiProgWidgetOptionBase<Widget extends IProgWidget> extends GuiPneumaticScreenBase{
    protected Widget widget;
    protected GuiProgrammer guiProgrammer;

    public GuiProgWidgetOptionBase(Widget widget, GuiProgrammer guiProgrammer){
        this.widget = widget;
        this.guiProgrammer = guiProgrammer;
        xSize = 183;
        ySize = 202;
    }

    @Override
    public void keyTyped(char key, int keyCode){
        super.keyTyped(key, keyCode);
        if(keyCode == 1) {
            onGuiClosed();
            if(guiProgrammer != null) {
                NetworkHandler.sendToServer(new PacketProgrammerUpdate(guiProgrammer.te));
                mc.displayGuiScreen(guiProgrammer);
            }
        }
    }

    @Override
    public void initGui(){
        super.initGui();
        String title = I18n.format("programmingPuzzle." + widget.getWidgetString() + ".name");
        addLabel(title, width / 2 - fontRendererObj.getStringWidth(title) / 2, guiTop + 5);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.GUI_WIDGET_OPTIONS;
    }

    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }
}
