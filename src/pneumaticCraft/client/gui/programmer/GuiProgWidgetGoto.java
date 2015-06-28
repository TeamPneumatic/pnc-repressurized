package pneumaticCraft.client.gui.programmer;

import java.util.ArrayList;
import java.util.List;

import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.widget.GuiRadioButton;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetGoToLocation;

public class GuiProgWidgetGoto extends GuiProgWidgetAreaShow{

    public GuiProgWidgetGoto(IProgWidget widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui(){
        super.initGui();

        List<GuiRadioButton> radioButtons = new ArrayList<GuiRadioButton>();
        GuiRadioButton radioButton = new GuiRadioButton(0, guiLeft + 4, guiTop + 44, 0xFF000000, "Done when arrived");
        radioButton.checked = !((ProgWidgetGoToLocation)widget).doneWhenDeparting;
        addWidget(radioButton);
        radioButtons.add(radioButton);
        radioButton.otherChoices = radioButtons;

        GuiRadioButton radioButton2 = new GuiRadioButton(1, guiLeft + 4, guiTop + 58, 0xFF000000, "Done when departing");
        radioButton2.checked = ((ProgWidgetGoToLocation)widget).doneWhenDeparting;
        addWidget(radioButton2);
        radioButtons.add(radioButton2);
        radioButton2.otherChoices = radioButtons;
    }

    @Override
    public void actionPerformed(IGuiWidget guiWidget){
        if(guiWidget.getID() == 0 || guiWidget.getID() == 1) {
            ((ProgWidgetGoToLocation)widget).doneWhenDeparting = guiWidget.getID() == 1;
        }
        super.actionPerformed(guiWidget);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        super.drawScreen(mouseX, mouseY, partialTicks);
        fontRendererObj.drawString("Move to the next puzzle piece", guiLeft + 8, guiTop + 20, 0xFF000000);
        fontRendererObj.drawString("when arrived or right away?", guiLeft + 8, guiTop + 30, 0xFF000000);
    }

}
