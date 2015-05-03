package pneumaticCraft.client.gui.programmer;

import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.widget.WidgetTextField;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetString;

public class GuiProgWidgetString extends GuiProgWidgetOptionBase{
    private WidgetTextField textfield;

    public GuiProgWidgetString(IProgWidget widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui(){
        super.initGui();
        textfield = new WidgetTextField(fontRendererObj, guiLeft + 10, guiTop + 20, 160, 10);
        textfield.setMaxStringLength(1000);
        textfield.setText(((ProgWidgetString)widget).string);
        addWidget(textfield);
    }

    @Override
    public void onGuiClosed(){
        super.onGuiClosed();
        ((ProgWidgetString)widget).string = textfield.getText();
    }
}
