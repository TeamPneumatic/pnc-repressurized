package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetBlockCondition;
import net.minecraft.client.resources.I18n;

public class GuiProgWidgetBlockCondition extends GuiProgWidgetCondition<ProgWidgetBlockCondition> {
    public GuiProgWidgetBlockCondition(ProgWidgetBlockCondition widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        addButton(new GuiCheckBox(guiLeft + 5, guiTop + 60, 0xFF404040,
                I18n.format("gui.progWidget.conditionBlock.checkForAir"),
                        b -> progWidget.checkingForAir = b.checked)
                .setChecked(progWidget.checkingForAir)
                .setTooltip(I18n.format("gui.progWidget.conditionBlock.checkForAir.tooltip"))
        );

        addButton(new GuiCheckBox(guiLeft + 5, guiTop + 72, 0xFF404040,
                I18n.format("gui.progWidget.conditionBlock.checkForLiquids"),
                b -> progWidget.checkingForLiquids = b.checked)
                .setChecked(progWidget.checkingForLiquids)
                .setTooltip(I18n.format("gui.progWidget.conditionBlock.checkForLiquids.tooltip")));
    }

    @Override
    protected boolean requiresNumber() {
        return false;
    }

    @Override
    protected boolean isSidedWidget() {
        return false;
    }
}
