package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetExternalProgram;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetExternalProgram extends GuiProgWidgetAreaShow<ProgWidgetExternalProgram> {

    private WidgetCheckBox shareVariables;

    public GuiProgWidgetExternalProgram(ProgWidgetExternalProgram widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        shareVariables = new WidgetCheckBox(guiLeft + 10, guiTop + 22, 0xFF404040,
                xlate("pneumaticcraft.gui.progWidget.externalProgram.shareVariables"));
        addButton(shareVariables);
        shareVariables.setTooltip(xlate("pneumaticcraft.gui.progWidget.externalProgram.shareVariables.tooltip"));
        shareVariables.setChecked(progWidget.shareVariables);
    }

    @Override
    public void onClose() {
        progWidget.shareVariables = shareVariables.checked;

        super.onClose();
    }
}
