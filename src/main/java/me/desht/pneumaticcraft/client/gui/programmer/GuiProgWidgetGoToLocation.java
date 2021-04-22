package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetGoToLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetGoToLocation<T extends ProgWidgetGoToLocation> extends GuiProgWidgetAreaShow<T> {

    public GuiProgWidgetGoToLocation(T progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        WidgetRadioButton.Builder.create()
                .addRadioButton(new WidgetRadioButton(guiLeft + 8, guiTop + 24, 0xFF404040,
                                xlate("pneumaticcraft.gui.progWidget.goto.doneWhenArrived"),
                                b -> progWidget.setDoneWhenDeparting(false))
                                .setTooltip(xlate("pneumaticcraft.gui.progWidget.goto.doneWhenArrived.tooltip")),
                        !progWidget.doneWhenDeparting())
                .addRadioButton(new WidgetRadioButton(guiLeft + 8, guiTop + 38, 0xFF404040,
                                xlate("pneumaticcraft.gui.progWidget.goto.doneWhenDeparting"),
                                b -> progWidget.setDoneWhenDeparting(true))
                                .setTooltip(xlate("pneumaticcraft.gui.progWidget.goto.doneWhenDeparting.tooltip")),
                        progWidget.doneWhenDeparting())
                .build(this::addButton);
    }
}
