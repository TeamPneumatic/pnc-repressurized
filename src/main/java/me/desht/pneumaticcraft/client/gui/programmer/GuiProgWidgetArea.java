package me.desht.pneumaticcraft.client.gui.programmer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.gui.GuiInventorySearcher;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiRadioButton;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.progwidgets.area.AreaType;
import me.desht.pneumaticcraft.common.progwidgets.area.AreaType.AreaTypeWidget;
import me.desht.pneumaticcraft.common.progwidgets.area.AreaType.AreaTypeWidgetEnum;
import me.desht.pneumaticcraft.common.progwidgets.area.AreaType.AreaTypeWidgetInteger;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

public class GuiProgWidgetArea extends GuiProgWidgetAreaShow<ProgWidgetArea> {
    private GuiInventorySearcher invSearchGui;
    private int pointSearched;
    private WidgetComboBox variableField1;
    private WidgetComboBox variableField2;

    private List<AreaType> allAreaTypes = ProgWidgetArea.getAllAreaTypes();
    private List<Pair<AreaTypeWidget,IGuiWidget>> areaTypeValueWidgets = new ArrayList<>();
    private List<IGuiWidget> areaTypeStaticWidgets = new ArrayList<>();

    public GuiProgWidgetArea(ProgWidgetArea widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
        xSize = 256;
    }

