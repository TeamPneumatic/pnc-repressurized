package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiRadioButton;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetPlace;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;

public class GuiProgWidgetPlace<Widget extends ProgWidgetPlace> extends GuiProgWidgetDigAndPlace<Widget> {

    public GuiProgWidgetPlace(Widget widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui() {
        super.initGui();
        List<GuiRadioButton> radioButtons = new ArrayList<GuiRadioButton>();
        for (int i = 0; i < 6; i++) {
            GuiRadioButton radioButton = new GuiRadioButton(i + 10, guiLeft + 4, guiTop + 80 + i * 12, 0xFF000000, PneumaticCraftUtils.getOrientationName(EnumFacing.getFront(i)));
            radioButton.checked = widget.placeDir.ordinal() == i;
            addWidget(radioButton);
            radioButtons.add(radioButton);
            radioButton.otherChoices = radioButtons;
        }
    }

    @Override
    protected boolean moveActionsToSide() {
        return true;
    }

    @Override
    public void actionPerformed(IGuiWidget guiWidget) {
        if (guiWidget.getID() >= 10 && guiWidget.getID() < 16)
            widget.placeDir = EnumFacing.getFront(guiWidget.getID() - 10);
        super.actionPerformed(guiWidget);
    }

}
