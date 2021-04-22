package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetDropItem;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetDropItem extends GuiProgWidgetImportExport<ProgWidgetDropItem> {

    public GuiProgWidgetDropItem(ProgWidgetDropItem progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        addLabel(xlate("pneumaticcraft.gui.progWidget.drop.dropMethod"), guiLeft + 8, guiTop + 70);

        WidgetRadioButton.Builder.create()
                .addRadioButton(new WidgetRadioButton(guiLeft + 8, guiTop + 82, 0xFF404040,
                                xlate("pneumaticcraft.gui.progWidget.drop.dropMethod.random"),
                                b -> progWidget.setDropStraight(false)),
                        !progWidget.dropStraight())
                .addRadioButton(new WidgetRadioButton(guiLeft + 8, guiTop + 94, 0xFF404040,
                                xlate("pneumaticcraft.gui.progWidget.drop.dropMethod.straight"),
                                b -> progWidget.setDropStraight(true)),
                        progWidget.dropStraight())
                .build(this::addButton);

        addButton(new WidgetCheckBox(guiLeft + 8, guiTop + 115, 0xFF404040,
                xlate("pneumaticcraft.gui.progWidget.drop.hasPickupDelay"), b -> progWidget.setPickupDelay(b.checked))
                .setChecked(progWidget.hasPickupDelay()));
    }

    @Override
    protected boolean showSides() {
        return false;
    }
}
