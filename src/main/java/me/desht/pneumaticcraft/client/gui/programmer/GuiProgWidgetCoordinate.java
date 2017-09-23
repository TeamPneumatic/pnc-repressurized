package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.gui.GuiInventorySearcher;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCoordinate;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiProgWidgetCoordinate extends GuiProgWidgetAreaShow<ProgWidgetCoordinate> {
    private GuiInventorySearcher invSearchGui;
    private WidgetTextFieldNumber[] coordFields;
    private WidgetComboBox variableField;
    private GuiButtonSpecial gpsButton;

    public GuiProgWidgetCoordinate(ProgWidgetCoordinate widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui() {
        super.initGui();

        if (invSearchGui != null) {
            BlockPos pos = invSearchGui.getSearchStack() != null ? ItemGPSTool.getGPSLocation(invSearchGui.getSearchStack()) : null;
            widget.setCoordinate(pos);
        }

        List<GuiRadioButton> radioButtons = new ArrayList<GuiRadioButton>();
        GuiRadioButton radioButton = new GuiRadioButton(0, guiLeft + 7, guiTop + 51, 0xFF000000, I18n.format("gui.progWidget.coordinate.constant"));
        if (!widget.isUsingVariable()) radioButton.checked = true;
        radioButtons.add(radioButton);
        radioButton.otherChoices = radioButtons;
        addWidget(radioButton);
        radioButton = new GuiRadioButton(1, guiLeft + 7, guiTop + 100, 0xFF000000, I18n.format("gui.progWidget.coordinate.variable"));
        if (widget.isUsingVariable()) radioButton.checked = true;
        radioButtons.add(radioButton);
        radioButton.otherChoices = radioButtons;
        addWidget(radioButton);

        gpsButton = new GuiButtonSpecial(0, guiLeft + 100, guiTop + 20, 20, 20, "");
        gpsButton.setRenderStacks(new ItemStack(Itemss.GPS_TOOL));
        gpsButton.setTooltipText(I18n.format("gui.progWidget.coordinate.selectFromGPS"));
        gpsButton.enabled = !widget.isUsingVariable();
        buttonList.add(gpsButton);
        coordFields = new WidgetTextFieldNumber[3];
        for (int i = 0; i < 3; i++) {
            coordFields[i] = new WidgetTextFieldNumber(fontRenderer, guiLeft + 100, guiTop + 50 + 13 * i, 40, fontRenderer.FONT_HEIGHT + 1);
            addWidget(coordFields[i]);
            coordFields[i].setEnabled(gpsButton.enabled);
        }
        coordFields[0].setValue(widget.getRawCoordinate().getX());
        coordFields[1].setValue(widget.getRawCoordinate().getY());
        coordFields[2].setValue(widget.getRawCoordinate().getZ());

        variableField = new WidgetComboBox(fontRenderer, guiLeft + 90, guiTop + 112, 80, fontRenderer.FONT_HEIGHT + 1);
        variableField.setElements(guiProgrammer.te.getAllVariables());
        addWidget(variableField);
        variableField.setText(widget.getVariable());
        variableField.setEnabled(widget.isUsingVariable());
    }

    @Override
    public void actionPerformed(IGuiWidget guiWidget) {
        if (guiWidget.getID() == 0 || guiWidget.getID() == 1) {
            widget.setUsingVariable(guiWidget.getID() == 1);
            gpsButton.enabled = guiWidget.getID() == 0;
            for (WidgetTextField textField : coordFields) {
                textField.setEnabled(gpsButton.enabled);
            }

            variableField.setEnabled(!gpsButton.enabled);
        }
        // super.actionPerformed(guiWidget);
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            invSearchGui = new GuiInventorySearcher(FMLClientHandler.instance().getClient().player);
            BlockPos area = widget.getRawCoordinate();
            ItemStack gps = new ItemStack(Itemss.GPS_TOOL);
            ItemGPSTool.setGPSLocation(gps, area);
            invSearchGui.setSearchStack(ItemGPSTool.getGPSLocation(gps) != null ? gps : null);
            FMLClientHandler.instance().showGuiScreen(invSearchGui);
        }
        super.actionPerformed(button);
    }

    @Override
    public void keyTyped(char chr, int keyCode) throws IOException {
        if (keyCode == 1) {
            widget.setCoordinate(new BlockPos(coordFields[0].getValue(), coordFields[1].getValue(), coordFields[2].getValue()));
            widget.setVariable(variableField.getText());
        }
        super.keyTyped(chr, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        fontRenderer.drawString("x:", guiLeft + 90, guiTop + 51, 0xFF000000);
        fontRenderer.drawString("y:", guiLeft + 90, guiTop + 64, 0xFF000000);
        fontRenderer.drawString("z:", guiLeft + 90, guiTop + 77, 0xFF000000);
        fontRenderer.drawString(I18n.format("gui.progWidget.coordinate.variableName"), guiLeft + 90, guiTop + 100, 0xFF000000);
    }
}
