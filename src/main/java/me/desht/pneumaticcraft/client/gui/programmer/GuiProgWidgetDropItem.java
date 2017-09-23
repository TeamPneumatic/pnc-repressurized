package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiRadioButton;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetDropItem;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetInventoryBase;

import java.util.ArrayList;
import java.util.List;

public class GuiProgWidgetDropItem extends GuiProgWidgetImportExport {

    public GuiProgWidgetDropItem(ProgWidgetInventoryBase widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    protected boolean showSides() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();

        List<GuiRadioButton> radioButtons = new ArrayList<>();
        GuiRadioButton radioButton = new GuiRadioButton(7, guiLeft + 4, guiTop + 80, 0xFF000000, "Random");
        radioButton.checked = !((ProgWidgetDropItem) widget).dropStraight();
        addWidget(radioButton);
        radioButtons.add(radioButton);
        radioButton.otherChoices = radioButtons;

        GuiRadioButton radioButton2 = new GuiRadioButton(8, guiLeft + 4, guiTop + 94, 0xFF000000, "Straight");
        radioButton2.checked = ((ProgWidgetDropItem) widget).dropStraight();
        addWidget(radioButton2);
        radioButtons.add(radioButton2);
        radioButton2.otherChoices = radioButtons;
    }

    @Override
    public void actionPerformed(IGuiWidget guiWidget) {
        if (guiWidget.getID() == 7 || guiWidget.getID() == 8) {
            ((ProgWidgetDropItem) widget).setDropStraight(guiWidget.getID() == 8);
        }
        super.actionPerformed(guiWidget);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        fontRenderer.drawString("Drop method:", guiLeft + 8, guiTop + 70, 0xFF000000);
    }
}
