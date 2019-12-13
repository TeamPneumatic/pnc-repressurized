package me.desht.pneumaticcraft.client.gui.programmer;

import com.google.common.collect.Lists;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.GuiInventorySearcher;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.progwidgets.area.AreaType;
import me.desht.pneumaticcraft.common.progwidgets.area.AreaType.AreaTypeWidget;
import me.desht.pneumaticcraft.common.progwidgets.area.AreaType.AreaTypeWidgetEnum;
import me.desht.pneumaticcraft.common.progwidgets.area.AreaType.AreaTypeWidgetInteger;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GuiProgWidgetArea extends GuiProgWidgetAreaShow<ProgWidgetArea> {
    private GuiInventorySearcher invSearchGui;
    private int pointSearched;
    private WidgetComboBox variableField1;
    private WidgetComboBox variableField2;

    private final List<AreaType> allAreaTypes = ProgWidgetArea.getAllAreaTypes();
    private final List<Pair<AreaTypeWidget, Widget>> areaTypeValueWidgets = new ArrayList<>();
    private final List<Widget> areaTypeStaticWidgets = new ArrayList<>();

    public GuiProgWidgetArea(ProgWidgetArea widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);

        xSize = 256;
    }

    @Override
    public void init() {
        super.init();

        addLabel(I18n.format("gui.progWidget.area.point1"), guiLeft + 50, guiTop + 10);
        addLabel(I18n.format("gui.progWidget.area.point2"), guiLeft + 177, guiTop + 10);
        addLabel(I18n.format("gui.progWidget.area.type"), guiLeft + 4, guiTop + 50);

        boolean advancedMode = PNCConfig.Client.programmerDifficulty == IProgWidget.WidgetDifficulty.ADVANCED;
        WidgetButtonExtended gpsButton1 = new WidgetButtonExtended(guiLeft + (advancedMode ? 6 : 55), guiTop + 20, 20, 20, "", b -> openInvSearchGUI(0));
        WidgetButtonExtended gpsButton2 = new WidgetButtonExtended(guiLeft + (advancedMode ? 133 : 182), guiTop + 20, 20, 20, "", b -> openInvSearchGUI(1));
        gpsButton1.setRenderStacks(new ItemStack(ModItems.GPS_TOOL));
        gpsButton2.setRenderStacks(new ItemStack(ModItems.GPS_TOOL));
        addButton(gpsButton1);
        addButton(gpsButton2);

        variableField1 = new WidgetComboBox(font, guiLeft + 28, guiTop + 25, 88, font.FONT_HEIGHT + 1);
        variableField2 = new WidgetComboBox(font, guiLeft + 155, guiTop + 25, 88, font.FONT_HEIGHT + 1);
        Set<String> variables = guiProgrammer == null ? Collections.emptySet() : guiProgrammer.te.getAllVariables();
        variableField1.setElements(variables);
        variableField2.setElements(variables);
        variableField1.setText(progWidget.getCoord1Variable());
        variableField2.setText(progWidget.getCoord2Variable());

        if (advancedMode) {
            addButton(variableField1);
            addButton(variableField2);
        }

        final int widgetsPerColumn = 5;
        List<WidgetRadioButton> radioButtons = new ArrayList<>();
        for (int i = 0; i < allAreaTypes.size(); i++) {
            final AreaType areaType = allAreaTypes.get(i);
            WidgetRadioButton radioButton = new WidgetRadioButton(guiLeft + widgetsPerColumn + i / widgetsPerColumn * 80, guiTop + 60 + i % widgetsPerColumn * 12, 0xFF404040, areaType.getName(), b -> {
                progWidget.type = areaType;
                switchToWidgets(areaType);
            });
            if (progWidget.type.getClass() == areaType.getClass()) {
                allAreaTypes.set(i, progWidget.type);
                radioButton.checked = true;
            }

            addButton(radioButton);
            radioButtons.add(radioButton);
            radioButton.otherChoices = radioButtons;
        }

        //typeInfoField.setTooltip(I18n.format("gui.progWidget.area.extraInfo.tooltip"));
        //addWidget(new WidgetLabel(guiLeft + 160, guiTop + 100, I18n.format("gui.progWidget.area.extraInfo")));
        switchToWidgets(progWidget.type);

        if (invSearchGui != null) {
            ItemStack stack = invSearchGui.getSearchStack();
            if (stack.getItem() instanceof IPositionProvider) {
                List<BlockPos> posList = ((IPositionProvider) stack.getItem()).getStoredPositions(stack);
                if (!posList.isEmpty()) {
                    BlockPos pos = posList.get(0);
                    if (pos != null) {
                        if (pointSearched == 0) {
                            progWidget.x1 = pos.getX();
                            progWidget.y1 = pos.getY();
                            progWidget.z1 = pos.getZ();
                        } else {
                            progWidget.x2 = pos.getX();
                            progWidget.y2 = pos.getY();
                            progWidget.z2 = pos.getZ();
                        }
                    } else {
                        if (pointSearched == 0) {
                            progWidget.x1 = progWidget.y1 = progWidget.z1 = 0;
                        } else {
                            progWidget.x2 = progWidget.y2 = progWidget.z2 = 0;
                        }
                    }
                }
            }
        }

        List<String> b1List = Lists.newArrayList(I18n.format("gui.progWidget.area.selectGPS1"));
        if (progWidget.x1 != 0 || progWidget.y1 != 0 || progWidget.z1 != 0) {
            b1List.add(String.format(TextFormatting.GRAY + "[Current] %d, %d, %d", progWidget.x1, progWidget.y1, progWidget.z1));
        }
        gpsButton1.setTooltipText(b1List);

        List<String> b2List = Lists.newArrayList(I18n.format("gui.progWidget.area.selectGPS2"));
        if (progWidget.x2 != 0 || progWidget.y2 != 0 || progWidget.z2 != 0) {
            b2List.add(String.format(TextFormatting.GRAY + "[Current] %d, %d, %d", progWidget.x2, progWidget.y2, progWidget.z2));
        }
        gpsButton2.setTooltipText(b2List);
    }

    private void openInvSearchGUI(int which) {
        ItemStack gpsStack = new ItemStack(ModItems.GPS_TOOL);
        if (which == 0) {
            ItemGPSTool.setGPSLocation(gpsStack, new BlockPos(progWidget.x1, progWidget.y1, progWidget.z1));
        } else {
            ItemGPSTool.setGPSLocation(gpsStack, new BlockPos(progWidget.x2, progWidget.y2, progWidget.z2));
        }
        ClientUtils.openContainerGui(ModContainerTypes.INVENTORY_SEARCHER, new StringTextComponent("Inventory Searcher (GPS)"));
        if (minecraft.currentScreen instanceof GuiInventorySearcher) {
            invSearchGui = (GuiInventorySearcher) minecraft.currentScreen;
            invSearchGui.setStackPredicate(itemStack -> itemStack.getItem() instanceof IPositionProvider);
            invSearchGui.setSearchStack(ItemGPSTool.getGPSLocation(gpsStack) != null ? gpsStack : ItemStack.EMPTY);
        }
        pointSearched = which;
    }

    private void switchToWidgets(AreaType type) {
        saveWidgets();

        areaTypeValueWidgets.forEach(p -> removeWidget(p.getRight()));
        areaTypeStaticWidgets.forEach(this::removeWidget);

        areaTypeValueWidgets.clear();
        areaTypeStaticWidgets.clear();

        int curY = guiTop + 60;
        int x = guiLeft + 150;
        List<AreaTypeWidget> atWidgets = new ArrayList<>();
        type.addUIWidgets(atWidgets);
        for (AreaTypeWidget areaTypeWidget : atWidgets) {
            WidgetLabel titleWidget = new WidgetLabel(x, curY, I18n.format(areaTypeWidget.title));
            addButton(titleWidget);
            areaTypeStaticWidgets.add(titleWidget);
            curY += font.FONT_HEIGHT + 1;

            if (areaTypeWidget instanceof AreaTypeWidgetInteger) {
                AreaTypeWidgetInteger intWidget = (AreaTypeWidgetInteger) areaTypeWidget;
                WidgetTextFieldNumber intField = new WidgetTextFieldNumber(font, x, curY, 40, font.FONT_HEIGHT + 1);
                intField.setValue(intWidget.readAction.get());
                addButton(intField);
                areaTypeValueWidgets.add(new ImmutablePair<>(areaTypeWidget, intField));

                curY += font.FONT_HEIGHT + 20;
            } else if (areaTypeWidget instanceof AreaTypeWidgetEnum<?>) {
                AreaTypeWidgetEnum<?> enumWidget = (AreaTypeWidgetEnum<?>) areaTypeWidget;
                WidgetComboBox enumCbb = new WidgetComboBox(font, x, curY, 80, font.FONT_HEIGHT + 1).setFixedOptions();
                enumCbb.setElements(getEnumNames(enumWidget.enumClass));
                enumCbb.setText(enumWidget.readAction.get().toString());
                addButton(enumCbb);
                areaTypeValueWidgets.add(new ImmutablePair<>(areaTypeWidget, enumCbb));

                curY += font.FONT_HEIGHT + 20;
            } else {
                throw new IllegalStateException("Invalid widget type: " + areaTypeWidget.getClass());
            }
        }
    }

    private void saveWidgets() {
        for (Pair<AreaTypeWidget, Widget> entry : areaTypeValueWidgets) {
            AreaTypeWidget widget = entry.getLeft();
            Widget guiWidget = entry.getRight();
            if (widget instanceof AreaTypeWidgetInteger) {
                AreaTypeWidgetInteger intWidget = (AreaTypeWidgetInteger) widget;
                intWidget.writeAction.accept(((WidgetTextFieldNumber) guiWidget).getValue());
            } else if (widget instanceof AreaTypeWidgetEnum<?>) {
                @SuppressWarnings("unchecked")
                AreaTypeWidgetEnum<Enum<?>> enumWidget = (AreaTypeWidgetEnum<Enum<?>>) widget;
                WidgetComboBox cbb = (WidgetComboBox) guiWidget;
                List<String> enumNames = getEnumNames(enumWidget.enumClass);
                Object[] enumValues = enumWidget.enumClass.getEnumConstants();
                Object selectedValue = enumValues[enumNames.indexOf(cbb.getText())];
                enumWidget.writeAction.accept((Enum<?>) selectedValue);
            }
        }
    }
    
    private List<String> getEnumNames(Class<?> enumClass) {
        Object[] enumValues = enumClass.getEnumConstants();
        List<String> enumNames = new ArrayList<>();
        for (Object enumValue : enumValues){
            enumNames.add(enumValue.toString());
        }
        return enumNames;
    }

    @Override
    protected void previewArea(Button button) {
        super.previewArea(button);

        saveWidgets();
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_WIDGET_AREA;
    }

    @Override
    public void onClose() {
        progWidget.setCoord1Variable(variableField1.getText());
        progWidget.setCoord2Variable(variableField2.getText());
        saveWidgets();

        super.onClose();
    }

}
