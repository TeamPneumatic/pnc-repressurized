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

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.GuiInventorySearcher;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCoordinate;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

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
            progWidget.setCoordinate(invSearchGui.getBlockPos());
        }

        WidgetRadioButton.Builder.create()
                .addRadioButton(new WidgetRadioButton(guiLeft + 7, guiTop + 51, 0xFF404040,
                                xlate("pneumaticcraft.gui.progWidget.coordinate.constant"), b -> setUsingVariable(false)),
                        !progWidget.isUsingVariable())
                .addRadioButton(new WidgetRadioButton(guiLeft + 7, guiTop + 100, 0xFF404040,
                                xlate("pneumaticcraft.gui.progWidget.coordinate.variable"), b -> setUsingVariable(true)),
                        progWidget.isUsingVariable())
                .build(this::addButton);

        gpsButton = new WidgetButtonExtended(guiLeft + 100, guiTop + 20, 20, 20, StringTextComponent.EMPTY, b -> openGPSSearcher());
        gpsButton.setRenderStacks(new ItemStack(ModItems.GPS_TOOL.get()));
        gpsButton.setTooltipText(xlate("pneumaticcraft.gui.progWidget.coordinate.selectFromGPS"));
        gpsButton.active = !progWidget.isUsingVariable();
        addButton(gpsButton);
        coordFields = new WidgetTextFieldNumber[3];
        for (int i = 0; i < 3; i++) {
            coordFields[i] = new WidgetTextFieldNumber(font, guiLeft + 100, guiTop + 50 + 13 * i, 40, font.lineHeight + 1);
            addButton(coordFields[i]);
            coordFields[i].setEditable(gpsButton.active);
        }
        coordFields[0].setValue(progWidget.getRawCoordinate().getX());
        coordFields[1].setValue(progWidget.getRawCoordinate().getY());
        coordFields[2].setValue(progWidget.getRawCoordinate().getZ());

        variableField = new WidgetComboBox(font, guiLeft + 90, guiTop + 112, 80, font.lineHeight + 1);
        variableField.setElements(guiProgrammer.te.getAllVariables());
        variableField.setMaxLength(GlobalVariableManager.MAX_VARIABLE_LEN);
        addButton(variableField);
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
        ClientUtils.openContainerGui(ModContainers.INVENTORY_SEARCHER.get(), new StringTextComponent("Inventory Searcher (GPS)"));
        if (minecraft.screen instanceof GuiInventorySearcher) {
            invSearchGui = (GuiInventorySearcher) minecraft.screen;
            invSearchGui.setStackPredicate(itemStack -> itemStack.getItem() instanceof IPositionProvider);
            BlockPos area = progWidget.getRawCoordinate();
            ItemStack gpsStack = new ItemStack(ModItems.GPS_TOOL.get());
            ItemGPSTool.setGPSLocation(gpsStack, area);
            invSearchGui.setSearchStack(ItemGPSTool.getGPSLocation(gpsStack) != null ? gpsStack : ItemStack.EMPTY);
        }
    }

    @Override
    public void removed() {
        progWidget.setCoordinate(new BlockPos(coordFields[0].getIntValue(), coordFields[1].getIntValue(), coordFields[2].getIntValue()));
        progWidget.setVariable(variableField.getValue());

        super.removed();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        font.draw(matrixStack, "x:", guiLeft + 90, guiTop + 51, 0xFF404040);
        font.draw(matrixStack, "y:", guiLeft + 90, guiTop + 64, 0xFF404040);
        font.draw(matrixStack, "z:", guiLeft + 90, guiTop + 77, 0xFF404040);
        font.draw(matrixStack, I18n.get("pneumaticcraft.gui.progWidget.coordinate.variableName"), guiLeft + 90, guiTop + 100, 0xFF404060);
    }
}
