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

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.progwidgets.ICountWidget;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ISidedWidget;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetImportExport<P extends IProgWidget & ISidedWidget & ICountWidget> extends GuiProgWidgetAreaShow<P> {

    private WidgetTextFieldNumber textField;

    public GuiProgWidgetImportExport(P progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        if (showSides()) {
            for (Direction dir : DirectionUtil.VALUES) {
                ITextComponent sideName = ClientUtils.translateDirectionComponent(dir);
                WidgetCheckBox checkBox = new WidgetCheckBox(guiLeft + 8, guiTop + 32 + dir.get3DDataValue() * 12, 0xFF404040,
                        sideName, b -> progWidget.getSides()[dir.get3DDataValue()] = b.checked);
                checkBox.checked = progWidget.getSides()[dir.get3DDataValue()];
                addButton(checkBox);

                addLabel(xlate("pneumaticcraft.gui.progWidget.inventory.accessingSides"), guiLeft + 6, guiTop + 20);
            }
        }

        WidgetCheckBox useItemCount = new WidgetCheckBox(guiLeft + 8, guiTop + (showSides() ? 115 : 30), 0xFF404040,
                xlate("pneumaticcraft.gui.progWidget.itemFilter.useItemCount"),
                b -> { progWidget.setUseCount(b.checked); textField.setEditable(b.checked); }
        ).setTooltipKey(countTooltipKey()).setChecked(progWidget.useCount());
        addButton(useItemCount);

        textField = new WidgetTextFieldNumber(font, guiLeft + 10, guiTop + (showSides() ? 128 : 43), 50, 11).setRange(0, Integer.MAX_VALUE);
        textField.setValue(progWidget.getCount());
        textField.setEditable(useItemCount.checked);
        textField.setResponder(s -> progWidget.setCount(textField.getIntValue()));
        addButton(textField);

    }

    protected boolean showSides() {
        return true;
    }

    protected String countTooltipKey() {
        return "pneumaticcraft.gui.progWidget.itemFilter.useItemCount.tooltip";
    }
}
