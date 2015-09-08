package pneumaticCraft.client.gui.programmer;

import java.util.ArrayList;
import java.util.List;

import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.widget.GuiRadioButton;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetDropItem;
import pneumaticCraft.common.progwidgets.ProgWidgetInventoryBase;

public class GuiProgWidgetDropItem extends GuiProgWidgetImportExport{

    public GuiProgWidgetDropItem(ProgWidgetInventoryBase widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    protected boolean showSides(){
        return false;
    }

    @Override
    public void initGui(){
        super.initGui();

        List<GuiRadioButton> radioButtons = new ArrayList<GuiRadioButton>();
        GuiRadioButton radioButton = new GuiRadioButton(7, guiLeft + 4, guiTop + 80, 0xFF000000, "Random");
        radioButton.checked = !((ProgWidgetDropItem)widget).dropStraight();
        addWidget(radioButton);
        radioButtons.add(radioButton);
        radioButton.otherChoices = radioButtons;

        GuiRadioButton radioButton2 = new GuiRadioButton(8, guiLeft + 4, guiTop + 94, 0xFF000000, "Straight");
        radioButton2.checked = ((ProgWidgetDropItem)widget).dropStraight();
        addWidget(radioButton2);
        radioButtons.add(radioButton2);
        radioButton2.otherChoices = radioButtons;
    }

    @Override
    public void actionPerformed(IGuiWidget guiWidget){
        if(guiWidget.getID() == 7 || guiWidget.getID() == 8) {
            ((ProgWidgetDropItem)widget).setDropStraight(guiWidget.getID() == 8);
        }
        super.actionPerformed(guiWidget);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        super.drawScreen(mouseX, mouseY, partialTicks);
        fontRendererObj.drawString("Drop method:", guiLeft + 8, guiTop + 70, 0xFF000000);
    }
}
