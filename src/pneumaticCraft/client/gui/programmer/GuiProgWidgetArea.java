package pneumaticCraft.client.gui.programmer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.client.gui.GuiButtonSpecial;
import pneumaticCraft.client.gui.GuiInventorySearcher;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.widget.GuiRadioButton;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.WidgetComboBox;
import pneumaticCraft.client.gui.widget.WidgetLabel;
import pneumaticCraft.client.gui.widget.WidgetTextFieldNumber;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.item.ItemGPSTool;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.progwidgets.ProgWidgetArea;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;

public class GuiProgWidgetArea extends GuiProgWidgetAreaShow<ProgWidgetArea>{
    private GuiInventorySearcher invSearchGui;
    private int pointSearched;
    private WidgetComboBox variableField1;
    private WidgetComboBox variableField2;
    private WidgetTextFieldNumber typeInfoField;

    public GuiProgWidgetArea(ProgWidgetArea widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
        xSize = 256;
    }

    @Override
    public void initGui(){
        super.initGui();

        addLabel("Point 1", guiLeft + 50, guiTop + 10);
        addLabel("Point 2", guiLeft + 177, guiTop + 10);
        addLabel("Area Type:", guiLeft + 4, guiTop + 50);

        boolean advancedMode = Config.getProgrammerDifficulty() == 2;
        GuiButtonSpecial gpsButton1 = new GuiButtonSpecial(0, guiLeft + (advancedMode ? 6 : 55), guiTop + 20, 20, 20, "");
        GuiButtonSpecial gpsButton2 = new GuiButtonSpecial(1, guiLeft + (advancedMode ? 133 : 182), guiTop + 20, 20, 20, "");
        gpsButton1.setTooltipText(I18n.format("gui.progWidget.area.selectGPS1"));
        gpsButton2.setTooltipText(I18n.format("gui.progWidget.area.selectGPS2"));
        gpsButton1.setRenderStacks(new ItemStack(Itemss.GPSTool));
        gpsButton2.setRenderStacks(new ItemStack(Itemss.GPSTool));
        buttonList.add(gpsButton1);
        buttonList.add(gpsButton2);

        variableField1 = new WidgetComboBox(fontRendererObj, guiLeft + 28, guiTop + 25, 88, fontRendererObj.FONT_HEIGHT + 1);
        variableField2 = new WidgetComboBox(fontRendererObj, guiLeft + 155, guiTop + 25, 88, fontRendererObj.FONT_HEIGHT + 1);
        Set<String> variables = guiProgrammer.te.getAllVariables();
        variableField1.setElements(variables);
        variableField2.setElements(variables);
        variableField1.setText(widget.getCoord1Variable());
        variableField2.setText(widget.getCoord2Variable());
        typeInfoField = new WidgetTextFieldNumber(fontRendererObj, guiLeft + 160, guiTop + 110, 20, fontRendererObj.FONT_HEIGHT + 1);
        typeInfoField.setValue(widget.typeInfo);
        typeInfoField.setTooltip(I18n.format("gui.progWidget.area.extraInfo.tooltip"));
        addWidget(typeInfoField);
        addWidget(new WidgetLabel(guiLeft + 160, guiTop + 100, I18n.format("gui.progWidget.area.extraInfo")));
        if(advancedMode) {
            addWidget(variableField1);
            addWidget(variableField2);
        }

        List<GuiRadioButton> radioButtons = new ArrayList<GuiRadioButton>();
        ProgWidgetArea.EnumAreaType[] areaTypes = ProgWidgetArea.EnumAreaType.values();
        for(int i = 0; i < areaTypes.length; i++) {
            GuiRadioButton radioButton = new GuiRadioButton(i, guiLeft + 7 + i / 7 * 80, guiTop + 60 + i % 7 * 12, 0xFF000000, areaTypes[i].toString());
            radioButton.checked = areaTypes[i] == widget.type;
            addWidget(radioButton);
            radioButtons.add(radioButton);
            radioButton.otherChoices = radioButtons;
        }
        if(invSearchGui != null) {
            ChunkPosition pos = invSearchGui.getSearchStack() != null ? ItemGPSTool.getGPSLocation(invSearchGui.getSearchStack()) : null;
            if(pos != null) {
                if(pointSearched == 0) {
                    widget.x1 = pos.chunkPosX;
                    widget.y1 = pos.chunkPosY;
                    widget.z1 = pos.chunkPosZ;
                } else {
                    widget.x2 = pos.chunkPosX;
                    widget.y2 = pos.chunkPosY;
                    widget.z2 = pos.chunkPosZ;
                }
            } else {
                if(pointSearched == 0) {
                    widget.x1 = widget.y1 = widget.z1 = 0;
                } else {
                    widget.x2 = widget.y2 = widget.z2 = 0;
                }
            }
        }
    }

    @Override
    public void actionPerformed(IGuiWidget guiWidget){
        if(guiWidget instanceof GuiRadioButton) {
            widget.type = ProgWidgetArea.EnumAreaType.values()[guiWidget.getID()];
            typeInfoField.setEnabled(widget.type.utilizesTypeInfo);
        }
        super.actionPerformed(guiWidget);
    }

    @Override
    public void actionPerformed(GuiButton button){
        if(button.id == 0 || button.id == 1) {
            invSearchGui = new GuiInventorySearcher(FMLClientHandler.instance().getClient().thePlayer);
            ItemStack gps = new ItemStack(Itemss.GPSTool);
            if(button.id == 0) {
                ItemGPSTool.setGPSLocation(gps, widget.x1, widget.y1, widget.z1);
            } else {
                ItemGPSTool.setGPSLocation(gps, widget.x2, widget.y2, widget.z2);
            }
            invSearchGui.setSearchStack(ItemGPSTool.getGPSLocation(gps) != null ? gps : null);
            FMLClientHandler.instance().showGuiScreen(invSearchGui);
            pointSearched = button.id;
        }
        super.actionPerformed(button);
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.GUI_WIDGET_AREA;
    }

    @Override
    public void keyTyped(char key, int keyCode){
        if(keyCode == 1) {
            widget.setCoord1Variable(variableField1.getText());
            widget.setCoord2Variable(variableField2.getText());
            widget.typeInfo = typeInfoField.getValue();
        }
        super.keyTyped(key, keyCode);
    }

}
