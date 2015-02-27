package pneumaticCraft.client.gui.programmer;

import net.minecraft.client.gui.GuiTextField;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetString;

public class GuiProgWidgetString extends GuiProgWidgetOptionBase{
    private GuiTextField textfield;

    public GuiProgWidgetString(IProgWidget widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui(){
        super.initGui();
        textfield = new GuiTextField(fontRendererObj, guiLeft + 10, guiTop + 20, 160, 10);
        textfield.setText(((ProgWidgetString)widget).string);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        super.drawScreen(mouseX, mouseY, partialTicks);
        textfield.drawTextBox();
    }

    @Override
    protected void mouseClicked(int x, int y, int par3){
        super.mouseClicked(x, y, par3);
        textfield.mouseClicked(x, y, par3);
    }

    @Override
    public void keyTyped(char par1, int par2){
        if(textfield.isFocused() && par2 != 1) {
            textfield.textboxKeyTyped(par1, par2);
            ((ProgWidgetString)widget).string = textfield.getText();
        } else {
            super.keyTyped(par1, par2);
        }
    }
}
