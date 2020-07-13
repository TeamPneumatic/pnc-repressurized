package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetHarvest;
import net.minecraft.client.resources.I18n;

public class GuiProgWidgetHarvest extends GuiProgWidgetDigAndPlace<ProgWidgetHarvest> {

    public GuiProgWidgetHarvest(ProgWidgetHarvest widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        WidgetCheckBox requiresHoe = new WidgetCheckBox(guiLeft + 8, guiTop + 85, 0xFF404040,
                I18n.format("pneumaticcraft.gui.progWidget.harvest.requiresHoe"), b -> progWidget.setRequiresTool(b.checked));
        requiresHoe.setTooltip("pneumaticcraft.gui.progWidget.harvest.requiresHoe.tooltip");
        requiresHoe.checked = progWidget.requiresTool();
        addButton(requiresHoe);
    }
}
