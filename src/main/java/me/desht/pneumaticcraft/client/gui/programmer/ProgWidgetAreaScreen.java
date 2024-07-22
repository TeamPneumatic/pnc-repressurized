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

import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.area.AreaType;
import me.desht.pneumaticcraft.api.drone.area.AreaTypeWidget;
import me.desht.pneumaticcraft.api.drone.area.AreaTypeWidget.EnumSelectorField;
import me.desht.pneumaticcraft.api.drone.area.AreaTypeWidget.IntegerField;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.client.gui.InventorySearcherScreen;
import me.desht.pneumaticcraft.client.gui.ProgrammerScreen;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.item.GPSToolItem;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetAreaScreen extends ProgWidgetAreaShowScreen<ProgWidgetArea> {
    private InventorySearcherScreen invSearchGui;
    private int pointSearched;
    private WidgetComboBox variableField1;
    private WidgetComboBox variableField2;

    private final List<? extends AreaType> allAreaTypes = ProgWidgetArea.getAllAreaTypes();
    private final List<Pair<AreaTypeWidget, AbstractWidget>> areaTypeValueWidgets = new ArrayList<>();
    private final List<AbstractWidget> areaTypeStaticWidgets = new ArrayList<>();

    public ProgWidgetAreaScreen(ProgWidgetArea widget, ProgrammerScreen guiProgrammer) {
        super(widget, guiProgrammer);

        xSize = 256;
    }

    @Override
    public void init() {
        super.init();

        boolean advancedMode = ConfigHelper.client().general.programmerDifficulty.get() == IProgWidget.WidgetDifficulty.ADVANCED;

        // GPS buttons
        WidgetButtonExtended gpsButton1 = new WidgetButtonExtended(guiLeft + (advancedMode ? 6 : 55), guiTop + 30, 20, 20, Component.empty(), b -> openInvSearchGUI(0))
                .setRenderStacks(new ItemStack(ModItems.GPS_TOOL.get()))
                .setTooltipText(xlate("pneumaticcraft.gui.progWidget.area.selectGPS1"));
        addRenderableWidget(gpsButton1);
        WidgetButtonExtended gpsButton2 = new WidgetButtonExtended(guiLeft + (advancedMode ? 133 : 182), guiTop + 30, 20, 20, Component.empty(), b -> openInvSearchGUI(1))
                .setRenderStacks(new ItemStack(ModItems.GPS_TOOL.get()))
                .setTooltipText(xlate("pneumaticcraft.gui.progWidget.area.selectGPS2"));
        addRenderableWidget(gpsButton2);

        // variable textfields
        variableField1 = new WidgetComboBox(font, guiLeft + 28, guiTop + 34, 88, font.lineHeight + 3);
        variableField2 = new WidgetComboBox(font, guiLeft + 155, guiTop + 34, 88, font.lineHeight + 3);
        Set<String> variables = guiProgrammer == null ? Collections.emptySet() : guiProgrammer.te.getAllVariables();
        variableField1.setElements(variables);
        variableField2.setElements(variables);
        variableField1.setValue(progWidget.getVarName(0));
        variableField2.setValue(progWidget.getVarName(1));
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
                progWidget.setAreaType(areaType);
                switchToWidgets(areaType);
            });
