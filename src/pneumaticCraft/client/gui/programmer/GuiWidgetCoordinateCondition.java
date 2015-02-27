package pneumaticCraft.client.gui.programmer;

import java.util.ArrayList;
import java.util.List;

import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.client.gui.widget.GuiRadioButton;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.common.progwidgets.ICondition;
import pneumaticCraft.common.progwidgets.ICondition.Operator;
import pneumaticCraft.common.progwidgets.ProgWidgetCoordinateCondition;

public class GuiWidgetCoordinateCondition extends GuiProgWidgetOptionBase<ProgWidgetCoordinateCondition>{
    private final GuiCheckBox[] checkingAxis = new GuiCheckBox[3];
    List<GuiRadioButton> radioButtons;

    public GuiWidgetCoordinateCondition(ProgWidgetCoordinateCondition widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui(){
        super.initGui();
        checkingAxis[0] = new GuiCheckBox(0, guiLeft + 10, guiTop + 30, 0xFF000000, "X");
        checkingAxis[1] = new GuiCheckBox(1, guiLeft + 10, guiTop + 42, 0xFF000000, "Y");
        checkingAxis[2] = new GuiCheckBox(2, guiLeft + 10, guiTop + 54, 0xFF000000, "Z");
        for(int i = 0; i < 3; i++) {
            checkingAxis[i] = new GuiCheckBox(i, guiLeft + 10, guiTop + 30 + i * 12, 0xFF000000, i == 0 ? "X" : i == 1 ? "Y" : "Z");
            addWidget(checkingAxis[i]);
            checkingAxis[i].setChecked(widget.checkingAxis[i]);
        }

        radioButtons = new ArrayList<GuiRadioButton>();
        for(int i = 0; i < ICondition.Operator.values().length; i++) {
            GuiRadioButton radioButton = new GuiRadioButton(3 + i, guiLeft + 80, guiTop + 30 + i * 12, 0xFF000000, ICondition.Operator.values()[i].toString());
            radioButton.checked = widget.getOperator().ordinal() == i;
            addWidget(radioButton);
            radioButtons.add(radioButton);
            radioButton.otherChoices = radioButtons;
        }
    }

    @Override
    public void actionPerformed(IGuiWidget w){
        super.actionPerformed(w);
        for(int i = 0; i < 3; i++) {
            widget.checkingAxis[i] = checkingAxis[i].checked;
        }
        for(int i = 0; i < ICondition.Operator.values().length; i++) {
            if(radioButtons.get(i).checked) widget.setOperator(Operator.values()[i]);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        super.drawScreen(mouseX, mouseY, partialTicks);
        String condition = widget.getCondition();
        fontRendererObj.drawString(condition, width / 2 - fontRendererObj.getStringWidth(condition) / 2, guiTop + 70, 0xFF000000);
    }
}
