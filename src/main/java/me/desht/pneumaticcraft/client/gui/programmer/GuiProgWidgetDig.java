package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetDig;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetHarvest;
import net.minecraft.client.resources.I18n;

public class GuiProgWidgetDig extends GuiProgWidgetDigAndPlace<ProgWidgetDig>{

    private GuiCheckBox requiresDiggingTool;
    
    public GuiProgWidgetDig(ProgWidgetDig widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui() {
        super.initGui();
        
        requiresDiggingTool = new GuiCheckBox(125, guiLeft + 4, guiTop + 85, 0xFF404040, I18n.format("gui.progWidget.dig.requiresDiggingTool"));
        requiresDiggingTool.setTooltip("gui.progWidget.dig.requiresDiggingTool.tooltip");
        requiresDiggingTool.checked = widget.requiresTool();
        addWidget(requiresDiggingTool);
    }

    @Override
    public void actionPerformed(IGuiWidget guiWidget) {
        if(guiWidget == requiresDiggingTool){
            widget.setRequiresTool(requiresDiggingTool.checked);
        }
        super.actionPerformed(guiWidget);
    }
}
