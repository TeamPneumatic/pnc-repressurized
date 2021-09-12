package me.desht.pneumaticcraft.client.gui.remote;

import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.ActionWidgetVariable;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import net.minecraft.util.text.StringTextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiRemoteVariable<A extends ActionWidgetVariable<?>> extends GuiRemoteOptionBase<A> {

    private WidgetComboBox variableField;

    public GuiRemoteVariable(A actionWidget, GuiRemoteEditor guiRemote) {
        super(actionWidget, guiRemote);
    }

    @Override
    public void init() {
        super.init();

        addLabel(xlate("pneumaticcraft.gui.progWidget.coordinate.variableName"), guiLeft + 10, guiTop + 70);
        addLabel(new StringTextComponent("#"), guiLeft + 10, guiTop + 81);

        variableField = new WidgetComboBox(font, guiLeft + 18, guiTop + 80, 152, 10);
        variableField.setElements(guiRemote.getMenu().variables);
        variableField.setValue(actionWidget.getVariableName());
        variableField.setTooltip(xlate("pneumaticcraft.gui.remote.variable.tooltip"));
        addButton(variableField);
    }

    @Override
    public void removed() {
        actionWidget.setVariableName(variableField.getValue());

        super.removed();
    }
}