    @Override
    public void initGui() {
        super.initGui();

        addLabel(I18n.format("gui.progWidget.area.point1"), guiLeft + 50, guiTop + 10);
        addLabel(I18n.format("gui.progWidget.area.point2"), guiLeft + 177, guiTop + 10);
        addLabel(I18n.format("gui.progWidget.area.type"), guiLeft + 4, guiTop + 50);

        boolean advancedMode = ConfigHandler.getProgrammerDifficulty() == 2;
        GuiButtonSpecial gpsButton1 = new GuiButtonSpecial(0, guiLeft + (advancedMode ? 6 : 55), guiTop + 20, 20, 20, "");
        GuiButtonSpecial gpsButton2 = new GuiButtonSpecial(1, guiLeft + (advancedMode ? 133 : 182), guiTop + 20, 20, 20, "");
        gpsButton1.setRenderStacks(new ItemStack(Itemss.GPS_TOOL));
        gpsButton2.setRenderStacks(new ItemStack(Itemss.GPS_TOOL));
        buttonList.add(gpsButton1);
        buttonList.add(gpsButton2);

        variableField1 = new WidgetComboBox(fontRenderer, guiLeft + 28, guiTop + 25, 88, fontRenderer.FONT_HEIGHT + 1);
        variableField2 = new WidgetComboBox(fontRenderer, guiLeft + 155, guiTop + 25, 88, fontRenderer.FONT_HEIGHT + 1);
        Set<String> variables = guiProgrammer == null ? Collections.emptySet() : guiProgrammer.te.getAllVariables();
        variableField1.setElements(variables);
        variableField2.setElements(variables);
        variableField1.setText(widget.getCoord1Variable());
        variableField2.setText(widget.getCoord2Variable());

        if (advancedMode) {
            addWidget(variableField1);
            addWidget(variableField2);
        }

        final int widgetsPerColumn = 5;
        List<GuiRadioButton> radioButtons = new ArrayList<>();
        for (int i = 0; i < allAreaTypes.size(); i++) {
            AreaType areaType = allAreaTypes.get(i);
            GuiRadioButton radioButton = new GuiRadioButton(i, guiLeft + widgetsPerColumn + i / widgetsPerColumn * 80, guiTop + 60 + i % widgetsPerColumn * 12, 0xFF404040, areaType.getName());
            if(widget.type.getClass() == areaType.getClass()){
                allAreaTypes.set(i, widget.type);
                radioButton.checked = true;
            }

            addWidget(radioButton);
            radioButtons.add(radioButton);
            radioButton.otherChoices = radioButtons;
        }
        
        //typeInfoField.setTooltip(I18n.format("gui.progWidget.area.extraInfo.tooltip"));
        //addWidget(new WidgetLabel(guiLeft + 160, guiTop + 100, I18n.format("gui.progWidget.area.extraInfo")));
        switchToWidgets(widget.type);
        
        if (invSearchGui != null) {
            BlockPos pos = !invSearchGui.getSearchStack().isEmpty() ? ItemGPSTool.getGPSLocation(invSearchGui.getSearchStack()) : null;
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

        List<String> b1List = Lists.newArrayList(I18n.format("gui.progWidget.area.selectGPS1"));
        if (widget.x1 != 0 || widget.y1 != 0 || widget.z1 != 0) {
            b1List.add(String.format(TextFormatting.GRAY + "[Current] %d, %d, %d", widget.x1, widget.y1, widget.z1));
        }
        gpsButton1.setTooltipText(b1List);

        List<String> b2List = Lists.newArrayList(I18n.format("gui.progWidget.area.selectGPS2"));
        if (widget.x2 != 0 || widget.y2 != 0 || widget.z2 != 0) {
            b2List.add(String.format(TextFormatting.GRAY + "[Current] %d, %d, %d", widget.x2, widget.y2, widget.z2));
        }
        gpsButton2.setTooltipText(b2List);
    }
    
    private void switchToWidgets(AreaType type){
        saveWidgets();
        
        areaTypeValueWidgets.forEach(p -> removeWidget(p.getRight()));
        areaTypeStaticWidgets.forEach(w -> removeWidget(w));
        
        areaTypeValueWidgets.clear();
        areaTypeStaticWidgets.clear();
        
        int curY = guiTop + 60;
        int x = guiLeft + 150;
        List<AreaTypeWidget> widgets = new ArrayList<>();
        type.addUIWidgets(widgets);
        for(AreaTypeWidget widget : widgets){
            WidgetLabel titleWidget = new WidgetLabel(x, curY, I18n.format(widget.title));
            addWidget(titleWidget);
            areaTypeStaticWidgets.add(titleWidget);
            curY += fontRenderer.FONT_HEIGHT + 1;
            
            if(widget instanceof AreaTypeWidgetInteger){
                AreaTypeWidgetInteger intWidget = (AreaTypeWidgetInteger)widget;
                WidgetTextFieldNumber intField = new WidgetTextFieldNumber(fontRenderer, x, curY, 40, fontRenderer.FONT_HEIGHT + 1);
                intField.setValue(intWidget.readAction.get());
                addWidget(intField);
                areaTypeValueWidgets.add(new ImmutablePair<AreaType.AreaTypeWidget, IGuiWidget>(widget, intField));
                
                curY += fontRenderer.FONT_HEIGHT + 20;
            }else if(widget instanceof AreaTypeWidgetEnum<?>){
                AreaTypeWidgetEnum<?> enumWidget = (AreaTypeWidgetEnum<?>)widget;                
                WidgetComboBox enumCbb = new WidgetComboBox(fontRenderer, x, curY, 80, fontRenderer.FONT_HEIGHT + 1).setFixedOptions();
                enumCbb.setElements(getEnumNames(enumWidget.enumClass));
                enumCbb.setText(enumWidget.readAction.get().toString());
                addWidget(enumCbb);
                areaTypeValueWidgets.add(new ImmutablePair<AreaType.AreaTypeWidget, IGuiWidget>(widget, enumCbb));
                
                curY += fontRenderer.FONT_HEIGHT + 20;
            }else{
                throw new IllegalStateException("Invalid widget type: " + widget.getClass());
            }
        }
    }
    
    private void saveWidgets(){
        for(Pair<AreaTypeWidget, IGuiWidget> entry : areaTypeValueWidgets){
            AreaTypeWidget widget = entry.getLeft();
            IGuiWidget guiWidget = entry.getRight();
            if(widget instanceof AreaTypeWidgetInteger){
                AreaTypeWidgetInteger intWidget = (AreaTypeWidgetInteger)widget;
                intWidget.writeAction.accept(((WidgetTextFieldNumber)guiWidget).getValue());
            }else if(widget instanceof AreaTypeWidgetEnum<?>){
                @SuppressWarnings("unchecked")
                AreaTypeWidgetEnum<Enum<?>> enumWidget = (AreaTypeWidgetEnum<Enum<?>>)widget;
                WidgetComboBox cbb = (WidgetComboBox)guiWidget;
                List<String> enumNames = getEnumNames(enumWidget.enumClass);
                Object[] enumValues = enumWidget.enumClass.getEnumConstants();
                Object selectedValue = enumValues[enumNames.indexOf(cbb.getText())];
                enumWidget.writeAction.accept((Enum<?>)selectedValue); 
            }
        }
    }
    
    private List<String> getEnumNames(Class<?> enumClass){
        Object[] enumValues = enumClass.getEnumConstants();
        List<String> enumNames = new ArrayList<String>();
        for(Object enumValue : enumValues){
            enumNames.add(enumValue.toString());
        }
        return enumNames;
    }

    @Override
    public void actionPerformed(IGuiWidget guiWidget) {
        if (guiWidget instanceof GuiRadioButton) {
            AreaType areaType = allAreaTypes.get(guiWidget.getID());
            widget.type = areaType; 
            switchToWidgets(areaType);
        }
        super.actionPerformed(guiWidget);
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0 || button.id == 1) {
            invSearchGui = new GuiInventorySearcher(FMLClientHandler.instance().getClient().player);
            invSearchGui.setStackPredicate(itemStack -> itemStack.getItem() instanceof IPositionProvider);
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
        if (button.id == 1000) { //When the area is going to be displayed.
            saveWidgets();
        }
        super.actionPerformed(button);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_WIDGET_AREA;
    }

    @Override
    public void onGuiClosed(){
        super.onGuiClosed();
        
        widget.setCoord1Variable(variableField1.getText());
        widget.setCoord2Variable(variableField2.getText());
        saveWidgets();
    }

}
