package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetDropItem;
import net.minecraft.client.resources.I18n;
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
        WidgetRadioButton radioButton = new WidgetRadioButton(guiLeft + 8, guiTop + 82, 0xFF404040,
                I18n.format("gui.progWidget.drop.dropMethod.random"), b -> progWidget.setDropStraight(false));
        radioButton.checked = !progWidget.dropStraight();
        addButton(radioButton);
        radioButtons.add(radioButton);
        radioButton.otherChoices = radioButtons;

        WidgetRadioButton radioButton2 = new WidgetRadioButton(guiLeft + 8, guiTop + 94, 0xFF404040,
                I18n.format("gui.progWidget.drop.dropMethod.straight"), b -> progWidget.setDropStraight(true));
        radioButton2.checked = progWidget.dropStraight();
        addButton(radioButton2);
        radioButtons.add(radioButton2);
        radioButton2.otherChoices = radioButtons;

        WidgetCheckBox pickupDelay = new WidgetCheckBox(guiLeft + 8, guiTop + 115, 0xFF404040,
                I18n.format("gui.progWidget.drop.hasPickupDelay"), b -> progWidget.setPickupDelay(b.checked));
        pickupDelay.checked = progWidget.hasPickupDelay();
        addButton(pickupDelay);
    }

    @Override
    protected boolean showSides() {
        return false;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        font.drawString(TextFormatting.UNDERLINE + I18n.format("gui.progWidget.drop.dropMethod"), guiLeft + 8, guiTop + 70, 0xFF404060);
    }
}