//            if (progWidget.type.getClass() == areaType.getClass()) {
//                allAreaTypes.set(i, progWidget.type);
//            }
            builder.addRadioButton(radioButton, progWidget.getAreaType().getName() == areaType.getName());
        }
        builder.build(this::addRenderableWidget);
        switchToWidgets(progWidget.getAreaType());

        if (invSearchGui != null) {
            // returning from GPS selection GUI; copy the selected blockpos to the progwidget
            progWidget.setPos(pointSearched, invSearchGui.getBlockPos());
            invSearchGui = null;
        }

        // blockpos labels
        String l1 = "P1: " + ChatFormatting.DARK_BLUE + formatPos(progWidget.getPos(0).orElse(PneumaticCraftUtils.invalidPos()));
        addLabel(Component.literal(l1), guiLeft + 8, guiTop + 20);
        String l2 = "P2: " + ChatFormatting.DARK_BLUE + formatPos(progWidget.getPos(1).orElse(PneumaticCraftUtils.invalidPos()));
        addLabel(Component.literal(l2), guiLeft + 133, guiTop + 20);
    }

    private String formatPos(BlockPos pos) {
        return PneumaticCraftUtils.isValidPos(pos) ? String.format("[ %d, %d, %d ]", pos.getX(), pos.getY(), pos.getZ()) : "-";
    }

    private void openInvSearchGUI(int which) {
        ItemStack gpsStack = new ItemStack(ModItems.GPS_TOOL.get());
        GPSToolItem.setGPSLocation(ClientUtils.getClientPlayer().getUUID(), gpsStack, progWidget.getPos(which).orElse(BlockPos.ZERO));
        ClientUtils.openContainerGui(ModMenuTypes.INVENTORY_SEARCHER.get(), Component.literal("Inventory Searcher (GPS)"));
        if (minecraft.screen instanceof InventorySearcherScreen) {
            invSearchGui = (InventorySearcherScreen) minecraft.screen;
            invSearchGui.setStackPredicate(itemStack -> itemStack.getItem() instanceof IPositionProvider);
            invSearchGui.setSearchStack(GPSToolItem.getGPSLocation(gpsStack).isPresent() ? gpsStack : ItemStack.EMPTY);
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
            WidgetLabel titleWidget = new WidgetLabel(x, curY, xlate(areaTypeWidget.getTranslationKey()));
            addRenderableWidget(titleWidget);
            areaTypeStaticWidgets.add(titleWidget);
            curY += font.lineHeight + 1;

            switch (areaTypeWidget) {
                case IntegerField intWidget -> {
                    WidgetTextFieldNumber intField = new WidgetTextFieldNumber(font, x, curY, 40, font.lineHeight + 1).setRange(0, Integer.MAX_VALUE);
                    intField.setValue(intWidget.readAction.getAsInt());
                    addRenderableWidget(intField);
                    areaTypeValueWidgets.add(new ImmutablePair<>(areaTypeWidget, intField));

                    curY += font.lineHeight + 20;
                }
                case EnumSelectorField<?> enumWidget -> {
                    WidgetComboBox enumCbb = new WidgetComboBox(font, x, curY, 80, font.lineHeight + 3).setFixedOptions(true);
                    enumCbb.setElements(getEnumNames(enumWidget.enumClass));
                    String txt = xlate(enumWidget.readAction.get().getTranslationKey()).getString();
                    enumCbb.setValue(txt);
                    addRenderableWidget(enumCbb);
                    areaTypeValueWidgets.add(new ImmutablePair<>(areaTypeWidget, enumCbb));

                    curY += font.lineHeight + 20;
                }
                default -> throw new IllegalStateException("Invalid widget type: " + areaTypeWidget.getClass());
            }
        }
    }

    private void saveWidgets() {
        for (Pair<AreaTypeWidget, AbstractWidget> entry : areaTypeValueWidgets) {
            AreaTypeWidget widget = entry.getLeft();
            AbstractWidget guiWidget = entry.getRight();
            if (widget instanceof IntegerField intWidget) {
                intWidget.writeAction.accept(((WidgetTextFieldNumber) guiWidget).getIntValue());
            } else if (widget instanceof EnumSelectorField<? extends ITranslatableEnum>) {
                @SuppressWarnings("unchecked")
                EnumSelectorField<ITranslatableEnum> enumWidget = (EnumSelectorField<ITranslatableEnum>) widget;
                WidgetComboBox cbb = (WidgetComboBox) guiWidget;
                List<String> enumNames = getEnumNames(enumWidget.enumClass);
                Object[] enumValues = enumWidget.enumClass.getEnumConstants();
                if (enumValues[enumNames.indexOf(cbb.getValue())] instanceof ITranslatableEnum tr) {
                    enumWidget.writeAction.accept(tr);
                }
            }
        }
    }
    
    private List<String> getEnumNames(Class<?> enumClass) {
        Object[] enumValues = enumClass.getEnumConstants();
        List<String> enumNames = new ArrayList<>();
        for (Object enumValue : enumValues) {
            if (enumValue instanceof ITranslatableEnum t) {
                enumNames.add(xlate(t.getTranslationKey()).getString());
            } else {
                enumNames.add(enumValue.toString());
            }
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
        progWidget.setVarName(0, variableField1.getValue());
        progWidget.setVarName(1, variableField2.getValue());
        saveWidgets();

        super.removed();
    }
}
