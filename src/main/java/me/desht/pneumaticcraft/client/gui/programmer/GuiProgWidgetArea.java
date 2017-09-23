package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.gui.GuiInventorySearcher;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GuiProgWidgetArea extends GuiProgWidgetAreaShow<ProgWidgetArea> {
    private GuiInventorySearcher invSearchGui;
    private int pointSearched;
    private WidgetComboBox variableField1;
    private WidgetComboBox variableField2;
    private WidgetTextFieldNumber typeInfoField;

    public GuiProgWidgetArea(ProgWidgetArea widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
        xSize = 256;
    }

    @Override
    public void initGui() {
        super.initGui();

        addLabel("Point 1", guiLeft + 50, guiTop + 10);
        addLabel("Point 2", guiLeft + 177, guiTop + 10);
        addLabel("Area Type:", guiLeft + 4, guiTop + 50);

        boolean advancedMode = ConfigHandler.getProgrammerDifficulty() == 2;
        GuiButtonSpecial gpsButton1 = new GuiButtonSpecial(0, guiLeft + (advancedMode ? 6 : 55), guiTop + 20, 20, 20, "");
        GuiButtonSpecial gpsButton2 = new GuiButtonSpecial(1, guiLeft + (advancedMode ? 133 : 182), guiTop + 20, 20, 20, "");
        gpsButton1.setTooltipText(I18n.format("gui.progWidget.area.selectGPS1"));
        gpsButton2.setTooltipText(I18n.format("gui.progWidget.area.selectGPS2"));
        gpsButton1.setRenderStacks(new ItemStack(Itemss.GPS_TOOL));
        gpsButton2.setRenderStacks(new ItemStack(Itemss.GPS_TOOL));
        buttonList.add(gpsButton1);
        buttonList.add(gpsButton2);

        variableField1 = new WidgetComboBox(fontRenderer, guiLeft + 28, guiTop + 25, 88, fontRenderer.FONT_HEIGHT + 1);
        variableField2 = new WidgetComboBox(fontRenderer, guiLeft + 155, guiTop + 25, 88, fontRenderer.FONT_HEIGHT + 1);
        Set<String> variables = guiProgrammer.te.getAllVariables();
        variableField1.setElements(variables);
        variableField2.setElements(variables);
        variableField1.setText(widget.getCoord1Variable());
        variableField2.setText(widget.getCoord2Variable());
        typeInfoField = new WidgetTextFieldNumber(fontRenderer, guiLeft + 160, guiTop + 110, 20, fontRenderer.FONT_HEIGHT + 1);
        typeInfoField.setValue(widget.typeInfo);
        typeInfoField.setTooltip(I18n.format("gui.progWidget.area.extraInfo.tooltip"));
        addWidget(typeInfoField);
        addWidget(new WidgetLabel(guiLeft + 160, guiTop + 100, I18n.format("gui.progWidget.area.extraInfo")));
        if (advancedMode) {
            addWidget(variableField1);
            addWidget(variableField2);
        }

        List<GuiRadioButton> radioButtons = new ArrayList<GuiRadioButton>();
        ProgWidgetArea.EnumAreaType[] areaTypes = ProgWidgetArea.EnumAreaType.values();
        for (int i = 0; i < areaTypes.length; i++) {
            GuiRadioButton radioButton = new GuiRadioButton(i, guiLeft + 7 + i / 7 * 80, guiTop + 60 + i % 7 * 12, 0xFF000000, areaTypes[i].toString());
            radioButton.checked = areaTypes[i] == widget.type;
            addWidget(radioButton);
            radioButtons.add(radioButton);
            radioButton.otherChoices = radioButtons;
        }
        if (invSearchGui != null) {
            BlockPos pos = invSearchGui.getSearchStack() != null ? ItemGPSTool.getGPSLocation(invSearchGui.getSearchStack()) : null;
            if (pos != null) {
                if (pointSearched == 0) {
                    widget.x1 = pos.getX();
                    widget.y1 = pos.getY();
                    widget.z1 = pos.getZ();
                } else {
                    widget.x2 = pos.getX();
                    widget.y2 = pos.getY();
                    widget.z2 = pos.getZ();
                }
            } else {
                if (pointSearched == 0) {
                    widget.x1 = widget.y1 = widget.z1 = 0;
                } else {
                    widget.x2 = widget.y2 = widget.z2 = 0;
                }
            }
        }
    }

    @Override
    public void actionPerformed(IGuiWidget guiWidget) {
        if (guiWidget instanceof GuiRadioButton) {
            widget.type = ProgWidgetArea.EnumAreaType.values()[guiWidget.getID()];
            typeInfoField.setEnabled(widget.type.utilizesTypeInfo);
        }
        super.actionPerformed(guiWidget);
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0 || button.id == 1) {
            invSearchGui = new GuiInventorySearcher(FMLClientHandler.instance().getClient().player);
            ItemStack gps = new ItemStack(Itemss.GPS_TOOL);
            if (button.id == 0) {
                ItemGPSTool.setGPSLocation(gps, new BlockPos(widget.x1, widget.y1, widget.z1));
            } else {
                ItemGPSTool.setGPSLocation(gps, new BlockPos(widget.x2, widget.y2, widget.z2));
            }
            invSearchGui.setSearchStack(ItemGPSTool.getGPSLocation(gps) != null ? gps : ItemStack.EMPTY);
            FMLClientHandler.instance().showGuiScreen(invSearchGui);
            pointSearched = button.id;
        }
        super.actionPerformed(button);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_WIDGET_AREA;
    }

    @Override
    public void keyTyped(char key, int keyCode) throws IOException {
        if (keyCode == 1) {
            widget.setCoord1Variable(variableField1.getText());
            widget.setCoord2Variable(variableField2.getText());
            widget.typeInfo = typeInfoField.getValue();
        }
        super.keyTyped(key, keyCode);
    }

}
