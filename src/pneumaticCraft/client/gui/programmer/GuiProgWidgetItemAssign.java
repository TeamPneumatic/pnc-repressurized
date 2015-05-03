package pneumaticCraft.client.gui.programmer;

import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.widget.WidgetLabel;
import pneumaticCraft.client.gui.widget.WidgetTextField;
import pneumaticCraft.common.progwidgets.ProgWidgetItemAssign;

public class GuiProgWidgetItemAssign extends GuiProgWidgetOptionBase<ProgWidgetItemAssign>{
    private WidgetTextField textfield;

    public GuiProgWidgetItemAssign(ProgWidgetItemAssign widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui(){
        super.initGui();
        textfield = new WidgetTextField(fontRendererObj, guiLeft + 10, guiTop + 40, 160, 10);
        textfield.setMaxStringLength(1000);
        textfield.setText(widget.getVariable());
        addWidget(textfield);

        addWidget(new WidgetLabel(guiLeft + 10, guiTop + 30, "Setting variable:"));
    }

    @Override
    public void onGuiClosed(){
        super.onGuiClosed();
        widget.setVariable(textfield.getText());
    }
}
