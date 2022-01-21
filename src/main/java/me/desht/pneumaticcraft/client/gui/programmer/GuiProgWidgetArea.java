/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.GuiInventorySearcher;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.progwidgets.area.AreaType;
import me.desht.pneumaticcraft.common.progwidgets.area.AreaType.AreaTypeWidget;
import me.desht.pneumaticcraft.common.progwidgets.area.AreaType.AreaTypeWidgetEnum;
import me.desht.pneumaticcraft.common.progwidgets.area.AreaType.AreaTypeWidgetInteger;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetArea extends GuiProgWidgetAreaShow<ProgWidgetArea> {
    private GuiInventorySearcher invSearchGui;
    private int pointSearched;
    private WidgetComboBox variableField1;
    private WidgetComboBox variableField2;

    private final List<AreaType> allAreaTypes = ProgWidgetArea.getAllAreaTypes();
    private final List<Pair<AreaTypeWidget, AbstractWidget>> areaTypeValueWidgets = new ArrayList<>();
    private final List<AbstractWidget> areaTypeStaticWidgets = new ArrayList<>();

    public GuiProgWidgetArea(ProgWidgetArea widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);

        xSize = 256;
    }

    @Override
    public void init() {
        super.init();

        boolean advancedMode = ConfigHelper.client().general.programmerDifficulty.get() == IProgWidget.WidgetDifficulty.ADVANCED;

        // GPS buttons
        WidgetButtonExtended gpsButton1 = new WidgetButtonExtended(guiLeft + (advancedMode ? 6 : 55), guiTop + 30, 20, 20, TextComponent.EMPTY, b -> openInvSearchGUI(0))
                .setRenderStacks(new ItemStack(ModItems.GPS_TOOL.get()))
                .setTooltipText(xlate("pneumaticcraft.gui.progWidget.area.selectGPS1"));
        addRenderableWidget(gpsButton1);
        WidgetButtonExtended gpsButton2 = new WidgetButtonExtended(guiLeft + (advancedMode ? 133 : 182), guiTop + 30, 20, 20, TextComponent.EMPTY, b -> openInvSearchGUI(1))
                .setRenderStacks(new ItemStack(ModItems.GPS_TOOL.get()))
                .setTooltipText(xlate("pneumaticcraft.gui.progWidget.area.selectGPS2"));
        addRenderableWidget(gpsButton2);

        // variable textfields
        variableField1 = new WidgetComboBox(font, guiLeft + 28, guiTop + 35, 88, font.lineHeight + 1);
        variableField2 = new WidgetComboBox(font, guiLeft + 155, guiTop + 35, 88, font.lineHeight + 1);
        Set<String> variables = guiProgrammer == null ? Collections.emptySet() : guiProgrammer.te.getAllVariables();
        variableField1.setElements(variables);
        variableField2.setElements(variables);
        variableField1.setValue(progWidget.getCoord1Variable());
        variableField2.setValue(progWidget.getCoord2Variable());
        if (advancedMode) {
            addRenderableWidget(variableField1);
            addRenderableWidget(variableField2);
        }

        // type selector radio buttons
        addLabel(xlate("pneumaticcraft.gui.progWidget.area.type"), guiLeft + 8, guiTop + 88);
        final int widgetsPerColumn = 5;
        WidgetRadioButton.Builder<WidgetRadioButton> builder = WidgetRadioButton.Builder.create();
        for (int i = 0; i < allAreaTypes.size(); i++) {
            final AreaType areaType = allAreaTypes.get(i);
            WidgetRadioButton radioButton = new WidgetRadioButton(guiLeft + widgetsPerColumn + i / widgetsPerColumn * 80, guiTop + 100 + i % widgetsPerColumn * 12, 0xFF404040, xlate(areaType.getTranslationKey()), b -> {
                progWidget.type = areaType;
                switchToWidgets(areaType);
            });
            if (progWidget.type.getClass() == areaType.getClass()) {
                allAreaTypes.set(i, progWidget.type);
            }
            builder.addRadioButton(radioButton, progWidget.type.getClass() == areaType.getClass());
        }
        builder.build(this::addRenderableWidget);
        switchToWidgets(progWidget.type);

        if (invSearchGui != null) {
            // returning from GPS selection GUI; copy the selected blockpos to the progwidget
            BlockPos searchPos = invSearchGui.getBlockPos();
            if (pointSearched == 0) {
                progWidget.x1 = searchPos.getX();
                progWidget.y1 = searchPos.getY();
                progWidget.z1 = searchPos.getZ();
            } else {
                progWidget.x2 = searchPos.getX();
                progWidget.y2 = searchPos.getY();
                progWidget.z2 = searchPos.getZ();
            }
        }

        // blockpos labels
        String l1 = "P1: " + ChatFormatting.DARK_BLUE + formatPos(progWidget.x1, progWidget.y1, progWidget.z1);
        addLabel(new TextComponent(l1), guiLeft + 8, guiTop + 20);
        String l2 = "P2: " + ChatFormatting.DARK_BLUE + formatPos(progWidget.x2, progWidget.y2, progWidget.z2);
        addLabel(new TextComponent(l2), guiLeft + 133, guiTop + 20);
    }

    private String formatPos(int x, int y, int z) {
        return x == 0 && y == 0 && z == 0 ? "-" : String.format("[ %d, %d, %d ]", x, y, z);
    }

    private void openInvSearchGUI(int which) {
        ItemStack gpsStack = new ItemStack(ModItems.GPS_TOOL.get());
        if (which == 0) {
            ItemGPSTool.setGPSLocation(gpsStack, new BlockPos(progWidget.x1, progWidget.y1, progWidget.z1));
        } else {
            ItemGPSTool.setGPSLocation(gpsStack, new BlockPos(progWidget.x2, progWidget.y2, progWidget.z2));
        }
        ClientUtils.openContainerGui(ModContainers.INVENTORY_SEARCHER.get(), new TextComponent("Inventory Searcher (GPS)"));
        if (minecraft.screen instanceof GuiInventorySearcher) {
            invSearchGui = (GuiInventorySearcher) minecraft.screen;
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

        int curY = guiTop + 100;
        int x = guiLeft + 150;
        List<AreaTypeWidget> atWidgets = new ArrayList<>();
        type.addUIWidgets(atWidgets);
        for (AreaTypeWidget areaTypeWidget : atWidgets) {
            WidgetLabel titleWidget = new WidgetLabel(x, curY, xlate(areaTypeWidget.title));
            addRenderableWidget(titleWidget);
            areaTypeStaticWidgets.add(titleWidget);
            curY += font.lineHeight + 1;

            if (areaTypeWidget instanceof AreaTypeWidgetInteger) {
                AreaTypeWidgetInteger intWidget = (AreaTypeWidgetInteger) areaTypeWidget;
                WidgetTextFieldNumber intField = new WidgetTextFieldNumber(font, x, curY, 40, font.lineHeight + 1).setRange(0, Integer.MAX_VALUE);
                intField.setValue(intWidget.readAction.get());
                addRenderableWidget(intField);
                areaTypeValueWidgets.add(new ImmutablePair<>(areaTypeWidget, intField));

                curY += font.lineHeight + 20;
            } else if (areaTypeWidget instanceof AreaTypeWidgetEnum<?>) {
                AreaTypeWidgetEnum<?> enumWidget = (AreaTypeWidgetEnum<?>) areaTypeWidget;
                WidgetComboBox enumCbb = new WidgetComboBox(font, x, curY, 80, font.lineHeight + 1).setFixedOptions();
                enumCbb.setElements(getEnumNames(enumWidget.enumClass));
                enumCbb.setValue(enumWidget.readAction.get().toString());
                addRenderableWidget(enumCbb);
                areaTypeValueWidgets.add(new ImmutablePair<>(areaTypeWidget, enumCbb));

                curY += font.lineHeight + 20;
            } else {
                throw new IllegalStateException("Invalid widget type: " + areaTypeWidget.getClass());
            }
        }
    }

    private void saveWidgets() {
        for (Pair<AreaTypeWidget, AbstractWidget> entry : areaTypeValueWidgets) {
            AreaTypeWidget widget = entry.getLeft();
            AbstractWidget guiWidget = entry.getRight();
            if (widget instanceof AreaTypeWidgetInteger) {
                AreaTypeWidgetInteger intWidget = (AreaTypeWidgetInteger) widget;
                intWidget.writeAction.accept(((WidgetTextFieldNumber) guiWidget).getIntValue());
            } else if (widget instanceof AreaTypeWidgetEnum<?>) {
                @SuppressWarnings("unchecked")
                AreaTypeWidgetEnum<Enum<?>> enumWidget = (AreaTypeWidgetEnum<Enum<?>>) widget;
                WidgetComboBox cbb = (WidgetComboBox) guiWidget;
                List<String> enumNames = getEnumNames(enumWidget.enumClass);
                Object[] enumValues = enumWidget.enumClass.getEnumConstants();
                Object selectedValue = enumValues[enumNames.indexOf(cbb.getValue())];
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
    protected void previewArea(WidgetCheckBox button) {
        super.previewArea(button);

        saveWidgets();
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_WIDGET_AREA;
    }

    @Override
    public void removed() {
        progWidget.setCoord1Variable(variableField1.getValue());
        progWidget.setCoord2Variable(variableField2.getValue());
        saveWidgets();

        super.removed();
    }

}
