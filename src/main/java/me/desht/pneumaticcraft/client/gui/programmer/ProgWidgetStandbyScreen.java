package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.ProgrammerScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetStandby;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetStandbyScreen<P extends ProgWidgetStandby> extends AbstractProgWidgetScreen<P> {
    public ProgWidgetStandbyScreen(P progWidget, ProgrammerScreen guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        WidgetCheckBox allowPickup = new WidgetCheckBox(guiLeft + 8, guiTop + 20, 0xFF404040,
                xlate("pneumaticcraft.gui.progWidget.standby.allowPickup"), b -> progWidget.setAllowStandbyPickup(b.checked))
                .setTooltipKey("pneumaticcraft.gui.progWidget.standby.allowPickup.tooltip")
                .setChecked(progWidget.allowPickupOnStandby());
        addRenderableWidget(allowPickup);
    }
}