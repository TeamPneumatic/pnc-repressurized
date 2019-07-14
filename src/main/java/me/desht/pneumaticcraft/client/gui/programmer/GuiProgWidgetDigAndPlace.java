package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.GuiRadioButton;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.progwidgets.IBlockOrdered.EnumOrder;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetDigAndPlace;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public abstract class GuiProgWidgetDigAndPlace<P extends ProgWidgetDigAndPlace> extends GuiProgWidgetAreaShow<P> {

    private WidgetTextFieldNumber textField;

    public GuiProgWidgetDigAndPlace(P progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        List<GuiRadioButton> radioButtons = new ArrayList<>();
        for (EnumOrder order : EnumOrder.values()) {
            GuiRadioButton radioButton = new GuiRadioButton(guiLeft + 4, guiTop + 30 + order.ordinal() * 12, 0xFF404040, order.getLocalizedName(),
                    b -> progWidget.setOrder(order));
            radioButton.checked = order == progWidget.getOrder();
            addButton(radioButton);
            radioButtons.add(radioButton);
            radioButton.otherChoices = radioButtons;
        }

        GuiCheckBox useMaxActions = new GuiCheckBox(guiLeft + (moveActionsToSide() ? 54 : 4), guiTop + 115, 0xFF404040,
                I18n.format("gui.progWidget.digAndPlace.useMaxActions"), b -> {
            progWidget.setUseMaxActions(b.checked);
            textField.setVisible(progWidget.useMaxActions());
        });
        useMaxActions.setTooltip("gui.progWidget.digAndPlace.useMaxActions.tooltip");
        useMaxActions.checked = progWidget.useMaxActions();
        addButton(useMaxActions);

        textField = new WidgetTextFieldNumber(font, guiLeft + (moveActionsToSide() ? 57 : 7), guiTop + 128, 50, 11);
        textField.setValue(progWidget.getMaxActions());
        textField.setVisible(useMaxActions.checked);
        addButton(textField);
    }

    protected boolean moveActionsToSide() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        progWidget.setMaxActions(textField.getValue());
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        font.drawString(TextFormatting.UNDERLINE + "Order", guiLeft + 6, guiTop + 20, 0xFF404060);
    }

}
