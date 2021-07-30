package me.desht.pneumaticcraft.client.gui.remote;

import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.ActionWidgetVariable;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.util.text.StringTextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiRemoteVariable<A extends ActionWidgetVariable<?>> extends GuiRemoteOptionBase<A> {
    private boolean playerGlobal;
    private WidgetButtonExtended varTypeButton;
    private WidgetComboBox variableField;

    public GuiRemoteVariable(A actionWidget, GuiRemoteEditor guiRemote) {
        super(actionWidget, guiRemote);
    }

    @Override
    public void init() {
        super.init();

        addLabel(xlate("pneumaticcraft.gui.progWidget.coordinate.variableName"), guiLeft + 10, guiTop + 70);

        playerGlobal = actionWidget.getVariableName().isEmpty() || actionWidget.getVariableName().startsWith("#");

        varTypeButton = new WidgetButtonExtended(guiLeft + 10, guiTop + 78, 12, 14, GlobalVariableHelper.getVarPrefix(playerGlobal),
                b -> togglePlayerGlobal())
                .setTooltipKey("pneumaticcraft.gui.remote.varType.tooltip");
        addButton(varTypeButton);

        variableField = new WidgetComboBox(font, guiLeft + 23, guiTop + 80, 147, 10);
        variableField.setElements(GlobalVariableHelper.extractVarnames(guiRemote.getContainer().variables, playerGlobal));
        variableField.setText(GlobalVariableHelper.stripVarPrefix(actionWidget.getVariableName()));
        variableField.setTooltip(xlate("pneumaticcraft.gui.remote.variable.tooltip"));
        addButton(variableField);
    }

    @Override
    public void onClose() {
        actionWidget.setVariableName(GlobalVariableHelper.getPrefixedVar(variableField.getText(), playerGlobal));

        super.onClose();
    }

    private void togglePlayerGlobal() {
        playerGlobal = !playerGlobal;
        variableField.setElements(GlobalVariableHelper.extractVarnames(guiRemote.getContainer().variables, playerGlobal));
        varTypeButton.setMessage(new StringTextComponent(GlobalVariableHelper.getVarPrefix(playerGlobal)));
    }
}
