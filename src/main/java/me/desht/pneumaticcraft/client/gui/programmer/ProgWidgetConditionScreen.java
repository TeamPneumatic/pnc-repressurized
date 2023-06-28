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

import me.desht.pneumaticcraft.client.gui.ProgrammerScreen;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.drone.progwidgets.ICondition;
import me.desht.pneumaticcraft.common.drone.progwidgets.ISidedWidget;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetCondition;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetEntityCondition;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetConditionScreen<T extends ProgWidgetCondition> extends ProgWidgetAreaShowScreen<T> {

    private WidgetTextFieldNumber textField;

    public ProgWidgetConditionScreen(T widget, ProgrammerScreen guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        if (isSidedWidget()) {
            for (Direction dir : DirectionUtil.VALUES) {
                Component sideName = ClientUtils.translateDirectionComponent(dir);
                WidgetCheckBox checkBox = new WidgetCheckBox(guiLeft + 8, guiTop + 30 + dir.get3DDataValue() * 12, 0xFF404040, sideName,
                        b -> ((ISidedWidget) progWidget).getSides()[dir.get3DDataValue()] = b.checked);
                checkBox.checked = ((ISidedWidget) progWidget).getSides()[dir.get3DDataValue()];
                addRenderableWidget(checkBox);
            }
        }

        int baseX = isSidedWidget() ? 90 : 8;
        int baseY = isUsingAndOr() ? 60 : 30;

        WidgetRadioButton radioButton;
        WidgetRadioButton.Builder<WidgetRadioButton> builder = WidgetRadioButton.Builder.create();
        if (isUsingAndOr()) {
            WidgetRadioButton.Builder.create()
                    .addRadioButton(new WidgetRadioButton(guiLeft + baseX, guiTop + 30, 0xFF404040,
                                    xlate("pneumaticcraft.gui.progWidget.condition.anyBlock"),
                                    b -> progWidget.setAndFunction(false)),
                            !progWidget.isAndFunction())
                    .addRadioButton(new WidgetRadioButton(guiLeft + baseX, guiTop + 42, 0xFF404040,
                                    xlate("pneumaticcraft.gui.progWidget.condition.allBlocks"),
                                    b -> progWidget.setAndFunction(true)),
                            progWidget.isAndFunction())
                    .build(this::addRenderableWidget);
        }

        if (requiresNumber()) {
            for (ICondition.Operator op : ICondition.Operator.values()) {
                radioButton = new WidgetRadioButton(guiLeft + baseX, guiTop + baseY + op.ordinal() * 12, 0xFF404040,
                        Component.literal(op.toString()), b -> progWidget.setOperator(op));
                builder.addRadioButton(radioButton, progWidget.getOperator() == op);
            }
            builder.build(this::addRenderableWidget);

            textField = new WidgetTextFieldNumber(font, guiLeft + baseX, guiTop + baseY + 40, 50, 11)
                    .setValue(progWidget.getRequiredCount()).setRange(0, Integer.MAX_VALUE);
            textField.setResponder(s -> progWidget.setRequiredCount(textField.getIntValue()));
            setInitialFocus(textField);
            addRenderableWidget(textField);
        }

        WidgetLabel label = addLabel(xlate("pneumaticcraft.gui.progWidget.condition.measure"), guiLeft + 8, guiTop + 152);
        label.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.progWidget.condition.measure.tooltip")));
        WidgetComboBox measureTextField = new WidgetComboBox(font, guiLeft + label.getWidth() + 8, guiTop + 150, 80, 11);
        measureTextField.setElements(guiProgrammer.te.getAllVariables());
        measureTextField.setMaxLength(GlobalVariableManager.MAX_VARIABLE_LEN);
        measureTextField.setValue(progWidget.getMeasureVar());
        measureTextField.setResponder(progWidget::setMeasureVar);
        addRenderableWidget(measureTextField);
    }

    protected boolean isSidedWidget() {
        return progWidget instanceof ISidedWidget;
    }

    protected boolean isUsingAndOr() {
        return true;
    }

    protected boolean requiresNumber() {
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        if (isSidedWidget()) {
            graphics.drawString(font, xlate("pneumaticcraft.gui.progWidget.inventory.accessingSides"), guiLeft + 4, guiTop + 20, 0xFF404060, false);
        }
        Component s = progWidget.getExtraStringInfo().get(0);
        graphics.drawString(font, s, guiLeft + xSize / 2 - font.width(s) / 2, guiTop + 120, 0xFF404060, false);
    }

    public static class Entity extends ProgWidgetConditionScreen<ProgWidgetEntityCondition> {
        public Entity(ProgWidgetEntityCondition widget, ProgrammerScreen guiProgrammer) {
            super(widget, guiProgrammer);
        }

        @Override
        protected boolean isSidedWidget() {
            return false;
        }

        @Override
        protected boolean isUsingAndOr() {
            return false;
        }
    }
}
