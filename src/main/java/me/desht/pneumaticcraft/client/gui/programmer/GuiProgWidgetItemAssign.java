package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetItemAssign;

public class GuiProgWidgetItemAssign extends GuiProgWidgetOptionBase<ProgWidgetItemAssign> {
    private WidgetComboBox textfield;

    public GuiProgWidgetItemAssign(ProgWidgetItemAssign widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui() {
        super.initGui();
        textfield = new WidgetComboBox(fontRenderer, guiLeft + 10, guiTop + 40, 160, 10);
        textfield.setElements(guiProgrammer.te.getAllVariables());
        textfield.setMaxStringLength(1000);
        textfield.setText(widget.getVariable());
        addWidget(textfield);

        addWidget(new WidgetLabel(guiLeft + 10, guiTop + 30, "Setting variable:"));
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        widget.setVariable(textfield.getText());
    }
}
