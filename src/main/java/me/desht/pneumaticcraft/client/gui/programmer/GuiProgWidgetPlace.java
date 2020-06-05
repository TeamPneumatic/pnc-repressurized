package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetPlace;

public class GuiProgWidgetPlace<P extends ProgWidgetPlace> extends GuiProgWidgetDigAndPlace<P> {
    public GuiProgWidgetPlace(P progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }
}
