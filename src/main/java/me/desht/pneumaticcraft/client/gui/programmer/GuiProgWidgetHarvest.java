package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetHarvest;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetHarvest extends GuiProgWidgetDigAndPlace<ProgWidgetHarvest> {

    public GuiProgWidgetHarvest(ProgWidgetHarvest widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        WidgetCheckBox requiresHoe = new WidgetCheckBox(guiLeft + 8, guiTop + 85, 0xFF404040,
                xlate("pneumaticcraft.gui.progWidget.harvest.requiresHoe"), b -> progWidget.setRequiresTool(b.checked))
                .setTooltipKey("pneumaticcraft.gui.progWidget.harvest.requiresHoe.tooltip")
                .setChecked(progWidget.requiresTool());
        addButton(requiresHoe);
    }
}
