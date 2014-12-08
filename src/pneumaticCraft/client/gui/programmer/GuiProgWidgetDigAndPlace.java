package pneumaticCraft.client.gui.programmer;

import java.util.ArrayList;
import java.util.List;

import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.widget.GuiRadioButton;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetDigAndPlace;

public class GuiProgWidgetDigAndPlace extends GuiProgWidgetAreaShow{

    public GuiProgWidgetDigAndPlace(IProgWidget widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui(){
        super.initGui();

        List<GuiRadioButton> radioButtons = new ArrayList<GuiRadioButton>();
        ProgWidgetDigAndPlace.EnumOrder[] orders = ProgWidgetDigAndPlace.EnumOrder.values();
        for(int i = 0; i < orders.length; i++) {
            GuiRadioButton radioButton = new GuiRadioButton(i, guiLeft + 4, guiTop + 30 + i * 12, 0xFF000000, orders[i].getLocalizedName());
            radioButton.checked = orders[i] == ((ProgWidgetDigAndPlace)widget).getOrder();
            addWidget(radioButton);
            radioButtons.add(radioButton);
            radioButton.otherChoices = radioButtons;
        }
    }

    @Override
    public void actionPerformed(IGuiWidget guiWidget){
        if(guiWidget.getID() >= 0 && guiWidget.getID() < ProgWidgetDigAndPlace.EnumOrder.values().length) ((ProgWidgetDigAndPlace)widget).setOrder(ProgWidgetDigAndPlace.EnumOrder.values()[guiWidget.getID()]);
        super.actionPerformed(guiWidget);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        super.drawScreen(mouseX, mouseY, partialTicks);
        fontRendererObj.drawString("Order:", guiLeft + 8, guiTop + 20, 0xFF000000);
    }

}
