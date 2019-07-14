package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCrafting;

public class GuiProgWidgetCrafting extends GuiProgWidgetImportExport<ProgWidgetCrafting> {
    public GuiProgWidgetCrafting(ProgWidgetCrafting progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    protected boolean showSides() {
        return false;
    }
}
