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
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.common.progwidgets.ICondition.Operator;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCoordinateCondition;
import net.minecraft.util.Direction;
import net.minecraft.util.text.StringTextComponent;

public class GuiProgWidgetCoordinateCondition extends GuiProgWidgetOptionBase<ProgWidgetCoordinateCondition> {

    public GuiProgWidgetCoordinateCondition(ProgWidgetCoordinateCondition widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        for (Direction.Axis axis : Direction.Axis.values()) {
            WidgetCheckBox checkBox = new WidgetCheckBox(guiLeft + 10, guiTop + 30 + axis.ordinal() * 12, 0xFF404040,
                    new StringTextComponent(axis.getName()), b -> progWidget.getAxisOptions().setCheck(axis, b.checked));
            addButton(checkBox);
            checkBox.setChecked(progWidget.getAxisOptions().shouldCheck(axis));
        }

        WidgetRadioButton.Builder<WidgetRadioButton> builder = WidgetRadioButton.Builder.create();
        for (Operator op : Operator.values()) {
            builder.addRadioButton(new WidgetRadioButton(guiLeft + 80, guiTop + 30 + op.ordinal() * 12, 0xFF404040,
                            new StringTextComponent(op.toString()), b -> progWidget.setOperator(op)),
                    progWidget.getOperator() == op);
        }
        builder.build(this::addButton);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        String condition = progWidget.getCondition();
        font.draw(matrixStack, condition, width / 2f - font.width(condition) / 2f, guiTop + 70, 0xFF404060);
    }
}
