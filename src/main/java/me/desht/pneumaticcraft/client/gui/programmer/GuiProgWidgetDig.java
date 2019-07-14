package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetDig;
import net.minecraft.client.resources.I18n;

public class GuiProgWidgetDig extends GuiProgWidgetDigAndPlace<ProgWidgetDig>{

    public GuiProgWidgetDig(ProgWidgetDig progWidget, GuiProgrammer guiProgrammer){
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        GuiCheckBox requiresDiggingTool = new GuiCheckBox(guiLeft + 4, guiTop + 85, 0xFF404040,
                I18n.format("gui.progWidget.dig.requiresDiggingTool"), b -> progWidget.setRequiresTool(b.checked));
        requiresDiggingTool.setTooltip("gui.progWidget.dig.requiresDiggingTool.tooltip");
        requiresDiggingTool.checked = progWidget.requiresTool();
        addButton(requiresDiggingTool);
    }
}
