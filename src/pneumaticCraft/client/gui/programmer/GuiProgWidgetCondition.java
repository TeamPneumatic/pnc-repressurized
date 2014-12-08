package pneumaticCraft.client.gui.programmer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.math.NumberUtils;

import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.client.gui.widget.GuiRadioButton;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.WidgetTextField;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketProgrammerUpdate;
import pneumaticCraft.common.progwidgets.ICondition;
import pneumaticCraft.common.progwidgets.ProgWidgetCondition;
import pneumaticCraft.common.progwidgets.ProgWidgetInventoryBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class GuiProgWidgetCondition extends GuiProgWidgetAreaShow<ProgWidgetCondition>{

    private WidgetTextField textField;

    public GuiProgWidgetCondition(ProgWidgetCondition widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui(){
        super.initGui();

        for(int i = 0; i < 6; i++) {
            String sideName = PneumaticCraftUtils.getOrientationName(ForgeDirection.getOrientation(i));
            GuiCheckBox checkBox = new GuiCheckBox(i, guiLeft + 4, guiTop + 30 + i * 12, 0xFF000000, sideName);
            checkBox.checked = ((ProgWidgetInventoryBase)widget).accessingSides[i];
            addWidget(checkBox);
        }

        List<GuiRadioButton> radioButtons = new ArrayList<GuiRadioButton>();
        GuiRadioButton radioButton = new GuiRadioButton(6, guiLeft + 90, guiTop + 30, 0xFF000000, "Any block");
        radioButton.checked = !widget.isAndFunction();
        addWidget(radioButton);
        radioButtons.add(radioButton);
        radioButton.otherChoices = radioButtons;

        radioButton = new GuiRadioButton(7, guiLeft + 90, guiTop + 42, 0xFF000000, "All blocks");
        radioButton.checked = widget.isAndFunction();
        addWidget(radioButton);
        radioButtons.add(radioButton);
        radioButton.otherChoices = radioButtons;

        radioButtons = new ArrayList<GuiRadioButton>();
        for(int i = 0; i < ICondition.Operator.values().length; i++) {
            radioButton = new GuiRadioButton(8 + i, guiLeft + 90, guiTop + 60 + i * 12, 0xFF000000, ICondition.Operator.values()[i].toString());
            radioButton.checked = widget.getOperator().ordinal() == i;
            addWidget(radioButton);
            radioButtons.add(radioButton);
            radioButton.otherChoices = radioButtons;
        }

        textField = new WidgetTextField(Minecraft.getMinecraft().fontRenderer, guiLeft + 90, guiTop + 90, 50, 11);
        textField.setText(widget.getRequiredCount() + "");
        addWidget(textField);
    }

    @Override
    public void actionPerformed(IGuiWidget checkBox){
        if(checkBox.getID() < 6) {
            widget.accessingSides[checkBox.getID()] = ((GuiCheckBox)checkBox).checked;
        } else {
            switch(checkBox.getID()){
                case 6:
                    widget.setAndFunction(false);
                    break;
                case 7:
                    widget.setAndFunction(true);
                    break;
                default:
                    widget.setOperator(ICondition.Operator.values()[checkBox.getID() - 8]);
            }
        }
        super.actionPerformed(checkBox);
    }

    @Override
    public void onKeyTyped(IGuiWidget widget){
        super.onKeyTyped(widget);
        this.widget.setRequiredCount(NumberUtils.toInt(textField.getText()));
        NetworkHandler.sendToServer(new PacketProgrammerUpdate(guiProgrammer.te));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        super.drawScreen(mouseX, mouseY, partialTicks);
        fontRendererObj.drawString("Accessing sides:", guiLeft + 4, guiTop + 20, 0xFF000000);
        fontRendererObj.drawString(widget.getExtraStringInfo(), guiLeft + xSize / 2 - fontRendererObj.getStringWidth(widget.getExtraStringInfo()) / 2, guiTop + 120, 0xFF000000);
    }

}
