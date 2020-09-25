package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetPickupItem;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetPickupItem extends GuiProgWidgetAreaShow<ProgWidgetPickupItem> {
    public GuiProgWidgetPickupItem(ProgWidgetPickupItem progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        addButton(new WidgetCheckBox(guiLeft + 8, guiTop + 25, 0xFF404040,
                xlate("pneumaticcraft.gui.progWidget.pickup.canSteal"), b -> progWidget.setCanSteal(b.checked))
                .setTooltipKey("pneumaticcraft.gui.progWidget.pickup.canSteal.tooltip")
                .setChecked(progWidget.canSteal())
        );
    }
}
