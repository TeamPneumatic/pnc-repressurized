package me.desht.pneumaticcraft.client.gui.remote;

import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.common.remote.ActionWidgetVariable;
import net.minecraft.client.resources.I18n;

public class GuiRemoteVariable<A extends ActionWidgetVariable> extends GuiRemoteOptionBase<A> {

    private WidgetComboBox variableField;

    public GuiRemoteVariable(A widget, GuiRemoteEditor guiRemote) {
        super(widget, guiRemote);
    }

    @Override
    public void init() {
        super.init();
        addLabel(I18n.format("gui.progWidget.coordinate.variableName"), guiLeft + 10, guiTop + 70);
        addLabel("#", guiLeft + 10, guiTop + 81);

        variableField = new WidgetComboBox(font, guiLeft + 18, guiTop + 80, 152, 10);
        variableField.setElements(guiRemote.getContainer().variables);
        variableField.setText(widget.getVariableName());
        variableField.setTooltip(I18n.format("gui.remote.variable.tooltip"));
        addButton(variableField);
    }

    @Override
    public void onClose() {
        super.onClose();

        widget.setVariableName(variableField.getText());
    }
}
