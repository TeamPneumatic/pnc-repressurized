package me.desht.pneumaticcraft.client.gui.programmer;

import net.minecraft.client.resources.I18n;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetHarvest;

public class GuiProgWidgetHarvest extends GuiProgWidgetDigAndPlace<ProgWidgetHarvest>{

    private GuiCheckBox requiresHoe;
    
    public GuiProgWidgetHarvest(ProgWidgetHarvest widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui() {
        super.initGui();
        
        requiresHoe = new GuiCheckBox(125, guiLeft + 4, guiTop + 85, 0xFF404040, I18n.format("gui.progWidget.harvest.requiresHoe"));
        requiresHoe.setTooltip("gui.progWidget.harvest.requiresHoe.tooltip");
        requiresHoe.checked = widget.requiresTool();
        addWidget(requiresHoe);
    }

    @Override
    public void actionPerformed(IGuiWidget guiWidget) {
        if(guiWidget == requiresHoe){
            widget.setRequiresTool(requiresHoe.checked);
        }
        super.actionPerformed(guiWidget);
    }
}
