package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetLiquidExport;

public class GuiProgWidgetLiquidExport extends GuiProgWidgetImportExport<ProgWidgetLiquidExport> {

    public GuiProgWidgetLiquidExport(ProgWidgetLiquidExport widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        WidgetCheckBox checkbox = new WidgetCheckBox(guiLeft + 8, guiTop + 150, 0xFF404040,
                "pneumaticcraft.gui.progWidget.liquidExport.placeFluidInWorld", b -> progWidget.setPlaceFluidBlocks(b.checked));
        checkbox.setChecked(progWidget.isPlacingFluidBlocks());
        addButton(checkbox);
    }
}
