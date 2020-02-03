package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetPlace;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.util.Direction;

import java.util.ArrayList;
import java.util.List;

public class GuiProgWidgetPlace<P extends ProgWidgetPlace> extends GuiProgWidgetDigAndPlace<P> {

    public GuiProgWidgetPlace(P progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();
        List<WidgetRadioButton> radioButtons = new ArrayList<>();
        for (Direction dir : Direction.VALUES) {
            WidgetRadioButton radioButton = new WidgetRadioButton(guiLeft + 8, guiTop + 80 + dir.getIndex() * 12, 0xFF404040,
                    PneumaticCraftUtils.getOrientationName(dir), b -> progWidget.placeDir = dir);
            radioButton.checked = progWidget.placeDir == dir;
            addButton(radioButton);
            radioButtons.add(radioButton);
            radioButton.otherChoices = radioButtons;
        }
    }

    @Override
    protected boolean moveActionsToSide() {
        return true;
    }
}
