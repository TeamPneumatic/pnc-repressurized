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
import me.desht.pneumaticcraft.client.gui.InventorySearcherScreen;
import me.desht.pneumaticcraft.client.gui.ProgrammerScreen;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetCoordinate;
import me.desht.pneumaticcraft.common.item.GPSToolItem;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetCoordinateScreen extends ProgWidgetAreaShowScreen<ProgWidgetCoordinate> {
    private InventorySearcherScreen invSearchGui;
    private WidgetTextFieldNumber[] coordFields;
    private WidgetComboBox variableField;
    private WidgetButtonExtended gpsButton;

    public ProgWidgetCoordinateScreen(ProgWidgetCoordinate widget, ProgrammerScreen guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        if (invSearchGui != null) {
            progWidget.setCoordinate(invSearchGui.getBlockPos());
            invSearchGui = null;
        }

        WidgetRadioButton.Builder.create()
                .addRadioButton(new WidgetRadioButton(guiLeft + 7, guiTop + 51, 0xFF404040,
                                xlate("pneumaticcraft.gui.progWidget.coordinate.constant"), b -> setUsingVariable(false)),
                        !progWidget.isUsingVariable())
                .addRadioButton(new WidgetRadioButton(guiLeft + 7, guiTop + 100, 0xFF404040,
                                xlate("pneumaticcraft.gui.progWidget.coordinate.variable"), b -> setUsingVariable(true)),
                        progWidget.isUsingVariable())
                .build(this::addRenderableWidget);

        gpsButton = new WidgetButtonExtended(guiLeft + 100, guiTop + 20, 20, 20, Component.empty(), b -> openGPSSearcher());
        gpsButton.setRenderStacks(new ItemStack(ModItems.GPS_TOOL.get()));
        gpsButton.setTooltipText(xlate("pneumaticcraft.gui.progWidget.coordinate.selectFromGPS"));
        gpsButton.active = !progWidget.isUsingVariable();
        addRenderableWidget(gpsButton);
        coordFields = new WidgetTextFieldNumber[3];
        for (int i = 0; i < 3; i++) {
            coordFields[i] = new WidgetTextFieldNumber(font, guiLeft + 100, guiTop + 50 + 13 * i, 40, font.lineHeight + 1);
            addRenderableWidget(coordFields[i]);
            coordFields[i].setEditable(gpsButton.active);
        }
        BlockPos coord = progWidget.getRawCoordinate().orElse(BlockPos.ZERO);
        coordFields[0].setValue(coord.getX());
        coordFields[1].setValue(coord.getY());
        coordFields[2].setValue(coord.getZ());

        variableField = new WidgetComboBox(font, guiLeft + 90, guiTop + 112, 80, font.lineHeight + 1);
        variableField.setElements(guiProgrammer.te.getAllVariables());
        variableField.setMaxLength(GlobalVariableManager.MAX_VARIABLE_LEN);
        addRenderableWidget(variableField);
        variableField.setValue(progWidget.getVariable());
        variableField.setEditable(progWidget.isUsingVariable());
    }

    private void setUsingVariable(boolean usingVariable) {
        progWidget.setUsingVariable(usingVariable);
        gpsButton.active = !usingVariable;
        for (WidgetTextField textField : coordFields) {
            textField.setEditable(!usingVariable);
        }
        variableField.setEditable(usingVariable);
    }

    private void openGPSSearcher() {
        ClientUtils.openContainerGui(ModMenuTypes.INVENTORY_SEARCHER.get(), Component.literal("Inventory Searcher (GPS)"));
        if (minecraft.screen instanceof InventorySearcherScreen) {
            invSearchGui = (InventorySearcherScreen) minecraft.screen;
            invSearchGui.setStackPredicate(itemStack -> itemStack.getItem() instanceof IPositionProvider);
            BlockPos coord = progWidget.getRawCoordinate().orElse(BlockPos.ZERO);
            ItemStack gpsStack = new ItemStack(ModItems.GPS_TOOL.get());
            GPSToolItem.setGPSLocation(ClientUtils.getClientPlayer().getUUID(), gpsStack, coord);
            invSearchGui.setSearchStack(GPSToolItem.getGPSLocation(gpsStack).isPresent() ? gpsStack : ItemStack.EMPTY);
        }
    }

    @Override
    public void removed() {
        progWidget.setCoordinate(new BlockPos(coordFields[0].getIntValue(), coordFields[1].getIntValue(), coordFields[2].getIntValue()));
        progWidget.setVariable(variableField.getValue());

        super.removed();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        graphics.drawString(font, "x:", guiLeft + 90, guiTop + 51, 0xFF404040, false);
        graphics.drawString(font, "y:", guiLeft + 90, guiTop + 64, 0xFF404040, false);
        graphics.drawString(font, "z:", guiLeft + 90, guiTop + 77, 0xFF404040, false);
        graphics.drawString(font, xlate("pneumaticcraft.gui.progWidget.coordinate.variableName"), guiLeft + 90, guiTop + 100, 0xFF404060, false);
    }
}
