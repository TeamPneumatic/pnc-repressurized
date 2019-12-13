package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.GuiInventorySearcher;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCoordinate;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class GuiProgWidgetCoordinate extends GuiProgWidgetAreaShow<ProgWidgetCoordinate> {
    private GuiInventorySearcher invSearchGui;
    private WidgetTextFieldNumber[] coordFields;
    private WidgetComboBox variableField;
    private WidgetButtonExtended gpsButton;

    public GuiProgWidgetCoordinate(ProgWidgetCoordinate widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        if (invSearchGui != null) {
            BlockPos pos = !invSearchGui.getSearchStack().isEmpty() ? ItemGPSTool.getGPSLocation(invSearchGui.getSearchStack()) : null;
            progWidget.setCoordinate(pos);
        }

        List<WidgetRadioButton> radioButtons = new ArrayList<>();

        WidgetRadioButton radioButton = new WidgetRadioButton(guiLeft + 7, guiTop + 51, 0xFF404040,
                I18n.format("gui.progWidget.coordinate.constant"), b -> setUsingVariable(false));
        if (!progWidget.isUsingVariable()) radioButton.checked = true;
        radioButtons.add(radioButton);
        radioButton.otherChoices = radioButtons;
        addButton(radioButton);

        radioButton = new WidgetRadioButton(guiLeft + 7, guiTop + 100, 0xFF404040,
                I18n.format("gui.progWidget.coordinate.variable"), b -> setUsingVariable(true));
        if (progWidget.isUsingVariable()) radioButton.checked = true;
        radioButtons.add(radioButton);
        radioButton.otherChoices = radioButtons;
        addButton(radioButton);

        gpsButton = new WidgetButtonExtended(guiLeft + 100, guiTop + 20, 20, 20, "", b -> openGPSSearcher());
        gpsButton.setRenderStacks(new ItemStack(ModItems.GPS_TOOL));
        gpsButton.setTooltipText(I18n.format("gui.progWidget.coordinate.selectFromGPS"));
        gpsButton.active = !progWidget.isUsingVariable();
        addButton(gpsButton);
        coordFields = new WidgetTextFieldNumber[3];
        for (int i = 0; i < 3; i++) {
            coordFields[i] = new WidgetTextFieldNumber(font, guiLeft + 100, guiTop + 50 + 13 * i, 40, font.FONT_HEIGHT + 1);
            addButton(coordFields[i]);
            coordFields[i].setEnabled(gpsButton.active);
        }
        coordFields[0].setValue(progWidget.getRawCoordinate().getX());
        coordFields[1].setValue(progWidget.getRawCoordinate().getY());
        coordFields[2].setValue(progWidget.getRawCoordinate().getZ());

        variableField = new WidgetComboBox(font, guiLeft + 90, guiTop + 112, 80, font.FONT_HEIGHT + 1);
        variableField.setElements(guiProgrammer.te.getAllVariables());
        addButton(variableField);
        variableField.setText(progWidget.getVariable());
        variableField.setEnabled(progWidget.isUsingVariable());
    }

    private void setUsingVariable(boolean usingVariable) {
        progWidget.setUsingVariable(usingVariable);
        gpsButton.active = !usingVariable;
        for (WidgetTextField textField : coordFields) {
            textField.setEnabled(!usingVariable);
        }
        variableField.setEnabled(usingVariable);
    }

    private void openGPSSearcher() {
        ClientUtils.openContainerGui(ModContainerTypes.INVENTORY_SEARCHER, new StringTextComponent("Inventory Searcher (GPS)"));
        if (minecraft.currentScreen instanceof GuiInventorySearcher) {
            invSearchGui = (GuiInventorySearcher) minecraft.currentScreen;
            invSearchGui.setStackPredicate(itemStack -> itemStack.getItem() instanceof IPositionProvider);
            BlockPos area = progWidget.getRawCoordinate();
            ItemStack gpsStack = new ItemStack(ModItems.GPS_TOOL);
            ItemGPSTool.setGPSLocation(gpsStack, area);
            invSearchGui.setSearchStack(ItemGPSTool.getGPSLocation(gpsStack) != null ? gpsStack : ItemStack.EMPTY);
        }
    }

    @Override
    public void onClose() {
        super.onClose();

        progWidget.setCoordinate(new BlockPos(coordFields[0].getValue(), coordFields[1].getValue(), coordFields[2].getValue()));
        progWidget.setVariable(variableField.getText());
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        font.drawString("x:", guiLeft + 90, guiTop + 51, 0xFF404040);
        font.drawString("y:", guiLeft + 90, guiTop + 64, 0xFF404040);
        font.drawString("z:", guiLeft + 90, guiTop + 77, 0xFF404040);
        font.drawString(I18n.format("gui.progWidget.coordinate.variableName"), guiLeft + 90, guiTop + 100, 0xFF404060);
    }
}
