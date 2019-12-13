package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetDropItem;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class GuiProgWidgetDropItem extends GuiProgWidgetImportExport<ProgWidgetDropItem> {

    public GuiProgWidgetDropItem(ProgWidgetDropItem progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        List<WidgetRadioButton> radioButtons = new ArrayList<>();
        WidgetRadioButton radioButton = new WidgetRadioButton(guiLeft + 4, guiTop + 80, 0xFF404040,
                "Random", b -> progWidget.setDropStraight(false));
        radioButton.checked = !progWidget.dropStraight();
        addButton(radioButton);
        radioButtons.add(radioButton);
        radioButton.otherChoices = radioButtons;

        WidgetRadioButton radioButton2 = new WidgetRadioButton(guiLeft + 4, guiTop + 94, 0xFF404040,
                "Straight", b -> progWidget.setDropStraight(true));
        radioButton2.checked = progWidget.dropStraight();
        addButton(radioButton2);
        radioButtons.add(radioButton2);
        radioButton2.otherChoices = radioButtons;
    }

    @Override
    protected boolean showSides() {
        return false;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        font.drawString(TextFormatting.UNDERLINE + "Drop method", guiLeft + 8, guiTop + 70, 0xFF404060);
    }
}
