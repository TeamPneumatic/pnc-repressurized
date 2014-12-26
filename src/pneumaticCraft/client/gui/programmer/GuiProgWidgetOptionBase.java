package pneumaticCraft.client.gui.programmer;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.gui.GuiPneumaticScreenBase;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.widget.IGuiWidget;
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
            mc.displayGuiScreen(guiProgrammer);
            onGuiClosed();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        String title = I18n.format("programmingPuzzle." + widget.getWidgetString() + ".name");
        fontRendererObj.drawString(title, width / 2 - fontRendererObj.getStringWidth(title) / 2, guiTop + 5, 0xFF000000);
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.GUI_WIDGET_OPTIONS;
    }

    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }

    @Override
    public void actionPerformed(GuiButton button){
        NetworkHandler.sendToServer(new PacketProgrammerUpdate(guiProgrammer.te));
    }

    @Override
    public void actionPerformed(IGuiWidget widget){
        NetworkHandler.sendToServer(new PacketProgrammerUpdate(guiProgrammer.te));
    }
}
