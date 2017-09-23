package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.GuiRadioButton;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetDigAndPlace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

public class GuiProgWidgetDigAndPlace<Widget extends ProgWidgetDigAndPlace> extends GuiProgWidgetAreaShow<Widget> {

    private GuiCheckBox useMaxActions;
    private WidgetTextFieldNumber textField;

    public GuiProgWidgetDigAndPlace(Widget widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui() {
        super.initGui();

        List<GuiRadioButton> radioButtons = new ArrayList<>();
        ProgWidgetDigAndPlace.EnumOrder[] orders = ProgWidgetDigAndPlace.EnumOrder.values();
        for (int i = 0; i < orders.length; i++) {
            GuiRadioButton radioButton = new GuiRadioButton(i, guiLeft + 4, guiTop + 30 + i * 12, 0xFF000000, orders[i].getLocalizedName());
            radioButton.checked = orders[i] == widget.getOrder();
            addWidget(radioButton);
            radioButtons.add(radioButton);
            radioButton.otherChoices = radioButtons;
        }

        useMaxActions = new GuiCheckBox(16, guiLeft + (moveActionsToSide() ? 54 : 4), guiTop + 115, 0xFF000000, I18n.format("gui.progWidget.digAndPlace.useMaxActions"));
        useMaxActions.setTooltip("gui.progWidget.digAndPlace.useMaxActions.tooltip");
        useMaxActions.checked = widget.useMaxActions();
        addWidget(useMaxActions);
        textField = new WidgetTextFieldNumber(Minecraft.getMinecraft().fontRenderer, guiLeft + (moveActionsToSide() ? 57 : 7), guiTop + 128, 50, 11);
        textField.setValue(widget.getMaxActions());
        textField.setEnabled(useMaxActions.checked);
        addWidget(textField);
    }

    protected boolean moveActionsToSide() {
        return false;
    }

    @Override
    public void actionPerformed(IGuiWidget guiWidget) {
        if (guiWidget.getID() >= 0 && guiWidget.getID() < ProgWidgetDigAndPlace.EnumOrder.values().length)
            widget.setOrder(ProgWidgetDigAndPlace.EnumOrder.values()[guiWidget.getID()]);
        if (guiWidget.getID() == 16) {
            widget.setUseMaxActions(((GuiCheckBox) guiWidget).checked);
            textField.setEnabled(widget.useMaxActions());
        }
        super.actionPerformed(guiWidget);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        widget.setMaxActions(textField.getValue());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        fontRenderer.drawString("Order:", guiLeft + 8, guiTop + 20, 0xFF000000);
    }

}
