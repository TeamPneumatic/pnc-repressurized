package pneumaticCraft.client.gui.programmer;

import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetLiquidExport;

public class GuiProgWidgetLiquidExport extends GuiProgWidgetImportExport<ProgWidgetLiquidExport>{

    public GuiProgWidgetLiquidExport(ProgWidgetLiquidExport widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui(){
        super.initGui();

        GuiCheckBox checkbox = new GuiCheckBox(300, guiLeft + 70, guiTop + 70, 0xFF000000, "gui.progWidget.liquidExport.placeFluidInWorld");
        checkbox.setChecked(widget.isPlacingFluidBlocks());
        addWidget(checkbox);
    }

    @Override
    public void actionPerformed(IGuiWidget w){
        if(w.getID() == 300) {
            widget.setPlaceFluidBlocks(((GuiCheckBox)w).checked);
        }
        super.actionPerformed(w);
    }
}
