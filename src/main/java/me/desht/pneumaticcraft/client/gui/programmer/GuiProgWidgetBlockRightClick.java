package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetBlockRightClick;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Direction;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuiProgWidgetBlockRightClick extends GuiProgWidgetPlace<ProgWidgetBlockRightClick> {
    private WidgetCheckBox checkboxSneaking;
    private WidgetComboBox sideSelector;

    public GuiProgWidgetBlockRightClick(ProgWidgetBlockRightClick widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        WidgetLabel label;
        addButton(label = new WidgetLabel(guiLeft + 8, guiTop + 75, I18n.format("pneumaticcraft.gui.progWidget.blockRightClick.clickSide")));

        sideSelector = new WidgetComboBox(font, guiLeft + 8 + label.getWidth() + 5, guiTop + 73, 50, 12);
        List<String> values = Arrays.stream(Direction.VALUES)
                .map(ClientUtils::translateDirection)
                .collect(Collectors.toList());
        sideSelector.setShouldSort(false).setElements(values).setFixedOptions();
        sideSelector.setText(values.get(progWidget.getClickSide().ordinal()));
        addButton(sideSelector);

        checkboxSneaking = new WidgetCheckBox(guiLeft + 8, guiTop + 95, 0xFF404040,
                I18n.format("pneumaticcraft.gui.progWidget.blockRightClick.sneaking"));
        checkboxSneaking.setChecked(progWidget.isSneaking());
        checkboxSneaking.setTooltip(I18n.format("pneumaticcraft.gui.progWidget.blockRightClick.sneaking.tooltip"));
        addButton(checkboxSneaking);
    }

    @Override
    public void onClose() {
        super.onClose();

        if (sideSelector.getSelectedElementIndex() >= 0) {
            progWidget.setClickSide(Direction.byIndex(sideSelector.getSelectedElementIndex()));
        }
        progWidget.setSneaking(checkboxSneaking.checked);
    }
}
