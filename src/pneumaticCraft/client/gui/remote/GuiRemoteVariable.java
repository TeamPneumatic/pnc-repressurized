package pneumaticCraft.client.gui.remote;

import net.minecraft.client.resources.I18n;
import pneumaticCraft.client.gui.GuiRemoteEditor;
import pneumaticCraft.client.gui.widget.WidgetTextField;
import pneumaticCraft.common.remote.ActionWidgetVariable;

public class GuiRemoteVariable<Widget extends ActionWidgetVariable> extends GuiRemoteOptionBase<Widget>{

    private WidgetTextField variableField;

    public GuiRemoteVariable(Widget widget, GuiRemoteEditor guiRemote){
        super(widget, guiRemote);
    }

    @Override
    public void initGui(){
        super.initGui();
        addLabel(I18n.format("gui.progWidget.coordinate.variableName"), guiLeft + 10, guiTop + 70);
        addLabel("#", guiLeft + 10, guiTop + 81);

        variableField = new WidgetTextField(fontRendererObj, guiLeft + 18, guiTop + 80, 152, 10);
        variableField.setText(widget.getVariableName());
        variableField.setTooltip(I18n.format("gui.remote.variable.tooltip"));
        addWidget(variableField);
    }

    @Override
    public void onGuiClosed(){
        super.onGuiClosed();
        widget.setVariableName(variableField.getText());
    }
}
