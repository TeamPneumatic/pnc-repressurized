package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetGoToLocation;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetGoToLocation<T extends ProgWidgetGoToLocation> extends GuiProgWidgetAreaShow<T> {

    public GuiProgWidgetGoToLocation(T progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        List<WidgetRadioButton> radioButtons = new ArrayList<>();
        WidgetRadioButton radioButton = new WidgetRadioButton(guiLeft + 8, guiTop + 24, 0xFF404040,
                xlate("pneumaticcraft.gui.progWidget.goto.doneWhenArrived"), b -> progWidget.setDoneWhenDeparting(false));
        radioButton.checked = !progWidget.doneWhenDeparting();
        radioButton.setTooltip(xlate("pneumaticcraft.gui.progWidget.goto.doneWhenArrived.tooltip"));
        addButton(radioButton);
        radioButtons.add(radioButton);
        radioButton.otherChoices = radioButtons;

        WidgetRadioButton radioButton2 = new WidgetRadioButton(guiLeft + 8, guiTop + 38, 0xFF404040,
                xlate("pneumaticcraft.gui.progWidget.goto.doneWhenDeparting"), b -> progWidget.setDoneWhenDeparting(true));
        radioButton2.checked = progWidget.doneWhenDeparting();
        radioButton2.setTooltip(xlate("pneumaticcraft.gui.progWidget.goto.doneWhenDeparting.tooltip"));
        addButton(radioButton2);
        radioButtons.add(radioButton2);
        radioButton2.otherChoices = radioButtons;
    }
}
