package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.progwidgets.IBlockRightClicker.RightClickType;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetBlockRightClick;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Direction;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetBlockRightClick extends GuiProgWidgetDigAndPlace<ProgWidgetBlockRightClick> {
    private WidgetCheckBox checkboxSneaking;
    private WidgetComboBox sideSelector;
    private WidgetComboBox clickTypeSelector;

    public GuiProgWidgetBlockRightClick(ProgWidgetBlockRightClick widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        WidgetLabel sideLabel;
        addButton(sideLabel = new WidgetLabel(guiLeft + 8, guiTop + 45, xlate("pneumaticcraft.gui.progWidget.blockRightClick.clickSide")));

        sideSelector = new WidgetComboBox(font, guiLeft + 8 + sideLabel.getWidth() + 5, guiTop + 43, 50, 12)
                .initFromEnum(progWidget.getClickSide(), ClientUtils::translateDirection);
        addButton(sideSelector);

        WidgetLabel opLabel;
        addButton(opLabel = new WidgetLabel(guiLeft + 8, guiTop + 65,
                xlate("pneumaticcraft.gui.progWidget.blockRightClick.operation")));

        clickTypeSelector = new WidgetComboBox(font, guiLeft + 8 + opLabel.getWidth() + 5, guiTop + 63, 80, 12)
                .initFromEnum(progWidget.getClickType());
        clickTypeSelector.setTooltip(PneumaticCraftUtils.splitStringComponent(
                I18n.format("pneumaticcraft.gui.progWidget.blockRightClick.clickType.tooltip")
        ));
        addButton(clickTypeSelector);

        checkboxSneaking = new WidgetCheckBox(guiLeft + 8, guiTop + 83, 0xFF404040,
                xlate("pneumaticcraft.gui.progWidget.blockRightClick.sneaking"));
        checkboxSneaking.setChecked(progWidget.isSneaking());
        checkboxSneaking.setTooltip(xlate("pneumaticcraft.gui.progWidget.blockRightClick.sneaking.tooltip"));
        addButton(checkboxSneaking);

    }

    @Override
    public void onClose() {
        if (sideSelector.getSelectedElementIndex() >= 0) {
            progWidget.setClickSide(Direction.byIndex(sideSelector.getSelectedElementIndex()));
        }
        if (clickTypeSelector.getSelectedElementIndex() >= 0) {
            progWidget.setClickType(RightClickType.values()[clickTypeSelector.getSelectedElementIndex()]);
        }
        progWidget.setSneaking(checkboxSneaking.checked);

        super.onClose();
    }
}
