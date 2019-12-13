package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetGoToLocation;

import java.util.ArrayList;
import java.util.List;

public class GuiProgWidgetGoToLocation extends GuiProgWidgetAreaShow<ProgWidgetGoToLocation> {

    public GuiProgWidgetGoToLocation(ProgWidgetGoToLocation progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        List<WidgetRadioButton> radioButtons = new ArrayList<>();
        WidgetRadioButton radioButton = new WidgetRadioButton(guiLeft + 4, guiTop + 44, 0xFF404040,
                "Done when arrived", b -> progWidget.doneWhenDeparting = false);
        radioButton.checked = !progWidget.doneWhenDeparting;
        addButton(radioButton);
        radioButtons.add(radioButton);
        radioButton.otherChoices = radioButtons;

        WidgetRadioButton radioButton2 = new WidgetRadioButton(guiLeft + 4, guiTop + 58, 0xFF404040,
                "Done when departing", b -> progWidget.doneWhenDeparting = true);
        radioButton2.checked = progWidget.doneWhenDeparting;
        addButton(radioButton2);
        radioButtons.add(radioButton2);
        radioButton2.otherChoices = radioButtons;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        font.drawString("Move to the next puzzle piece", guiLeft + 8, guiTop + 20, 0xFF404060);
        font.drawString("when arrived or right away?", guiLeft + 8, guiTop + 30, 0xFF404060);
    }

}
