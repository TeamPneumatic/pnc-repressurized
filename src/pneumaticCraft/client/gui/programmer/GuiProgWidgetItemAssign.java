package pneumaticCraft.client.gui.programmer;

import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.widget.WidgetComboBox;
import pneumaticCraft.client.gui.widget.WidgetLabel;
import pneumaticCraft.common.progwidgets.ProgWidgetItemAssign;

public class GuiProgWidgetItemAssign extends GuiProgWidgetOptionBase<ProgWidgetItemAssign>{
    private WidgetComboBox textfield;

    public GuiProgWidgetItemAssign(ProgWidgetItemAssign widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui(){
        super.initGui();
        textfield = new WidgetComboBox(fontRendererObj, guiLeft + 10, guiTop + 40, 160, 10);
        textfield.setElements(guiProgrammer.te.getAllVariables());
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
