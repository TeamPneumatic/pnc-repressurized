package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.common.progwidgets.IBlockOrdered;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetLiquidImport;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetLiquidImport extends GuiProgWidgetImportExport<ProgWidgetLiquidImport> {
    private WidgetComboBox orderSelector;

    public GuiProgWidgetLiquidImport(ProgWidgetLiquidImport progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        WidgetLabel orderLabel = new WidgetLabel(guiLeft + 8, guiTop + 150, xlate("pneumaticcraft.gui.progWidget.digAndPlace.order"));
        addButton(orderLabel);

        orderSelector = new WidgetComboBox(font,guiLeft + 8 + orderLabel.getWidth() + 5, guiTop + 148, 80, 12)
                .initFromEnum(progWidget.getOrder());
        addButton(orderSelector);
    }

    @Override
    public void onClose() {
        if (orderSelector.getSelectedElementIndex() >= 0) {
            progWidget.setOrder(IBlockOrdered.Ordering.values()[orderSelector.getSelectedElementIndex()]);
        }

        super.onClose();
    }
}
